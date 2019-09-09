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

import org.ui.designer.component.geometry.basic.Point3D;
import org.ui.designer.component.geometry.hand.HandRectangle;
import org.ui.designer.component.geometry.hand.HandShape;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 3/31/14
 */
public class Designer extends JFrame {
    private HandShape[] line = new HandShape[]{
            new HandRectangle(new Point3D(50, 50), new Point3D(200, 100)),
            new HandRectangle(new Point3D(250, 150), new Point3D(400, 300)),
            new HandRectangle(new Point3D(450, 50), new Point3D(600, 300))
    };

    public Designer() {
        super("UML Component Designer");

        initComponents();
        initLayout();
        initActions();
    }

    public static void main(String[] args) {
        Designer designer = new Designer();
        designer.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        designer.setSize(new Dimension(800, 600));
        designer.setLocationRelativeTo(null);
        designer.setVisible(true);
    }

    private void initComponents() {

    }

    private void initLayout() {
        setLayout(null);

//        UComponent component = new UComponent("test", 2, 3);
//        component.setSize(new Dimension(100, 100));
//        component.setBounds(10, 10, component.getSize().width, component.getSize().height);
//
//        add(component);
    }

    private void initActions() {

    }

    public void paint(Graphics g) {
        super.paint(g);

        if (g instanceof Graphics2D) {
            ((Graphics2D) g).setStroke(new BasicStroke(2f));
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            for (HandShape shape : line) {
                shape.drawShape((Graphics2D) g);
            }
        }

//        drawCurvedLine(g, 100, new Point3D(0, 0), new Point3D(150, 220), new Point3D(300, 400));
//        drawCurvedLine(g, 100, new Point3D(0, 0), new Point3D(150, 220), new Point3D(310, 400));
    }
}
