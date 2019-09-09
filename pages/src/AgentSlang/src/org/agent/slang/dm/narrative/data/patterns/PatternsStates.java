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
import org.agent.slang.dm.narrative.data.GenericStates;

import java.util.ArrayList;
import java.util.List;

/**
 * A generic class in order to manage Patterns States of the story.
 * OS Compatibility: Windows and Linux
 * @author William Boisseleau, william.boisseleau@insa-rouen.fr
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 07/23/13 - Initial commit, William Boisseleau
 *          2, 12/03/13 - AgentSlang 1.1 integration, Ovidiu Serban
 */

public class PatternsStates extends GenericStates {

    private ArrayList<PatternsState> listOfPatternsStates;
    private int next;
    private List<String> lCommands;
    private boolean weMatchedStepI;

    protected void initItem() {
        setListOfPatternsStates(new ArrayList<PatternsState>());
        for (int i = 0; i < getNumberOfStates(); i++) {
            listOfPatternsStates.add(new PatternsState(i, getStatesElements()));
        }
        setNext(0);
        setlCommands(new ArrayList<String>());
    }
    
    /**
     * Gets the list of all pattern states
     * @return array list of pattern states
     */
    public ArrayList<PatternsState> getListOfPatternsStates() {
        return listOfPatternsStates;
    }

    /**
     * sets the list of all pattern states
     * @param listOfPStates array list of pattern states
     */
    private void setListOfPatternsStates(ArrayList<PatternsState> listOfPStates) {
        this.listOfPatternsStates = listOfPStates;
    }

    /**
     * gets next pattern
     * @return next pattern
     */
    public int getNext() {
        return next;
    }

    /**
     * sets next pattern
     * @param next next pattern
     */
    private void setNext(int next) {
        this.next = next;
    }

    /**
     * Gets lists of commands
     * @return list of commands
     */
    public List<String> getlCommands() {
        return lCommands;
    }

    /**
     * sets lists of commands
     * @param lCommands list of commands
     */
    private void setlCommands(List<String> lCommands) {
        this.lCommands = lCommands;
    }

    /**
     * gets one pattern state
     * @param i index
     * @return pattern state
     */
    public PatternsState getPatternsState(int i) {
        return getListOfPatternsStates().get(i);
    }

    /**
     * Check if a match happened on the step
     * @return boolean response
     */
    public boolean getWeMatchedStepI() {
        return weMatchedStepI;
    }

    /**
     * Matches the input data with different patterns
     * @param anno input annotated data
     * @param weAreInOutOfContext check if system is looking in out of context patterns or in original scenario of the story.
     * @return matched answer for the input
     */
    public String checkMatching(GenericTextAnnotation anno, boolean weAreInOutOfContext) {
    	//System.out.println("\n00 *****ZJ,Pattern states, anno:      "+anno);
    	//System.out.println("\n01 *****ZJ,Pattern states, weAreInOutOfContext:      "+weAreInOutOfContext);
    	this.weMatchedStepI = false;
        ArrayList<String> allAnswers = new ArrayList<String>();
        ArrayList<String> temp;
        String returnedAnswer;
        int numbOfStates = getNumberOfStates();
        PatternsState pS;
        
        //We will choose the first matched state, so $defvar pattern should be always the last pattern in our scenario states
        for (int i = 0; i < numbOfStates && !this.weMatchedStepI; i++) {
        	//System.out.println("\n02*****ZJ,Pattern states, getListOfPatternsStates():      "+getListOfPatternsStates().get(i).getlPatterns());
        	temp = getListOfPatternsStates().get(i).checkMatching(anno);
        	
            //System.out.println("\n03*****ZJ,Pattern states, temp:      "+temp+",    temp.size()   "+temp.size());
            //System.out.println("\n04*****ZJ,Pattern states,  weMatchedStepI   "+weMatchedStepI);
            // If we match
            if (!temp.isEmpty()) {
            	
                this.weMatchedStepI = true;
                //System.out.println("\n05*****ZJ,Pattern states,  weMatchedStepI   "+weMatchedStepI);
                // if Next is not empty
                if (!getListOfPatternsStates().get(i).getNext().equals("")) {
                    setNext(Integer.parseInt(getListOfPatternsStates().get(i).getNext()));
                }
                // if Command is not empty
                if (!getListOfPatternsStates().get(i).getlCommands().isEmpty()) {
                    setlCommands(getListOfPatternsStates().get(i).getlCommands());
                }
                allAnswers.addAll(temp);
            }
        }

        // If we are working on the OutOfContext file :
        if (weAreInOutOfContext) {
            pS = getPatternsState(numbOfStates - 1);
            // We check that the last pattern from outOfContext is well the default
            if (allAnswers.isEmpty() && pS.getPattern(0).equals("default")) {
                // if Next is not empty
                if (!pS.getNext().equals("")) {
                    setNext(Integer.parseInt(pS.getNext()));
                }
                // if Command is not empty
                if (!pS.getlCommands().isEmpty()) {
                    setlCommands(pS.getlCommands());
                }
                allAnswers.addAll(pS.getlAnswers());

            }
            if (!pS.getPattern(0).equals("default")) {
                allAnswers.add("The OutOfContext.xml has been wrongly set." +
                        "\n Please add as last pattern the default* content." +
                        "\n*default : misunderstanding between the agent and the user");
            }
            //System.out.println("\n\n*****ZJ,Pattern states, allAnswers,OutOfContext:      "+allAnswers);
        }
        // If we are working on a Step_i
        else {
            //If we didn't find any match
            if (allAnswers.isEmpty()) {
                allAnswers.add("");
            }

        }

        // Finally, we pick up randomly an answer from the list of possible answers.
        int size = allAnswers.size();
        returnedAnswer = allAnswers.get(random(0, size - 1));

        //displayAllAnswers(allAnswers);
        //System.out.println("\n06*****ZJ,Pattern states, get(random(0, size - 1), size:      "+size);
        //System.out.println("\n07*****ZJ,Pattern states, allAnswers:      "+allAnswers);
        //System.out.println("\n08*****ZJ,Pattern states, returnedAnswer:      "+returnedAnswer);
        return returnedAnswer;
    }

    /**
     * random function 
     * @param min min value
     * @param max max value
     * @return random generated value
     */
    private int random(int min, int max) {
        return (int) (min + (Math.random() * (max - min + 1)));
    }
}
