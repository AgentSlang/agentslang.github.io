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

package org.ib.logger;

import org.ib.component.base.Publisher;
import org.ib.data.DebugData;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/21/12
 */
public class Logger {
    public final static int DEBUG = DebugData.DEBUG;
    public final static int INFORM = DebugData.INFORM;
    public final static int CRITICAL = DebugData.CRITICAL;

    private final static Map<Publisher, String> topicMapper = new HashMap<Publisher, String>();

    public static void setupDebug(Publisher source) {
        String topic = generateTopic(source);

        topicMapper.put(source, topic);
        source.publish(topic, topic);
        source.addOutboundTypeChecker(topic, DebugData.class);
    }

    public static String generateTopic(Publisher source) {
        return source.getClass().getName() + ".debug";
    }

    public static String generateTopic(String source) {
        return source + ".debug";
    }

    public static void log(Publisher source, int level, String message) {
        log(source, level, message, null);
    }

    public static void log(Publisher source, int level, String message, Exception e) {
        String topic = topicMapper.get(source);
        if (topic != null) {
            String exception = null;
            if (e != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                exception = sw.toString();
            }
            source.publishData(topic, new DebugData(level, source.getClass().getName(), message, exception));
        } else if (level == CRITICAL) {
            System.err.println(String.format("(CRITICAL)[%s] %s", source.getClass().getName(), message));
            if (e != null) {
                e.printStackTrace();
            }
        }
    }
}
