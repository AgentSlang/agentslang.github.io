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

package org.agent.slang.dm.narrative.data.patterns;

import org.agent.slang.data.annotation.GenericTextAnnotation;
import org.agent.slang.data.template.TemplateData;
import org.jdom2.Element;
import org.syn.n.bad.pattern.Matcher;
import org.syn.n.bad.pattern.PatternMatcher;
import org.syn.n.bad.pattern.TemplateMatchResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A data type stores PatternsState type characteristics.
 * OS Compatibility: Windows and Linux
 * @author William Boisseleau, william.boisseleau@insa-rouen.fr
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 07/23/13 - Initial commit, William Boisseleau
 *          2, 12/03/13 - AgentSlang 1.1 integration, Ovidiu Serban
 */

public class PatternsState {

    private ArrayList<String> lPatterns;
    private ArrayList<String> lAnswers;
    private ArrayList<String> lCommands;
    private String next;
    private int stateNumber;
    private ArrayList<Element> StatesElement;
    private Matcher matcher;

    public PatternsState(int i, ArrayList<Element> el) {
        init(i, el);
    }

    /**
     * Gets list of patterns
     * @return array list of patterns
     */
    public ArrayList<String> getlPatterns() {
        return lPatterns;
    }

    /**
     * gets a specific pattern
     * @param i pattern index
     * @return pattern
     */
    public String getPattern(int i) {
        return lPatterns.get(i);
    }

    /**
     * sets list of patterns
     * @param lPatterns array list of patterns
     */
    private void setlPatterns(ArrayList<String> lPatterns) {
        this.lPatterns = lPatterns;
    }

    /**
     * get list of possible answers
     * @return array list of possible answers
     */
    public ArrayList<String> getlAnswers() {
        return lAnswers;
    }

    /**
     * set list of possible answers
     * @param lAnswers array list possible answers
     */
    private void setlAnswers(ArrayList<String> lAnswers) {
        this.lAnswers = lAnswers;
    }

    /**
     * gets list of commands 
     * @return list of commands
     */
    public List<String> getlCommands() {
        return lCommands;
    }

    /**
     * sets list of commands
     * @param lCommands list of commands
     */
    private void setlCommands(ArrayList<String> lCommands) {
        this.lCommands = lCommands;
    }

    /**
     * Getting next pattern
     * @return next pattern
     */
    public String getNext() {
        return next;
    }

    /**
     * Setting next pattern
     * @param next pattern
     */
    private void setNext(String next) {
        this.next = next;
    }

    /**
     * Gets state number
     * @return state number
     */
    public int getStateNumber() {
        return stateNumber;
    }

    /**
     * Sets state number
     * @param stateNumber state number
     */
    private void setStateNumber(int stateNumber) {
        this.stateNumber = stateNumber;
    }

    /**
     * Gets list of states' elements
     * @return array list of states' elements
     */
    public ArrayList<Element> getStatesElement() {
        return StatesElement;
    }

    /**
     * sets list of states' elements
     * @param statesElement array list of states' elements
     */
    private void setStatesElement(ArrayList<Element> statesElement) {
        StatesElement = statesElement;
    }

    /**
     * Initializing and loading states and their elements
     * @param i state index
     * @param el array list of elements
     */
    private void init(int i, ArrayList<Element> el) {
        setStateNumber(i);
        setStatesElement(el);
        loadState();
    }

    /**
     * load current state items
     */
    private void loadState() {
        Element current = getStatesElement().get(getStateNumber());

        setlPatterns(new ArrayList<String>());
        for (Element subCurrent: current.getChild("lPatterns").getChildren("pattern")) {
            getlPatterns().add(subCurrent.getText());
        }

        setlAnswers(new ArrayList<String>());
        for (Element subCurrent: current.getChild("lAnswers").getChildren("answer")) {
            getlAnswers().add(subCurrent.getText());
        }

        setlCommands(new ArrayList<String>());
        for (Element subCurrent: current.getChild("lCommands").getChildren("command")) {
            getlCommands().add(subCurrent.getText());
        }

        setNext(current.getChild("next").getText());
    }

    /**
     * Checks the matching between received answer and potential templates
     * @param anno input annotated answer
     * @return list of matched items
     */
    public ArrayList<String> checkMatching(GenericTextAnnotation anno) {
        ArrayList<String> answersMatched = new ArrayList<String>();
        loadMatchers();

        TemplateData templateData = findMatch(anno);
        if (!templateData.getTemplateIDs().isEmpty() && (!templateDataContainTheEmptyPattern(templateData))) {
            answersMatched = variableGenerationList(templateData, getlAnswers());
        }
        return answersMatched;
    }

    /**
     * loading pattern matcher
     */
    private void loadMatchers() {
        this.matcher = new Matcher();
        for (int i = 0; i < lPatterns.size(); i++) {
        	//System.out.println("\n*****ZJ,Pattern state,  loadMatchers(), String.valueOf(i):   "+String.valueOf(i)+", lPatterns.get(i): "+lPatterns.get(i));
            this.matcher.addMatcher(new PatternMatcher(String.valueOf(i), lPatterns.get(i)));
        }
    }

    /**
     * finding a match 
     * @param anno input annotated answer
     * @return matched template data
     */
    private TemplateData findMatch(GenericTextAnnotation anno) {
        TemplateMatchResult templateMatchResult = matcher.match(anno);
        TemplateData result = new TemplateData(anno.getId(), anno.getLanguage());
        result.updateVariables(templateMatchResult.getExtractedVars());
        result.addTemplateIds(templateMatchResult.getTemplateIDs());

        return result;

    }
  
    /**
     * Generates list of variables inside possible answers 
     * @param templateData template data to be matched
     * @param lAnswers list of answers
     * @return array list of matched answers
     */
    private ArrayList<String> variableGenerationList(TemplateData templateData, ArrayList<String> lAnswers) {
        int j = 0, aMSize = lAnswers.size();
        String aMI;
        ArrayList<String> answersMatched = new ArrayList<String>();
        //System.out.println("0. pattern state  variableGenerationList :::: aMSize:    "+aMSize);
        for (int i = 0; i < aMSize; i++) {
            aMI = lAnswers.get(i);
            //System.out.println("1. pattern state variableGenerationList :::: aMI:    "+aMI);
            lAnswers.set(i, variableGeneration(templateData, aMI));
            aMI = lAnswers.get(i);
            if ((!aMI.isEmpty()) && (!aMI.contains("$")) || aMI.equals("")) {
            	//System.out.println("2. pattern state matched variableGenerationList :::: aMI:    "+aMI);
                answersMatched.add(j, aMI);
                j++;
            }
        }

        return answersMatched;

    }

    /**
     * Checks if template data contains an empty pattern
     * @param templateData input template data
     * @return boolean response
     */
    private boolean templateDataContainTheEmptyPattern(TemplateData templateData) {
        boolean bool = false;

        Iterator iterator = templateData.getExtractedVars().entrySet().iterator();

        while ((iterator.hasNext()) && !bool) {
            Map.Entry mapEntry = (Map.Entry) iterator.next();
            if (mapEntry.getValue().equals("wxcvbn")) {
                bool = true;
            }
        }
        return bool;
    }

    /**
     * Generates variables inside possible answer 
     * @param templateData templateData template data to be matched
     * @param str answer
     * @return matched answer
     */
    private String variableGeneration(TemplateData templateData, String str) {
        int strSize = str.length();
        int start, end, varSize = 0, i = 0;
        String varName, valueTemplateReplacing, varToReplace;
        String strGenerated = "";
        while (i < strSize) {
            if (str.charAt(i) == '$') {
                start = i;
                do {
                    i++;
                } while ((str.charAt(i) != ' ') && (str.charAt(i) != '*'));
                end = i;
                varName = str.substring(start + 1, end);
                valueTemplateReplacing = templateData.getExtractedVars().get(varName);
                if ((!valueTemplateReplacing.isEmpty()) && (!valueTemplateReplacing.equals("wxcvbn"))) {
                    varSize = valueTemplateReplacing.length();
                    if (str.charAt(i) == ' ') {
                        varToReplace = "\\$" + varName + " ";
                    } else {
                        varToReplace = "\\$" + varName + "*";
                    }

                    strGenerated = str.replaceAll(varToReplace, valueTemplateReplacing + ' ');
                }
                i = start + varSize;
                str = strGenerated;
                strSize = str.length();

            }
            i++;
        }

        return str;
    }
}

