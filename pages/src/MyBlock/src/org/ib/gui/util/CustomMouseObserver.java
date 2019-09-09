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
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.util.MouseManager;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 8/29/12
 */
public class CustomMouseObserver implements MouseListener, MouseMotionListener, MouseWheelListener {
    private Point startPoint = null;
    private GraphicElement selectedElement = null;
    private ViewPanel view;

    public static final int LISTENER_ALL = -1;
    public static final int LISTENER_CLICKED = 0;
    public static final int LISTENER_PRESSED = 1;
    public static final int LISTENER_RELEASED = 2;

    private Map<Integer, java.util.List<NodeActivationListener>> listeners = new HashMap<Integer, List<NodeActivationListener>>();

    public CustomMouseObserver(ViewPanel view) {
        this.view = view;
        setup();
    }

    public void addActivationListener(NodeActivationListener listener) {
        addActivationListener(LISTENER_PRESSED, listener);
    }

    public void addActivationListener(int listenerPolicy, NodeActivationListener listener) {
        if (listenerPolicy == LISTENER_ALL) {
            addActivationListener(LISTENER_PRESSED, listener);
            addActivationListener(LISTENER_CLICKED, listener);
            addActivationListener(LISTENER_RELEASED, listener);
        } else {
            java.util.List<NodeActivationListener> list = listeners.get(listenerPolicy);
            if (list == null) {
                list = new LinkedList<NodeActivationListener>();
                listeners.put(listenerPolicy, list);
            }
            list.add(listener);
        }
    }

    public void removeActivationListener(NodeActivationListener listener) {
        removeActivationListener(LISTENER_PRESSED, listener);
    }

    public void removeActivationListener(int listenerPolicy, NodeActivationListener listener) {
        if (listenerPolicy == LISTENER_ALL) {
            removeActivationListener(LISTENER_PRESSED, listener);
            removeActivationListener(LISTENER_CLICKED, listener);
            removeActivationListener(LISTENER_RELEASED, listener);
        } else {
            java.util.List<NodeActivationListener> list = listeners.get(listenerPolicy);
            if (list != null) {
                list.remove(listener);
            }
        }
    }

    private void fireSelection(int listenerPolicy, String nodeID, MouseEvent event) {
        java.util.List<NodeActivationListener> list = listeners.get(listenerPolicy);
        if (list != null) {
            for (NodeActivationListener listener : list) {
                listener.nodeActivated(nodeID, event);
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        GraphicElement element = view.findNodeOrSpriteAt(e.getX(), e.getY());
        if (element != null) {
            fireSelection(LISTENER_CLICKED, element.getId(), e);
        }
    }

    public void mousePressed(MouseEvent e) {
        GraphicElement element = view.findNodeOrSpriteAt(e.getX(), e.getY());
        if (element != null) {
            selectedElement = element;
            fireSelection(LISTENER_PRESSED, element.getId(), e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        selectedElement = null;

//        ((DefaultView) view).graph.getNode("22002");

        GraphicElement element = view.findNodeOrSpriteAt(e.getX(), e.getY());
        if (element != null) {
            fireSelection(LISTENER_RELEASED, element.getId(), e);
        }
    }

    public void mouseEntered(MouseEvent e) {
        //not implemented
    }

    public void mouseExited(MouseEvent e) {
        //not implemented
    }

    public void mouseDragged(MouseEvent e) {
        view.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        if (selectedElement != null) {
            moveElement(e);
        } else {
            moveCentre(e);
        }
        startPoint = new Point(e.getX(), e.getY());
    }

    public void mouseMoved(MouseEvent e) {
        if (startPoint != null) {
            view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            startPoint = null;
        }
    }

    private void moveCentre(MouseEvent e) {
        if (startPoint != null) {
            Point3 endPoint = new Point3(startPoint.x - e.getX(), startPoint.y - e.getY(), 0);
            Point3 center = view.getCamera().getViewCenter();
            center = view.getCamera().transformGuToPx(center.x, center.y, center.z);

            center.set(center.x + endPoint.x, center.y + endPoint.y, center.z);
            center = view.getCamera().transformPxToGu(center.x, center.y);
            view.getCamera().setViewCenter(center.x, center.y, center.z);
        }
    }

    private void moveElement(MouseEvent e) {
        if (selectedElement != null) {
            view.moveElementAtPx(selectedElement, e.getX(), e.getY());
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        view.getCamera().setViewPercent(Math.max(view.getCamera().getViewPercent() + Math.signum(e.getUnitsToScroll()) * 0.05, 0.01));
    }

    public void setup() {
        view.setMouseManager(new NullMouseManager());

        view.addMouseMotionListener(this);
        view.addMouseWheelListener(this);
        view.addMouseListener(this);
    }

    public interface NodeActivationListener {
        public void nodeActivated(String nodeID, MouseEvent e);
    }

    public class NullMouseManager implements MouseManager {
        public void init(GraphicGraph nodes, View view) {
        }

        public void release() {
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
        }

        public void mouseMoved(MouseEvent e) {
        }
    }
}
