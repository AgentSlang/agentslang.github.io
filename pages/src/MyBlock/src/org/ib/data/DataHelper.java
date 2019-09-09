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

package org.ib.data;

import org.ib.service.generic.ClientManager;
import org.ib.service.topic.TopicClient;
import org.msgpack.MessagePack;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/21/12
 */
public class DataHelper {
    private static final MessagePack packer = new MessagePack();
    private static final DataFactory factory = new DataFactory(packer);

    public static void registerAllPrimitiveClasses() {
        // here register any funny type
        // Note: the Exception class cannot be registered
    }

    public static boolean registerAllGenericDataClasses() {
        registerAllPrimitiveClasses();

        Reflections reflections = new Reflections("");
        Set<Class<? extends GenericData>> subTypes = reflections.getSubTypesOf(GenericData.class);

        boolean result = subTypes.size() > 0;
        for (Class<? extends GenericData> item : subTypes) {
            if (!item.isAnonymousClass() && !Modifier.isAbstract(item.getModifiers())) {
                if (item.isAnnotationPresent(TypeIdentification.class)) {
                    try {
                        TypeIdentification annotation = item.getAnnotation(TypeIdentification.class);
                        boolean success = true;

                        for (Class primitive : annotation.primitiveRegisters()) {
                            if (primitive != void.class) {
                                try {
                                    packer.register(primitive);
                                } catch (Throwable e) {
                                    success = false;
                                    result = false;
                                    System.err.println("Unable to register primitive type: " + primitive.getName() + "\n" +
                                            "Check if the class has a default constructor, a getter and a setter for all the fields.");
                                    System.err.println(e.getMessage());
                                }
                            }
                        }

                        if (success) {
                            factory.registerType(item);
                        }
                    } catch (Exception e) {
                        System.err.println("Class not properly annotated for Type Registration: " + item.getName());
                        System.err.println("Check if the class has a default constructor, a getter and a setter for all the fields.");
                        System.err.println(e.getMessage());
                        result = false;
                    }
                } else {
                    System.err.println("Class not properly annotated for Type Registration: " + item.getName());
                    result = false;
                }
            }
        }
        if (result) {
            factory.printTypeAssociations();
        }
        return result;
    }

    public static DecodeResult decodeData(byte[] buffer) throws InvalidDataException {
        try {
            TopicClient tc = ClientManager.getClient(ClientManager.TOPIC);
            return new DecodeResult(tc.getTopic(extractTopic(buffer)), factory.readObject(extractData(buffer)));
        } catch (IOException e) {
            throw new InvalidDataException(e);
        }
    }

    public static byte[] encodeData(String topic, String host, GenericData data) throws InvalidDataException {
        try {
            TopicClient tc = ClientManager.getClient(ClientManager.TOPIC);
            return merge(tc.getEncoding(topic, host), factory.writeObject(data));
        } catch (IOException e) {
            throw new InvalidDataException(e);
        }
    }

    public static byte[] merge(byte[] prefix, byte[] data) {
        byte[] result = new byte[prefix.length + data.length];

        System.arraycopy(prefix, 0, result, 0, prefix.length);
        System.arraycopy(data, 0, result, prefix.length, data.length);

        return result;
    }

    public static byte[] extract(byte[] data, int from, int length) {
        byte[] result = new byte[length];

        System.arraycopy(data, from, result, 0, length);

        return result;
    }

    private static byte[] extractTopic(byte[] data) {
        return extract(data, 0, TopicClient.ENCODING_SIZE);
    }

    private static byte[] extractData(byte[] data) {
        return extract(data, TopicClient.ENCODING_SIZE, data.length - TopicClient.ENCODING_SIZE);
    }

    public static class DecodeResult {
        String topic;
        GenericData data;

        public DecodeResult(String topic, GenericData data) {
            this.topic = topic;
            this.data = data;
        }

        public String getTopic() {
            return topic;
        }

        public GenericData getData() {
            return data;
        }
    }
}
