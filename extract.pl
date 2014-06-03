#!/usr/bin/perl
# The same thing as the "extract" part of ROMHelper, but written in Perl.
# Let's see how much smaller it gets.
# Also, I want to practice my Perl skillz.
use strict;
use warnings;

# For dealing with the char table which is formatted Windows-style:
$/ = "\r\n";

my $usage = "usage: $0 <ROM file> <output file> [<table file>]\n";
my $filename = shift @ARGV or die $usage;
my $outFilename = shift @ARGV or die $usage;
my $tableFilename = shift @ARGV || "resources/eng_table.txt";
open(my $ROM, "<", $filename) or die "Couldn't find ROM file $filename: $!.\n";
open(my $outfile, ">", $outFilename) or die "Couldn't create output file $outFilename: $!.\n";
open(my $table, "<", $tableFilename) or die "Couldn't load table file $tableFilename: $!.\n";
binmode($ROM);

my @charTable;
print "Parsing character table: $tableFilename...";
while (my $line = <$table>) {
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

print scalar @charTable," entries\n";

extract($ROM, $outfile);

sub extract {
    print "finding end of script data in ROM...";
    my ($ROM, $outfile) = @_;
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
    my $line = "000";
    my $is_cc = 0;
    LINE: for (my $ptrLoc = $ptrStart; $ptrLoc < $eod; $ptrLoc += 4) {
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
                    print { $outfile } $line, ": ", $string, "\n";
                    print { $outfile } $line, "-E: ", $string, "\n";
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
                    $string .= $charTable[$locBytes[0]];
                }
            }
        } else {
            # weird random 00 00 00 00 pointer
            print { $outfile } $line, ": ", $string, "\n";
            print { $outfile } $line, "-E: ", $string, "\n";
            $string = "";
            $line++;
        }
    }
    close $outfile;
    close $ROM;
    print "done\n";
}
