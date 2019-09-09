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

package org.ib.gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/10/13
 */
public class StateButton extends JButton {
    public interface StateListener {
        public void stateChanged(int newState);
    }


    private int state;
    private Icon[] states;

    private java.util.List<StateListener> listeners = new LinkedList<StateListener>();

    public StateButton(Icon... states) {
        this.states = states;
        state = 0;

        initComponent();
        initActions();
    }

    private void initComponent() {
        this.setMargin(new Insets(0, 0, 0, 0));
        this.setBackground(Color.white);
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setIcon(this.states[state]);
    }

    private void initActions() {
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                state++;
                if (state >= states.length) {
                    state = 0;
                }

                StateButton.this.setIcon(StateButton.this.states[state]);
                fireButtonStateChanged();
            }
        });
    }

    private void fireButtonStateChanged() {
        for (StateListener listener : listeners) {
            listener.stateChanged(state);
        }
    }

    public void addStateListener(StateListener listener) {
        listeners.add(listener);
    }

    public void removeStateListener(StateListener listener) {
        listeners.remove(listener);
    }

    public int getState() {
        return state;
    }
}
