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

package org.agent.slang.in.facereader;

import javolution.osgi.internal.OSGiServices;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @version 1, 5/28/15
 */
public class FaceReaderSender {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final OutputStream outputStream;

    public FaceReaderSender(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    private void writeInt32(int n) throws IOException {
        outputStream.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(n).array());
    }

    private void writeMessage(String messageType, byte[] message) throws IOException {
        final byte[] messageTypeBytes = messageType.getBytes(UTF8);
        writeInt32(4 + 4 + messageTypeBytes.length + message.length);
        writeInt32(messageTypeBytes.length);
        outputStream.write(messageTypeBytes);
        outputStream.write(message);
    }

    public void sendActionMessage(ActionMessage message) throws XMLStreamException, IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        XMLStreamWriter writer = OSGiServices.getXMLOutputFactory().createXMLStreamWriter(bytes);

        writer.writeStartDocument();
        writer.writeStartElement("ActionMessage");
        if (message.id != null) {
            writer.writeStartElement("Id");
                writer.writeCharacters(message.id);
            writer.writeEndElement();
        }

            writer.writeStartElement("ActionType");
                writer.writeCharacters(message.type.toString());
            writer.writeEndElement();

        if (message.information != null && !message.information.isEmpty()) {
            writer.writeStartElement("Information");
            for (String information: message.information) {
                writer.writeStartElement("string");
                    writer.writeCharacters(information);
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
        writer.writeEndDocument();

        writeMessage("FaceReaderAPI.Messages.ActionMessage", bytes.toByteArray());
    }
}
