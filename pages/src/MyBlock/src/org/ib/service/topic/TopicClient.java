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

import org.ib.service.generic.AbstractClient;

import java.util.HashMap;
import java.util.Map;

import static org.ib.data.StringUtils.byteUnwrap;
import static org.ib.data.StringUtils.byteWrap;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/1/12
 */

public class TopicClient extends AbstractClient<String, String> {
    public final static int ENCODING_SIZE = 3;
    /*
        Topic client commands:
        +<topic>@<host>  - adds a new topic
        -<topic>@<host>  - removes a topic
        !<topic>@<host>  - returns the encoding
        ?<encoding>      - returns a topic
     */

    private Map<String, EncodingKey> topicCache = new HashMap<String, EncodingKey>();
    private Map<EncodingKey, String> encodingCache = new HashMap<EncodingKey, String>();

    public TopicClient(String host, String port) {
        super(host, port);
    }

    protected String convertData(byte[] data) {
        return new String(data);
    }

    protected byte[] convertData(String data) {
        return data.getBytes();
    }

    public boolean addTopic(String topic, String host) {
        String result = request("+" + topic + "@" + host);
        return result.toLowerCase().equals("t");
    }

    public boolean removeTopic(String topic, String host) {
        String result = request("-" + topic + "@" + host);
        return result.toLowerCase().equals("t");
    }

    public byte[] getEncoding(String topic, String host) {
        String topicHost = topic + "@" + host;
        byte[] result = fromEncodingCache(topicHost);
        if (result == null) {
            return cache(topicHost, byteWrap(request("!" + topicHost), TopicClient.ENCODING_SIZE));
        } else {
            return result;
        }
    }

    public String getTopic(byte[] encoding) {
        String result = fromTopicCache(encoding);
        if (result == null) {
            return cache(encoding, request("?" + byteUnwrap(encoding)));
        } else {
            return result;
        }
    }

    private byte[] fromEncodingCache(String key) {
        EncodingKey result = topicCache.get(key);
        return result == null ? null : result.key;
    }

    private String fromTopicCache(byte[] encoding) {
        EncodingKey key = new EncodingKey(encoding);
        return encodingCache.get(key);
    }

    private byte[] cache(String key, byte[] value) {
        topicCache.put(key, new EncodingKey(value));
        return value;
    }

    private String cache(byte[] key, String value) {
        encodingCache.put(new EncodingKey(key), value);
        return value;
    }
}
