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

package org.ib.component.model.validation;

import org.ib.component.model.ComponentConfig;
import org.ib.component.model.ComponentInfo;
import org.ib.component.model.ComponentModel;
import org.ib.service.cns.CNClient;
import org.ib.service.cns.CNService;
import org.ib.utils.SimpleXMLParser;
import org.ib.utils.XMLProperties;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/16/13
 */
public class HostnameValidation implements ModelValidation {
    public ModelValidationResult checkModel(ComponentModel model) {
        ModelValidationResult result = new ModelValidationResult();
        Map<String, String> cnsModel = new HashMap<String, String>();
        Set<String> configuredServiceHostNames = new HashSet<String>();

        loadCNSModel(model, configuredServiceHostNames, cnsModel, result);
        if (cnsModel.isEmpty()) {
            result.addErrorMessage("No valid Computer Name list provided ...");
        } else {
            configuredServiceHostNames = resolveHostPairs(configuredServiceHostNames, cnsModel, result);
            validateClientsHostNames(model, configuredServiceHostNames, cnsModel, result);
            validateComponentHostNames(model, cnsModel, result);
        }

        return result;
    }

    private void loadCNSModel(ComponentModel model, Set<String> configuredServiceHostNames,
                              Map<String, String> cnsModel, ModelValidationResult modelValidationResult) {
        for (String componentID : model.getAllIds(ComponentModel.ComponentType.SERVICE)) {
            ComponentInfo info = model.getComponent(ComponentModel.ComponentType.SERVICE, componentID);
            String hostPort = info.getProperties().getProperty(ComponentConfig.PROPERTY_MACHINE_NAME) + ":"
                    + info.getProperties().getProperty(ComponentConfig.PROPERTY_PORT);
            if (CNService.class.getName().equals(info.getName())) {
                Map<String, String> partialModel = loadHostNames(modelValidationResult, info.getProperties());
                if (partialModel.isEmpty()) {
                    modelValidationResult.addErrorMessage("Invalid Component Name Service model provided on "
                            + info.getProperties().getProperty(ComponentConfig.PROPERTY_MACHINE_NAME)
                            + " * Profile: " + info.getProperties().getProperty(ComponentConfig.PROPERTY_PROFILE));
                } else {
                    cnsModel.putAll(partialModel);
                }
                configuredServiceHostNames.add(hostPort);
            } else {
                configuredServiceHostNames.add(hostPort);
            }
        }
    }

    private Map<String, String> loadHostNames(ModelValidationResult modelValidationResult, XMLProperties properties) {
        Map<String, String> result = new HashMap<String, String>();
        File pathFileName = new File(properties.getProperty(ComponentConfig.PROPERTY_FILENAME)).getParentFile();
        File configFileName = new File(pathFileName, properties.getProperty(CNService.P_CONFIG));
        XMLProperties machineProperties = SimpleXMLParser.parseDocument(configFileName);
        for (String item : machineProperties.getPropertyList(CNService.P_MACHINE)) {
            String[] pair = item.trim().split("@");
            if (pair.length > 1) {
                if (isAccessible(pair[1])) {
                    result.put(pair[0], pair[1]);
                } else {
                    modelValidationResult.addErrorMessage("Invalid ip configuration provided: " + item.trim()
                            + " * CNService config name: " + configFileName.getAbsolutePath());
                }
            } else {
                modelValidationResult.addErrorMessage("Invalid hostname pair: " + item.trim());
            }
        }
        return result;
    }

    /**
     * Resolve configured service list to real ip:port pairs
     *
     * @param configuredServiceHostNames - a set of host:port pairs
     * @param cnsModel                   - the computer name resolution model
     * @param modelValidationResult      - error report
     * @return a set of ip:pairs, corresponding to the configuredServiceHostNames parameter
     */
    private Set<String> resolveHostPairs(Set<String> configuredServiceHostNames,
                                         Map<String, String> cnsModel, ModelValidationResult modelValidationResult) {
        Set<String> result = new HashSet<String>();
        for (String item : configuredServiceHostNames) {
            String[] pair = item.split(":");
            String ip = cnsModel.get(pair[0]);
            if (ip == null) {
                modelValidationResult.addErrorMessage("Cannot resolve computer name: " + item);
            } else {
                Set<String> realIp = getIP(ip);
                if (realIp.isEmpty()) {
                    modelValidationResult.addErrorMessage("Cannot resolve hostname: " + ip);
                } else {
                    for (String ipItem : realIp) {
                        result.add(ipItem + ":" + pair[1]);
                    }
                }
            }
        }
        return result;
    }

    private void validateClientsHostNames(ComponentModel model, Set<String> configuredServiceHostNames,
                                          Map<String, String> cnsModel, ModelValidationResult modelValidationResult) {
        for (String componentID : model.getAllIds(ComponentModel.ComponentType.CLIENT)) {
            ComponentInfo info = model.getComponent(ComponentModel.ComponentType.CLIENT, componentID);
            if (CNClient.class.getName().equals(info.getName())) {
                String hostPort = info.getProperties().getProperty(ComponentConfig.PROPERTY_HOST)
                        + ":" + info.getProperties().getProperty(ComponentConfig.PROPERTY_PORT);
                if (!configuredServiceHostNames.contains(hostPort)) {
                    modelValidationResult.addErrorMessage("Invalid client connection parameters for " + hostPort);
                }
            } else {
                String ip = cnsModel.get(info.getProperties().getProperty(ComponentConfig.PROPERTY_HOST));
                if (ip == null) {
                    ip = info.getProperties().getProperty(ComponentConfig.PROPERTY_HOST);
                }

                Set<String> realIp = getIP(ip);
                if (realIp.isEmpty()) {
                    modelValidationResult.addErrorMessage("Cannot resolve hostname: " + ip);
                } else {
                    boolean found = false;
                    for (String item : realIp) {
                        String hostPort = item + ":" + info.getProperties().getProperty(ComponentConfig.PROPERTY_PORT);
                        if (configuredServiceHostNames.contains(hostPort)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        modelValidationResult.addErrorMessage("Invalid client connection parameters for " + ip);
                    }
                }
            }
        }
    }

    private void validateComponentHostNames(ComponentModel model, Map<String, String> cnsModel,
                                            ModelValidationResult modelValidationResult) {
        for (String componentID : model.getAllIds(ComponentModel.ComponentType.COMPONENT)) {
            ComponentInfo info = model.getComponent(ComponentModel.ComponentType.COMPONENT, componentID);
            for (String subscription : info.getProperties().getPropertyList(ComponentConfig.PROPERTY_SUBSCRIBE)) {
                String[] topicHost = subscription.split("@");
                if (topicHost.length > 1) {
                    String[] pair = topicHost[1].split(":");
                    if (!cnsModel.containsKey(pair[0])) {
                        modelValidationResult.addErrorMessage("Invalid hostname subscription for component: "
                                + info.getName() + " * Subscribed for: " + subscription);
                    }
                }
            }
        }
    }

    private boolean isAccessible(String ip) {
//        try {
//            return InetAddress.getByName(ip).isReachable(1000);
//        } catch (IOException e) {
//            return false;
//        }
        //todo; due to a stupid java bug, is reachable always returns false on windows 7 and 8. Disabled it until another solution is found.
        return true;
    }

    private Set<String> getIP(String hostname) {
        Set<String> result = new HashSet<String>();
        try {
            for (InetAddress item : InetAddress.getAllByName(hostname)) {
                result.add(item.getHostAddress());
                if (item.isAnyLocalAddress() || item.isLoopbackAddress()) {
                    result.add("127.0.0.1");
                }
                try {
                    if (NetworkInterface.getByInetAddress(item) != null) {
                        result.add("127.0.0.1");
                    }
                } catch (SocketException e) {
                    //-- ignore
                }
            }
        } catch (IOException e) {
            //-- ignore error
        }
        return result;
    }
}
