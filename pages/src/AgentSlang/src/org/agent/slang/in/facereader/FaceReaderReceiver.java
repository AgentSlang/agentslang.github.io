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

import javolution.xml.stream.XMLStreamException;
import org.apache.commons.io.input.BoundedInputStream;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.EventListener;

interface ClassificationListener extends EventListener {
    void onClassification(Classification classification);
}

interface ResponseMessageListener extends EventListener {
    void onResponseMessage(ResponseMessage message);
}

/**
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @version 1, 5/28/15
 */
class FaceReaderReceiver implements Runnable {
    private final InputStream stream;
    private final EventListenerList listeners = new EventListenerList();
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final ByteBuffer sizeBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);

    public FaceReaderReceiver(InputStream stream) {
        this.stream = stream;
    }

    public void addClassificationListener(ClassificationListener listener) {
        listeners.add(ClassificationListener.class, listener);
    }

    public void removeClassificationListener(ClassificationListener listener) {
        listeners.remove(ClassificationListener.class, listener);
    }

    public void addResponseMessageListener(ResponseMessageListener listener) {
        listeners.add(ResponseMessageListener.class, listener);
    }

    public void removeResponseMessageListener(ResponseMessageListener listener) {
        listeners.remove(ResponseMessageListener.class, listener);
    }

    @Override
    public void run() {
        try {
            while (true) {
                int packetSize = readInt32() - 4; // - 4 because the packet size takes its own size into account

                int messageTypeSize = readInt32();
                packetSize -= 4;

                byte[] messageTypeBytes = new byte[messageTypeSize];
                for (int read = 0; read < messageTypeSize; read += stream.read(messageTypeBytes, read, messageTypeSize - read));
                String messageType = new String(messageTypeBytes, UTF8);
                packetSize -= messageTypeSize;

                InputStream boundedMessageStream = new BoundedInputStream(stream, packetSize);

                switch (messageType) {
                    case "FaceReaderAPI.Data.Classification":
                        Classification classification = XMLReader.readClassification(boundedMessageStream);
                        for (ClassificationListener listener: listeners.getListeners(ClassificationListener.class)) {
                            listener.onClassification(classification);
                        }
                        break;
                    case "FaceReaderAPI.Messages.ResponseMessage": {
                        ResponseMessage message = XMLReader.readResponseMessage(boundedMessageStream);
                        for (ResponseMessageListener listener: listeners.getListeners(ResponseMessageListener.class)) {
                            listener.onResponseMessage(message);
                        }
                        break;
                    }
                    default: {
                        System.err.println("Received unknown message type " + messageType);
                        byte[] message = new byte[packetSize];
                        for (int read = 0; read < packetSize; read += boundedMessageStream.read(message, read, packetSize - read));
                        System.err.println("Message: " + new String(message, UTF8));
                        break;
                    }
                }

                /* make sure that every single byte is read, we don’t want to
                 * interpret part of the message as the size of the next one
                 */
                boundedMessageStream.skip(packetSize);
            }
        }
        catch (XMLStreamException | IOException e) {
            Thread t = Thread.currentThread();
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
        }
    }

    private int readInt32() throws IOException {
        for (int read = 0; read < 4; read += stream.read(sizeBuffer.array(), read, 4 - read));
        return sizeBuffer.getInt(0);
    }
}
