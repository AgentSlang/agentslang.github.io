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

import org.ib.utils.XMLProperties;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/23/12
 */
public class ComponentConfig extends XMLProperties {
    public static final String TAG_PROJECT = "project";
    public static final String TAG_PROFILE = "profile";

    public static final String TAG_SCHEDULER = "scheduler";

    public static final String TAG_SERVICES = "services";
    public static final String TAG_SERVICE = "service";

    public static final String TAG_CLIENTS = "clients";
    public static final String TAG_CLIENT = "client";

    public static final String TAG_COMPONENTS = "components";
    public static final String TAG_COMPONENT = "component";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_MACHINE_NAME = "hostname";
    public static final String PROPERTY_PROFILE = TAG_PROFILE;
    public static final String PROPERTY_FILENAME = "filename";
    // used by the clients to connect to a service, for components use hostname
    public static final String PROPERTY_HOST = "host";
    public static final String PROPERTY_PORT = "port";

    public static final String PROPERTY_TIMEOUT = "timeout";
    public static final String PROPERTY_SUBSCRIBE = "subscribe";
    public static final String PROPERTY_PUBLISH = "publish";
    public static final String PROPERTY_SCHEDULER = "scheduler";

    public static final Set<String> specialProperties = new HashSet<String>();

    static {
        specialProperties.add(PROPERTY_NAME);
        specialProperties.add(PROPERTY_MACHINE_NAME);
        specialProperties.add(PROPERTY_PROFILE);
        specialProperties.add(PROPERTY_FILENAME);
        specialProperties.add(PROPERTY_HOST);
        specialProperties.add(PROPERTY_PORT);
        specialProperties.add(PROPERTY_TIMEOUT);
        specialProperties.add(PROPERTY_SUBSCRIBE);
        specialProperties.add(PROPERTY_PUBLISH);
        specialProperties.add(PROPERTY_SCHEDULER);
    }

    private File configFilePath = null;

    public ComponentConfig() {
    }

    public ComponentConfig(XMLProperties properties) {
        super(properties);
    }

    public File getConfigFile() {
        if (configFilePath == null) {
            if (hasProperty(PROPERTY_FILENAME)) {
                configFilePath = new File(getProperty(PROPERTY_FILENAME));
            }
        }
        return configFilePath;
    }

    public File getConfigPath() {
        if (getConfigFile() != null) {
            if (configFilePath.isFile()) {
                return configFilePath.getParentFile();
            } else {
                return configFilePath;
            }
        } else {
            return null;
        }
    }

    public File getFileProperty(String property, String defaultValue, boolean failOnNotFound) {
        if (hasProperty(property)) {
            File result = new File(getProperty(property));
            if (!result.isAbsolute()) {
                result = new File(getConfigPath(), getProperty(property));
            }
            return result;
        } else if (defaultValue == null) {
            if (failOnNotFound) {
                throw new IllegalArgumentException("Invalid property: " + property);
            } else {
                return null;
            }
        } else {
            return new File(defaultValue);
        }
    }

    public File getFileProperty(String property, boolean failOnNotFound) {
        return getFileProperty(property, null, failOnNotFound);
    }

    public File getFileProperty(String property) {
        return getFileProperty(property, false);
    }
}
