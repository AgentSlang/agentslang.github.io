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

package org.ib.gui.designer;

import net.miginfocom.swing.MigLayout;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.ib.component.annotations.AnnotationUtils;
import org.ib.component.model.ComponentConfig;
import org.ib.component.model.ComponentInfo;
import org.ib.component.model.ComponentModel;
import org.ib.gui.components.ComponentRenderer;
import org.ib.gui.components.EditableList;
import org.ib.gui.components.events.EditEvent;
import org.ib.gui.components.events.EditListener;
import org.ib.gui.data.DataProvider;
import org.ib.gui.util.*;
import org.ib.gui.util.springbox.SpringBox;
import org.ib.service.cns.CNService;
import org.ib.utils.SimpleXMLParser;
import org.ib.utils.XMLProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/10/13
 */
public class DesignerMain extends JFrame implements ActionListener {
    private DataProvider hostProvider;
    private DataProvider componentHostProvider;
    private DataProvider profileProvider;
    private DataProvider schedulerProvider;

    private int nextPort = 1;

    private JTabbedPane tabbedPane;

    private JPanel settingsPanel;
    private JPanel maximizedComponentsPanel;
    private JPanel minimizedComponentsPanel;
    private JPanel viewPanel;
    private JPanel deployPanel;

    private ComponentModel model;

    private Graph graph;
    private ViewPanel view;

    private Map<String, ComponentRenderer> rendererMap = new HashMap<String, ComponentRenderer>();

    private JList availableComponentsList;
    private JList configuredComponentsList;

    private static final String ACTION_ADD_COMPONENT = "add.component";
    private static final String ACTION_COPY_COMPONENT = "copy.component";
    private static final String ACTION_REMOVE_COMPONENT = "remove.component";

    private JButton addComponent;
    private JButton copyComponent;
    private JButton removeComponent;

    private EditableList hostsValue;
    private EditableList profilesValue;
    private EditableList schedulersValue;


    public DesignerMain() {
        super("AgentSlang Designer");
        setIconImage(ImageHelper.buildImage("../icons/logo.png", DesignerMain.class));

        initComponents();
        initLayout();
        initActions();
        initGraph();
        loadAvailableList();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        setSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(800, 600));

        hostProvider = new DataProvider("hostnames", true);
        componentHostProvider = new DataProvider("componentHostProvider", true);
        profileProvider = new DataProvider("profiles", true);
        schedulerProvider = new DataProvider("schedulers", true);

        tabbedPane = new JTabbedPane();

        settingsPanel = new JPanel();
        maximizedComponentsPanel = new JPanel();
        minimizedComponentsPanel = new JPanel();
        minimizedComponentsPanel.setPreferredSize(new Dimension(455, 455));

        viewPanel = new JPanel();
        deployPanel = new JPanel();

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

        availableComponentsList = new JList(new DefaultListModel());
        configuredComponentsList = new JList(new DefaultListModel());

        addComponent = new JButton("Add component", ImageHelper.buildIcon("../icons/new.png", DesignerMain.class));
        addComponent.setMargin(new Insets(2, 2, 2, 2));

        copyComponent = new JButton("Copy component", ImageHelper.buildIcon("../icons/copy.png", DesignerMain.class));
        copyComponent.setMargin(new Insets(2, 2, 2, 2));

        removeComponent = new JButton("Remove component", ImageHelper.buildIcon("../icons/cut.png", DesignerMain.class));
        removeComponent.setMargin(new Insets(2, 2, 2, 2));

        hostsValue = new EditableList("Hosts", 2, "=");
        profilesValue = new EditableList("Profiles:", 2, "@");
        schedulersValue = new EditableList("Schedulers", 2, ":");
    }

    private void initLayout() {
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);

        JPanel fullComponentsPanel = new JPanel(new BorderLayout(5, 5));
        fullComponentsPanel.add(new JScrollPane(maximizedComponentsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        fullComponentsPanel.add(new JScrollPane(minimizedComponentsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.WEST);

        JPanel fullSettingsPanel = new JPanel(new BorderLayout(5, 5));
        fullSettingsPanel.add(new JScrollPane(settingsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

        tabbedPane.addTab("1. Settings", ImageHelper.buildIcon("../icons/gear.png", DesignerMain.class), fullSettingsPanel);
        tabbedPane.addTab("2. Components", ImageHelper.buildIcon("../icons/computer.png", DesignerMain.class), fullComponentsPanel);
        tabbedPane.addTab("3. View", ImageHelper.buildIcon("../icons/view.png", DesignerMain.class), viewPanel);
        tabbedPane.addTab("4. Deploy", ImageHelper.buildIcon("../icons/deploy.png", DesignerMain.class), deployPanel);

        maximizedComponentsPanel.setLayout(new MigLayout("wrap", "[][]", "top"));
        minimizedComponentsPanel.setLayout(new MigLayout("wrap", "", "top"));

        viewPanel.setLayout(new BorderLayout());
        viewPanel.add(view, BorderLayout.CENTER);

        JPanel availableComponentsPanel = new JPanel(new MigLayout("right,fill"));
        fullComponentsPanel.add(new JScrollPane(availableComponentsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.EAST);

        UIUtils.addSeparator(availableComponentsPanel, "Available components");
        availableComponentsPanel.add(new JScrollPane(availableComponentsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), "w 335!, h 400!,span, wrap");
        availableComponentsPanel.add(addComponent, "skip, wrap");
        UIUtils.addSeparator(availableComponentsPanel, "Configured components");
        availableComponentsPanel.add(new JScrollPane(configuredComponentsList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), "w 335!, h 400!, span, wrap");
        availableComponentsPanel.add(copyComponent, "");
        availableComponentsPanel.add(removeComponent, "wrap");

        JPanel topSettingsPanel = new JPanel(new MigLayout("fill", "", "top"));
        topSettingsPanel.add(hostsValue, "grow, h 400!");
        topSettingsPanel.add(profilesValue, "grow, h 400!");
        topSettingsPanel.add(schedulersValue, "grow, h 400!");
        fullSettingsPanel.add(topSettingsPanel, BorderLayout.NORTH);

        settingsPanel.setLayout(new MigLayout("wrap", "[][][]", "top"));
    }

    private void initActions() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        new CustomMouseObserver(view);

        addComponent.setActionCommand(ACTION_ADD_COMPONENT);
        addComponent.addActionListener(this);

        copyComponent.setActionCommand(ACTION_COPY_COMPONENT);
        copyComponent.addActionListener(this);

        removeComponent.setActionCommand(ACTION_REMOVE_COMPONENT);
        removeComponent.addActionListener(this);

        profilesValue.subscribeParamToDataProvider(1, hostProvider);
        schedulersValue.subscribeParamToDataProvider(0, hostProvider);

        hostsValue.addEditListener(new EditListener() {
            public void dataUpdated(EditEvent event) {
                String newValue = event.getNewValue().split("=")[0];
                String oldValue = event.getOldValue().split("=")[0];
                if (EditEvent.ACTION_NEW.equals(event.getAction())) {
                    hostProvider.addItem(newValue);
                } else if (EditEvent.ACTION_EDIT.equals(event.getAction())) {
                    hostProvider.removeItem(oldValue);
                    hostProvider.addItem(newValue);
                } else if (EditEvent.ACTION_DELETE.equals(event.getAction())) {
                    hostProvider.removeItem(oldValue);
                }
            }
        });

        profilesValue.addEditListener(new EditListener() {
            public void dataUpdated(EditEvent event) {
                String newValue = event.getNewValue().split("@")[0];
                String oldValue = event.getOldValue().split("@")[0];
                if (EditEvent.ACTION_NEW.equals(event.getAction())) {
                    profileProvider.addItem(newValue);
                } else if (EditEvent.ACTION_EDIT.equals(event.getAction())) {
                    profileProvider.removeItem(oldValue);
                    profileProvider.addItem(newValue);
                } else if (EditEvent.ACTION_DELETE.equals(event.getAction())) {
                    profileProvider.removeItem(oldValue);
                }
            }
        });
    }

    private void initGraph() {
        graph.clear();

        graph.addAttribute("ui.stylesheet",
                "graph { padding: 50px; } " +
                        "node { size-mode: fit; shape: rounded-box; fill-color: lightgray; stroke-mode: plain; padding: 10px; text-size: 20;}" +
                        "edge { shape: angle; size: 15px; arrow-shape: arrow; arrow-size: 20px, 6px; fill-color: #36454F; }");
    }

    //todo; listen to component edit event -> change ids ?
    public void actionPerformed(ActionEvent e) {
        if (ACTION_ADD_COMPONENT.equals(e.getActionCommand())) {
            addNewComponent();
        } else if (ACTION_COPY_COMPONENT.equals(e.getActionCommand())) {
            //todo; implement
        } else if (ACTION_REMOVE_COMPONENT.equals(e.getActionCommand())) {
            //todo; implement
        }
    }

    private void renderAllGraph() {
        initGraph();

        ComponentModel.ComponentType[] types = new ComponentModel.ComponentType[]{ComponentModel.ComponentType.SCHEDULER, ComponentModel.ComponentType.COMPONENT};

        for (ComponentModel.ComponentType type : types) {
            renderNodes(type, model.getAllIds(type));
        }

        for (ComponentModel.ComponentType type : types) {
            renderEdges(type, model.getAllIds(type));
        }
        DotLayoutManager.computeNodePositions(graph);
    }

    private void renderNodes(ComponentModel.ComponentType type, final Set<String> nodeIDs) {
        for (String id : nodeIDs) {
            ComponentInfo node = model.getComponent(type, id);
            if (graph.getNode(node.getId()) == null) {
                graph.addNode(node.getId());
                graph.getNode(node.getId()).setAttribute("ui.label", node.getName());
            }
        }
    }

    private void renderEdges(ComponentModel.ComponentType type, final Set<String> nodeIDs) {
        for (String id : nodeIDs) {
            Set<String> outNodes = model.getSubscribeComponentConnexions(type, id);
            for (String oid : outNodes) {
                graph.addEdge(id + "-" + oid, id, oid, true);
            }

            outNodes = model.getSchedulerIDs(type, id);
            for (String oid : outNodes) {
                graph.addEdge(id + "-" + oid, id, oid, true);
            }
        }
    }

    private void addNewComponent() {
        if (availableComponentsList.getSelectedValue() != null) {
            String componentName = availableComponentsList.getSelectedValue().toString();

            if (hostProvider.getItems().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please add at least one host", "Host error", JOptionPane.ERROR_MESSAGE);
            } else {
                String firstHost = hostProvider.getItems().iterator().next();
                String firstPort = "" + nextPort;
                ComponentInfo componentInfo = new ComponentInfo(ComponentModel.generateID(), componentName);
                componentInfo.getProperties().setProperty(ComponentConfig.PROPERTY_MACHINE_NAME, firstHost);
                componentInfo.getProperties().setProperty(ComponentConfig.PROPERTY_PORT, firstPort);

                addComponent(componentInfo, true);
            }
        }
    }

    private void addComponent(ComponentInfo componentInfo, boolean refresh) {
        ComponentRenderer renderer = new ComponentRenderer(componentInfo, false, minimizedComponentsPanel, maximizedComponentsPanel);
        renderer.subscribeToHostnameProvider(hostProvider);
        renderer.subscribeToProfileProvider(profileProvider);
        renderer.subscribeToSchedulerProvider(schedulerProvider);
        renderer.subscribeToComponentHostProvider(componentHostProvider);
        renderer.subscribeToParamNamesProvider(DataProvider.createStaticDataProvider("paramNames", AnnotationUtils.getAvailableProperties(componentInfo.getName())));
        renderer.subscribeToOutputChannelsProvider(DataProvider.createStaticDataProvider("outputChannels", AnnotationUtils.getAvailableOutputChannels(componentInfo.getName())));

        nextPort = Math.max(nextPort, Integer.parseInt(componentInfo.getProperties().getProperty(ComponentConfig.PROPERTY_PORT)) + 1);

        maximizedComponentsPanel.add(renderer, "w 485!, height ::650");
        renderer.setMaximizationConstraints("w 485!, height ::650");
        renderer.setMinimizationConstraints("w 450!, height ::650");

        rendererMap.put(componentInfo.getId(), renderer);
        componentHostProvider.addItem(componentInfo.getId().replace('@', ':'));

        if (refresh) {
            model.addComponent(ComponentModel.ComponentType.COMPONENT, componentInfo.getId(), componentInfo);

            maximizedComponentsPanel.revalidate();
            maximizedComponentsPanel.repaint();

            renderAllGraph();
            loadConfiguredList();
        } else {
            renderer.minimize();
        }
    }

    private void addServices(ComponentInfo componentInfo, boolean refresh) {
        ComponentRenderer renderer = new ComponentRenderer(componentInfo, true);
        renderer.subscribeToHostnameProvider(hostProvider);
        renderer.subscribeToProfileProvider(profileProvider);
        renderer.subscribeToParamNamesProvider(DataProvider.createStaticDataProvider("paramNames", AnnotationUtils.getAvailableProperties(componentInfo.getName())));

        settingsPanel.add(renderer, "w 485!, height ::650");

        if (refresh) {
            settingsPanel.revalidate();
            settingsPanel.repaint();
        } else {
            renderer.minimize();
        }
    }

    @SuppressWarnings("unchecked")
    private void loadAvailableList() {
        ((DefaultListModel) availableComponentsList.getModel()).clear();

        for (String item : AnnotationUtils.getAvailableComponents()) {
            ((DefaultListModel) availableComponentsList.getModel()).addElement(item);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadConfiguredList() {
        ((DefaultListModel) configuredComponentsList.getModel()).clear();

        for (ComponentRenderer item : rendererMap.values()) {
            ((DefaultListModel) configuredComponentsList.getModel()).addElement(item.getComponentInfo().getName());
        }
    }

    private void refreshList(EditableList listComponent, Set<String> values) {
        listComponent.updateList(values);
    }

    public boolean loadModel(File path) {
        if (path.exists() && path.canRead()) {
            model = new ComponentModel(path.getAbsolutePath());

            if (!model.isEmpty()) {
                File hostsConfigFile = null;

                for (String id : model.getAllIds(ComponentModel.ComponentType.SERVICE)) {
                    ComponentInfo componentInfo = model.getComponent(ComponentModel.ComponentType.SERVICE, id);
                    if (componentInfo.getName().equals(CNService.class.getName())) {
                        File pathFileName = new File(componentInfo.getProperties().getProperty(ComponentConfig.PROPERTY_FILENAME)).getParentFile();
                        URI url = pathFileName.toURI();
                        hostsConfigFile = new File(url.resolve(componentInfo.getProperties().getProperty(CNService.P_CONFIG)));
                    }
                }

                hostProvider.addItems(model.getHosts());
                refreshList(hostsValue, loadHosts(hostsConfigFile));

                profileProvider.addItems(model.getProfiles());
                refreshList(profilesValue, model.getProfileHosts());

                for (String id : model.getAllIds(ComponentModel.ComponentType.SCHEDULER)) {
                    schedulerProvider.addItem(id);
                    schedulersValue.updateList(id.replace('@', ':'));
                }

                for (String id : model.getAllIds(ComponentModel.ComponentType.COMPONENT)) {
                    ComponentInfo componentInfo = model.getComponent(ComponentModel.ComponentType.COMPONENT, id);
                    addComponent(componentInfo, false);
                }

                for (String id : model.getAllIds(ComponentModel.ComponentType.SERVICE)) {
                    ComponentInfo componentInfo = model.getComponent(ComponentModel.ComponentType.SERVICE, id);
                    addServices(componentInfo, false);
                }

                maximizedComponentsPanel.revalidate();
                maximizedComponentsPanel.repaint();

                settingsPanel.revalidate();
                settingsPanel.repaint();

                renderAllGraph();
                loadConfiguredList();

                return true;
            }
        }
        return false;
    }

    private Set<String> loadHosts(File hostsConfig) {
        Set<String> result = new HashSet<String>();
        if (hostsConfig != null && hostsConfig.exists()) {
            XMLProperties machineProperties = SimpleXMLParser.parseDocument(hostsConfig);
            for (String item : machineProperties.getPropertyList(CNService.P_MACHINE)) {
                result.add(item.trim().replace('@', '='));
            }
        }
        return result;
    }

    public static void main(String[] args) {
        SplashScreen splashScreen = new SplashScreen();
        splashScreen.setVisible(true);
    }
}
