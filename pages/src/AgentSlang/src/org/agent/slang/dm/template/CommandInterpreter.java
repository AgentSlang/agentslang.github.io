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

package org.agent.slang.dm.template;

import org.agent.slang.data.template.TemplateData;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.StringData;
import org.ib.utils.SimpleXMLParser;
import org.ib.utils.XMLProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides command interpreting tools for agentslang. 
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/2/13
 */
@ConfigureParams(mandatoryConfigurationParams = "commandModel",
        outputChannels = "command.data", outputDataTypes = StringData.class,
        inputDataTypes = TemplateData.class)
public class CommandInterpreter extends MixedComponent {
    private static final String feedbackChannel = "command.data";
    private static final String PROP_COMMANDS = "commandModel";
    private Map<String, CommandTemplate> commandTemplateMap;

    public CommandInterpreter(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
    protected void setupComponent(ComponentConfig config) {
        commandTemplateMap = new HashMap<String, CommandTemplate>();
        loadCommandTemplates(config.getFileProperty(PROP_COMMANDS, true));
    }

    /**
     * Loads commands templates from the input file which is mentioned in configuration
     * @param path input file path
     */
    private void loadCommandTemplates(File path) {
        if (path != null && path.canRead()) {
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(path);
                doc.getDocumentElement().normalize();

                NodeList nList = doc.getElementsByTagName(CommandTemplate.TAG_COMMANDS);
                for (int i = 0; i < nList.getLength(); i++) {
                    Node nNode = nList.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;

                        NodeList commands = eElement.getElementsByTagName(CommandTemplate.TAG_COMMAND);
                        for (int j = 0; j < commands.getLength(); j++) {
                            if (commands.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                XMLProperties properties = SimpleXMLParser.parseNode((Element) commands.item(j));

                                String id = properties.getProperty(CommandTemplate.PROP_ID);
                                CommandTemplate ct = new CommandTemplate(id, properties.getProperty(CommandTemplate.PROP_REPLY_TEXT),
                                        properties.getProperty(CommandTemplate.PROP_REPLY_COMMAND));
                                commandTemplateMap.put(id, ct);
                            }
                        }
                    }
                }
            } catch (ParserConfigurationException e) {
                throw new IllegalArgumentException(e);
            } catch (SAXException e) {
                throw new IllegalArgumentException(e);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    /**
     * Managing input and output data in the class.
     * @param data input data
     */
    protected void handleData(GenericData data) {
        if (!((TemplateData) data).isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String id : ((TemplateData) data).getTemplateIDs()) {
                CommandTemplate template = commandTemplateMap.get(id);
                if (template != null) {
                    if (template.getReplyText() != null) {
                        sb.append(template.getReplyText()).append(" ");
                    }
                    executeCommand(template.getReplyCommand());
                }
            }
            publishData(feedbackChannel, new StringData(data.getId(), sb.toString(), ((TemplateData) data).getLanguage()));
        }
    }

    /**
     * Executes a command
     * @param command command string
     */
    private void executeCommand(String command) {
        if (command != null) {
            //todo; implement with care
        }
    }

    /**
     * Checking type of input data 
     */
    public void defineReceivedData() {
        addInboundTypeChecker(TemplateData.class);
    }

    /**
     * Checking type of output data.
     */
    public void definePublishedData() {
        addOutboundTypeChecker(feedbackChannel, StringData.class);
    }
}
