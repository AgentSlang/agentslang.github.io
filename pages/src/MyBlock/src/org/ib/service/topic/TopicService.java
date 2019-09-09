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

package org.ib.service.topic;

import org.ib.component.annotations.ConfigureParams;
import org.ib.service.generic.AbstractService;

import java.util.HashMap;
import java.util.Map;

import static org.ib.data.StringUtils.byteUnwrap;
import static org.ib.data.StringUtils.byteWrap;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/29/12
 */
@ConfigureParams()
public class TopicService extends AbstractService<String, String> {
    /*
       Topic client commands:
       +<topic>@<host>  - adds a new topic
       -<topic>@<host>  - removes a topic
       !<topic>@<host>  - returns the encoding
       ?<encoding>      - returns a topic
    */

    private Map<String, EncodingKey> topicCache = new HashMap<String, EncodingKey>();
    private Map<EncodingKey, String> encodingCache = new HashMap<EncodingKey, String>();

    private EncodingKey lastEncodingKey = new EncodingKey(new byte[]{1, 0, 0});

    public TopicService(String port) {
        super(port);
    }

    protected String convertData(byte[] data) {
        return new String(data);
    }

    protected byte[] convertData(String data) {
        return data.getBytes();
    }

    protected String handleRequest(String data) {
        char command = data.charAt(0);
        switch (command) {
            case '+':
                data = data.substring(1);
                return addTopic(data);
            case '-':
                data = data.substring(1);
                return removeTopic(data);
            case '!':
                data = data.substring(1);
                return getEncoding(data);
            case '?':
                data = data.substring(1);
                return getTopic(data);
        }
        return "";
    }

    private String addTopic(String topicHost) {
        if (!topicCache.containsKey(topicHost)) {
            EncodingKey encoding = generateEncoding();

            encodingCache.put(encoding, topicHost);
            topicCache.put(topicHost, encoding);
        }
        return "t";
    }

    private String removeTopic(String topicHost) {
        EncodingKey encoding = topicCache.get(topicHost);
        if (encoding != null) {
            topicCache.remove(topicHost);
            encodingCache.remove(encoding);
        }
        return "t";
    }

    private EncodingKey generateEncoding() {
        EncodingKey current = lastEncodingKey;
        lastEncodingKey = lastEncodingKey.createNewEncodingKey();
        return current;
    }

    public String getTopic(String encoding) {
        String topic = encodingCache.get(new EncodingKey(byteWrap(encoding, TopicClient.ENCODING_SIZE)));
        return topic == null ? "" : topic;
    }

    public String getEncoding(String topic) {
        if (topicCache.containsKey(topic)) {
            return byteUnwrap(topicCache.get(topic).key);
        } else {
            System.out.println("Requested non existing topic ... Generating it:" + topic);
            if (topic.contains("@")) {
                addTopic(topic);
                return getEncoding(topic);
            } else {
                return "";
            }
        }
    }
}
