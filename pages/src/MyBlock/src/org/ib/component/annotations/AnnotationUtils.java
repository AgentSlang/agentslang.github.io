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

package org.ib.component.annotations;

import org.ib.component.base.AbstractComponent;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/17/13
 */
public class AnnotationUtils {
    public static Set<String> getAvailableComponents() {
        Set<String> result = new HashSet<String>();
        Reflections reflections = new Reflections("");
        Set<Class<? extends AbstractComponent>> subTypes = reflections.getSubTypesOf(AbstractComponent.class);

        for (Class<? extends AbstractComponent> item : subTypes) {
            if (!item.isAnonymousClass() && !Modifier.isAbstract(item.getModifiers())) {
                if (!item.isAnnotationPresent(TestClass.class)) {
                    result.add(item.getName());
                }
            }
        }
        return result;
    }

    public static Set<String> getAvailableProperties(String className) {
        Set<String> result = new HashSet<String>();

        try {
            Class classLoaded = Class.forName(className);
            ConfigureParams params = (ConfigureParams) classLoaded.getAnnotation(ConfigureParams.class);

            if (params != null && params.mandatoryConfigurationParams() != null) {
                for (String param : params.mandatoryConfigurationParams()) {
                    if (param.trim().length() > 0) {
                        result.add(param.trim());
                    }
                }
            }

            if (params != null && params.optionalConfigurationParams() != null) {
                for (String param : params.optionalConfigurationParams()) {
                    if (param.trim().length() > 0) {
                        result.add(param.trim());
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            //-- ignore
        }

        return result;
    }

    public static Set<String> getAvailableOutputChannels(String className) {
        Set<String> result = new HashSet<String>();

        try {
            Class classLoaded = Class.forName(className);
            ConfigureParams params = (ConfigureParams) classLoaded.getAnnotation(ConfigureParams.class);

            if (params != null && params.outputChannels() != null) {
                for (String param : params.outputChannels()) {
                    if (param.trim().length() > 0) {
                        result.add(param.trim());
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            //-- ignore
        }

        return result;
    }

    public static void main(String[] args) {
        for (String item : getAvailableComponents()) {
            System.out.println("Class: " + item);
            Set<String> results = getAvailableProperties(item);
            if (!results.isEmpty()) {
                System.out.println("\tProperties:");
                for (String result : results) {
                    System.out.println("\t\t" + result);
                }
            }
            results = getAvailableOutputChannels(item);
            if (!results.isEmpty()) {
                System.out.println("\tChannels:");
                for (String result : results) {
                    System.out.println("\t\t" + result);
                }
            }
            System.out.println();
        }
    }

}
