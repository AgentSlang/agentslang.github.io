/*
 * Copyright (c) Ovidiu Serban, ovidiu@roboslang.org
 *               web:http://ovidiu.roboslang.org/
 * All Rights Reserved. Use is subject to license terms.
 *
 * This file is part of AgentSlang Project (http://agent.roboslang.org/).
 *
 * AgentSlang is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License and CECILL-B.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * The CECILL-B license file should be a part of this project. If not,
 * it could be obtained at  <http://www.cecill.info/>.
 *
 * The usage of this project makes mandatory the authors citation in
 * any scientific publication or technical reports. For websites or
 * research projects the AgentSlang website and logo needs to be linked
 * in a visible area.
 */

package org.syn.n.bad.annotation;

import java.util.*;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/9/12
 */
public class TextAnnotationConstants {
    public static final String POS = "POS"; // Part Of Speech
    public static final String CHK = "CHK"; // Chunking
    public static final String NER = "NER"; // Name entity Recognition
    public static final String SRL = "SRL"; // Semantic Role Labeling
    public static final String PSG = "PSG"; // Syntactic Parsing
    public static final String DUN = "DUN"; // Dialogue Units Notation
    //--
    private static final Map<String, Integer> annotationLevels = new HashMap<String, Integer>();

    static {
        annotationLevels.put(POS, 0);
        annotationLevels.put(CHK, 1);
        annotationLevels.put(NER, 2);
        annotationLevels.put(SRL, 3);
        annotationLevels.put(PSG, 4);
        annotationLevels.put(DUN, 5);
    }

    //---- POS Mappings
    private static final Map<String, Byte> posLabels = new HashMap<String, Byte>();
    private static final Map<Byte, String> posIndex = new HashMap<Byte, String>();
    private static final Map<String, Set<Byte>> genericPOSLabel = new HashMap<String, Set<Byte>>();
    private static final Map<Byte, String> genericPOSIndex = new HashMap<Byte, String>();
    //---- Chunking Mappings
    private static final Map<String, Byte> chkLabels = new HashMap<String, Byte>();
    private static final Map<Byte, String> chkIndex = new HashMap<Byte, String>();
    //---- NER Mappings
    private static final Map<String, Byte> nerLabels = new HashMap<String, Byte>();
    private static final Map<Byte, String> nerIndex = new HashMap<Byte, String>();

    public static int getLevel(String annotationType) {
        return annotationLevels.get(annotationType);
    }

    public static byte transformAnnotationLabel(int level, String label) {
        try {
            switch (level) {
                case 0: //POS
                    return posLabels.get(label);
                case 1: //CHK
                    return chkLabels.get(label);
                case 2: //NER
                    return nerLabels.get(label);
                default:
                    return 0;
            }
        } catch (NullPointerException e) {
            System.err.println("Invalid label: " + label);
            return -1;
        }
    }

    public static String transformAnnotationLabel(int level, byte label) {
        try {
            switch (level) {
                case 0: //POS
                    return posIndex.get(label);
                case 1: //CHK
                    return chkIndex.get(label);
                case 2: //NER
                    return nerIndex.get(label);
                default:
                    return "";
            }
        } catch (NullPointerException e) {
            System.err.println("Invalid label: " + label);
            return "";
        }
    }

    public static Set<Byte> getGenericPOSLabel(String pattern) {
        if (pattern.endsWith("*")) {
            Set<Byte> result = genericPOSLabel.get(pattern);
            if (result != null) {
                return result;
            } else {
                pattern = pattern.substring(0, pattern.indexOf('*') - 1);
                Byte res = posLabels.get(pattern);
                if (res != null) {
                    return new HashSet<Byte>(Arrays.asList(res));
                } else {
                    return new HashSet<Byte>();
                }
            }
        } else {
            return new HashSet<Byte>(Arrays.asList(posLabels.get(pattern)));
        }
    }

    public static String getGenericPOSLabel(byte index) {
        return genericPOSIndex.get(index);
    }

    private static void addPOSLabel(String label, int index) {
        posLabels.put(label, (byte) index);
        posIndex.put((byte) index, label);
    }

    private static void addGenericPOSLabel(String label, int... indexes) {
        Set<Byte> set = new HashSet<Byte>();
        for (int index : indexes) {
            set.add((byte) index);
            genericPOSIndex.put((byte) index, label);
        }
        genericPOSLabel.put(label, set);
    }

    static {
        //English PENN Tagset
        addPOSLabel("CC", 1);
        addPOSLabel("CD", 2);
        addPOSLabel("DT", 3);
        addPOSLabel("EX", 4);
        addPOSLabel("FW", 5);
        addPOSLabel("IN", 6);

        addPOSLabel("JJ", 7);
        addPOSLabel("JJR", 8);
        addPOSLabel("JJS", 9);

        addPOSLabel("LS", 10);
        addPOSLabel("MD", 11);

        addPOSLabel("NN", 12);
        addPOSLabel("NNS", 13);
        addPOSLabel("NNP", 14);
        addPOSLabel("NNPS", 15);

        addPOSLabel("PDT", 16);
        addPOSLabel("POS", 17);
        addPOSLabel("PRP", 18);
        addPOSLabel("PP$", 19);
        addPOSLabel("PP", 19);

        addPOSLabel("RB", 20);
        addPOSLabel("RBR", 21);
        addPOSLabel("RBS", 22);
        addPOSLabel("RP", 23);

        addPOSLabel("SYM", 24);
        addPOSLabel("TO", 25);
        addPOSLabel("UH", 26);

        addPOSLabel("VB", 27);
        addPOSLabel("VBD", 28);
        addPOSLabel("VBG", 29);
        addPOSLabel("VBN", 30);
        addPOSLabel("VBP", 31);
        addPOSLabel("VBZ", 32);

        addPOSLabel("WDT", 33);
        addPOSLabel("WP", 34);
        addPOSLabel("WP$", 35);
        addPOSLabel("WRB", 36);

        addPOSLabel("#", 37);
        addPOSLabel("$", 38);
        addPOSLabel(".", 39);
        addPOSLabel(",", 40);
        addPOSLabel(":", 41);
        addPOSLabel("(", 42);
        addPOSLabel(")", 43);
        addPOSLabel("\"", 44);
        addPOSLabel("\'", 45);

        //French Tagset
        addPOSLabel("ABR", 50);

        addPOSLabel("ADJ", 51);

        addPOSLabel("ADV", 52);

        addPOSLabel("DET:ART", 53);
        addPOSLabel("DET:POS", 54);

        addPOSLabel("INT", 55);
        addPOSLabel("KON", 56);

        addPOSLabel("NAM", 57);
        addPOSLabel("NOM", 58);

        addPOSLabel("NUM", 59);

        addPOSLabel("PRO", 60);
        addPOSLabel("PRO:DEM", 61);
        addPOSLabel("PRO:IND", 62);
        addPOSLabel("PRO:PER", 63);
        addPOSLabel("PRO:POS", 64);
        addPOSLabel("PRO:REL", 65);

        addPOSLabel("PRP", 66);
        addPOSLabel("PRP:det", 67);

        addPOSLabel("PUN", 68);
        addPOSLabel("SENT", 69);
        addPOSLabel("SYM", 70);

        addPOSLabel("VER:cond", 71);
        addPOSLabel("VER:futu", 72);
        addPOSLabel("VER:impe", 73);
        addPOSLabel("VER:impf", 74);
        addPOSLabel("VER:infi", 75);
        addPOSLabel("VER:pper", 76);
        addPOSLabel("VER:ppre", 77);
        addPOSLabel("VER:pres", 78);
        addPOSLabel("VER:simp", 79);
        addPOSLabel("VER:subi", 80);
        addPOSLabel("VER:subp", 81);

        //-- Generic TagSet
        addGenericPOSLabel("NN*", 12, 13, 14, 15, 57, 58);
        addGenericPOSLabel("JJ*", 7, 8, 9, 51);
        addGenericPOSLabel("RB*", 20, 21, 22, 52);
        addGenericPOSLabel("VB*", 27, 28, 29, 30, 31, 32, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81);
        addGenericPOSLabel("#*", 37, 38, 39, 40, 41, 42, 43, 44, 45, 68, 69, 70);
    }

    private static void addCHKLabel(String label, int index) {
        chkLabels.put(label, (byte) index);
        chkIndex.put((byte) index, label);
    }

    static {
        addCHKLabel("O", -1);
        addCHKLabel("0", -1);

        addCHKLabel("B-NP", 2);
        addCHKLabel("I-NP", 3);
        addCHKLabel("E-NP", 4);
        addCHKLabel("S-NP", 5);

        addCHKLabel("B-VP", 6);
        addCHKLabel("I-VP", 7);
        addCHKLabel("E-VP", 8);
        addCHKLabel("S-VP", 9);

        addCHKLabel("B-PP", 10);
        addCHKLabel("I-PP", 11);
        addCHKLabel("E-PP", 12);
        addCHKLabel("S-PP", 13);

        addCHKLabel("B-SBAR", 14);
        addCHKLabel("I-SBAR", 15);
        addCHKLabel("E-SBAR", 16);
        addCHKLabel("S-SBAR", 17);

        addCHKLabel("B-ADJP", 18);
        addCHKLabel("I-ADJP", 19);
        addCHKLabel("E-ADJP", 20);
        addCHKLabel("S-ADJP", 21);

        addCHKLabel("B-ADVP", 22);
        addCHKLabel("I-ADVP", 23);
        addCHKLabel("E-ADVP", 24);
        addCHKLabel("S-ADVP", 25);
    }

    private static void addNERLabel(String label, int index) {
        nerLabels.put(label, (byte) index);
        nerIndex.put((byte) index, label);
    }

    static {
        addNERLabel("O", -1);
        addNERLabel("0", -1);

        addNERLabel("B-ORG", 2);
        addNERLabel("I-ORG", 3);
        addNERLabel("E-ORG", 4);
        addNERLabel("S-ORG", 5);

        addNERLabel("B-LOC", 6);
        addNERLabel("I-LOC", 7);
        addNERLabel("E-LOC", 8);
        addNERLabel("S-LOC", 9);

        addNERLabel("B-PER", 10);
        addNERLabel("I-PER", 11);
        addNERLabel("E-PER", 12);
        addNERLabel("S-PER", 13);

        addNERLabel("B-MISC", 14);
        addNERLabel("I-MISC", 15);
        addNERLabel("E-MISC", 16);
        addNERLabel("S-MISC", 17);
    }
}
