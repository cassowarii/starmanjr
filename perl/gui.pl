#!/usr/bin/perl
use strict;
use warnings;

package StarmanJr::Window;
use Wx qw(:everything);
use base 'Wx::Frame';
use Wx::Event qw(EVT_BUTTON EVT_TOGGLEBUTTON EVT_TEXT_ENTER EVT_MENU);

my @lines = ();
my ($prevLine, $nextLine);
my $lineBase = 10;
my $scriptFilename = '';

sub new {
    # OBJECT INITIALIZATION
    my $ref = shift;
    my $self = $ref->SUPER::new (   undef,          # parent
                                    -1,             # default id
                                    $_[0],          # title
                                    [-1,-1],        # default position
                                    [649,354]       # size
                                                );
    # VARIABLE INITIALIZATION
    Wx::InitAllImageHandlers();
    my %icons = (
        hex => open_png("images/numbers-hex.png"),
        dec => open_png("images/numbers-dec.png"),
        left => open_png("images/arrow-left.png"),
        right => open_png("images/arrow-right.png"),
        format => open_png("images/autoformat.png"),
        gear => open_png("images/gear.png"),
    );
    my $currentLine = 0;

    # GUI BUILDING
    my $panel = Wx::Panel->new ($self, -1);

    my ($newTextBox, $oldTextBox, $commentBox, $lineNum);

    my $menubar = $self->create_menu([
        [ File => [
            [ Load => sub { $self->load_lines($self->select_script());
                            $self->get_line(0, $oldTextBox, $newTextBox, $commentBox, $lineNum); }],
            [ Save => sub { $self->save_line($currentLine, $oldTextBox, $newTextBox, $commentBox);
                            $self->save_to($scriptFilename); }],
            ['Save as' => sub { $self->save_line($currentLine, $oldTextBox, $newTextBox, $commentBox);
                                $self->save_to($self->select_save_location()); }],
            ['--------'],
            [ Extract => sub { print "extracting...\n" }],
            [ Compile => sub { print "compiling...\n" }],
            ['--------'],
            ['Exit'],
        ]],
        [ Insert => [
            [ Symbol => [
                [ Alpha => sub { insertBrackets("ALPHA", $newTextBox) }],
                [ Beta => sub { insertBrackets("BETA", $newTextBox) }],
                [ Gamma => sub { insertBrackets("GAMMA", $newTextBox) }],
                [ Pi => sub { insertBrackets("PI", $newTextBox) }],
                [ Omega => sub { insertBrackets("OMEGA", $newTextBox) }],
                ['--------'],
                [ Note => sub { insertBrackets("FF", $newTextBox) }],
                ['Double Zero' => sub { insertBrackets("DOUBLEZERO", $newTextBox) }],
            ]],
            ['Control Code' => [
                [ stuff => sub { ...; }],
            ]],
        ]],
    ]);

    $self->SetMenuBar($menubar);

    # Button to go to the previous line.parent,id, label,          position,    size
    $prevLine = Wx::BitmapButton->new ($panel, -1, $icons{'left'}, genPos(0,0), genSize(2,2));
    # Button to go to the next line.
    $nextLine = Wx::BitmapButton->new ($panel, -1, $icons{'right'}, genPos(4,0), genSize(2,2));
    EVT_BUTTON ($self, $prevLine, sub {
        $self->save_line($currentLine, $oldTextBox, $newTextBox, $commentBox);
        if ($currentLine > 0) {
            $currentLine--;
        }
        $self->get_line($currentLine, $oldTextBox, $newTextBox, $commentBox, $lineNum);
    });
    EVT_BUTTON ($self, $nextLine, sub {
        $self->save_line($currentLine, $oldTextBox, $newTextBox, $commentBox);
        if ($currentLine < $#lines) {
            $currentLine++;
        }
        $self->get_line($currentLine, $oldTextBox, $newTextBox, $commentBox, $lineNum);
    });


    my ($hexButton, $decButton);
    $lineNum = Wx::TextCtrl->new ($panel, -1, $currentLine, genPos(2,0), genSize(2,1), wxTE_PROCESS_ENTER);
    EVT_TEXT_ENTER ($self, $lineNum, sub {
        my $num = $lineNum->GetValue();
        $self->save_line($currentLine, $oldTextBox, $newTextBox, $commentBox);
        my $valid;
        if ($num =~ /^-?[0-9]+$/) {
            if ($lineBase == 10) {
                $valid = $num;
            } else {
                $valid = hex $num;
            }
        } elsif ($num =~ /^-?[0-9A-F]+$/i) {
            if ($lineBase == 10) {
                $lineBase = 16;
                $hexButton->SetValue(1);
                $decButton->SetValue(0);
            }
            $valid = hex $num;
        } else {
            $self->complain(':(', "That's not a valid line number!");
            $lineNum->SetValue($lineBase == 10 ? $currentLine : sprintf "%X", $currentLine);
            $valid = $currentLine;
        }
        if ($valid > $#lines) {
            my $formattedMaxNum = $lineBase == 10 ? $#lines : sprintf "0x%X", $#lines;
            $self->complain(':(', "That line number is too big!\n"
                    ."The biggest line number is $formattedMaxNum.");;
            $lineNum->SetValue($lineBase == 10 ? $currentLine : sprintf "%X", $currentLine);
        } elsif ($valid < 0) {
            $self->complain(':(', "Line $num?\nThat doesn't even make any sense!");
            $lineNum->SetValue($lineBase == 10 ? $currentLine : sprintf "%X", $currentLine);
        } else {
            $self->get_line($valid, $oldTextBox, $newTextBox, $commentBox, $lineNum);
            $currentLine = $valid;
        }
    });

    # apparently bitmaptogglebutton doesn't work well in perl (and the python version says
    # it's not available in all ports anyway) (thanks wxwidgets geez) so we're going to
    # use a regular toggle button instead
    $decButton = Wx::ToggleButton->new ($panel, -1, '10', genPos(2,1), genSize(1,1));
    $hexButton = Wx::ToggleButton->new ($panel, -1, '0x', genPos(3,1), genSize(1,1));
    EVT_TOGGLEBUTTON ($self, $decButton, sub {
        $lineBase = 10;
        $lineNum->SetValue($currentLine);
        $hexButton->SetValue(0);
    });
    EVT_TOGGLEBUTTON ($self, $hexButton, sub {
        $lineBase = 16;
        $lineNum->SetValue(sprintf "%X", $currentLine);
        $decButton->SetValue(0);
    });
    $decButton->SetValue(1);

    my $formatButton = Wx::BitmapButton->new ($panel, -1, $icons{'format'}, genPos(0,2), genSize(1,1));
    my $previewButton = Wx::Button->new ($panel, -1, 'Preview', genPos(1,2), genSize(4,1));
    my $settingsButton = Wx::BitmapButton->new ($panel, -1, $icons{'gear'}, genPos(5,2), genSize(1,1));

    $oldTextBox = Wx::TextCtrl->new ($panel, # pos   # size
                                     -1, '', [5, 5], [639,75],
                                     wxTE_MULTILINE|wxTE_WORDWRAP|wxTE_READONLY);

    $newTextBox = Wx::TextCtrl->new ($panel, # pos   # size
                                     -1, '', [5,85], [639,75],
                                     wxTE_MULTILINE|wxTE_WORDWRAP);

    $commentBox = Wx::TextCtrl->new ($panel, -1, '',
                                     genPos(0,3), genSize(6,2),
                                     wxTE_MULTILINE|wxTE_WORDWRAP);

    return $self;
}

sub select_script {
    my $self = shift;
    my $dialog = Wx::FileDialog->new($self, "Please select a script file",
            '', '', "All files|*", wxFD_OPEN|wxFD_FILE_MUST_EXIST);
    $dialog->ShowModal();
    return $dialog->GetPath;
}

sub select_save_location {
    my $self = shift;
    my $dialog = Wx::FileDialog->new($self, "Save to",
            '', '', "Text files (*.txt)|*.txt|All files|*", wxFD_SAVE|wxFD_OVERWRITE_PROMPT);
    $dialog->ShowModal();
    return $dialog->GetPath;
}

sub save_to {
    my ($self, $filename) = @_;
    return if $filename eq '';
    open my $fh, '>', $filename or
        $self->complain(':(', "Can't save to $filename:\n$!.")
        and return 0;
    for my $i (0..$#lines) {
        my $fmt = sprintf "%03d", $i;
        my @entries = split /\n/, $lines[$i], 3;
        # remove windows codes; I'm p sure \n handles \r\n automatically on Windows
        # TODO: actually test that
        s/\r//g for @entries;
        print { $fh } "$fmt: $entries[0]\n";
        print { $fh } "$fmt-E: $entries[1]\n";
        print { $fh } "$entries[2]\n" if $entries[2];
    }
}

sub load_lines {
    my ($self, $filename) = @_;
    return if $filename eq '';
    open my $fh, '<', $filename or
        $self->complain(':(', "Can't open $filename:\n$!.")
        and return 0;
    $scriptFilename = $filename;
    my $tempLine = '';
    while (my $line = <$fh>) {
        if ($line =~ /^[0-9A-F]{3}: /) {
            # don't push a line if it's the first line, because
            # it'll throw everything off by 1
            push @lines, $tempLine unless $. == 1;
            $tempLine = '';
        }
        $line =~ s/^[0-9A-F]{3}(-E)?: //;
        $tempLine .= $line;
    }
}

sub get_line {
    my ($self, $line_num, $old, $new, $comment, $numField) = @_;
    my @entries = split /\n/, $lines[$line_num], 3;
    # remove trailing newlines
    chomp for @entries;
    # insert text into text fields
    $old->SetValue($entries[0]);
    $new->SetValue($entries[1]);
    $comment->SetValue($entries[2]);
    $numField->SetValue($lineBase == 10 ? $line_num : sprintf "%X", $line_num);
    if ($line_num == 0) {
        $prevLine->Enable(0);
    } else {
        $prevLine->Enable(1);
    }
    if ($line_num == $#lines) {
        $nextLine->Enable(0);
    } else {
        $nextLine->Enable(1);
    }
}

sub save_line {
    my ($self, $line_num, $old, $new, $comment) = @_;
    my $entry = $old->GetValue . "\n" . $new->GetValue . "\n" . $comment->GetValue;
    $lines[$line_num] = $entry;
}

sub insertBrackets {
    my ($thing, $box) = @_;
    $box->WriteText('['.$thing.']');
}

sub complain {
    my ($self, $title, $msg) = @_;
    return Wx::MessageDialog->new($self, $msg, $title, wxICON_EXCLAMATION)->ShowModal();
}

sub open_png {
    return Wx::Bitmap->new($_[0], wxBITMAP_TYPE_PNG);
}

sub create_menu {
    my ($self, $menu) = @_;
    my $top_level = ! defined $_[2];
    my $menubar;
    if ($top_level) {
        $menubar = Wx::MenuBar->new();
    } else {
        $menubar = Wx::Menu->new();
    }
    for my $i (@$menu) {
        my @i = @$i;
        #print ref $i, ref $i->[0], ref $i->[1], "\n";
        if (defined $i[1]) {
            if (ref $i[1] eq "ARRAY") {
                # menu/submenu
                if ($top_level) {
                    $menubar->Append($self->create_menu($i[1], 1), $i[0]);
                } else {
                    $menubar->Append(-1, $i[0], $self->create_menu($i[1], 1));
                }
            } elsif (ref $i[1] eq "CODE") {
                # menu item with attached subroutine
                my $item = Wx::MenuItem->new(undef, -1, $i[0]);
                EVT_MENU($self, $item, $i[1]);
                if ($top_level) {
                    $menubar->Append($item, $i[0]);
                } else {
                    $menubar->Append($item);
                }
            }
        } else {
            if ($i[0] =~ /-+/) {
                $menubar->AppendSeparator;
            } elsif ($i[0] eq 'Exit') {
                my $item = Wx::MenuItem->new(undef, wxID_EXIT, $i[0]);
                EVT_MENU($self, $item, sub { $self->Close(1) });
                $menubar->Append($item);
            }
        }
    }
    return $menubar;
}

{   my $spacers = 5;
    my $gridSize = 28;
    my $init_x = 5;
    my $init_y = 165;
    sub genPos {
        my ($grid_x, $grid_y) = @_;
        return [$init_x + $grid_x * ($spacers + $gridSize), $init_y + $grid_y * ($spacers + $gridSize)];
    }
    sub genSize {
        my ($grid_w, $grid_h) = @_;
        return [$grid_w * $gridSize + $spacers * ($grid_w - 1), $grid_h * $gridSize + $spacers * ($grid_h - 1)];
    }
}

package StarmanJr::GUI;

use base 'Wx::App';

sub OnInit {
    my $frame = StarmanJr::Window->new(generateTitle());
    # show the frame
    $frame->Show(1);
    return 1;
}

sub generateTitle {
    return 'Starman Jr.';
}

package main;
my $app = new StarmanJr::GUI();
# process events
$app->MainLoop;
