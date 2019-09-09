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

package org.ui.designer.component;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 3/31/14
 */
public class UComponent extends JPanel {
    private static final float FONT_SIZE = 8;
    private static final Dimension paramBtnConnection = new Dimension(30, 10);
    private String name;
    private int inputSize;
    private int outputSize;
    private JButton[] inputComponents;
    private JButton[] outputComponents;

    private volatile int screenX = 0;
    private volatile int screenY = 0;
    private volatile int myX = 0;
    private volatile int myY = 0;

    public UComponent(String name, int inputSize, int outputSize) {
        this.name = name;
        this.inputSize = inputSize;
        this.outputSize = outputSize;

        initComponents();
        initLayout();
        initActions();
    }

    private void initComponents() {
        setBackground(Color.white);
        setOpaque(true);
        setBorder(BorderFactory.createLineBorder(Color.black));

        inputComponents = new JButton[inputSize];
        outputComponents = new JButton[outputSize];
    }

    private void initLayout() {
        setLayout(null);

        createParamConnections(inputComponents, 0, "i", BorderFactory.createMatteBorder(1, 0, 1, 1, Color.black));
        createParamConnections(outputComponents, getWidth() - (int) paramBtnConnection.getWidth(), "o", BorderFactory.createMatteBorder(1, 1, 1, 0, Color.black));
    }

    private void initActions() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                screenX = e.getXOnScreen();
                screenY = e.getYOnScreen();

                myX = getX();
                myY = getY();
            }
        });

        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
                int deltaX = e.getXOnScreen() - screenX;
                int deltaY = e.getYOnScreen() - screenY;

                setLocation(myX + deltaX, myY + deltaY);
            }

            public void mouseMoved(MouseEvent e) {
            }
        });

    }

    private void createParamConnections(JButton[] components, int leftPosition, String prefix, Border border) {
        int dist = computeDist(components);
        for (int i = 0; i < components.length; i++) {
            JButton btn = new JButton(prefix + i);
            btn.setBackground(Color.white);
            btn.setBorder(border);
            btn.setMargin(new Insets(0, 0, 0, 0));
            btn.setFocusable(false);
            btn.setFont(btn.getFont().deriveFont(FONT_SIZE));

            btn.setSize(paramBtnConnection);

            btn.setBounds(leftPosition, i * (btn.getHeight() + dist) + dist, btn.getWidth(), btn.getHeight());
            add(btn);
            components[i] = btn;
        }
    }

    private void revalidateComponents(JButton[] components, int leftPosition) {
        int dist = computeDist(components);
        for (int i = 0; i < components.length; i++) {
            JButton btn = components[i];
            btn.setBounds(leftPosition, i * (btn.getHeight() + dist) + dist, btn.getWidth(), btn.getHeight());
        }
    }

    private int computeDist(JButton[] components) {
        return (int) ((getHeight() - components.length * paramBtnConnection.getHeight()) / (components.length + 1));
    }

    public void paint(Graphics g) {
        super.paint(g);
        revalidateComponents(inputComponents, 0);
        revalidateComponents(outputComponents, getWidth() - (int) paramBtnConnection.getWidth());
    }
}
