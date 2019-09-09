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

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/3/12
 */
public class SimpleXMLParser {
    public static XMLProperties parseNode(Element node) {
        XMLProperties result = new XMLProperties();

        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            result.addProperty(attributes.item(i).getNodeName(), attributes.item(i).getNodeValue());
        }

        NodeList propNodes = node.getChildNodes();
        for (int i = 0; i < propNodes.getLength(); i++) {
            if (propNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                result.addProperty(propNodes.item(i).getNodeName(), propNodes.item(i).getTextContent().trim());
            }
        }

        return result;
    }

    public static XMLProperties parseDocument(Document document) {
        if (document != null) {
            return parseNode(document.getDocumentElement());
        } else {
            return XMLProperties.empty;
        }
    }

    public static XMLProperties parseDocument(File filename) {
        if (filename != null && filename.exists() && filename.canRead()) {
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(filename);
                doc.getDocumentElement().normalize();
                return parseDocument(doc);
            } catch (ParserConfigurationException e) {
                throw new IllegalArgumentException(e);
            } catch (SAXException e) {
                throw new IllegalArgumentException(e);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            return XMLProperties.empty;
        }
    }
}
