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

import net.miginfocom.swing.MigLayout;
import org.ib.component.model.ComponentConfig;
import org.ib.component.model.ComponentInfo;
import org.ib.gui.data.DataProvider;
import org.ib.gui.util.ImageHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.*;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/10/13
 */
public class ComponentRenderer extends JPanel implements DataProvider.DataListener, ActionListener {
    private ComponentInfo componentInfo;

    private Map<String, Map.Entry<String, DefaultComboBoxModel>> subscriptionMap = new HashMap<String, Map.Entry<String, DefaultComboBoxModel>>();

    private JLabel componentName;
    private StateButton smallBtn;

    private JPanel fullPanel;

    private JComboBox hostnameValue;
    private JComboBox profileValue;
    private JFormattedTextField portValue;

    private JLabel schedulerLabel;
    private JComboBox schedulerValue;

    private EditableList publishValue;
    private EditableList subscribeValue;
    private EditableList paramsValue;

    private Icon minimizeIcon = ImageHelper.buildIcon("../icons/collapse_arrow.png", ComponentRenderer.class);
    private Icon maximizeIcon = ImageHelper.buildIcon("../icons/collapse_arrow1.png", ComponentRenderer.class);

    private boolean updating = false;

    private JPanel minimizedPanel;
    private String minimizationConstraints;

    private JPanel maximizedPanel;
    private String maximizationConstraints;

    public ComponentRenderer(ComponentInfo componentInfo) {
        this(componentInfo, null, null);
    }

    public ComponentRenderer(ComponentInfo componentInfo, JPanel minimizedPanel, JPanel maximizedPanel) {
        this(componentInfo, false, minimizedPanel, maximizedPanel);
    }

    public ComponentRenderer(ComponentInfo componentInfo, boolean smallDisplay) {
        this(componentInfo, smallDisplay, null, null);
    }

    public ComponentRenderer(ComponentInfo componentInfo, boolean smallDisplay, JPanel minimizedPanel, JPanel maximizedPanel) {
        super();

        this.componentInfo = componentInfo;
        this.minimizedPanel = minimizedPanel;
        this.maximizedPanel = maximizedPanel;

        initComponents();
        initLayout();
        initActions();

        updateField(ComponentConfig.PROPERTY_PORT, portValue);
        updateList(ComponentConfig.PROPERTY_PUBLISH, publishValue);
        updateList(ComponentConfig.PROPERTY_SUBSCRIBE, subscribeValue);

        updateProperties();

        schedulerLabel.setVisible(!smallDisplay);
        schedulerValue.setVisible(!smallDisplay);

        publishValue.setVisible(!smallDisplay);
        subscribeValue.setVisible(!smallDisplay);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black, 1, true),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));

        componentName = new JLabel(componentInfo.getName());
        smallBtn = new StateButton(minimizeIcon, maximizeIcon);

        fullPanel = new JPanel();

        hostnameValue = new JComboBox();
        profileValue = new JComboBox();
        portValue = new JFormattedTextField(NumberFormat.getIntegerInstance());

        schedulerLabel = new JLabel("Scheduler ");
        schedulerLabel.setVisible(false);

        schedulerValue = new JComboBox();
        schedulerValue.setVisible(false);

        publishValue = new EditableList("Publish", 2, "@");
        publishValue.setVisible(false);

        subscribeValue = new EditableList("Subscribe", 2, "@");
        subscribeValue.setVisible(false);

        paramsValue = new EditableList("Params", 2, "=");
        paramsValue.setVisible(false);
    }

    private void initLayout() {
        this.setLayout(new BorderLayout(2, 2));

        JPanel title = new JPanel(new BorderLayout(2, 2));
        title.add(smallBtn, BorderLayout.WEST);
        title.add(componentName, BorderLayout.CENTER);

        fullPanel.setLayout(new MigLayout("hidemode 2", "[pref!][fill,grow][pref!]"));

        fullPanel.add(new JLabel("Hostname "));
        fullPanel.add(hostnameValue, "wrap");

        fullPanel.add(new JLabel("Profile "));
        fullPanel.add(profileValue, "wrap");

        fullPanel.add(new JLabel("Port "));
        fullPanel.add(portValue, "wrap");

        fullPanel.add(schedulerLabel);
        fullPanel.add(schedulerValue, "wrap");

        fullPanel.add(publishValue, "span, grow, wrap");
        fullPanel.add(subscribeValue, "span, grow, wrap");

        fullPanel.add(paramsValue, "span, grow, wrap");

        add(title, BorderLayout.NORTH);
        add(fullPanel, BorderLayout.CENTER);
    }

    public void setMinimizationConstraints(String minimizationConstraints) {
        this.minimizationConstraints = minimizationConstraints;
    }

    public void setMaximizationConstraints(String maximizationConstraints) {
        this.maximizationConstraints = maximizationConstraints;
    }

    private void initActions() {
        smallBtn.addStateListener(new StateButton.StateListener() {
            public void stateChanged(int newState) {
                fullPanel.setVisible(newState == 0);
                if (minimizedPanel != null && maximizedPanel != null) {
                    if (newState == 0) {
                        minimizedPanel.remove(ComponentRenderer.this);
                        maximizedPanel.add(ComponentRenderer.this, maximizationConstraints);
                    } else {
                        maximizedPanel.remove(ComponentRenderer.this);
                        minimizedPanel.add(ComponentRenderer.this, minimizationConstraints);
                    }
                    minimizedPanel.revalidate();
                    minimizedPanel.repaint();

                    maximizedPanel.revalidate();
                    maximizedPanel.repaint();
                }
            }
        });

        hostnameValue.setActionCommand("hostname");
        hostnameValue.addActionListener(this);

        profileValue.setActionCommand("profile");
        profileValue.addActionListener(this);

        schedulerValue.setActionCommand("scheduler");
        schedulerValue.addActionListener(this);

        portValue.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                componentInfo.getProperties().setProperty(ComponentConfig.PROPERTY_PORT, evt.getNewValue() != null ? evt.getNewValue().toString() : null);
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        if (!updating) {
            if ("hostname".equals(e.getActionCommand())) {
                componentInfo.getProperties().setProperty(ComponentConfig.PROPERTY_MACHINE_NAME, getItemValue(hostnameValue));
            } else if ("profile".equals(e.getActionCommand())) {
                componentInfo.getProperties().setProperty(ComponentConfig.PROPERTY_PROFILE, getItemValue(profileValue));
            } else if ("scheduler".equals(e.getActionCommand())) {
                componentInfo.getProperties().setProperty(ComponentConfig.PROPERTY_SCHEDULER, getItemValue(schedulerValue));
            }
        }
    }

    public void minimize() {
        if (smallBtn.getState() == 0) {
            smallBtn.doClick();
        }
    }

    public ComponentInfo getComponentInfo() {
        return componentInfo;
    }

    private String getItemValue(JComboBox list) {
        return list.getSelectedItem() != null ? list.getSelectedItem().toString() : null;
    }

    private void updateList(String property, EditableList component) {
        component.updateList(componentInfo.getProperties().getPropertyList(property));
    }

    @SuppressWarnings("unchecked")
    private void updateProperties() {
        paramsValue.clean();
        Set<String> extraProperties = new HashSet<String>(componentInfo.getProperties().getPropertyTags());
        extraProperties.removeAll(ComponentConfig.specialProperties);

        for (String property : extraProperties) {
            for (String item : componentInfo.getProperties().getPropertyList(property)) {
                paramsValue.updateList(property + "=" + item);
            }
        }
    }

    private void updateField(String property, JTextField component) {
        component.setText(componentInfo.getProperties().getProperty(property));
    }

    @SuppressWarnings("unchecked")
    public void dataUpdated(DataProvider provider) {
        Map.Entry<String, DefaultComboBoxModel> listModelItem = subscriptionMap.get(provider.getId());
        if (listModelItem != null) {
            updating = true;
            listModelItem.getValue().removeAllElements();
            Collection<String> elements = new HashSet<String>();
            for (String item : provider.getItems()) {
                item = item.replace('@', ':');
                listModelItem.getValue().addElement(item);
                elements.add(item);
            }

            if (elements.contains(componentInfo.getProperties().getProperty(listModelItem.getKey()))) {
                listModelItem.getValue().setSelectedItem(componentInfo.getProperties().getProperty(listModelItem.getKey()));
            } else {
                listModelItem.getValue().setSelectedItem(null);
            }
            updating = false;
        }
    }

    private void subscribeToDataProvider(String propertyName, DataProvider dataProvider, DefaultComboBoxModel listModel) {
        subscriptionMap.put(dataProvider.getId(), new AbstractMap.SimpleEntry<String, DefaultComboBoxModel>(propertyName, listModel));
        synchronized (dataProvider) {
            dataUpdated(dataProvider);
            dataProvider.addDataListener(this);
        }
    }

    public void subscribeToHostnameProvider(DataProvider hostnameProvider) {
        subscribeToDataProvider(ComponentConfig.PROPERTY_MACHINE_NAME, hostnameProvider, (DefaultComboBoxModel) hostnameValue.getModel());
    }

    public void subscribeToComponentHostProvider(DataProvider componentHostProvider) {
        subscribeValue.subscribeParamToDataProvider(1, componentHostProvider);
    }

    public void subscribeToProfileProvider(DataProvider profileProvider) {
        subscribeToDataProvider(ComponentConfig.PROPERTY_PROFILE, profileProvider, (DefaultComboBoxModel) profileValue.getModel());
    }

    public void subscribeToSchedulerProvider(DataProvider schedulerProvider) {
        subscribeToDataProvider(ComponentConfig.PROPERTY_SCHEDULER, schedulerProvider, (DefaultComboBoxModel) schedulerValue.getModel());
    }

    public void subscribeToParamNamesProvider(DataProvider paramNamesProvider) {
        paramsValue.subscribeParamToDataProvider(0, paramNamesProvider);
        paramsValue.setVisible(!paramNamesProvider.getItems().isEmpty());
//        if (!paramNamesProvider.getItems().isEmpty()) {
//            fullPanel.add(paramsValue, "span, grow, wrap");
//        }
    }

    public void subscribeToOutputChannelsProvider(DataProvider outputChannelsProvider) {
        publishValue.subscribeParamToDataProvider(1, outputChannelsProvider);
        publishValue.setVisible(!outputChannelsProvider.getItems().isEmpty());
    }

    public static void main(String[] args) {
        DataProvider hostProvider = new DataProvider("hostnames", true);
        hostProvider.addItem("hostname1");
        hostProvider.addItem("hostname2");

        DataProvider profileProvider = new DataProvider("profiles", true);
        profileProvider.addItem("profile1");
        profileProvider.addItem("profile2");

        DataProvider schedulerProvider = new DataProvider("schedulers", true);
        schedulerProvider.addItem("hostname:1223");
        schedulerProvider.addItem("hostname:1224");

        ComponentInfo info = new ComponentInfo("123", "Simple Name");
        info.getProperties().addProperty(ComponentConfig.PROPERTY_PUBLISH, "feed1@internal1");
        info.getProperties().addProperty(ComponentConfig.PROPERTY_PUBLISH, "feed2@internal2");

        info.getProperties().addProperty(ComponentConfig.PROPERTY_SUBSCRIBE, "feed1@machine1");
        info.getProperties().addProperty(ComponentConfig.PROPERTY_SUBSCRIBE, "feed2@machine2");

        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout(10, 10));

        ComponentRenderer renderer = new ComponentRenderer(info);
        frame.add(renderer, BorderLayout.NORTH);

        renderer.subscribeToHostnameProvider(hostProvider);
        renderer.subscribeToProfileProvider(profileProvider);
        renderer.subscribeToSchedulerProvider(schedulerProvider);

        frame.setSize(new Dimension(400, 600));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        hostProvider.addItem("hostname3");
    }
}
