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

import org.agent.slang.data.annotation.GenericTextAnnotation;
import org.agent.slang.data.template.TemplateData;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.logger.Logger;
import org.ib.utils.SimpleXMLParser;
import org.ib.utils.XMLProperties;
import org.syn.n.bad.dictionary.Dictionary;
import org.syn.n.bad.dictionary.DictionaryException;
import org.syn.n.bad.pattern.Matcher;
import org.syn.n.bad.pattern.PatternMatcher;
import org.syn.n.bad.pattern.TemplateMatchResult;
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
import java.util.HashSet;
import java.util.Set;

/**
 * Provides agentslnag with the ability of extracting and matching commands and dialogues.
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/31/12
 */
@ConfigureParams(mandatoryConfigurationParams = {"commandModel", "dialogueModel", "dictionaryConfig"},
        outputChannels = {"templateExc.command.data", "templateExc.dialogue.data"},
        outputDataTypes = {TemplateData.class, TemplateData.class},
        inputDataTypes = GenericTextAnnotation.class)
public class TemplateExtractor extends MixedComponent {
    private static final String commandOutFeed = "templateExc.command.data";
    private static final String dialogueOutFeed = "templateExc.dialogue.data";

    private static final String PROP_COMMANDS = "commandModel";
    private static final String PROP_DIALOGUE = "dialogueModel";
    private static final String PROP_DICTIONARY = "dictionaryConfig";

    private Matcher matcherCommand;
    private Matcher matcherDialogue;

    public TemplateExtractor(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
    protected void setupComponent(ComponentConfig config) {
        try {
            Dictionary.setupDictionary(config.getFileProperty(PROP_DICTIONARY));
        } catch (DictionaryException e) {
            throw new IllegalStateException(e);
        }

        matcherCommand = new Matcher();
        matcherDialogue = new Matcher();

        loadCommandMatcher(config.getFileProperty(PROP_COMMANDS, true), matcherCommand);
        loadDialogueMatcher(config.getFileProperty(PROP_DIALOGUE, true), matcherDialogue);
    }

    /**
     * initiates and loads command matcher based on input configuration
     * @param path input file configuration path 
     * @param matcher command matcher 
     */
    private void loadCommandMatcher(File path, Matcher matcher) {
        if (path != null && path.canRead()) {
            Set<String> ids = new HashSet<String>();

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

                        String prefix = eElement.getAttributes().getNamedItem(CommandTemplate.PROP_PREFIX).getTextContent().trim();

                        NodeList commands = eElement.getElementsByTagName(CommandTemplate.TAG_COMMAND);
                        for (int j = 0; j < commands.getLength(); j++) {
                            if (commands.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                XMLProperties properties = SimpleXMLParser.parseNode((Element) commands.item(j));

                                String id = properties.getProperty(CommandTemplate.PROP_ID);
                                if (ids.contains(id)) {
                                    Logger.log(this, Logger.INFORM, "Duplicate matcher for command. id=" + id);
                                } else {

                                    matcher.addMatcher(new PatternMatcher(id, prefix + " " + properties.getProperty(CommandTemplate.PROP_PATTERN)));
                                    ids.add(id);
                                }
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
     * initiates and loads dialogue matcher based on input configuration
     * @param path input file configuration path 
     * @param matcher dialogue matcher 
     */
    private void loadDialogueMatcher(File path, Matcher matcher) {
        if (path != null && path.canRead()) {
            Set<String> ids = new HashSet<String>();

            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(path);
                doc.getDocumentElement().normalize();

                NodeList nList = doc.getElementsByTagName(DialogueUnit.TAG_DIALOGUE_UNITS);
                for (int i = 0; i < nList.getLength(); i++) {
                    Node nNode = nList.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;

                        NodeList commands = eElement.getElementsByTagName(DialogueUnit.TAG_DIALOGUE_UNIT);
                        for (int j = 0; j < commands.getLength(); j++) {
                            if (commands.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                XMLProperties properties = SimpleXMLParser.parseNode((Element) commands.item(j));

                                String id = properties.getProperty(DialogueUnit.PROP_ID);
                                if (ids.contains(id)) {
                                    Logger.log(this, Logger.INFORM, "Duplicate matcher for dialogue. id=" + id);
                                } else {
                                    matcher.addMatcher(new PatternMatcher(id, properties.getProperty(DialogueUnit.PROP_PATTERN)));
                                    ids.add(id);
                                }
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
        TemplateData result = processText((GenericTextAnnotation) data, matcherCommand);
        if (!result.isEmpty()) {
            publishData(commandOutFeed, result);
        } else {
            result = processText((GenericTextAnnotation) data, matcherDialogue);
            publishData(dialogueOutFeed, result);
        }
    }

    /**
     * processes input annotation text and provides template data for further processing
     * @param annotation generic text annotation input data
     * @param matcher matcher object (command/dialogue)
     * @return prepared template data
     */
    private TemplateData processText(GenericTextAnnotation annotation, Matcher matcher) {
        TemplateMatchResult templateMatchResult = matcher.match(annotation);
        TemplateData result = new TemplateData(annotation.getId(), annotation.getLanguage());
        result.updateVariables(templateMatchResult.getExtractedVars());
        result.addTemplateIds(templateMatchResult.getTemplateIDs());

        return result;
    }

    /**
     * Checking type of input data 
     */
    public void defineReceivedData() {
        addInboundTypeChecker(GenericTextAnnotation.class);
    }

    /**
     * Checking type of output data.
     */
    public void definePublishedData() {
        addOutboundTypeChecker(commandOutFeed, TemplateData.class);
        addOutboundTypeChecker(dialogueOutFeed, TemplateData.class);
    }
}
