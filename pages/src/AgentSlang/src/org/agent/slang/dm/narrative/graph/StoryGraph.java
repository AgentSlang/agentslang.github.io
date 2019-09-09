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

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import org.agent.slang.dm.narrative.data.patterns.PatternsState;
import org.agent.slang.dm.narrative.data.patterns.PatternsStates;
import org.agent.slang.dm.narrative.data.story.StoryState;
import org.agent.slang.dm.narrative.data.story.StoryStates;
import org.apache.commons.lang.StringUtils;
import org.ib.logger.Logger;

import javax.swing.*;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class StoryNode implements Serializable {
    private final StoryState state;
    private final int stateNumber;

    public StoryNode(StoryState state, int stateNumber) {
        this.state = state;
        this.stateNumber = stateNumber;
    }

    public StoryState getState() {
        return state;
    }

    public int getStateNumber() {
        return stateNumber;
    }

    @Override
    public String toString() {
        return state.getStory();
    }
}

/**
 * Provides general requirements for Story graph component.
 * OS Compatibility: Windows and Linux
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @version 1, 7/9/15
 */
class StoryGraph extends mxGraph {
    StoryStates storyStates = new StoryStates();
    Map<Integer, PatternsStates> patternsStatesList = new HashMap<>();
    private final Map<Integer, mxICell> storyNodes;

    private String graphType;

    public StoryGraph(File modelDirectory, String storyStateFilePath, String patternFolder, String regexForPatternFiles) {

        if(storyStateFilePath != null) {
            storyStates.init(new File(modelDirectory, storyStateFilePath));
        }

        Pattern stepPattern = Pattern.compile(regexForPatternFiles);

        int patternID = 1;

        for (File step : new File(modelDirectory, patternFolder).listFiles()) {
            Matcher matcher = stepPattern.matcher(step.getName());
            if (matcher.matches()) {
                PatternsStates patternsStates = new PatternsStates();
                patternsStates.init(step);
                try {
                    patternID = Integer.parseInt(matcher.group("i"));
                }
                catch (Exception e) {
                    patternID++;
                }
                patternsStatesList.put(patternID, patternsStates);
            }
        }

        setCellsLocked(true);
        setAllowDanglingEdges(false);
        setAutoSizeCells(true);

        {
            Map<String, Object> inactiveStoryNodeStyle = new HashMap<>();
            inactiveStoryNodeStyle.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "#aaffaa");
            stylesheet.putCellStyle("InactiveStoryNode", inactiveStoryNodeStyle);

            Map<String, Object> activeStoryNodeStyle = new HashMap<>(inactiveStoryNodeStyle);
            activeStoryNodeStyle.put(mxConstants.STYLE_LABEL_BACKGROUNDCOLOR, "#00dd55");
            stylesheet.putCellStyle("ActiveStoryNode", activeStoryNodeStyle);
        }

        {
            Map<String, Object> patternNodeStyle = new HashMap<>();
            patternNodeStyle.put(mxConstants.STYLE_ROUNDED, true);
            stylesheet.putCellStyle("PatternNode", patternNodeStyle);
        }

        getModel().beginUpdate();
        try {
            storyNodes = new HashMap<>(storyStates.getNumberOfStates(), 1.f);
            int i = 0;
            if(storyStateFilePath != null)
                for (StoryState s: storyStates.getListOfStoryStates()) {
                    mxICell cell = (mxICell) insertVertex(getDefaultParent(), null, new StoryNode(s, i), 0, 0, 0, 0);
                    updateCellSize(cell);

                    cell.setStyle("InactiveStoryNode");

                    storyNodes.put(i, cell);
                    ++i;
                }

            for (Map.Entry<Integer, PatternsStates> patternsStates: patternsStatesList.entrySet()) {
                for (PatternsState patternsState: patternsStates.getValue().getListOfPatternsStates()) {
                    mxICell cell = (mxICell) insertVertex(getDefaultParent(), null, StringUtils.join(patternsState.getlPatterns(), " / "), 0, 0, 0, 0);
                    updateCellSize(cell);

                    insertEdge(getDefaultParent(), null, "", storyNodes.get(patternsStates.getKey() - 1), cell);
                    if (!patternsState.getNext().isEmpty()) {
                        insertEdge(getDefaultParent(), null, StringUtils.join(patternsState.getlAnswers(), " / "), cell, storyNodes.get(Integer.parseInt(patternsState.getNext()) - 1));
                    }

                    cell.setStyle("PatternNode");
                }
            }
        }
        finally {
            getModel().endUpdate();
        }

        new mxHierarchicalLayout(this, SwingConstants.WEST).execute(getDefaultParent());
    }

    /**
     * Sets the active node on the story graph component that shows the current state of the story.
     * @param i node index
     * @return object that is currently activated
     */
    public Object setActiveStoryNode(Integer i) {
        Object cell = null;

        getModel().beginUpdate();
        try {
            for (Map.Entry<Integer, mxICell> entry: storyNodes.entrySet()) {
                if (entry.getKey().equals(i)) {
                    entry.getValue().setStyle("ActiveStoryNode");
                    cell = entry.getValue();
                }
                else {
                    entry.getValue().setStyle("InactiveStoryNode");
                }
            }
        }
        finally {
            getModel().endUpdate();
        }

        return cell;
    }
}

