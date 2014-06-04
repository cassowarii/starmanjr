#!/usr/bin/perl
package RomHelper::Extract;
# The same thing as the "extract" part of ROMHelper, but written in Perl.
# Let's see how much smaller it gets.
# Also, I want to practice my Perl skillz.
use strict;
use warnings;
use parent 'Exporter';
our @EXPORT = qw(extract);
our @EXPORT_OK = qw(extract tbl extract_file);

sub extract {
    my ($from, $to, $tableFilename) = @_;
    # For dealing with the char table which is formatted Windows-style:
    local $/ = "\r\n";
    open(my $ROM, "<", $from) or die "Couldn't find ROM file $from: $!.\n";
    open(my $outfile, ">", $to) or die "Couldn't create output file $to: $!.\n";
    open(my $table, "<", $tableFilename) or die "Couldn't load table file $tableFilename: $!.\n";
    binmode($ROM);
    extract_file($ROM, $outfile, tbl($table));
}

sub tbl {
    my $tableFile = shift;
    my @charTable;
    while (my $line = <$tableFile>) {
        chomp $line;
        # remove hex code
        $line =~ s/.* //;
        # convert _ to space
        $line = " " if $line eq "_";
        # detect wonky CCs and fix them
        $line = "[".$line."]" if $line =~ /^[^\[].*[^\]]$/ && length $line > 1;
        # make 03 work better
        $line = "[03 " if $line eq "[03]";
        push @charTable, $line;
    }
    return @charTable;
}

sub extract_file {
    print "finding end of script data in ROM...";
    my ($ROM, $outfile, @charTable) = @_;
    my $ptrStart = 0xF27A90;
    my $eod = $ptrStart;
    my $precision = 9;
    while ($precision > -1) {
        my $byteString = 0;
        my @bytes = ( 0, 0, 0, 0 );
        while ($bytes[3] == 8 || $bytes[3] == 0) {
            $eod += 4 << $precision;
            seek $ROM, $eod, 0;
            read $ROM, $byteString, 4;
            @bytes = unpack "W*", $byteString;
        }
        $eod -= 4 << $precision;
        $precision--;
    }
    $eod += 4 << ($precision + 1);
    printf "done, EOD seems to be at 0x%X\n", $eod;
    my $string = "";
    my $line = 0;
    my $is_cc = 0;
    print "Extracting script from file...";
    LINE: for (my $ptrLoc = $ptrStart; $ptrLoc < $eod; $ptrLoc += 4) {
        my $fmtLine = sprintf("%03X", $line);
        my $addr = 0;
        seek $ROM, $ptrLoc, 0;
        read $ROM, my $byteString, 4;
        my @bytes = unpack "W*", $byteString;
        $addr += $bytes[0];
        $addr += $bytes[1] << 8;
        $addr += $bytes[2] << 16;
        last LINE if $addr == 0x100000;
        unless ($addr == 0) { # it must be a real line
            CHARACTER: for (my $loc = $addr;; $loc++) {
                seek $ROM, $loc, 0;
                read $ROM, my $temp, 2;
                my @locBytes = unpack "W*", $temp;
                if ($locBytes[0] == 0) {
                    print { $outfile } $fmtLine, ": ", $string, "\n";
                    print { $outfile } $fmtLine, "-E: ", $string, "\n";
                    $string = "";
                    $line++;
                    last CHARACTER;
                } elsif ($locBytes[0] == 3 && $locBytes[1] == 2) {
                    $string .= "[PAUSE]";
                    $loc++; # skip over [02]
                } elsif ($locBytes[0] == 3) { # control code
                    $string .= sprintf ("[03 %02X]", $locBytes[1]);
                    $loc++; # skip over second part of CC
                } else { # normal character
                    if (defined $charTable[$locBytes[0]]) {
                        $string .= $charTable[$locBytes[0]];
                    } else {
                        print "Byte value $locBytes[0] doesn't correspond to a valid character.\n
                            Ensure that the character table file has all byte values listed.\n";
                    }
                }
            }
        } else {
            # weird random 00 00 00 00 pointer
            print { $outfile } $fmtLine, ": ", $string, "\n";
            print { $outfile } $fmtLine, "-E: ", $string, "\n";
            $string = "";
            $line++;
        }
    }
    close $outfile;
    close $ROM;
    print "done\n";
}
1;
