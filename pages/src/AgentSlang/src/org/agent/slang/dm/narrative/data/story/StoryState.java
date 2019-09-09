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

package org.agent.slang.dm.narrative.data.story;

import org.jdom2.Element;

import java.util.ArrayList;

/**
 * A data type which can be used in order to capture story states.
 * OS Compatibility: Windows and Linux
 * @author William Boisseleau, william.boisseleau@insa-rouen.fr
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 07/23/13 - Initial commit, William Boisseleau
 *          2, 12/03/13 - AgentSlang 1.1 integration, Ovidiu Serban
 */

public class StoryState {
    private ArrayList<String> lCommands;
    private String story;
    private int stateNumber;
    private ArrayList<Element> StatesElement;

    public StoryState(int i, ArrayList<Element> el){
        init(i,el);
    }

    /**
     * gets the story
     * @return story
     */
    public String getStory() {
        return story;
    }

    /**
     * sets the story
     * @param story story
     */
    private void setStory(String story) {
        this.story = story;
    }

    /**
     * get state number
     * @return state number
     */
    public int getStateNumber() {
        return stateNumber;
    }

    /**
     * sets state number
     * @param stateNumber state number
     */
    private void setStateNumber(int stateNumber) {
        this.stateNumber = stateNumber;
    }

    /**
     * gets array list of commands
     * @return array list of commands
     */
    public ArrayList<String> getlCommands() {
        return lCommands;
    }

    /**
     * sets array list of commands
     * @param lCommands array list of commands
     */
    public void setlCommands(ArrayList<String> lCommands) {
        this.lCommands = lCommands;
    }

    /**
     * gets list of state elements
     * @return array list of state elements
     */
    public ArrayList<Element> getStatesElement() {
        return StatesElement;
    }

    /**
     * sets list of state elements
     * @param statesElement array list of state element
     */
    private void setStatesElement(ArrayList<Element> statesElement) {
        StatesElement = statesElement;
    }

    /**
     * initializes and loads state and their elements
     * @param i state index
     * @param el array list of elements
     */
    private void init(int i, ArrayList<Element> el){
        setStateNumber(i);
        setStatesElement(el);
        loadState();
    }

    /**
     * load states of the story
     */
    private void loadState(){
    	try{
    		    	
        Element current = getStatesElement().get(stateNumber);
        setStory(current.getChild("story").getText());
        setlCommands(new ArrayList<String>());
        for (Element subCurrent: current.getChild("lCommands").getChildren("command")) {
            getlCommands().add(subCurrent.getText());
        }
    	}catch(Exception e){
    		System.out.println("\n\n\n\n\n********************\n\n\n\n\n\n\n");
    	}
    }
}
