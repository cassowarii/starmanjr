Starman Jr.
===========

A program to edit the script of the Mother 1 part of Mother 1+2.

Created during production of [Saturnbound Zero](http://forum.starmen.net/forum/Community/PKHack/SaturnBound-Zero/first after frustration with existing tools.)

This repository contains the Java source code for the initial version, as well as a version ported to Perl 5, which must be run from the command line.

Command-line version usage
--------------------------

The Perl version accepts several flags, which should come after the specification of the output file (the first argument):

```
    -e, --extract   The ROM file from which the script should be extracted.
    -i, --insert    The script file from which a ROM should be compiled.
    -b, --base      The base ROM file to which changes should be made when compiling.
    -t, --table     The character table which should be used to convert characters into bytes (optional).
                    See the perl/resources/eng_table.txt file for an example.
```

So an example call in order to compile a ROM to "test.gba" might be:
`./starmanjr.pl test.gba -i script.txt -b mother12.gba`

In order to extract a ROM from "mother12.gba" to "script.txt" a call might be:
`./starmanjr.pl script.txt -e mother12.gba`

If you were using a different character table (for example, if extracting the script of the Japanese version):
`./starmanjr.pl j_script.txt -e mother12.gba -t jpn_table.txt`

(However, note that table files for languages other than English do not yet exist.)
