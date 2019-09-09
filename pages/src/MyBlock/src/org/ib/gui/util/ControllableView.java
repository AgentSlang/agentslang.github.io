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

import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.view.Viewer;
import org.ib.gui.icons.IconPlaceholder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/18/13
 */
public class ControllableView extends DefaultView implements ActionListener {
    private static final int INDEX_TOOLTIP = 0;
    private static final int INDEX_ACTION = 1;

    private static final String ACTION_MOVE_LEFT = "left";
    private static final String ACTION_MOVE_RIGH = "right";
    private static final String ACTION_MOVE_UP = "up";
    private static final String ACTION_MOVE_DOWN = "down";
    private static final String ACTION_RESET = "reset";
    private static final String ACTION_ZOOM_IN = "in";
    private static final String ACTION_ZOOM_OUT = "out";

    private final Map<Rectangle, String[]> buttonBounds = new HashMap<Rectangle, String[]>();

    private final Rectangle defaultBound = new Rectangle(0, 0, 200, 200);

    public ControllableView(Viewer viewer) {
        super(viewer, Viewer.DEFAULT_VIEW_ID, Viewer.newGraphRenderer());

        viewer.addView(this);

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (defaultBound.contains(e.getX(), e.getY())) {
                    for (Map.Entry<Rectangle, String[]> item : buttonBounds.entrySet()) {
                        if (item.getKey().contains(e.getX(), e.getY())) {
                            actionPerformed(new ActionEvent(item.getValue()[INDEX_ACTION], 1, item.getValue()[INDEX_ACTION]));
                            break;
                        }
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
            }

            public void mouseMoved(MouseEvent e) {
                if (defaultBound.contains(e.getX(), e.getY())) {
                    ControllableView.this.setCursor(Cursor.getDefaultCursor());
                    ControllableView.this.setToolTipText(null);

                    for (Map.Entry<Rectangle, String[]> item : buttonBounds.entrySet()) {
                        if (item.getKey().contains(e.getX(), e.getY())) {
                            ControllableView.this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            ControllableView.this.setToolTipText(item.getValue()[INDEX_TOOLTIP]);
                            forceTooltip();
                            break;
                        }
                    }
                }
            }
        });
    }

    private void forceTooltip() {
        Action toolTipAction = this.getActionMap().get("postTip");

        if (toolTipAction != null) {
            ActionEvent postTip = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");
            toolTipAction.actionPerformed(postTip);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (ACTION_RESET.equals(e.getActionCommand())) {
            getCamera().resetView();
        } else if (ACTION_ZOOM_IN.equals(e.getActionCommand())) {
            getCamera().setViewPercent(Math.max(getCamera().getViewPercent() - 0.05, 0.01));
        } else if (ACTION_ZOOM_OUT.equals(e.getActionCommand())) {
            getCamera().setViewPercent(Math.max(getCamera().getViewPercent() + 0.05, 0.01));
        } else if (ACTION_MOVE_LEFT.equals(e.getActionCommand())) {
            moveCentre(-10, 0);
        } else if (ACTION_MOVE_RIGH.equals(e.getActionCommand())) {
            moveCentre(10, 0);
        } else if (ACTION_MOVE_UP.equals(e.getActionCommand())) {
            moveCentre(0, -10);
        } else if (ACTION_MOVE_DOWN.equals(e.getActionCommand())) {
            moveCentre(0, 10);
        }
    }

    private void moveCentre(int x, int y) {
        Point3 center = getCamera().getViewCenter();
        center = getCamera().transformGuToPx(center.x, center.y, center.z);

        center.set(center.x + x, center.y + y, center.z);
        center = getCamera().transformPxToGu(center.x, center.y);
        getCamera().setViewCenter(center.x, center.y, center.z);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawImage(g, 10, 60, "arrow_left.png", "Move left", ACTION_MOVE_LEFT);
        drawImage(g, 110, 60, "arrow_right.png", "Move right", ACTION_MOVE_RIGH);
        drawImage(g, 60, 10, "arrow_up.png", "Move up", ACTION_MOVE_UP);
        drawImage(g, 60, 110, "arrow_down.png", "Move down", ACTION_MOVE_DOWN);

        drawImage(g, 60, 60, "zoom_reset.png", "Reset Zoom Level / Position", ACTION_RESET);

        drawImage(g, 10, 130, "zoom_in.png", "Zoom in", ACTION_ZOOM_IN);
        drawImage(g, 110, 130, "zoom_out.png", "Zoom out", ACTION_ZOOM_OUT);
    }

    private void drawImage(Graphics g, int x, int y, String imagePath, String tooltip, String action) {
        Image image = ImageHelper.buildImage(imagePath, IconPlaceholder.class);
        g.drawImage(image, x, y, null);
        buttonBounds.put(new Rectangle(x, y, image.getWidth(null), image.getHeight(null)), new String[]{tooltip, action});
    }
}
