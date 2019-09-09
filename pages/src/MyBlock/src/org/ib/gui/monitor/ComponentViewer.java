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

package org.ib.gui.monitor;

import net.miginfocom.swing.MigLayout;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.ib.component.model.ComponentInfo;
import org.ib.component.model.ComponentModel;
import org.ib.data.DebugData;
import org.ib.gui.util.*;
import org.ib.gui.util.springbox.SpringBox;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 8/22/12
 */
public class ComponentViewer extends JFrame {
    private static final String ANY = "*";

    private ComponentModel componentModel;

    private Graph graph;
    private ViewPanel view;

    private JSplitPane splitPane;
    private JTextPane componentsLog;
    private JComboBox<SeverityWrapper> severityFilterComponent;
    private JComboBox<String> nameFilterComponent;
    private Set<String> nameItems = new HashSet<String>();

    private ColorHighlighter colorHighlighter = new ColorHighlighter();

    public ComponentViewer() {
        this(null);
    }

    public ComponentViewer(File filename) {
        initModel(filename);
        initComponents();
        initLayout();
        initActions();

        initGraph();
        renderAllGraph();
    }

    private void initModel(File filename) {
        if (filename != null) {
            componentModel = new ComponentModel(filename.getAbsolutePath());
        } else {
            componentModel = new ComponentModel();
        }
    }

    private void initComponents() {
//        System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        setTitle("Component monitor");
        setSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(800, 600));

        graph = new SingleGraph("noID");
        graph.setAutoCreate(true);

        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        if (DotLayoutManager.canProcess()) {
            viewer.disableAutoLayout();
        } else {
            System.err.println("Cannot activate DOT Layout, using auto layouts instead !");
            viewer.enableAutoLayout(new SpringBox());
        }
        view = new ControllableView(viewer);

        componentsLog = new JTextPane();
        componentsLog.setSize(new Dimension(400, 400));
        componentsLog.setPreferredSize(new Dimension(400, 400));
        componentsLog.setEditable(false);
        componentsLog.getDocument().addDocumentListener(new LimitLinesDocumentListener(300));

        DefaultComboBoxModel<SeverityWrapper> severityModel = new DefaultComboBoxModel<SeverityWrapper>();
        severityModel.addElement(SeverityWrapper.any);
        severityModel.addElement(SeverityWrapper.debug);
        severityModel.addElement(SeverityWrapper.inform);
        severityModel.addElement(SeverityWrapper.critical);

        severityFilterComponent = new JComboBox<SeverityWrapper>(severityModel);

        nameFilterComponent = new JComboBox<String>();
        nameItems.add(ANY);
        nameFilterComponent.addItem(ANY);
    }

    private void initLayout() {
        setLayout(new BorderLayout(5, 5));

        JPanel toolPanel = new JPanel(new MigLayout(""));
        UIUtils.addSeparator(toolPanel, "Log Filters");
        toolPanel.add(new JLabel("Filter by severity:"), "skip");
        toolPanel.add(severityFilterComponent, "h 18!,w 300!,growx,wrap");
        toolPanel.add(new JLabel("Filter by source:"), "skip");
        toolPanel.add(nameFilterComponent, "h 18!,w 300!,growx,wrap");
        UIUtils.addSeparator(toolPanel, "Debug Log");

        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.add(toolPanel, BorderLayout.NORTH);
        sidePanel.add(new JScrollPane(componentsLog, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.CENTER);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, view, sidePanel);
        splitPane.setDividerLocation((int) (0.65 * this.getSize().getWidth()));
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        add(splitPane, BorderLayout.CENTER);
    }

    private void initActions() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        CustomMouseObserver mouseObserver = new CustomMouseObserver(view);
        mouseObserver.addActivationListener(new CustomMouseObserver.NodeActivationListener() {
            public void nodeActivated(String nodeID, MouseEvent e) {
                if (e.isShiftDown()) {
                    cleanupEdges();
                    highlightNode(nodeID);
                }

                if (e.isControlDown()) {
                    highlightNode(nodeID);
                }
            }
        });

        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                splitPane.setDividerLocation((int) (0.65 * ComponentViewer.this.getSize().getWidth()));
            }
        });
    }

    private void cleanupEdges() {
        for (Edge e : graph.getEachEdge()) {
            e.setAttribute("ui.style", "fill-color:black;");
        }
    }

    private void highlightNode(String id) {
        for (Edge e : graph.getNode(id).getEdgeSet()) {
            e.setAttribute("ui.style", "fill-color:blue;");
        }
    }

    private void initGraph() {
        graph.clear();

        graph.addAttribute("ui.stylesheet",
                "graph { padding: 50px; } " +
                        "node { size-mode: fit; shape: rounded-box; fill-color: lightgray; stroke-mode: plain; padding: 10px; text-size: 20;}" +
                        "edge { shape: angle; size: 15px; arrow-shape: arrow; arrow-size: 20px, 6px; fill-color: #36454F; }");
    }

    private void renderAllGraph() {
        ComponentModel.ComponentType[] types = new ComponentModel.ComponentType[]{ComponentModel.ComponentType.SCHEDULER, ComponentModel.ComponentType.COMPONENT};

        for (ComponentModel.ComponentType type : types) {
            renderNodes(type, componentModel.getAllIds(type));
        }

        for (ComponentModel.ComponentType type : types) {
            renderEdges(type, componentModel.getAllIds(type));
        }
        DotLayoutManager.computeNodePositions(graph);
    }

    private void renderNodes(ComponentModel.ComponentType type, final Set<String> nodeIDs) {
        for (String id : nodeIDs) {
            ComponentInfo node = componentModel.getComponent(type, id);
            if (graph.getNode(node.getId()) == null) {
                graph.addNode(node.getId());
                graph.getNode(node.getId()).setAttribute("ui.label", node.getName());
            }
        }
    }

    private void renderEdges(ComponentModel.ComponentType type, final Set<String> nodeIDs) {
        for (String id : nodeIDs) {
            Set<String> outNodes = componentModel.getSubscribeComponentConnexions(type, id);
            for (String oid : outNodes) {
                graph.addEdge(id + "~" + oid, id, oid, true);
            }

            outNodes = componentModel.getSchedulerIDs(type, id);
            for (String oid : outNodes) {
                graph.addEdge(id + "~" + oid, id, oid, true);
            }
        }
    }

    public void highlightNode(String nodeID, long timestamp) {
        nodeID = componentModel.convertHostPortToUUID(nodeID);
        if (nodeID != null) {
            colorHighlighter.addNode(new HighlightClass(nodeID, "#32CD32", timestamp, 100));
            colorHighlighter.addNode(new HighlightClass(nodeID, "lightgray", timestamp, 200));
            if (!colorHighlighter.isAlive()) {
                colorHighlighter.start();
            }
        }
    }

    public void addMessage(int level, String source, String message) {
        if (validateSeverity(level) && validateAndUpdateName(source)) {
            StyledDocument doc = componentsLog.getStyledDocument();
            try {
                doc.insertString(doc.getLength(), message + "\n", formatMessage(level));
                componentsLog.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                //-- ignore
            }
        }
    }

    private boolean validateSeverity(int level) {
        SeverityWrapper selected = (SeverityWrapper) severityFilterComponent.getSelectedItem();
        return selected == null || selected.equals(SeverityWrapper.any) || selected.level == level;
    }

    private boolean validateAndUpdateName(String source) {
        String selectedName = (String) nameFilterComponent.getSelectedItem();

        if (!nameItems.contains(source)) {
            nameItems.add(source);
            nameFilterComponent.addItem(source);
        }

        return selectedName == null || selectedName.equals(ANY) || selectedName.equals(source);
    }

    private static final String debugStyle = "ds";

    private Style formatMessage(int level) {
        Style style = componentsLog.getStyle(debugStyle);
        if (style == null) {
            style = componentsLog.addStyle(debugStyle, null);
        }

        switch (level) {
            case DebugData.INFORM:
                StyleConstants.setForeground(style, Color.blue);
                break;
            case DebugData.CRITICAL:
                StyleConstants.setForeground(style, Color.red);
                break;
            case DebugData.DEBUG:
            default:
                StyleConstants.setForeground(style, Color.black);
                break;
        }
        return style;
    }

    private static enum SeverityWrapper {
        inform(DebugData.INFORM, "INFORM"),
        critical(DebugData.CRITICAL, "CRITICAL"),
        debug(DebugData.DEBUG, "DEBUG"),
        any(-1, ANY);

        private int level;
        private String label;

        private SeverityWrapper(int level, String label) {
            this.level = level;
            this.label = label;
        }

        public String toString() {
            return label;
        }
    }

    private static class HighlightClass implements org.ib.gui.util.Event {
        private String nodeID;
        private String color;
        private long timestamp;
        private int timeout;

        private HighlightClass(String nodeID, String color, long timestamp, int timeout) {
            this.nodeID = nodeID;
            this.color = color;
            this.timestamp = timestamp;
            this.timeout = timeout;
        }

        public String getEventID() {
            return nodeID + "." + color;
        }
    }

    private class ColorHighlighter extends Thread {
        private boolean running = true;
        private final ColapsableEventQueue<HighlightClass> executionQueue = new ColapsableEventQueue<HighlightClass>();

        private ColorHighlighter() {
        }

        public void addNode(HighlightClass node) {
            synchronized (executionQueue) {
                executionQueue.add(node);
            }
        }

        public void run() {
            while (running) {
                synchronized (executionQueue) {
                    if (!executionQueue.isEmpty()) {
                        HighlightClass node = executionQueue.poll();
                        if (node != null) {
                            if (System.currentTimeMillis() - node.timestamp >= node.timeout) {
                                Node graphNode = graph.getNode(node.nodeID);
                                if (graphNode != null) {
                                    graphNode.setAttribute("ui.style", "fill-color:" + node.color + ";");
                                }
                            } else {
                                executionQueue.add(node);
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    //-- nothing
                }
            }
        }
    }

    public static void main(String[] args) {
        ComponentViewer gui = new ComponentViewer();
        gui.setVisible(true);
    }
}
