#!/usr/bin/perl
# This is pretty much what Perl was made for.
package RomHelper::Insert;
use strict;
use warnings;
use List::Util qw(first);
use File::Copy;
use parent 'Exporter';
our @EXPORT = qw(insert);
our @EXPORT_OK = qw(insert tbl insert_file);

sub insert {
    my ($from, $to, $baseFilename, $tableFilename) = @_;
    # For dealing with the char table which is formatted Windows-style:
    local $/ = "\r\n";
    open(my $script, "<", $from) or die "Couldn't find script file $from: $!.\n";
    open(my $table, "<", $tableFilename) or die "Couldn't load table file $tableFilename: $!.\n";
    open(my $base, "<", $baseFilename) or die "Couldn't open base ROM $baseFilename: $!.\n";
    copy($baseFilename, $to) or die "Couldn't create ROM file $to: $!.\n";
    open(my $ROM, "+<", $to) or die "Copied base file successfully, but couldn't open $to: $!.\n";
    binmode($ROM);
    insert_file($ROM, $script, tbl($table));
}

sub tbl {
    my $tableFile = shift;
    my %charTable;
    print "Compiling character table...";
    while (my $line = <$tableFile>) {
        chomp $line;
        # capture values for hash
        $line =~ /(.+) (.+)/;
        my $code = hex $1;
        my $toInsert = $2;
        if (length $toInsert > 1) {
            # must be a control code
            $toInsert = "[".$toInsert."]" if $toInsert !~ /\[.+\]/;
            $charTable{$toInsert} = $code;
            next;
        }
        $toInsert = " " if $toInsert eq "_";
        next if $charTable{$toInsert}; # we've already found a code for it; probably a space
        # put it in the table
        $charTable{$toInsert} = $code;
    }
    print "@{[scalar keys %charTable]} entries found.\n";
    return \%charTable;
}

sub insert_file {
    # For dealing with files output by this program, Unix-style:
    local $/ = "\n";
    my ($ROM, $script, $charTable) = @_;
    my $lineNum = 0;
    my $offset = 0;
    my $start = 0xF7EA00;
    my $ptrStart = 0xF27A90;
    while (my $line = <$script>) {
        chomp $line;
        # If it's not an edited line, don't bother with it
        next if $line !~ /[0-9A-F]{3}-E: .*/;
        # If it is, remove the prefix
        $line =~ s/[0-9A-F]{3}-E: //;
        my @tokens = split /(\[|\])/, $line;

        # tokenize!
        my $index = 0;
        until (not defined $tokens[$index]) {
            if ($tokens[$index] eq "[") {
                splice @tokens, $index, 3, "[".$tokens[$index+1]."]";
                $index++;
            } else {
                my $len = length $tokens[$index];
                splice @tokens, $index, 1, split ('', $tokens[$index]);
                $index += $len;
            }
        }

        # now convert it using the character table
        my @bytes = ();
        for my $char (@tokens) {
            if (exists $charTable->{$char}) {
                push @bytes, $charTable->{$char};
            } elsif ($char eq "[PAUSE]") {
                push @bytes, 3, 2;
            } elsif ($char =~ /\[([0-9A-F]{2} ?)+\]/) {
                while ($char =~ /([0-9A-F]{2})/g) {
                    push @bytes, hex $1;
                }
            } else {
                print "Unrecognized character/control code $char at line $. of script file.\n";
            }
        }
        push @bytes, 0 if scalar @bytes > 0;
        print scalar @bytes, " ";

        # and lastly, write it to the ROM
        if (scalar @bytes > 0) {
            seek $ROM, $start + $offset, 0;
            my $byteString = pack("C*", @bytes);
            print { $ROM } $byteString;
            seek $ROM, $ptrStart + $lineNum * 4, 0;
            my $ptrLoc = 0x08000000 + $start + $offset;
            my @ptrBytes =
                (($ptrLoc & 0x000000FF)      , ($ptrLoc & 0x0000FF00) >> 8,
                 ($ptrLoc & 0x00FF0000) >> 16, ($ptrLoc & 0xFF000000) >> 24);
            my $ptrByteString = pack("C*", @ptrBytes);
            print { $ROM } $ptrByteString;
            $offset += scalar @bytes;
        } else {
            seek $ROM, $ptrStart + $lineNum * 4, 0;
            my $ptrByteString = pack("C*", 0, 0, 0, 0);
            print { $ROM } $ptrByteString;
        }

        $lineNum++;
    }
}
