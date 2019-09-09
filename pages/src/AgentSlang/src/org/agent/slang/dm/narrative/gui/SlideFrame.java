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

package org.agent.slang.dm.narrative.gui;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.ArrayList;

/**
 * This class provides managing the slide show frame of the agentslang. Displaying images, titles, steps and mouse position.
 * OS Compatibility: Windows and Linux
 * @author William Boisseleau, william.boisseleau@insa-rouen.fr
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @version 1, 07/23/13 - Initial commit, William Boisseleau
 *          2, 12/03/13 - AgentSlang 1.1 integration, Ovidiu Serban
 *          3, 07/16/15 - Publish the cursor position, Sami Boukortt
 */

public class SlideFrame extends JFrame {
    private IntegrationJPanel integrationJPanel;

    private File modelPath;
    private EventListenerList listeners = new EventListenerList();

    private enum Launch {
        displaySlide, changeTitle, tellStory, changeStepName, launch
    }

    public SlideFrame(File modelPath) {
        this.modelPath = modelPath;
        initComponents();
    }

    /**
     * Initializing component based on input configuration of the scenario. 
     */
    private void initComponents() {
        setTitle("ACAMODIA - AGENTSLANG");
        setSize(new Dimension(1200, 730));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        integrationJPanel = new IntegrationJPanel(modelPath);
        this.setContentPane(integrationJPanel);

        final MouseMotionAdapter mouseMotionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                if (SwingUtilities.isRightMouseButton(event)) {
                    for (MousePositionListener listener: listeners.getListeners(MousePositionListener.class)) {
                        listener.onMouseMoved(event.getX(), event.getY());
                    }
                }
            }
        };
        integrationJPanel.addMouseMotionListener(mouseMotionAdapter);
        integrationJPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                mouseMotionAdapter.mouseDragged(event);
            }
        });
    }

    /**
     * Handles story changes and controls the slide show panel based on these changes.
     * @param lLaunches array list of changes
     * @return number of state of the story to tell next
     */
    public int processChanges(ArrayList<String> lLaunches) {
        int numbOfStoryToTell = -1; // > 0 if there is a story to tell

        for (String value : lLaunches) {
            if (!value.equals("")) {
                // We recover the type of launch & the information for these changes
                int j = value.indexOf(' ');

                if(j>0) {
                    String type = value.substring(0, j);
                    String info = value.substring(j + 1);

                    // Switch case using enum.
                    Launch launch = Launch.valueOf(type);
                    switch (launch) {
                        case displaySlide:
                        	changeSlide(Integer.parseInt(info));
                            break;

                        case changeTitle:
                            changeTitle(info);
                            break;

                        case tellStory:
                            numbOfStoryToTell = loadStory(Integer.parseInt(info));
                            changeStepName(info);
                            break;

                        case changeStepName:
                            changeStepName(info);
                            break;

                        case launch:
                            // launch command
                            break;

                        default:
                            break;
                    }
                }
            }

        }

        return numbOfStoryToTell;
    }

    /**
     * Changes slide image
     * @param i slide number
     */
    private void changeSlide(int i) {
        this.integrationJPanel.setSlideIcon(new File(modelPath, "/slides/slide" + i + ".png"));
    }

    /**
     * changes slide title
     * @param str title
     */
    private void changeTitle(String str) {
        this.integrationJPanel.setScenarioText(str);

    }

    /**
     * load an specific step of the story
     * @param i step number
     * @return loaded step number
     */
    private int loadStory(int i) {
        return i;
    }

    /**
     * changes story step name
     * @param str story step name 
     */
    private void changeStepName(String str) {
        this.integrationJPanel.setStep_iText("Step " + str);
    }

    /**
     * Enables mouse position listening 
     * @param listener Mouse Position Listener
     */
    public void addMousePositionListener(MousePositionListener listener) {
        listeners.add(MousePositionListener.class, listener);
    }

    /**
     * Disables mouse position listening
     * @param listener Mouse Position Listener
     */
    public void removeMousePositionListener(MousePositionListener listener) {
        listeners.remove(MousePositionListener.class, listener);
    }
}
