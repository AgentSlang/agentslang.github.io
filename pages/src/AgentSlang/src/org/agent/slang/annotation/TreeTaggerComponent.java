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

package org.agent.slang.annotation;

import org.agent.slang.data.annotation.GenericTextAnnotation;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.StringData;
import org.ib.logger.Logger;
import org.ib.utils.FileUtils;
import org.syn.n.bad.annotation.Annotation;
import org.syn.n.bad.annotation.AnnotationToken;
import org.syn.n.bad.annotation.TextAnnotationConstants;
import org.syn.n.bad.annotation.TextToken;

import java.io.*;

/**
 * The TreeTagger is a tool for annotating text with part-of-speech and lemma information.
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 30/10/13
 */

@ConfigureParams(mandatoryConfigurationParams = {"treeTaggerPath"}, optionalConfigurationParams = {"treeTaggerParams"},
        outputChannels = "treeTagger.data", outputDataTypes = GenericTextAnnotation.class,
        inputDataTypes = StringData.class)
public class TreeTaggerComponent extends MixedComponent {
    private static final String TREETAGGER_PATH = "treeTaggerPath";
    private static final String TREETAGGER_PARAMS = "treeTaggerParams";
    private static final String TREETAGGER_OUT = "treeTagger.data";
    private static final int TREETAGGER_IDX_TOKEN = 0;
    private static final int TREETAGGER_IDX_POS = 1;
    private static final int TREETAGGER_IDX_LEMMA = 2;
    private static final int TREETAGGER_IDX_LENGTH = 3;
    private File treeTaggerPath;
    private String treeTaggerCommand;

    public TreeTaggerComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
    protected void setupComponent(ComponentConfig config) {
        treeTaggerPath = config.getFileProperty(TREETAGGER_PATH, true);
        FileUtils.checkReadableFile(treeTaggerPath, true);

        treeTaggerCommand = treeTaggerPath.getAbsolutePath();
        if (config.hasProperty(TREETAGGER_PARAMS)) {
            treeTaggerCommand = treeTaggerCommand + " " + config.getProperty(TREETAGGER_PARAMS);
        }
    }

    /**
     * Managing input and output data in the class.
     * @param data input data
     */
    protected void handleData(GenericData data) {
        publishData(TREETAGGER_OUT, processData(data.getId(), ((StringData) data).getData()));
    }

    /**
     * Processing input data and providing output. 
     * @param id ID number of the input data 
     * @param data string input data to be annotated.
     * @return Annotated data
     */
    private GenericTextAnnotation processData(long id, String data) {
        int lineCount = estimateSize(data);
        GenericTextAnnotation result = new GenericTextAnnotation(id, lineCount);
        result.addAnnotation(TextAnnotationConstants.getLevel(TextAnnotationConstants.POS), new Annotation());

        try {
            Process process = Runtime.getRuntime().exec(treeTaggerCommand, new String[0], treeTaggerPath.getParentFile().getParentFile());

            PrintWriter pw = new PrintWriter(process.getOutputStream());
            pw.println(data);
            pw.flush();
            pw.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    String[] items = line.split("\t");
                    if (items.length != TREETAGGER_IDX_LENGTH) {
                        Logger.log(this, Logger.INFORM, "Something went wrong. TreeTagger line has " + items.length + " tokens. Line = " + line);
                    } else {
                        TextToken token = new TextToken(items[TREETAGGER_IDX_TOKEN].trim());
                        int index = result.addTextToken(token);

                        int level = TextAnnotationConstants.getLevel(TextAnnotationConstants.POS);
                        byte label = TextAnnotationConstants.transformAnnotationLabel(level, items[TREETAGGER_IDX_POS].trim());
                        if (label >= 0) {
                            result.addAnnotationToken(level, new AnnotationToken(index, label));
                        }

                        if (items[TREETAGGER_IDX_LEMMA] != null && items[TREETAGGER_IDX_LEMMA].trim().length() > 0
                                && !items[TREETAGGER_IDX_LEMMA].trim().equals("<unknown>")) {
                            result.setLemma(index, new TextToken(items[TREETAGGER_IDX_LEMMA].trim()));
                        }
                    }
                    lineCount++;
                }
            }
            reader.close();

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                if (!line.contains("reading parameters") && !line.contains("tagging") && !line.contains("finished")) {
                    sb.append(line).append("\n");
                }
            }
            errorReader.close();
            if (sb.length() > 0) {
                Logger.log(this, Logger.CRITICAL, sb.toString());
            }

            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Estimating the size of input data. 
     * @param data input data
     * @return size of input data
     */
    private int estimateSize(String data) {
        int count = 0;
        int index = -1;
        while ((index = data.indexOf(' ', index + 1)) != -1) {
            count++;
        }
        return count;
    }

    /**
     * Checking type of input data 
     */
    public void defineReceivedData() {
        addInboundTypeChecker(StringData.class);
    }

    /**
     * Checking type of output data.
     */
    public void definePublishedData() {
        addOutboundTypeChecker(TREETAGGER_OUT, GenericTextAnnotation.class);
    }
}
