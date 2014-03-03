package net.milesianmedia.starmanjr;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.regex.*;
import java.util.Scanner;
import java.util.Arrays;

public class TextProcessor {
	public static String compileLines(String o, String e, String c, int i) {
		return addNumPrefix(o, i) +"\n"+ addNumEPrefix(e, i) +"\n"+ c;
	}

	public static String[] getLineArray(String filename) {
		BufferedReader bufferedReader = null, lineTest = null;
		String outThing = "";
		//String[] returner = null;

		try {
			bufferedReader = new BufferedReader(new FileReader(filename));
			lineTest = new BufferedReader(new FileReader(filename));

			String testline = null;
			while ((testline = lineTest.readLine()) != null) {
				outThing += testline + "\n";
			}
			lineTest.close();
			String[] returner = Pattern.compile("[0-9A-F][0-9A-F][0-9A-F]: ").split(outThing);
			for (int i = 0; i < returner.length; i++) {
				returner[i] = addNumPrefix(returner[i], i);
			}
			return returner;
		} catch (FileNotFoundException ex) {
			System.out.println("that's not a thing!!");
			return null;
		} catch (IOException ex) {
			System.out.println("something bad happened :(");
			return null;
		}
	}

	public static String getOrigLine(String[] whatnot, int lineNum) {
		return removeFirstWord(whatnot[lineNum].split("\n")[0]);
	}

	public static String getEditLine(String[] whatnot, int lineNum) {
		return removeFirstWord(whatnot[lineNum].split("\n")[1]);
	}

	public static String getCommentLine(String[] whatnot, int lineNum) {
		String comment = "";
		if (whatnot[lineNum].split("\n").length >= 3) {
			String[] cut = Arrays.copyOfRange(whatnot[lineNum].split("\n"), 2, whatnot[lineNum].split("\n").length);
			for(int i = 0; i < cut.length; i++) {
				comment += cut[i] + "\n";
			}
		}
		return comment;
	}

	public static String removeFirstWord(String thing) {
		String returner = "";
		String[] trunc = Arrays.copyOfRange(thing.split(" "), 1, thing.split(" ").length);
		for (int i = 0; i < trunc.length; i++) {
			returner += trunc[i] + " ";
		}
		if (returner.length() > 0) {
			return returner.substring(0, returner.length() - 1);
		} else {
			return "";
		}
	}

	public static String addNumPrefix(String thing, int num) {
		String x = Integer.toString(num-1, 16).toUpperCase();
		while (x.length() < 3) { x = "0"+x; }
		return x + ": "+thing;
	}

	public static String addNumEPrefix(String thing, int num) {
		String x = Integer.toString(num-1, 16).toUpperCase();
		while (x.length() < 3) { x = "0"+x; }
		return x + "-E: "+thing;
	}

	public static String autoFormat(String thing, int width) {
		thing = thing.replaceAll("(?i)\\[BREAK]", "\n");
		thing = thing.replaceAll("\\[03 ([0-9A-F][0-9A-F])]", "\\[03$1]");
		thing = thing.replaceAll("(?i)\\[PAUSE]", "\n[PAUSE]");
		String sthing = thing.replace("[PAUSE]", "");
		sthing = sthing.replace("[0310]", "Ninten");
		sthing = sthing.replace("[0311]", "Lloiyd");
		sthing = sthing.replace("[0312]", "Annnna");
		sthing = sthing.replace("[0313]", "Tedddy");
		sthing = sthing.replace("[0314]", "(Name)");
		sthing = sthing.replace("[0315]", "Fooood");
		sthing = sthing.replace("[0316]", "Ninten");
		sthing = sthing.replace("[0317]", "'s party");
		sthing = sthing.replace("[0318]", "amount");
		sthing = sthing.replace("[0319]", "balance");
		sthing = sthing.replace("[031A]", "Member");
		sthing = sthing.replace("[031B]", "Person");
		sthing = sthing.replace("[031C]", "(Item Name)");
		sthing = sthing.replace("[031D]", "(Item Name)");
		sthing = sthing.replace("[031E]", "amount");
		sthing = sthing.replace("[031F]", "]");
		sthing = sthing.replace("[0320]", "Someone");
		sthing = sthing.replace("[0321]", "Someone");
		sthing = sthing.replace("[0322]", "Thing");
		sthing = sthing.replace("[0323]", "amount");
		sthing = sthing.replace("[033C]", ">");
		sthing = sthing.replace("[033E]", "Dudes!");
		sthing = sthing.replace("[033F]", ",");
		sthing = sthing.replaceAll("(?i)\\[ALPHA]", "=");
		sthing = sthing.replaceAll("(?i)\\[BETA]", "_");
		sthing = sthing.replaceAll("(?i)\\[GAMMA]", "|");
		sthing = sthing.replaceAll("(?i)\\[PIZ]", "{");
		sthing = sthing.replaceAll("(?i)\\[OMEGA]", "}");
		sthing = sthing.replaceAll("(?i)\\[FF]", "^");
		sthing = sthing.replaceAll("(?i)\\[DOUBLEZERO]", "%");
		String[] split = thing.split("\n");
		String[] ssplit = sthing.split("\n");
		String end = "";
		int count = 0;
		// for each line...
		for (int i = 0; i < split.length; i++) {
			// split the line into words, one with the right lengths and one with the right codes
			String[] supersplit = split[i].split(" ");
			String[] superssplit = ssplit[i].split(" ");
			// for each word...
			for (int j = 0; j < supersplit.length; j++) {
				// if the word starts with @ move back a space b/c that's how it works
				if (superssplit[j].startsWith("@")) count --;
				// add the word's length to the char counter
				count += superssplit[j].length();
				// if we're not on the last word of the line (otherwise it freaks out)
				if (j + 1 < superssplit.length) {
					// if a space plus the next word would fit...
					if (count + superssplit[j+1].length() <= width -1) {
						// add this word plus a space
						end += supersplit[j] + " ";
						// account for the spaces in the character counter
						count++;
					// if it just plain doesn't fit on a single line...
					} else if (superssplit[j].length() > width) {
						for (int k = 0; k < superssplit[j].length(); k+=width) {
							if (superssplit[j].startsWith("@") && k == 0) {
								end += "\n"+supersplit[j].substring(k, Math.min(superssplit[j].length(), k + width + 1));
								k++;
							} else {
								end += "\n"+supersplit[j].substring(k, Math.min(superssplit[j].length(), k + width));
							}
						}
						count = superssplit[j].length() % width;
						if (count + superssplit[j+1].length() <= width -1) {
							end += " ";
						}
					// if a space + the next word DOESN'T fit...
					} else {
						// ...but it would fit on a single new line
						if (superssplit[j+1].length() < width) {
							// add this word plus a newline
							end += supersplit[j] + "\n";
						// otherwise it'll do the above case
						} else {
							// which does its own newlines so we don't need that
							end += supersplit[j];
						}
						// reset the counter
						count = 0;
					}
				// if we ARE on the last word of the line, but NOT the last line...
				} else {
						// if the LAST word is too big for the line, then do this
						if (superssplit[j].length() > width) {
							for (int k = 0; k < superssplit[j].length(); k+=width) {
								if (superssplit[j].startsWith("@") && k == 0) {
									end += "\n"+supersplit[j].substring(k, Math.min(superssplit[j].length(), k + width + 1));
									k++;
								} else {
									end += "\n"+supersplit[j].substring(k, Math.min(superssplit[j].length(), k + width));
								}
							}
							count = superssplit[j].length() % width;
						// otherwise act normal
						} else {
							end += supersplit[j];
						}
				}
			}
			if (i < split.length - 1) {
				end += "\n";
				count = 0;
			}
		}
		end = end.replaceAll("\\[03([0-9A-F][0-9A-F])]", "[03 $1]");
		end = end.replace("\n", "[BREAK]");
		end = end.replaceAll("(?i) \\[PAUSE]", "[PAUSE]");
		end = end.replace("[PAUSE][BREAK]", "[PAUSE]");
		end = end.replace("[PAUSE]", "[BREAK][PAUSE]");
		end = end.replace("[BREAK][BREAK]", "[BREAK]");
		end = end.replace("[BREAK][BREAK]", "[BREAK]");
		if (!end.toLowerCase().endsWith("[break]")) {
			// i.e. if end doesn't end with "[BREAK]," case-insensitive
			end += "[BREAK]";
		}
		return end;
	}
}
