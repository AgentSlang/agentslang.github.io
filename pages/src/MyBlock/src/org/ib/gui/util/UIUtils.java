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

package org.ib.gui.util;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 8/24/12
 */
public class UIUtils {
    private static final Color LABEL_COLOR = new Color(0, 70, 213);

    public static void addSeparator(JPanel panel, String text) {
        if (!panel.getLayout().getClass().isAssignableFrom(MigLayout.class)) {
            throw new IllegalArgumentException("Only MIG Layout can be used ...");
        }
        JLabel l = createLabel(text);
        l.setForeground(LABEL_COLOR);

        panel.add(l, "gapbottom 1, span, split 2, aligny center");
        panel.add(new JSeparator(), "gapleft rel, growx");
    }

    private static JLabel createLabel(String text) {
        return createLabel(text, SwingConstants.LEADING);
    }

    private static JLabel createLabel(String text, int align) {
        return new JLabel(text, align);
    }

    public static Point getLocationOnWindow(JComponent component) {
        Point curLocation = component.getLocationOnScreen();
        Point windowPoint = SwingUtilities.getWindowAncestor(component).getLocationOnScreen();

        curLocation.x -= windowPoint.getX();
        curLocation.y -= windowPoint.getY();

        return curLocation;
    }

    public static Point getCenterLocationOnWindow(JComponent component) {
        Point point = getLocationOnWindow(component);
        Dimension size = component.getSize();

        point.x += size.width / 2;
        point.y += size.height / 2;

        return point;
    }

    public static Point getCenterLocationOnScreen(JComponent component) {
        Point point = component.getLocationOnScreen();
        Dimension size = component.getSize();

        point.x += size.width / 2;
        point.y += size.height / 2;

        return point;
    }

    public static JPanel getGlassPane(JComponent component) {
        Window window = SwingUtilities.getWindowAncestor(component);
        if (window instanceof RootPaneContainer) {
            return (JPanel) ((RootPaneContainer) window).getGlassPane();
        } else {
            return new JPanel();
        }
    }
}
