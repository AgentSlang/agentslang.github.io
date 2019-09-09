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

package org.agent.slang.dm.narrative.data;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * A data type stores GenericStates data characteristics.
 * OS Compatibility: Windows and Linux
 * @author William Boisseleau, william.boisseleau@insa-rouen.fr
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 07/23/13 - Initial commit, William Boisseleau
 *          2, 12/03/13 - AgentSlang 1.1 integration, Ovidiu Serban
 */

public abstract class GenericStates implements Serializable{
    protected ArrayList<Element> statesElements;
    private int numberOfStates;
    private File fileName;

    private static Document document;
    private static Element root;

    public GenericStates() {
    }

    public GenericStates(File fileN) {
        init(fileN);
    }

    /**
     * initiates states from a file
     * @param fileN states file name
     */
    public void init(File fileN) {
        setFileName(fileN);
        loadStates();
    }

    /**
     * loads all states
     */
    protected void loadStates() {
        SAXBuilder sxb = new SAXBuilder();

        try {
            setDocument(sxb.build(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Loading the root
        setRoot(document.getRootElement());
        // Loading the number of states
        setNumberOfStates(root.getChildren("state").size());
        // Loading all elements
        setStatesElements(new ArrayList<Element>(root.getChildren("state")));
        initItem();
    }

    // This method is usually overwritten
    protected abstract void initItem();

    protected static void setDocument(Document document) {
        GenericStates.document = document;
    }

    /**
     * sets root element
     * @param root root element
     */
    protected static void setRoot(Element root) {
        GenericStates.root = root;
    }

    /**
     * gets all states elements 
     * @return array list of state elements
     */
    public ArrayList<Element> getStatesElements() {

        return statesElements;
    }

    /**
     * sets all states elements 
     * @param statesElements array list of states elements
     */
    protected void setStatesElements(ArrayList<Element> statesElements) {
        this.statesElements = statesElements;
    }

    /**
     * gets number of states
     * @return number of states
     */
    public int getNumberOfStates() {
        return numberOfStates;
    }

    /**
     * sets number of states
     * @param numberOfStates number of states
     */
    protected void setNumberOfStates(int numberOfStates) {
        this.numberOfStates = numberOfStates;
    }

    /**
     * gets file name of states
     * @return file name
     */
    public File getFileName() {
        return fileName;
    }

    /**
     * sets file name of states
     * @param fileName file name
     */
    protected void setFileName(File fileName) {
        this.fileName = fileName;
    }
}


