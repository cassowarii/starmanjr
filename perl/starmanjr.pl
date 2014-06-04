#!/usr/bin/perl

use strict;
use warnings;
use lib '.';
use RomHelper::Extract;
use RomHelper::Insert;

my $usage = "$0 outfile [-e infile | -i infile -b basefile] [-t tablefile]\n"
           ."\t-e, --extract\tExtract from ROM infile to script-file outfile.\n"
           ."\t-i, --insert\tCompile from script infile to ROM outfile.\n"
           ."\t-b, --base\tBase file to use when compiling to ROM.\n"
           ."\t-t, --table\tOptional alternative table file to use when extracting/compiling.\n";
my $outfile = shift @ARGV;
die "Improper number of arguments given.\n" unless @ARGV % 2 == 0;
my %args = @ARGV;
my $mode = "";
my ($infile, $basefile);
if (exists $args{'-e'} || exists $args{'--extract'}) {
    $infile = $args{'-e'} || $args{'--extract'};
    $mode = "e";
} elsif (exists $args{'-i'} || exists $args{'--insert'}) {
    $infile = $args{'-i'} || $args{'--insert'};
    $basefile = $args{'-b'} || $args{'--base'} or die "You must provide a base file for compilation!\n";
    $mode = "i";
}
die $usage unless $mode;
my $tablefile = $args{'-t'} || $args{'--table'} || 'resources/eng_table.txt';

if ($mode eq "e") {
    #RomHelper::extract($infile, $outfile, $tablefile);
    extract($infile, $outfile, $tablefile);
} else {
    #RomHelper::insert($infile, $outfile, $tablefile);
    insert($infile, $outfile, $basefile, $tablefile);
}
