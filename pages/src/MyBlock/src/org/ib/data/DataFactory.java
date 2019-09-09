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

import org.msgpack.MessagePack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 10/1/13
 */
public class DataFactory {
    public static final byte[] EMPTY_BUFFER = new byte[0];

    private final Map<Integer, Class> factoryTypes = new HashMap<Integer, Class>();
    private MessagePack packer;

    public DataFactory(MessagePack packer) {
        this.packer = packer;
    }

    public void registerType(Class clazz) {
        synchronized (factoryTypes) {
            TypeIdentification identification = (TypeIdentification) clazz.getAnnotation(TypeIdentification.class);
            if (identification != null) {
                int typeID = identification.typeID();
                Class another = factoryTypes.get(typeID);
                if (another == null || another.equals(clazz)) {
                    factoryTypes.put(typeID, clazz);
                    packer.register(clazz);
                } else {
                    throw new IllegalArgumentException("Invalid typeID provided, Generic Type is already registered: " + another.getName() + " != " + clazz.getName());
                }
            } else {
                throw new IllegalArgumentException("Type class is not annotated with the TypeIdentification: " + clazz.getName());
            }
        }
    }

    public byte[] writeObject(GenericData genericData) throws IOException {
        TypeIdentification identification = genericData.getClass().getAnnotation(TypeIdentification.class);
        if (identification != null) {
            int typeID = identification.typeID();
            Class another = factoryTypes.get(typeID);

            if (another == null || !another.equals(genericData.getClass())) {
                throw new IllegalArgumentException("Invalid typeID provided, Generic Type is already registered: " + genericData.getClass().getName());
            } else {
                byte[] className = packer.write(typeID);
                byte[] dataBuffer = packer.write(genericData);

                return DataHelper.merge(className, dataBuffer);
            }
        }
        return EMPTY_BUFFER;
    }

    public GenericData readObject(byte[] buffer) throws IOException {
        ByteBuffer bbuffer = ByteBuffer.wrap(buffer);

        int typeID = packer.read(bbuffer, Integer.class);
        Class classType = factoryTypes.get(typeID);
        if (classType == null) {
            throw new IllegalArgumentException("Invalid typeID provided, Generic Type is not registered!");
        } else {
            GenericData result = (GenericData) packer.read(bbuffer, classType);
            if (result == null) {
                throw new IllegalArgumentException("Invalid typeID provided, Generic Type is NULL!");
            } else {
                return result;
            }
        }
    }

    public void printTypeAssociations() {
        System.out.println("Class type associations: ");
        for (Map.Entry<Integer, Class> item : factoryTypes.entrySet()) {
            System.out.println("\t" + item.getValue().getName() + " -> " + item.getKey());
        }
    }
}
