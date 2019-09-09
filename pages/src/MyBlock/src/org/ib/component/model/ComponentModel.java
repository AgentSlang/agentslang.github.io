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

package org.ib.component.model;

import org.ib.component.ScheduleManager;
import org.ib.utils.SimpleXMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/4/12
 */
public class ComponentModel {
    public static enum ComponentType {
        SERVICE,
        CLIENT,
        SCHEDULER,
        COMPONENT
    }

    private static final ComponentType[] availableTypes =
            new ComponentType[]{ComponentType.SERVICE, ComponentType.CLIENT,
                    ComponentType.SCHEDULER, ComponentType.COMPONENT};

    private Map<ComponentType, Map<String, ComponentInfo>> componentInfoMap = new HashMap<ComponentType, Map<String, ComponentInfo>>();
    private Map<String, String> hostUUIDMap = new HashMap<String, String>();

    private Set<String> profiles = new HashSet<String>();
    private Set<String> profileHosts = new HashSet<String>();
    private Set<String> hosts = new HashSet<String>();

    public ComponentModel() {
    }

    public ComponentModel(String filename) {
        parseModel(new File(filename));
    }

    public ComponentModel(File filename) {
        parseModel(filename);
    }

    private void parseModel(File filename) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(filename);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName(ComponentConfig.TAG_PROJECT);
            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    NodeList profiles = eElement.getElementsByTagName(ComponentConfig.TAG_PROFILE);
                    for (int j = 0; j < profiles.getLength(); j++) {
                        Node profileNode = profiles.item(j);
                        if (profileNode.getNodeType() == Node.ELEMENT_NODE) {
                            String host = profileNode.getAttributes().getNamedItem(ComponentConfig.PROPERTY_MACHINE_NAME).getTextContent().trim();
                            String profile = profileNode.getAttributes().getNamedItem(ComponentConfig.PROPERTY_NAME).getTextContent().trim();

                            this.profileHosts.add(profile + "@" + host);
                            this.profiles.add(profile);
                            this.hosts.add(host);

                            parseElement((Element) profileNode,
                                    ComponentConfig.TAG_SERVICES, ComponentConfig.TAG_SERVICE, ComponentType.SERVICE,
                                    host, profile, filename);
                            parseElement((Element) profileNode,
                                    ComponentConfig.TAG_CLIENTS, ComponentConfig.TAG_CLIENT, ComponentType.CLIENT,
                                    host, profile, filename);
                            parseElement((Element) profileNode,
                                    ComponentConfig.TAG_COMPONENTS, ComponentConfig.TAG_COMPONENT, ComponentType.COMPONENT,
                                    host, profile, filename);
                            parseSchedulers((Element) profileNode,
                                    ComponentConfig.TAG_SCHEDULER,
                                    host, profile, filename);
                        }
                    }
                }
            }
        } catch (Exception e) {
            componentInfoMap.clear();
            e.printStackTrace();
        }
    }

    private void parseElement(Element profileNode, String listTag, String elementTag, ComponentType componentType,
                              String host, String profile, File filename) {
        NodeList elementsByTagName = profileNode.getElementsByTagName(listTag);
        for (int k = 0; k < elementsByTagName.getLength(); k++) {
            if (elementsByTagName.item(k).getNodeType() == Node.ELEMENT_NODE) {
                NodeList nodeList = ((Element) elementsByTagName.item(k)).getElementsByTagName(elementTag);
                for (int p = 0; p < nodeList.getLength(); p++) {
                    if (nodeList.item(p).getNodeType() == Node.ELEMENT_NODE
                            && nodeList.item(p).getParentNode().equals(elementsByTagName.item(k))) {
                        ComponentConfig properties = extractProperties((Element) nodeList.item(p));
                        properties.addProperty(ComponentConfig.PROPERTY_MACHINE_NAME, host);
                        properties.addProperty(ComponentConfig.PROPERTY_PROFILE, profile);
                        properties.addProperty(ComponentConfig.PROPERTY_FILENAME, filename.getAbsolutePath());
                        addComponent(componentType, setupComponent(componentType, properties));
                    }
                }
            }
        }
    }

    private void parseSchedulers(Element profileNode, String schedulerTag, String host, String profile, File filename) {
        NodeList schedulers = profileNode.getElementsByTagName(schedulerTag);
        for (int k = 0; k < schedulers.getLength(); k++) {
            if (schedulers.item(k).getNodeType() == Node.ELEMENT_NODE && schedulers.item(k).getParentNode().equals(profileNode)) {
                ComponentConfig properties = extractProperties((Element) schedulers.item(k));
                properties.addProperty(ComponentConfig.PROPERTY_MACHINE_NAME, host);
                properties.addProperty(ComponentConfig.PROPERTY_PROFILE, profile);
                properties.addProperty(ComponentConfig.PROPERTY_FILENAME, filename.getAbsolutePath());
                addComponent(ComponentType.SCHEDULER, setupComponent(ComponentType.SCHEDULER, properties));
            }
        }
    }

    private ComponentConfig extractProperties(Element element) {
        return new ComponentConfig(SimpleXMLParser.parseNode(element));
    }

    private ComponentInfo setupComponent(ComponentType type, ComponentConfig properties) {
        String name = properties.getProperty(ComponentConfig.PROPERTY_NAME);
        if (type == ComponentType.SCHEDULER) {
            name = ScheduleManager.class.getName();
        }
        return new ComponentInfo(generateID(), name, properties);
    }

    public void addComponent(ComponentType type, String id, ComponentInfo componentInfo) {
        if (ComponentType.SCHEDULER == type || ComponentType.COMPONENT == type) {
            String hostPortPair = joinHostPort(componentInfo.getProperties().getProperty(ComponentConfig.PROPERTY_MACHINE_NAME),
                    componentInfo.getProperties().getProperty(ComponentConfig.PROPERTY_PORT));
            if (hostUUIDMap.containsKey(hostPortPair)) {
                throw new IllegalArgumentException("Hostname already configured for component ! Hostname = " + hostPortPair);
            } else {
                hostUUIDMap.put(hostPortPair, id);
            }
        }

        Map<String, ComponentInfo> components = componentInfoMap.get(type);
        if (components == null) {
            components = new LinkedHashMap<String, ComponentInfo>();
            componentInfoMap.put(type, components);
        }
        components.put(id, componentInfo);
    }

    public static String generateID() {
        return UUID.randomUUID().toString();
    }

    private static String joinHostPort(String host, String port) {
        return host + ":" + port;
    }

    public String convertHostPortToUUID(String hostPort) {
        return hostUUIDMap.get(hostPort);
    }

    public Set<String> getProfiles() {
        return profiles;
    }

    public Set<String> getHosts() {
        return hosts;
    }

    public Set<String> getProfileHosts() {
        return profileHosts;
    }

    public void addComponent(ComponentType type, ComponentInfo componentInfo) {
        addComponent(type, componentInfo.getId(), componentInfo);
    }

    public void removeComponent(ComponentType type, String id) {
        Map<String, ComponentInfo> components = componentInfoMap.get(type);
        if (components != null) {
            components.remove(id);
        }
    }

    public ComponentInfo getComponent(ComponentType type, String id) {
        return componentInfoMap.get(type).get(id);
    }

    public Set<String> getAllIds(ComponentType type) {
        Map<String, ComponentInfo> items = componentInfoMap.get(type);
        if (items == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(items.keySet());
        }
    }

    public Set<String> getSubscribeComponentConnexions(ComponentType type, String id) {
        Set<String> result = new HashSet<String>();

        ComponentInfo componentInfo = getComponent(type, id);
        if (componentInfo != null) {
            for (String subscription : componentInfo.getProperties().getPropertyList(ComponentConfig.PROPERTY_SUBSCRIBE)) {
                String[] split = subscription.split("@");
                String uid = hostUUIDMap.get(split[1]);
                if (uid != null) {
                    result.add(uid);
                }
            }
        }

        return result;
    }

    public Set<String> getSchedulerIDs(ComponentType type, String id) {
        Set<String> result = new HashSet<String>();
        ComponentInfo componentInfo = getComponent(type, id);
        if (componentInfo != null) {
            for (String scheduler : componentInfo.getProperties().getPropertyList(ComponentConfig.PROPERTY_SCHEDULER)) {
                String uid = hostUUIDMap.get(scheduler);
                if (uid != null) {
                    result.add(uid);
                }
            }
        }

        return result;
    }

    public ComponentType[] getAvailableTypes() {
        return availableTypes;
    }

    public boolean isEmpty() {
        return componentInfoMap.isEmpty();
    }
}
