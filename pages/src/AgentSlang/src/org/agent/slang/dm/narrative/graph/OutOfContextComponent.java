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

import org.agent.slang.dm.narrative.data.StateChangeData;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.base.SourceComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;

import javax.swing.*;

/**
 * Provides a graphic display of a story, to control its narration.
 * <p>
 * The <code>modelPath</code> parameter should point to an appropriate Nareca
 * directory.
 * <p>
 * The component can receive and react to
 * {@link StateChangeData state changes} (to
 * track the narration of the story), as well as send them (on the
 * <code>stateChange.data</code> channel) when a particular state is clicked.
 * The components that should be controlled by this one should subscribe to
 * this channel.
 * 
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @author Julien Baron, julien.baron@insa-rouen.fr
 */
@ConfigureParams(mandatoryConfigurationParams = "modelPath",
                 outputChannels = "stateChange.data", outputDataTypes = StateChangeData.class)
public class OutOfContextComponent extends SourceComponent {
    private static final String STATE_CHANGE_CHANNEL = "stateChange.data";
    private static final String MODEL_PROP = "modelPath";

    private long messageID = 0;

    private GraphFrame gui;

    public OutOfContextComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Sets up out of context component based on input configuration.
     * @param config Component configuration
     */
    @Override
    protected void setupComponent(ComponentConfig config) {
        gui = new OutOfContextGraphFrame(config.getFileProperty(MODEL_PROP, true), null, "outOfContext", "OutOfContext.xml");

        start();
    }

    /**
     * Checking type of output data.
     */
    @Override
    public void definePublishedData() {
        addOutboundTypeChecker(STATE_CHANGE_CHANNEL, StateChangeData.class);
    }

    /**
     * closes form and stops out of context component
     */
    @Override
    public void close() {
        super.close();
        stop();
    }

    /**
     * Starts and initiates out of context component
     */
    private void start() {
        gui.addStateChangeListener(new StateChangeListener() {
            @Override
            public void onStateChange(int newState) {
                publishData(STATE_CHANGE_CHANNEL, new StateChangeData(getMessageID(), newState));
            }
        });
        gui.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        gui.setVisible(true);
    }

    /**
     * Dispose gui of out of context component
     */
    private void stop() {
        gui.dispose();
    }

    /**
     * gets message id
     * @return message id
     */
    private long getMessageID() {
        messageID++;
        if (messageID > Long.MAX_VALUE - 2) {
            messageID = 0;
        }
        return messageID;
    }

}
