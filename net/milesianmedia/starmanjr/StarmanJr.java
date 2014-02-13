package net.milesianmedia.starmanjr;

import net.milesianmedia.starmanjr.TextProcessor;

import java.lang.NumberFormatException;

import java.util.Random;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileNotFoundException;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class StarmanJr extends JFrame {
	private final Dimension BIG_TEXT = new Dimension(639, 75);

	private static String[] lines = null;
	private static int lineNum = 1;
	private static boolean isHex = false;
	private static String filename;
	private final JFileChooser filechooser = new JFileChooser();

	public final String version = "0.4";
	public final String updateDate = "January 29, 2014";

	// this prevents two "bad number" dialogs from showing up in a row if you enter "fjdsiaofj" for the line #
	private static boolean badNumber;

	public StarmanJr () {
		try {
			initUI();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "An error occurred. The exact error is recorded in the console.", "???", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void showHideComp(JComponent comp) {
		comp.setVisible(!comp.isVisible());
	}

	public final void initUI () {

		// init menubar
		JMenuBar menubar = new JMenuBar();
		
		// init icons
		ImageIcon iconOpen = new ImageIcon(getClass().getResource("images/open.png"));
		ImageIcon iconSave = new ImageIcon(getClass().getResource("images/save.png"));
		ImageIcon iconExit = new ImageIcon(getClass().getResource("images/exit.png"));
		ImageIcon iconLeft = new ImageIcon(getClass().getResource("images/arrow-left.png"));
		ImageIcon iconRight= new ImageIcon(getClass().getResource("images/arrow-right.png"));
		ImageIcon iconGear = new ImageIcon(getClass().getResource("images/gear.png"));
		ImageIcon iconFmt  = new ImageIcon(getClass().getResource("images/autoformat.png"));
		ImageIcon iconHex  = new ImageIcon(getClass().getResource("images/numbers-hex.png"));
		ImageIcon iconDec  = new ImageIcon(getClass().getResource("images/numbers-dec.png"));

		ImageIcon logo = new ImageIcon(getClass().getResource("images/logo.gif"));

		final ImageIcon previewFont = new ImageIcon(getClass().getResource("images/font.png"));
		
		// init textareas
		final JTextArea oldTextArea = new JTextArea();
		oldTextArea.setLineWrap(true);
		oldTextArea.setWrapStyleWord(true);
		JScrollPane oldText = new JScrollPane(oldTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		oldText.setPreferredSize(BIG_TEXT);
		oldTextArea.setEnabled(false);
		final JTextArea editTextArea = new JTextArea();
		editTextArea.setLineWrap(true);
		editTextArea.setWrapStyleWord(true);
		JScrollPane editText = new JScrollPane(editTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		editText.setPreferredSize(BIG_TEXT);
		//editTextArea.setEnabled(false);
		final JTextArea commentTextArea = new JTextArea();
		commentTextArea.setLineWrap(true);
		commentTextArea.setWrapStyleWord(true);
		commentTextArea.setEnabled(false);
		//commentTextArea.setPreferredSize(new Dimension(165, 55));
		JScrollPane commentText = new JScrollPane(commentTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		commentText.setPreferredSize(new Dimension(170, 53));
		final JTextField currentLine = new JTextField(); 
		currentLine.setPreferredSize(new Dimension(53, 24));
		currentLine.setEnabled(false);
		
		// init menus
		JMenu file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		//JMenu view = new JMenu("View");
		//view.setMnemonic(KeyEvent.VK_V);
		JMenu insert = new JMenu("Insert");
		insert.setMnemonic(KeyEvent.VK_I);
		JMenu format = new JMenu("Format");
		format.setMnemonic(KeyEvent.VK_O);
		// init submenus of insert
		JMenu insertSymbol = new JMenu("Symbol");
		insertSymbol.setMnemonic(KeyEvent.VK_S);
		JMenu insertCode = new JMenu("Control Code");
		insertCode.setMnemonic(KeyEvent.VK_C);
		
		// init panels
		final JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		selectorPanel.setVisible(false);

		JPanel jumperPanel = new JPanel();
		jumperPanel.setLayout(new GridLayout(2,1,5,5));

		JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		textPanel.setPreferredSize(new Dimension(644,160));

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
		buttonPanel.setPreferredSize(new Dimension(180,160));

		JPanel previewButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

		JPanel previewPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		previewPanel.setBackground(Color.black);
		previewPanel.setPreferredSize(new Dimension(464,140));
		final PreviewFrame previewFrame = new PreviewFrame (previewFont, 28, 2);
		previewFrame.setPreferredSize(new Dimension(464,140));
		//JScrollPane previewScroller = new JScrollPane(previewPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		//previewScroller.setPreferredSize(new Dimension(480, 145));

		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, -5));
		JPanel previewSpacePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

		JPanel paddingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
		paddingPanel.setPreferredSize(new Dimension(100, 0));
		
		JPanel commentSpacingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		
		JPanel baseTogglePanel = new JPanel();
		baseTogglePanel.setLayout(new GridLayout(1, 2, 5, 5));
		
		final JPanel openButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		openButtonPanel.setPreferredSize(new Dimension(174, 53));

		// init buttons
		final JButton prevLine = new JButton(iconLeft);
		prevLine.setPreferredSize(new Dimension(53, 53));
		final JButton nextLine = new JButton(iconRight);
		nextLine.setPreferredSize(new Dimension(53, 53));
		final JButton giantOpenButton = new JButton("Load Script");
		giantOpenButton.setPreferredSize(new Dimension(169, 53));

		//hex/dec toggle buttons
		final JToggleButton hexButton = new JToggleButton(iconHex);
		hexButton.setPreferredSize(new Dimension(24, 24));
		final JToggleButton decButton = new JToggleButton(iconDec);
		decButton.setPreferredSize(new Dimension(24, 24));
		final ButtonGroup baseToggle = new ButtonGroup();
		baseToggle.add(decButton);
		baseToggle.add(hexButton);
		//baseToggle.setSelected(decButton.getModel(), true);

		//JButton jumpButton = new JButton("Go");
		//jumpButton.setPreferredSize(new Dimension(55, 25));
		//jumpButton.setToolTipText("Jump to specified line");
		final JButton previewButton = new JButton("Preview");
		previewButton.setPreferredSize(new Dimension(111,24));
		//previewButton.setEnabled(false);
		final JButton settingsButton = new JButton(iconGear);
		settingsButton.setPreferredSize(new Dimension(24,24));
		//settingsButton.setEnabled(false);
		JButton formatButton = new JButton(iconFmt);
		formatButton.setPreferredSize(new Dimension(24, 24));
		
		// init actionlistener to exit; this is attached to the "file > exit" menu item and some other stuff too
		ActionListener alExit = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		};
		
		// set up the panels
		baseTogglePanel.add(decButton);
		baseTogglePanel.add(hexButton);
		jumperPanel.add(currentLine);
		jumperPanel.add(baseTogglePanel);
		selectorPanel.add(prevLine);
		selectorPanel.add(jumperPanel);
		selectorPanel.add(nextLine);
		openButtonPanel.add(giantOpenButton);
		previewButtonPanel.add(formatButton, BorderLayout.EAST);
		previewButtonPanel.add(previewButton, BorderLayout.EAST);
		previewButtonPanel.add(settingsButton, BorderLayout.CENTER);
		buttonPanel.add(paddingPanel, BorderLayout.NORTH);
		buttonPanel.add(openButtonPanel, BorderLayout.NORTH);
		buttonPanel.add(selectorPanel, BorderLayout.NORTH);
		buttonPanel.add(previewButtonPanel, BorderLayout.CENTER);
		//buttonPanel.add(paddingPanel, BorderLayout.SOUTH);
		commentSpacingPanel.add(commentText);
		buttonPanel.add(commentSpacingPanel, BorderLayout.SOUTH);
		bottomPanel.add(buttonPanel, BorderLayout.WEST);
		//bottomPanel.add(paddingPanel);
		previewSpacePanel.add(previewPanel, BorderLayout.NORTH);
		previewPanel.add(previewFrame);
		//previewPanel.setBounds(174, 0, 638, 150);
		bottomPanel.add(previewSpacePanel, BorderLayout.CENTER);
		textPanel.add(oldText);
		textPanel.add(editText);
		pack();
		//textPanel.add(commentText);

		// button-y things
		currentLine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int oldLine = getLine();
				if (getHex()) {
					try {
						// tries to set line in hex
						setLine(Integer.parseInt(currentLine.getText(), 16) +1);
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null, "That's not a hex number!", ":(", JOptionPane.ERROR_MESSAGE);
						setLine(oldLine);
					}
				} else {
					try {
						// tries to set line in dec
						setLine(Integer.parseInt(currentLine.getText()) +1);
					} catch (NumberFormatException e) {
						try {
							// contains something other than 0-9, but might be a hex number and they forgot to change it, so tries to set line in hex
							setLine(Integer.parseInt(currentLine.getText(), 16) +1);
							setHex(true);
							baseToggle.setSelected(hexButton.getModel(), true);
						} catch (NumberFormatException f) {
							// contains something other than 0-9a-f
							JOptionPane.showMessageDialog(null, "That's not a number! That's not even a hex number!", ":(", JOptionPane.ERROR_MESSAGE);
							setBadNumberFlag();
						}
					}
				}
				try {
					loadLines(lines, getLine(), oldTextArea, editTextArea, commentTextArea);
				} catch (ArrayIndexOutOfBoundsException e) {
					if (!badNumberFlag()) {
						// index out of bounds of all lines
						JOptionPane.showMessageDialog(null, "Sorry, that line number is either negative or too big.", ":(", JOptionPane.ERROR_MESSAGE);
					}
					unsetBadNumberFlag();
					setLine(oldLine);
				}
				nextLine.setEnabled(true);
				prevLine.setEnabled(true);
				if (getLine() == 1) {
					prevLine.setEnabled(false);
				}
				if (getLine() == getNumberOfLines() -1) {
					nextLine.setEnabled(false);
				}
				previewFrame.setText(editTextArea.getText());
				previewFrame.repaint();

			}
		});
		Action goNextLine = new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
				lines[getLine()] = TextProcessor.compileLines(oldTextArea.getText(), editTextArea.getText(), commentTextArea.getText(), getLine());
				setLine(getLine()+1);
				if (getHex()) {
					currentLine.setText(Integer.toHexString(getLine() -1).toUpperCase());
				} else {
					currentLine.setText(Integer.toString(getLine() -1));
				}
				loadLines(lines, getLine(), oldTextArea, editTextArea, commentTextArea);
				prevLine.setEnabled(true);
				if (getLine() == getNumberOfLines() -1) {
					nextLine.setEnabled(false);
				}
				previewFrame.setText(editTextArea.getText());
				previewFrame.repaint();

			}
		};
		Action goPrevLine = new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
				lines[getLine()] = TextProcessor.compileLines(oldTextArea.getText(), editTextArea.getText(), commentTextArea.getText(), getLine());
				setLine(getLine()-1);
				if (getHex()) {
					currentLine.setText(Integer.toHexString(getLine() -1).toUpperCase());
				} else {
					currentLine.setText(Integer.toString(getLine() -1));
				}
				loadLines(lines, getLine(), oldTextArea, editTextArea, commentTextArea);
				nextLine.setEnabled(true);
				if (getLine() == 1) {
					prevLine.setEnabled(false);
				}
				previewFrame.setText(editTextArea.getText());
				previewFrame.repaint();
			}
		};
		nextLine.setAction(goNextLine);
		prevLine.setAction(goPrevLine);
		nextLine.setIcon(iconRight);
		prevLine.setIcon(iconLeft);
		nextLine.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, ActionEvent.CTRL_MASK), "pressed");
		prevLine.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, ActionEvent.CTRL_MASK), "pressed");
		nextLine.getActionMap().put("pressed", goNextLine);
		prevLine.getActionMap().put("pressed", goPrevLine);
		nextLine.setEnabled(false);
		prevLine.setEnabled(false);

		// these have to go before the open dialog action
		final JMenuItem fileSave = new JMenuItem();
		final JMenuItem fileSaveAs = new JMenuItem();
		
		// open dialog
		Action openDialog = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				giantOpenButton.setEnabled(false);
				giantOpenButton.setText("Loading...");
				int didPickFile = filechooser.showOpenDialog(null);
				if (didPickFile == JFileChooser.APPROVE_OPTION) {
					try {
						File file = filechooser.getSelectedFile();
						setFilename(file.getPath());
						lines = TextProcessor.getLineArray(getFilename());
						setLine(1);

						// enable everything...
						nextLine.setEnabled(true);
						//prevLine.setEnabled(true);
						currentLine.setEnabled(true);
						previewButton.setEnabled(true);
						settingsButton.setEnabled(true);
						oldTextArea.setEnabled(true);
						editTextArea.setEnabled(true);
						commentTextArea.setEnabled(true);
						decButton.setEnabled(true);
						hexButton.setEnabled(true);
						baseToggle.setSelected(decButton.getModel(), true);
						fileSave.setEnabled(true);
						fileSaveAs.setEnabled(true);

						openButtonPanel.setVisible(false);
						selectorPanel.setVisible(true);
						loadLines(lines, getLine(), oldTextArea, editTextArea, commentTextArea);
						currentLine.setText(Integer.toString(getLine() - 1));
						System.out.println("Opened "+getFilename()+" <"+file.getPath()+">");
						previewFrame.setText(editTextArea.getText());
						previewFrame.repaint();
					} catch (ArrayIndexOutOfBoundsException f) {
						JOptionPane.showMessageDialog(null, "Are you sure that's a Mother 1+2 script file?", "Unable to open file", JOptionPane.QUESTION_MESSAGE);
						selectorPanel.setVisible(false);
						openButtonPanel.setVisible(true);
						giantOpenButton.setEnabled(true);
						giantOpenButton.setText("Load Script");
					} catch (Exception f) {
						JOptionPane.showMessageDialog(null, "Something went horribly wrong when trying to open this file!"
										+ "\nMost likely, the file no longer exists, or it never did."
										+ "\nIf the file does exist, and you've still somehow received"
										+ "\nthis message, please try again.",
						"Unable to open file", JOptionPane.ERROR_MESSAGE);
						selectorPanel.setVisible(false);
						openButtonPanel.setVisible(true);
						giantOpenButton.setEnabled(true);
						giantOpenButton.setText("Load Script");
					}
				} else {
					System.out.println("Open cancelled.");
					giantOpenButton.setEnabled(true);
					giantOpenButton.setText("Load Script");
				}
			}
		};
		giantOpenButton.setAction(openDialog);
		giantOpenButton.setText("Load Script");
		giantOpenButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK), "pressed");
		giantOpenButton.getActionMap().put("pressed", openDialog);


		// save dialog
		Action saveDialog = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				lines[getLine()] = TextProcessor.compileLines(oldTextArea.getText(), editTextArea.getText(), commentTextArea.getText(), getLine());
				giantOpenButton.setEnabled(false);
				giantOpenButton.setText("Saving...");
				int picked = filechooser.showSaveDialog(null);
				if (picked == JFileChooser.APPROVE_OPTION) {
					try {
						File f = filechooser.getSelectedFile();
						setFilename(f.getPath());
						selectorPanel.setVisible(false);
						openButtonPanel.setVisible(true);
						save(getFilename());
					} catch (Exception f) {
						JOptionPane.showMessageDialog(null, "Something went horribly wrong saving the file!", ":(", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					System.out.println("Save cancelled.");
				}
				openButtonPanel.setVisible(false);
				selectorPanel.setVisible(true);
			}
		};
		// save to already-open file (no dialog)
		Action saveToCurrentFile = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				selectorPanel.setVisible(false);
				openButtonPanel.setVisible(true);
				lines[getLine()] = TextProcessor.compileLines(oldTextArea.getText(), editTextArea.getText(), commentTextArea.getText(), getLine());
				try {
					if (filename != null) {
						giantOpenButton.setEnabled(false);
						giantOpenButton.setText("Saving...");
						save(getFilename());
					} else {
						// there's no file, although how did this happen??
						JOptionPane.showMessageDialog(null, "Don't you want to open a file first? ;)", "Hmm...", JOptionPane.QUESTION_MESSAGE);
					}
				} catch (Exception f) {
					// ???
					JOptionPane.showMessageDialog(null, "Something went horribly wrong saving the file!", ":(", JOptionPane.ERROR_MESSAGE);
				}
				selectorPanel.setVisible(true);
				openButtonPanel.setVisible(false);
			}
		};

		// preview button action
		Action preview = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				previewFrame.setText(editTextArea.getText());
				previewFrame.repaint();
			}
		};
		previewButton.setAction(preview);
		previewButton.setText("Preview");
		previewButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK), "pressed");
		previewButton.getActionMap().put("pressed", preview);

		// autoformat button action
		Action formatAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				editTextArea.setText(TextProcessor.autoFormat(editTextArea.getText(), previewFrame.getCharWidth()));
				previewFrame.setText(editTextArea.getText());
				previewFrame.repaint();
			}
		};
		formatButton.setAction(formatAction);
		formatButton.setIcon(iconFmt);
		formatButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK), "pressed");
		formatButton.getActionMap().put("pressed", formatAction);

		// toggle button thing
		Action numbersToHex = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (!getHex()) {
					currentLine.setText(Integer.toHexString(getLine() -1).toUpperCase());
					setHex(true);
				}
				baseToggle.setSelected(hexButton.getModel(), true);
			}
		};
		Action numbersToDec = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (getHex()) {
					currentLine.setText(Integer.toString(getLine() -1));
					setHex(false);
				}
				baseToggle.setSelected(decButton.getModel(), true);
			}
		};
		hexButton.setAction(numbersToHex);
		decButton.setAction(numbersToDec);
		hexButton.setIcon(iconHex);
		decButton.setIcon(iconDec);
		hexButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK), "pressed");
		hexButton.getActionMap().put("pressed", numbersToHex);
		decButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK), "pressed");
		decButton.getActionMap().put("pressed", numbersToDec);
		decButton.setEnabled(false);
		hexButton.setEnabled(false);

		// these have to go down here after all the input maps because that erases the buttons' properties for some reason??? dumb
		prevLine.setToolTipText("Previous line");
		nextLine.setToolTipText("Next line");
		giantOpenButton.setToolTipText("Load a script file");
		hexButton.setToolTipText("Display line numbers in hexadecimal");
		decButton.setToolTipText("Display line numbers in decimal");
		previewButton.setToolTipText("Update preview");
		settingsButton.setToolTipText("Preview Options");
		formatButton.setToolTipText("Auto-format");

		// about dialog
		final JDialog aboutDialog = new JDialog(this, "About Starman Junior v"+version, true);
		JPanel aboutPanel = new JPanel();
		aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
		JLabel logoLabel = new JLabel(logo);
		logoLabel.setPreferredSize(new Dimension(288, 48));
		JLabel title = new JLabel("Starman Junior", JLabel.CENTER);
		title.setPreferredSize(new Dimension(288, 23));
		title.setFont(new Font("Serif", Font.BOLD, 20));
		JLabel versionLabel = new JLabel("version "+version, JLabel.CENTER);
		versionLabel.setPreferredSize(new Dimension(288, 10));
		versionLabel.setFont(new Font("Sans", Font.ITALIC, 10));
		JLabel disclaimer = new JLabel("<html><center>Programmed by broomweed<br />Based on Mother 1 Funland by JeffMan<br />(and the Mother 1+2 Tools by Tomato)<br />Last updated: "+updateDate+"</center></html>", JLabel.CENTER);
		JButton closeAboutButton = new JButton("Close");
		// make the button go down to the bottom always
		aboutPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		addAndCenter(logoLabel, aboutPanel);
		aboutPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		addAndCenter(title, aboutPanel);
		addAndCenter(versionLabel, aboutPanel);
		aboutPanel.add(Box.createVerticalGlue());
		addAndCenter(disclaimer, aboutPanel);
		aboutPanel.add(Box.createVerticalGlue());
		addAndCenter(closeAboutButton, aboutPanel);
		aboutPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		// actions to show/hide the about dialog
		Action closeAboutAction = new AbstractAction() { public void actionPerformed(ActionEvent e) { aboutDialog.setVisible(false); }};
		Action openAboutAction = new AbstractAction() { public void actionPerformed(ActionEvent e) { aboutDialog.setVisible(true); }};
		closeAboutButton.setAction(closeAboutAction);
		closeAboutButton.setText("Close");
		aboutDialog.add(aboutPanel);
		aboutDialog.setLocationRelativeTo(null);
		aboutDialog.setSize(308, 270);
		aboutDialog.setResizable(false);

		// view options dialog
		final JFrame prefsDialog = new JFrame("Preview Options");
		JPanel prefsDialogPanel = new JPanel();
		JPanel spacerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		Action changePreviewWidth = new AbstractAction() { public void actionPerformed(ActionEvent e) { previewFrame.setCharWidth(Integer.parseInt(e.getActionCommand())); previewFrame.repaint(); }};
		JRadioButton width28 = new JRadioButton("28 characters");
		JLabel width28p2 = new JLabel("      (Mother 1+2 Fan Translation)");
		JRadioButton width20 = new JRadioButton("20 characters");
		JLabel width20p2 = new JLabel("      (EB0, small M1+2 boxes)");
		width28.setSelected(true);
		width28.setActionCommand("28"); 
		width20.setActionCommand("20");
		width28.addActionListener(changePreviewWidth);
		width20.addActionListener(changePreviewWidth);
		ButtonGroup previewWidth = new ButtonGroup(); previewWidth.add(width28); previewWidth.add(width20);
		Action changePreviewScale = new AbstractAction() { public void actionPerformed(ActionEvent e) { previewFrame.setScale(Integer.parseInt(e.getActionCommand())); previewFrame.repaint(); }};
		JRadioButton scale1x = new JRadioButton("1x");
		JRadioButton scale2x = new JRadioButton("2x");
		scale2x.setSelected(true);
		scale1x.setActionCommand("1");
		scale2x.setActionCommand("2");
		scale1x.addActionListener(changePreviewScale);
		scale2x.addActionListener(changePreviewScale);
		ButtonGroup previewScale = new ButtonGroup(); previewScale.add(scale1x); previewScale.add(scale2x);
		JLabel widthLabel = new JLabel("Characters per line");
		JLabel scaleLabel = new JLabel("Preview text scale");
		Action closePrefs = new AbstractAction() { public void actionPerformed(ActionEvent e) { prefsDialog.setVisible(false); }};
		JButton closePrefsWindow = new JButton("Close");
		closePrefsWindow.setAction(closePrefs);
		closePrefsWindow.setText("Close");
		prefsDialogPanel.setLayout(new GridLayout(9, 1, 0, 0));
		prefsDialogPanel.add(widthLabel);
		prefsDialogPanel.add(width28);
		prefsDialogPanel.add(width28p2);
		prefsDialogPanel.add(width20);
		prefsDialogPanel.add(width20p2);
		prefsDialogPanel.add(scaleLabel);
		prefsDialogPanel.add(scale1x);
		prefsDialogPanel.add(scale2x);
		prefsDialogPanel.add(closePrefsWindow);
		settingsButton.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				prefsDialog.setVisible(true);
			}
		});
		spacerPanel.add(prefsDialogPanel);
		prefsDialogPanel.setSize(new Dimension(250, 200));
		prefsDialog.add(spacerPanel);
		prefsDialog.setVisible(false);
		prefsDialog.setLocationRelativeTo(null);
		prefsDialog.setSize(250, 253);

		// file
		JMenuItem fileOpen = new JMenuItem("Load", iconOpen);
		fileOpen.setAction(openDialog);
		fileOpen.setText("Load");
		fileOpen.setIcon(iconOpen);
		fileOpen.setToolTipText("Load a new script file.");
		fileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		fileOpen.setMnemonic(KeyEvent.VK_O);
		// defined earlier
		//JMenuItem fileSave = new JMenuItem("Save", iconSave);
		fileSave.setAction(saveToCurrentFile);
		fileSave.setMnemonic(KeyEvent.VK_S);
		fileSave.setText("Save");
		fileSave.setIcon(iconSave);
		fileSave.setToolTipText("Save the current file.");
		fileSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		// defined earlier
		//JMenuItem fileSaveAs = new JMenuItem("Save as...");
		fileSaveAs.setAction(saveDialog);
		fileSaveAs.setText("Save as...");
		fileSaveAs.setMnemonic(KeyEvent.VK_A);
		fileSaveAs.setToolTipText("Save to a different file.");
		fileSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		fileSave.setEnabled(false);
		fileSaveAs.setEnabled(false);
		JMenuItem fileExit = new JMenuItem("Exit", iconExit);
		fileExit.setMnemonic(KeyEvent.VK_C);
		fileExit.setToolTipText("Exit the application.");
		fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		fileExit.addActionListener(alExit);	// [ FILE ]
		file.add(fileOpen);			// o Load
		file.add(fileSave);			// v Save
		file.add(fileSaveAs);			//   Save as...
		file.addSeparator();			// ------------
		file.add(fileExit);			// x Exit

		// view
		//JCheckBoxMenuItem sbar = new JCheckBoxMenuItem("Show statusbar");
		//sbar.setState(true);
		//sbar.addActionListener(new ActionListener() {
		//	public void actionPerformed(ActionEvent event) {
		//		showHideComponent(statusbar);
		//	}
		//});
		//view.add(sbar);
		//view.add(showtool);

		// insert actions
		// DONE: Replace this with an action command!
		Action insertBracketSymbol = new AbstractAction () { public void actionPerformed (ActionEvent e) { 
			insertBracketThing(editTextArea, e.getActionCommand()); 
		}};

		// insert > symbol
		JMenuItem insertSymbolAlpha = new JMenuItem("Alpha");
		JMenuItem insertSymbolBeta = new JMenuItem("Beta");
		JMenuItem insertSymbolGamma = new JMenuItem("Gamma");
		JMenuItem insertSymbolPi = new JMenuItem("Pi");
		JMenuItem insertSymbolOmega = new JMenuItem("Omega");
		JMenuItem insertSymbolNote = new JMenuItem("Note");
		JMenuItem insertSymbolDoubleZero = new JMenuItem("Double Zero");
		insertSymbolAlpha.setAction(insertBracketSymbol);
		insertSymbolBeta.setAction(insertBracketSymbol);
		insertSymbolGamma.setAction(insertBracketSymbol);
		insertSymbolPi.setAction(insertBracketSymbol);
		insertSymbolOmega.setAction(insertBracketSymbol);
		insertSymbolNote.setAction(insertBracketSymbol);
		insertSymbolDoubleZero.setAction(insertBracketSymbol);
		insertSymbolAlpha.setActionCommand("ALPHA");
		insertSymbolBeta.setActionCommand("BETA");
		insertSymbolGamma.setActionCommand("GAMMA");
		insertSymbolPi.setActionCommand("PIZ");
		insertSymbolOmega.setActionCommand("OMEGA");
		insertSymbolNote.setActionCommand("FF");
		insertSymbolDoubleZero.setActionCommand("DOUBLEZERO");
		insertSymbolAlpha.setText("Alpha");
		insertSymbolBeta.setText("Beta");
		insertSymbolGamma.setText("Gamma");
		insertSymbolPi.setText("Pi");
		insertSymbolOmega.setText("Omega");
		insertSymbolNote.setText("Note");
		insertSymbolDoubleZero.setText("Double Zero");
		insertSymbol.add(insertSymbolAlpha);
		insertSymbol.add(insertSymbolBeta);
		insertSymbol.add(insertSymbolGamma);
		insertSymbol.add(insertSymbolPi);
		insertSymbol.add(insertSymbolOmega);
		insertSymbol.addSeparator();
		insertSymbol.add(insertSymbolNote);
		insertSymbol.add(insertSymbolDoubleZero);
		// insert > control code
		JMenu insertCodeNames = new JMenu("Names");
		JMenuItem iCN10 = new JMenuItem(); iCN10.setAction(insertBracketSymbol); iCN10.setActionCommand("03 10"); iCN10.setText("[03 10] Ninten");
		JMenuItem iCN11 = new JMenuItem(); iCN11.setAction(insertBracketSymbol); iCN11.setActionCommand("03 11"); iCN11.setText("[03 11] Lloyd");
		JMenuItem iCN12 = new JMenuItem(); iCN12.setAction(insertBracketSymbol); iCN12.setActionCommand("03 12"); iCN12.setText("[03 12] Ana");
		JMenuItem iCN13 = new JMenuItem(); iCN13.setAction(insertBracketSymbol); iCN13.setActionCommand("03 13"); iCN13.setText("[03 13] Teddy");
		JMenuItem iCN14 = new JMenuItem(); iCN14.setAction(insertBracketSymbol); iCN14.setActionCommand("03 14"); iCN14.setText("[03 14] Player");
		JMenuItem iCN15 = new JMenuItem(); iCN15.setAction(insertBracketSymbol); iCN15.setActionCommand("03 15"); iCN15.setText("[03 15] Fav. Food");
		JMenuItem iCN16 = new JMenuItem(); iCN16.setAction(insertBracketSymbol); iCN16.setActionCommand("03 16"); iCN16.setText("[03 16] Ninten/Party leader");
		JMenuItem iCN1D = new JMenuItem(); iCN1D.setAction(insertBracketSymbol); iCN1D.setActionCommand("03 1D"); iCN1D.setText("[03 1D] Item"); 
		JMenuItem iCN22 = new JMenuItem(); iCN22.setAction(insertBracketSymbol); iCN22.setActionCommand("03 22"); iCN22.setText("[03 22] Item/PSI used in battle");
		JMenuItem iCN3E = new JMenuItem(); iCN3E.setAction(insertBracketSymbol); iCN3E.setActionCommand("03 3E"); iCN3E.setText("[03 3E] Party leader");
		JMenuItem[] codeNames = {iCN10, iCN11, iCN12, iCN13, iCN14, iCN15, iCN16, iCN1D, iCN22, iCN3E}; for (int i = 0; i < codeNames.length; i++) { insertCodeNames.add(codeNames[i]); }
		JMenu insertCodeNumbers = new JMenu("Numbers");
		JMenuItem iCN18 = new JMenuItem(); iCN18.setAction(insertBracketSymbol); iCN18.setActionCommand("03 18"); iCN18.setText("[03 18] $ deposited since last call");
		JMenuItem iCN19 = new JMenuItem(); iCN19.setAction(insertBracketSymbol); iCN19.setActionCommand("03 19"); iCN19.setText("[03 19] ATM balance");
		JMenuItem iCN1E = new JMenuItem(); iCN1E.setAction(insertBracketSymbol); iCN1E.setActionCommand("03 1E"); iCN1E.setText("[03 1E] Price; level-up EXP; HP/PP/stat change amt.");
		JMenuItem iCN23 = new JMenuItem(); iCN23.setAction(insertBracketSymbol); iCN23.setActionCommand("03 23"); iCN23.setText("[03 23] HP damage/stat change amt./stat increase");
		JMenuItem[] codeNumbers = {iCN18, iCN19, iCN1E, iCN23}; for (int i = 0; i < codeNumbers.length; i++) { insertCodeNumbers.add(codeNumbers[i]); }
		JMenu insertCodeActions = new JMenu("Action things");
		JMenuItem iCN1A = new JMenuItem(); iCN1A.setAction(insertBracketSymbol); iCN1A.setActionCommand("03 1A"); iCN1A.setText("[03 1A] Action subject character");
		JMenuItem iCN1B = new JMenuItem(); iCN1B.setAction(insertBracketSymbol); iCN1B.setActionCommand("03 1B"); iCN1B.setText("[03 1B] Action object character");
		JMenuItem iCN1C = new JMenuItem(); iCN1C.setAction(insertBracketSymbol); iCN1C.setActionCommand("03 1C"); iCN1C.setText("[03 1C] Item/PSI used");
		JMenuItem iCN20 = new JMenuItem(); iCN20.setAction(insertBracketSymbol); iCN20.setActionCommand("03 20"); iCN20.setText("[03 20] Battle action subject");
		JMenuItem iCN21 = new JMenuItem(); iCN21.setAction(insertBracketSymbol); iCN21.setActionCommand("03 21"); iCN21.setText("[03 21] Battle action object");
		JMenuItem[] codeActions = {iCN1A, iCN1B, iCN1C, iCN20, iCN21}; for (int i = 0; i < codeNumbers.length; i++) { insertCodeActions.add(codeActions[i]); }
		JMenu insertCodeEtc = new JMenu("Etc.");
		JMenuItem iCN17 = new JMenuItem(); iCN17.setAction(insertBracketSymbol); iCN17.setActionCommand("03 17"); iCN17.setText("[03 17] 's party (if multiple members)");
		JMenuItem iCN3C = new JMenuItem(); iCN3C.setAction(insertBracketSymbol); iCN3C.setActionCommand("03 3C"); iCN3C.setText("[03 3C] Right-pointing triangle");
		JMenuItem iCN3F = new JMenuItem(); iCN3F.setAction(insertBracketSymbol); iCN3F.setActionCommand("03 3F"); iCN3F.setText("[03 3F] Copyright comma");
		JMenuItem iCNF0 = new JMenuItem(); iCNF0.setAction(insertBracketSymbol); iCNF0.setActionCommand("03 F0"); iCNF0.setText("[03 F0] Definite item article");
		JMenuItem iCNF1 = new JMenuItem(); iCNF1.setAction(insertBracketSymbol); iCNF1.setActionCommand("03 F1"); iCNF1.setText("[03 F1] Indefinite item article");
		JMenuItem[] codeEtc = {iCN17, iCN3C, iCN3F, iCNF0, iCNF1}; for (int i = 0; i < codeEtc.length; i++) { insertCodeEtc.add(codeEtc[i]); }
		insertCode.add(insertCodeNames);
		insertCode.add(insertCodeNumbers);
		insertCode.add(insertCodeActions);
		insertCode.add(insertCodeEtc);
		// insert
		JMenuItem insertPause = new JMenuItem("Pause");
		insertPause.setAction(insertBracketSymbol);
		insertPause.setActionCommand("PAUSE");
		insertPause.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		insertPause.setText("Pause");
		JMenuItem insertBreak = new JMenuItem("Break");
		insertBreak.setAction(insertBracketSymbol);
		insertBreak.setActionCommand("BREAK");
		insertBreak.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
		insertBreak.setText("Break");
		insert.add(insertSymbol);
		insert.add(insertCode);
		insert.addSeparator();
		insert.add(insertPause);
		insert.add(insertBreak);

		// format
		Action loadFont = new AbstractAction() { public void actionPerformed(ActionEvent e) { 
			int picked = filechooser.showOpenDialog(null);
			if (picked == JFileChooser.APPROVE_OPTION) {
				try {
					File newFont = filechooser.getSelectedFile();
					ImageIcon font = new ImageIcon(getClass().getResource(newFont.getPath()));
					previewFrame.setFontImg(font);
				} catch (Exception f) {
					JOptionPane.showMessageDialog(null, "Unable to open file.", "Couldn't change font", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				System.out.println("Font open cancelled.");
			}
		}};
		Action resetFont = new AbstractAction() { public void actionPerformed(ActionEvent e) { previewFrame.setFontImg(previewFont); }};
		Action stripBreaks = new AbstractAction() { public void actionPerformed(ActionEvent e) { editTextArea.setText(editTextArea.getText().replace("[BREAK]", " ")); 
													 editTextArea.setText(editTextArea.getText().replace(" [PAUSE]","[PAUSE]")); }};
		JMenuItem fontLoad = new JMenuItem();
		fontLoad.setAction(loadFont);
		fontLoad.setText("Load preview font...");
		JMenuItem fontReset = new JMenuItem();
		fontReset.setAction(resetFont);
		fontReset.setText("Reset preview font");
		JMenuItem breakStrip = new JMenuItem();
		breakStrip.setAction(stripBreaks);
		breakStrip.setText("Strip [BREAK]s from text");
		JMenuItem menuAutoFormat = new JMenuItem();
		menuAutoFormat.setAction(formatAction);
		menuAutoFormat.setText("Auto-format");
		format.add(fontLoad);
		format.add(fontReset);
		format.addSeparator();
		format.add(breakStrip);
		format.add(menuAutoFormat);

		// help
		JMenu help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);
		JMenuItem aboutItem = new JMenuItem();
		aboutItem.setAction(openAboutAction);
		aboutItem.setText("About");
		help.add(aboutItem);

		// main menu bar
		menubar.add(file);
		//menubar.add(view);
		menubar.add(insert);
		menubar.add(format);
		menubar.add(help);
		setJMenuBar(menubar);
		add(bottomPanel, BorderLayout.SOUTH);
		//panel		w	h
		// textPanel	644	160
		// buttonPanel	180	160
		// previewPanel	464	135
		previewPanel.setBounds(185, 165, 649, 325);
		add(textPanel, BorderLayout.NORTH);
		add(bottomPanel, BorderLayout.CENTER);
		pack();
		
		// init window
		setTitle(generateTitle() + "v" + version);
		setSize(657, 354);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public static void main (String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				StarmanJr ex = new StarmanJr();
				ex.setVisible(true);
			}
		});
	}

	public final void loadLines (String[] lines, int index, JTextArea origArea, JTextArea editArea, JTextArea commArea) {
		origArea.setText(TextProcessor.getOrigLine(lines, index));
		editArea.setText(TextProcessor.getEditLine(lines, index));
		commArea.setText(TextProcessor.getCommentLine(lines, index));
	}

	public final void insertBracketThing (JTextArea a, String t) {
		a.replaceSelection("["+t+"]");
	}

	public final void addAndCenter(JComponent comp, Container cont) {
		comp.setAlignmentX(Component.CENTER_ALIGNMENT);
		cont.add(comp);
	}

	// getters and setters to prevent dumb final variables that don't work
	public static void setLine (int i) {
		lineNum = i;
	}
	public static int getLine () {
		return lineNum;
	}
	public static String getLineAt (int i) {
		return lines[i];
	}
	public static void setHex (boolean h) {
		isHex = h;
	}
	public static boolean getHex () {
		return isHex;
	}
	public static void setFilename (String f) {
		filename = f;
	}
	public static String getFilename () {
		return filename;
	}
	public static int getNumberOfLines () {
		return lines.length;
	}
	public static void setBadNumberFlag() { badNumber = true; }
	public static void unsetBadNumberFlag() { badNumber = false; }
	public static boolean badNumberFlag() { return badNumber; }

	// this saves the text to a certain filename ("save as" makes you choose, "save" uses the default)
	public void save (String filenameToSaveTo) throws FileNotFoundException {
		String toSave = "";
		for (int i = 1; i < getNumberOfLines(); i++) {
			toSave += getLineAt(i);
		}
		PrintWriter out = new PrintWriter(filenameToSaveTo);
		out.println(toSave);
		// heh heh whoops this line is important
		out.close();
	}

	// this generates a random silly title
	public String generateTitle() {
		Random r = new Random();
		String[] forms = {"AN", "AAN", "ANN", "NN", "BAN", "BA"};
		String[] starters = {"Oh Look, It's", "Extremely", "More Than Just", "My", "The", "Just Kidding, It's Not Really", "Hey! It's", "PK", "What the Heck Is", "The", "My", "Who Created", "The Abomination That Is", "Good Ol'"};
		String[] adjectives = {"Super", "Healthy", "Holy", "Unholy", "Abominable", "Awesome", "Fun", "Crazy", "Confusing", "Official", "Unofficial", "Useful", "Picturesque", "Accidental", "PK", "Fresh", "Irreparable", "Common", "Rare", "Stupid", "Intelligent", "Good", "Bad", "Evil", "Worldly", "Total", "Extreme", "X-Treme", "Smilin'", "Frownin'"};
		String[] nouns = {"Fish", "Creator", "Replacer", "Editor", "Abomination", "Ice Cream", "Script Editor", "Script", "Word", "God", "Father", "Mother", "Planet", "City", "Funland", "Hack", "Text", "Cow", "Scent", "Cat", "Fur", "Replacement", "Extra", "Bonus", "Button", "Comma", "Punctuation Mark", "Ball", "Sphere", "String", "Nose", "Eye", "Face"};
		String formChosen = forms[r.nextInt(forms.length)];
		String title = "";
		for (int i = 0; i < formChosen.length(); i++) {
			if (formChosen.charAt(i) == 'A') {
				title += adjectives[r.nextInt(adjectives.length)]+ " ";
			} else if (formChosen.charAt(i) == 'B') {
				title += starters[r.nextInt(starters.length)] + " ";
			} else {
				title += nouns[r.nextInt(nouns.length)]+ " ";
			}
		}
		return title;
	}

	static class PreviewFrame extends JComponent {
		private String text = "";
		private Image font;
		private ImageIcon fontIcon;
		private int charWidth, scale;
		private String fontDecoder = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789?<$*\"'():;,-./!=_|{}^%@ []>+";
		public void setText(String t) { 
			text = t;
			text = text.replaceAll("(?i)\\[ALPHA]", "=");
			text = text.replaceAll("(?i)\\[BETA]", "_");
			text = text.replaceAll("(?i)\\[GAMMA]", "|");
			text = text.replaceAll("(?i)\\[PIZ]", "{");
			text = text.replaceAll("(?i)\\[OMEGA]", "}");
			text = text.replaceAll("(?i)\\[FF]", "^");
			text = text.replaceAll("(?i)\\[DOUBLEZERO]", "%");
			text = text.replaceAll("(?i)\\[BREAK]", "\n");
			text = text.replaceAll("(?i)\\[PAUSE]", "+");
			text = text.replace("[03 01]", "\n&[ Battle action");
			for (int i = 2; i < 16; i++) {
				text = text.replace("[03 0"+Integer.toHexString(i)+"]", "&]");
			}
			text = text.replace("[03 10]", "&N&i&n&t&e&n");
			text = text.replace("[03 11]", "&L&l&o&i&y&d");
			text = text.replace("[03 12]", "&A&n&n&n&n&a");
			text = text.replace("[03 13]", "&T&e&d&d&d&y");
			text = text.replace("[03 14]", "&(&N&a&m&e&)");
			text = text.replace("[03 15]", "&F&o&o&o&o&d");
			text = text.replace("[03 16]", "&N&i&n&t&e&n");
			text = text.replace("[03 17]", "&'&s& &p&a&r&t&y");
			text = text.replace("[03 18]", "&a&m&o&u&n&t");
			text = text.replace("[03 19]", "&b&a&l&a&n&c&e");
			text = text.replace("[03 1A]", "&M&e&m&b&e&r");
			text = text.replace("[03 1B]", "&P&e&r&s&o&n");
			text = text.replace("[03 1C]", "&(&I&t&e&m& &N&a&m&e&)");
			text = text.replace("[03 1D]", "&(&I&t&e&m& &N&a&m&e&)");
			text = text.replace("[03 1E]", "&a&m&o&u&n&t");
			text = text.replace("[03 1F]", "&]");
			text = text.replace("[03 20]", "&S&o&m&e&o&n&e");
			text = text.replace("[03 21]", "&S&o&m&e&o&n&e");
			text = text.replace("[03 22]", "&T&h&i&n&g");
			text = text.replace("[03 23]", "&a&m&o&u&n&t");
			for (int i = 36; i < 44; i++) {
				text = text.replace("[03 "+Integer.toHexString(i)+"]", "&]");
			}
			text = text.replace("[03 2C]", "\n&[ Battle action?");
			for (int i = 44; i < 60; i++) {
				text = text.replace("[03 "+Integer.toHexString(i)+"]", "&]");
			}
			text = text.replace("[03 3C]", ">");
			text = text.replace("[03 3D]", "&]");
			text = text.replace("[03 3E]", "&M&e&m&b&e&r");
			text = text.replace("[03 3F]", "&,");
			text = text.replace("[03 F0]", "&t&h&e& ");
			text = text.replace("[03 F1]", "&a&(&n&)& ");
			//setSize(8*scale*charWidth, 8*scale*figureOutNumberOfLines());
			//panel.setSize(8*scale*charWidth, 8*scale*figureOutNumberOfLines());
			//System.out.println("W:"+getWidth()+" H:"+getHeight()+" | W:"+panel.getWidth()+" H:"+getHeight());
			repaint();
		}
		public String getText() { return text; }
		public void setFontImg(ImageIcon i) { fontIcon = i; font = fontIcon.getImage(); }
		public ImageIcon getFontImg() { return fontIcon; }
		public void setCharWidth(int w) { charWidth = w; }
		public int getCharWidth() { return charWidth; }
		public void setScale(int s) { scale = s; }
		public int getScale() { return scale; }
	
		public PreviewFrame (ImageIcon font, int width, int scale) {
			this.fontIcon = font;
			this.font = fontIcon.getImage();
			this.charWidth = width;
			this.scale = scale;
			//this.setOpaque(true);
			//this.setBackground(Color.black);
		}

		private int figureOutNumberOfLines() {
			String[] s = text.split("\n");
			int n = 0;
			for (int i = 0; i < s.length; i++) {
				if (s[i] != null) {
					double r = (double) s[i].length()/charWidth;
					n += Math.ceil(r);
				}
			}
			return n;
		}
	
		public void paintComponent (Graphics g) {
			int x = 1, y = 0, verticalFontOffset = 0, pauses = 0;
			boolean faded = false;
			//g.clearRect(0, 0, getWidth(), getHeight());
			for(int i = 0; i < text.length(); i++) {
				char charToDraw = text.charAt(i);
				//System.out.print(charToDraw);
				int charID = fontDecoder.indexOf(charToDraw);
				if (x == charWidth+1 && charToDraw != '\n') {
					x = 1;
					y++;
					verticalFontOffset = 8;
				}
				if (charToDraw == '@') {
					x--;
				}
				if (charToDraw == '\n') {
					x = 0;
					y++;
					verticalFontOffset = 0;
				} else if (charToDraw == '+') {
					pauses++;
					for (int j = 0; j < 58/scale; j++) {
						g.drawImage(font, 8*scale*j, scale*(8*y+pauses)+2*pauses-1, 8*scale*(j+1), scale*(8*(y+1)+pauses)+2*pauses-1, 8*charID, verticalFontOffset, 8*(charID+1), verticalFontOffset+8, null);
					}
					x--;
				} else if (charToDraw == '&') {
					verticalFontOffset += 16;
					faded = true;
					x--;
				} else {
					g.drawImage(font, 8*scale*x, scale*(8*y+pauses+1)+2*pauses, 8*scale*(x+1), scale*(8*(y+1)+pauses+1)+2*pauses, 8*charID, verticalFontOffset, 8*(charID+1), verticalFontOffset+8, null);
				}
				if (charToDraw != '&' && faded) {
					verticalFontOffset -= 16;
					faded = false;
				}
				x++;
			}
			//panel.setSize(8*scale*charWidth, 8*scale*(y+1));
			//System.out.println("window size should now be "+8*scale*(y+1));
			//panel.setHeight(8*scale*(y+1));
		}
	}
}
