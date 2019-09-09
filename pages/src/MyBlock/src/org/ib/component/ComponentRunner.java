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

package org.ib.component;

import com.beust.jcommander.JCommander;
import org.ib.component.base.AbstractComponent;
import org.ib.component.base.Closeable;
import org.ib.component.model.ComponentConfig;
import org.ib.component.model.ComponentInfo;
import org.ib.component.model.ComponentModel;
import org.ib.component.model.validation.*;
import org.ib.data.DataHelper;
import org.ib.service.generic.AbstractClient;
import org.ib.service.generic.AbstractService;
import org.ib.service.generic.ClientManager;
import org.zeromq.ZMQException;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/23/12
 */
public class ComponentRunner {
    private static final Collection<Closeable> closable = new LinkedList<Closeable>();
    private static final ModelValidationManager modelValidationManager = new ModelValidationManager();

    static {
        modelValidationManager.addValidator(new ComponentsValidation());
        modelValidationManager.addValidator(new HostnameValidation());
        modelValidationManager.addValidator(new LinkValidation());
        modelValidationManager.addValidator(new DebugValidation());
        modelValidationManager.addValidator(new ComponentParametersValidation());
        modelValidationManager.addValidator(new ComponentDataTypeValidation());
    }

    public static void main(String[] args) {
        RunParams params = new RunParams();
        JCommander cmdParser = new JCommander(params, args);
        cmdParser.setProgramName(ComponentRunner.class.getSimpleName());

        File configFilename = null;
        Set<String> profileNames = new HashSet<String>();

        if (params.configFile != null && !params.profiles.isEmpty()) {
            //new style params
            configFilename = new File(params.configFile);
            profileNames.addAll(params.profiles);
        } else if (params.oldStyle.size() > 1) {
            // old style
            System.err.println("Warning: Using parameters in old style mode, without switch syntax. " +
                    "Please update your script to use the new style params.");
            configFilename = new File(params.oldStyle.remove(0));
            profileNames.addAll(params.oldStyle);
        } else {
            cmdParser.usage();
            System.exit(1);
        }

        if (!configFilename.exists() || !configFilename.canRead()) {
            System.err.println("Error: Invalid config file provided: " + configFilename.getAbsolutePath());
            cmdParser.usage();
            System.exit(1);
        }

        if (profileNames.isEmpty()) {
            System.err.println("Error: Empty profile list provided");
            cmdParser.usage();
            System.exit(1);
        }

        if (!DataHelper.registerAllGenericDataClasses()) {
            System.err.println("Error: Unable to register all the Data Types");
            System.exit(2);
        }

        ComponentModel model = new ComponentModel(configFilename);
        ModelValidationResult validationResult = modelValidationManager.checkComponentModel(model, false);
        if (validationResult.hasErrors()) {
            for (String line : validationResult.getErrorList()) {
                System.err.println("Error: " + line);
            }
            System.exit(2);
        }

        if (!params.ignoreWarnings) {
            if (validationResult.hasWarnings()) {
                for (String line : validationResult.getWarningList()) {
                    System.err.println("Warning: " + line);
                }
                if (!params.noExitOnWarnings) {
                    System.exit(2);
                }
            }
        }

        ScheduleManager scheduleManager = null;
        boolean validProfile = false;
        boolean schedulerExists = false;
        for (ComponentModel.ComponentType type : model.getAvailableTypes()) {
            for (String id : model.getAllIds(type)) {
                ComponentInfo componentInfo = model.getComponent(type, id);

                if (type == ComponentModel.ComponentType.SCHEDULER) {
                    schedulerExists = true;
                }

                if (profileNames.contains(componentInfo.getProperties().getProperty(ComponentConfig.PROPERTY_PROFILE))) {
                    //At least one component is loaded
                    validProfile = true;
                    try {
                        switch (type) {
                            case SERVICE:
                                setupService(componentInfo.getProperties());
                                break;
                            case CLIENT:
                                setupClient(componentInfo.getProperties());
                                break;
                            case SCHEDULER:
                                scheduleManager = setupScheduler(componentInfo.getProperties());
                                break;
                            case COMPONENT:
                                setupComponent(componentInfo.getProperties());
                                break;
                        }
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                        System.err.println("Got exception: " + e.getCause().getMessage());
                        System.err.println("ComponentID: " + componentInfo.getId());
                        System.err.println("Component Name: " + componentInfo.getName());
                        System.err.println("Cannot start platform ... exiting the current instance !");
                        System.exit(2);
                    } catch (ZMQException e) {
                        System.err.println("Got exception: " + e.getMessage());
                        System.err.println("ComponentID: " + componentInfo.getId());
                        System.err.println("Component Name: " + componentInfo.getName());
                        System.err.println("Cannot start platform ... exiting the current instance !");
                        System.exit(2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (validProfile) {
            if (scheduleManager != null) {
                scheduleManager.start();
            } else if (!schedulerExists) {
                System.err.println("Something went wrong, the scheduler is still null ...");
                System.exit(3);
            }
        } else {
            System.err.println("Invalid profile names provided: " + Arrays.toString(profileNames.toArray()));
            System.exit(3);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                for (Closeable closeable : closable) {
                    try {
                        closeable.close();
                    } catch (Throwable e) {
                        //-- ignore
                    }
                }
            }
        });
    }

    private static ScheduleManager setupScheduler(ComponentConfig properties) throws Exception {
        ScheduleManager sm = new ScheduleManager(Integer.parseInt(properties.getProperty(ComponentConfig.PROPERTY_TIMEOUT)),
                properties.getProperty(ComponentConfig.PROPERTY_PORT), properties.getProperty(ComponentConfig.PROPERTY_MACHINE_NAME));
        closable.add(sm);
        return sm;
    }

    private static void setupService(ComponentConfig properties) throws Exception {
        Constructor constructor = Class.forName(properties.getProperty(ComponentConfig.PROPERTY_NAME)).getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        AbstractService service = (AbstractService) constructor.newInstance(properties.getProperty(ComponentConfig.PROPERTY_PORT));
        service.configure(properties);
        service.start();
        closable.add(service);
    }

    private static void setupClient(ComponentConfig properties) throws Exception {
        Constructor constructor = Class.forName(properties.getProperty(ComponentConfig.PROPERTY_NAME)).getDeclaredConstructor(String.class, String.class);
        constructor.setAccessible(true);
        AbstractClient client = (AbstractClient) constructor.newInstance(properties.getProperty(ComponentConfig.PROPERTY_HOST), properties.getProperty(ComponentConfig.PROPERTY_PORT));
        client.configure(properties);
        ClientManager.addClient(client.getClass().getSimpleName(), client);
        closable.add(client);
    }

    private static void setupComponent(ComponentConfig properties) throws Exception {
        Constructor constructor = Class.forName(properties.getProperty(ComponentConfig.PROPERTY_NAME)).getDeclaredConstructor(String.class, ComponentConfig.class);
        constructor.setAccessible(true);
        AbstractComponent component = (AbstractComponent) constructor.newInstance(properties.getProperty(ComponentConfig.PROPERTY_PORT), properties);
        closable.add(component);

        for (String subscription : properties.getPropertyList(ComponentConfig.PROPERTY_SUBSCRIBE)) {
            String[] topicHost = subscription.split("@");
            if (topicHost.length > 1) {
                component.subscribe(topicHost[0], topicHost[1], component);
            }
        }

        for (String publish : properties.getPropertyList(ComponentConfig.PROPERTY_PUBLISH)) {
            String[] topicMap = publish.split("@");
            if (topicMap.length > 1) {
                component.publish(topicMap[0], topicMap[1]);
            }
        }

        component.setupComponentConfig(properties);
    }
}
