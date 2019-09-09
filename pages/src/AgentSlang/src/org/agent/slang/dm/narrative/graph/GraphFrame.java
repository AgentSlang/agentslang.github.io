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

package org.agent.slang.dm.narrative.graph;

import com.mxgraph.model.mxICell;

import com.mxgraph.swing.mxGraphComponent;

import javax.swing.*;
import javax.swing.event.EventListenerList;

import org.hamcrest.core.StringStartsWith;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.EventListener;

interface StateChangeListener extends EventListener {
    void onStateChange(int newState);
}

class GraphThread implements Runnable {

    private String title;
    private String storyStateFilePath;
    private String patternFolder;
    private String regexForPatternFiles;

    public GraphThread(String title, String storyStateFilePath, String patternFolder, String regexForPatternFiles) {
        this.title = title;
        this.storyStateFilePath = storyStateFilePath;
        this.patternFolder = patternFolder;
        this.regexForPatternFiles = regexForPatternFiles;
    }

    @Override
    public void run() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ignored) {}

        GraphFrame gui = new GraphFrame(title, new File("../Nareca/scenario"), storyStateFilePath, patternFolder, regexForPatternFiles);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setVisible(true);
    }
}

/**
 * It is a class in order to provide frame for story graph to show story states.
 * OS Compatibility: Windows and Linux
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @author Julien Baron, julien.baron@insa-rouen.fr
 * @author sahba ZOJAJI, sahba.zojaji@insa-rouen.fr
 * @version 1,Sami Boukortt
 * 			2,Julien Baron
 *          3, 26/11/2016 - Playing the sound whenever a pilot click on an out of context graph state, Sahba ZOJAJI
 */
class GraphFrame extends JFrame {
    private final EventListenerList listeners = new EventListenerList();
    private final StoryGraph graph;
    private final mxGraphComponent graphComponent;

    public GraphFrame(String title, File modelDirectory, String storyStateFilePath, String patternFolder, String regexForPatternFiles) {

        setTitle(title);

        graph = new StoryGraph(modelDirectory, storyStateFilePath, patternFolder, regexForPatternFiles);
        graphComponent = new mxGraphComponent(graph);
        graphComponent.setConnectable(false);
        add(graphComponent);

        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1) {
                    mxICell cell = (mxICell) graphComponent.getCellAt(event.getX(), event.getY());
                    if (cell != null) {
                        if (cell.getValue() instanceof StoryNode) {
                            StoryNode node = (StoryNode) cell.getValue();
                            int newStateNumber = node.getStateNumber();
                            setStateNumber(newStateNumber);
                            
                            for (StateChangeListener listener: listeners.getListeners(StateChangeListener.class)) {
                                listener.onStateChange(newStateNumber);
                            }
                        }
                        //Sahba modification starts, Version 3
                        else{
                        	String str = cell.getValue().toString();
                        	if(str.startsWith("#")){
                        			int newState = Integer.parseInt((str.substring(1, str.indexOf(" "))));
                        			newState *= -1;
                        			setStateNumber(newState);
	                            
                        			for (StateChangeListener listener: listeners.getListeners(StateChangeListener.class)) {
                        				listener.onStateChange(newState);
	                            }
                            }
                        }
                        //Sahba modification ends, Version 3
                    }
                }
            }
        });

        pack();
    }

    /**
     * Sets current state number in story.
     * @param newStateNumber state number
     */
    public void setStateNumber(int newStateNumber) {
        Object cell = graph.setActiveStoryNode(newStateNumber);
        graphComponent.refresh();
        graphComponent.scrollCellToVisible(cell, true);
    }

    /**
     * Enable listening to the state change in story.
     * @param listener state change listener
     */
    public void addStateChangeListener(StateChangeListener listener) {
        listeners.add(StateChangeListener.class, listener);
    }

    /**
     * Disable listening to the state change in story.
     * @param listener state change listener
     */
    public void removeStateChangeListener(StateChangeListener listener) {
        listeners.remove(StateChangeListener.class, listener);
    }

}
