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

package org.ib.utils;

import java.util.*;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/3/12
 */
public class XMLProperties {
    public final static XMLProperties empty = new XMLProperties() {
        public void addProperty(String name, String value) {
            //-- removed
        }
    };

    private final Map<String, String> properties = new HashMap<String, String>();
    private final Map<String, List<String>> listProperties = new HashMap<String, List<String>>();

    public XMLProperties() {
    }

    public XMLProperties(XMLProperties properties) {
        addAll(properties);
    }

    public void setProperty(String name, String value) {
        if (properties.containsKey(name) || !listProperties.containsKey(name)) {
            properties.put(name, value);
        }
    }

    public void addProperty(String name, String value) {
        if (properties.containsKey(name)) {
            String oldValue = properties.get(name);
            removeProperty(name);
            addListProperty(name, oldValue);
            addListProperty(name, value);
        } else if (listProperties.containsKey(name)) {
            addListProperty(name, value);
        } else {
            properties.put(name, value);
        }
    }

    private void addListProperty(String name, String value) {
        List<String> values = listProperties.get(name);
        if (values == null) {
            values = new LinkedList<String>();
            listProperties.put(name, values);
        }
        values.add(value);
    }

    public void addAll(XMLProperties properties) {
        this.properties.putAll(properties.properties);
        this.listProperties.putAll(properties.listProperties);
    }

    public void removeProperty(String name) {
        properties.remove(name);
        listProperties.remove(name);
    }

    public Set<String> getPropertyTags() {
        return Collections.unmodifiableSet(properties.keySet());
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name) || listProperties.containsKey(name);
    }

    public String getProperty(String name) {
        if (properties.containsKey(name)) {
            return properties.get(name);
        } else if (listProperties.containsKey(name)) {
            return listProperties.get(name).iterator().next();
        } else {
            return null;
        }
    }

    public String getProperty(String name, String defaultProperty) {
        if (hasProperty(name)) {
            return getProperty(name);
        } else {
            return defaultProperty;
        }
    }

    public Collection<String> getPropertyList(String name) {
        if (properties.containsKey(name)) {
            return Arrays.asList(properties.get(name));
        } else if (listProperties.containsKey(name)) {
            return Collections.unmodifiableCollection(listProperties.get(name));
        } else {
            return Collections.emptyList();
        }
    }
}
