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
import javolution.text.CharArray;
import javolution.util.FastTable;
import javolution.xml.stream.XMLStreamConstants;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReader;

import java.io.InputStream;

/**
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @version 1, 5/28/15
 */
final class XMLReader {
    private XMLReader() {
    }

    public static Classification readClassification(InputStream in) throws XMLStreamException {
        XMLStreamReader reader = OSGiServices.getXMLInputFactory().createXMLStreamReader(in);

        Classification classification = new Classification();
        classification.setClassificationValues(new FastTable<Classification.ClassificationValue>());

        while (reader.hasNext()) {
            if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                CharArray tag = reader.getLocalName();

                if (tag.equals("LogType")) {
                    classification.setLogType(Classification.LogType.valueOf(reader.getElementText().toString()));
                }
                else if (tag.equals("AnalysisType")) {
                    classification.setAnalysisType(Classification.AnalysisType.valueOf(reader.getElementText().toString()));
                }
                else if (tag.equals("FrameNumber")) {
                    classification.setFrameNumber(reader.getElementText().toInt());
                }
                else if (tag.equals("AnalysisStartTime")) {
                    classification.setAnalysisStartTime(reader.getElementText().toString());
                }
                else if (tag.equals("FrameTimeTicks")) {
                    classification.setFrameTimeTicks(reader.getElementText().toInt());
                }
                else if (tag.equals("ClassificationValues")) {
                    while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                        Classification.ClassificationValue cv = new Classification.ClassificationValue();

                        while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                            CharArray cvTag = reader.getLocalName();

                            if (cvTag.equals("Type")) {
                                CharArray type = reader.getElementText();
                                if (type.equals("Value")) {
                                    cv.setType(Classification.ClassificationValue.Type.Value);
                                    cv.setValues(new FastTable<Float>());
                                }
                                else {
                                    cv.setType(Classification.ClassificationValue.Type.State);
                                    cv.setStates(new FastTable<String>());
                                }
                            }

                            else if (cvTag.equals("Label")) {
                                cv.setLabel(reader.getElementText().toString());
                            }

                            else if (cvTag.equals("Value")) {
                                while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                                    cv.getValues().add(reader.getElementText().toFloat());
                                }
                            }

                            else if (cvTag.equals("State")) {
                                while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                                    cv.getStates().add(reader.getElementText().toString());
                                }
                            }
                        }

                        classification.getClassificationValues().add(cv);
                    }
                }
            }
        }

        return classification;
    }

    public static ResponseMessage readResponseMessage(InputStream in) throws XMLStreamException {
        XMLStreamReader reader = OSGiServices.getXMLInputFactory().createXMLStreamReader(in);

        ResponseMessage message = new ResponseMessage();

        while (reader.hasNext()) {
            if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                CharArray tag = reader.getLocalName();

                if (tag.equals("Id")) {
                    message.id = reader.getElementText().toString();
                }

                else if (tag.equals("ResponseType")) {
                    message.type = ResponseMessage.Type.valueOf(reader.getElementText().toString());
                }

                else if (tag.equals("Information")) {
                    message.information = new FastTable<>();
                    while (reader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                        message.information.add(reader.getElementText().toString());
                    }
                }
            }
        }

        return message;
    }
}
