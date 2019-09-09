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
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 30/10/13
 */

@ConfigureParams(mandatoryConfigurationParams = {"morfettePath"}, optionalConfigurationParams = {"morfetteParams"},
        outputChannels = "morfette.data", outputDataTypes = GenericTextAnnotation.class,
        inputDataTypes = StringData.class)
public class MorfetteComponent extends MixedComponent {
    private static final String MORFETTE_PATH = "morfettePath";
    private static final String MORFETTE_PARAMS = "morfetteParams";
    private static final String MORFETTE_OUT = "morfette.data";
    private static final int MORFETTE_IDX_TOKEN = 0;
    private static final int MORFETTE_IDX_LEMMA = 1;
    private static final int MORFETTE_IDX_POS = 2;
    private static final int MORFETTE_IDX_LENGTH = 3;
    private File morfettePath;
    private String morfetteCommand;

    public MorfetteComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    protected void setupComponent(ComponentConfig config) {
        morfettePath = config.getFileProperty(MORFETTE_PATH, true);
        FileUtils.checkReadableFile(morfettePath, true);

        morfetteCommand = morfettePath.getAbsolutePath();
        morfetteCommand = morfetteCommand + " " + config.getProperty(MORFETTE_PARAMS);
    }

    protected void handleData(GenericData data) {
        publishData(MORFETTE_OUT, processData(data.getId(), ((StringData) data).getData()));
    }

    private GenericTextAnnotation processData(long id, String data) {
        int lineCount = estimateSize(data);
        GenericTextAnnotation result = new GenericTextAnnotation(id, lineCount);
        result.addAnnotation(TextAnnotationConstants.getLevel(TextAnnotationConstants.POS), new Annotation());

        try {
            Process process = Runtime.getRuntime().exec(morfetteCommand, new String[0], morfettePath.getParentFile());

            PrintWriter pw = new PrintWriter(process.getOutputStream());
            pw.println(data);
            pw.flush();
            pw.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    String[] items = line.split(" ");
                    if (items.length != MORFETTE_IDX_LENGTH) {
                        Logger.log(this, Logger.INFORM, "Something went wrong. Morfette line has " + items.length + " tokens. Line = " + line);
                    } else {
                        TextToken token = new TextToken(items[MORFETTE_IDX_TOKEN].trim());
                        int index = result.addTextToken(token);

                        int level = TextAnnotationConstants.getLevel(TextAnnotationConstants.POS);
                        byte label = TextAnnotationConstants.transformAnnotationLabel(level, items[MORFETTE_IDX_POS].trim());
                        if (label >= 0) {
                            result.addAnnotationToken(level, new AnnotationToken(index, label));
                        }

                        if (items[MORFETTE_IDX_LEMMA] != null && items[MORFETTE_IDX_LEMMA].trim().length() > 0
                                && !items[MORFETTE_IDX_LEMMA].trim().equals("<unknown>")) {
                            result.setLemma(index, new TextToken(items[MORFETTE_IDX_LEMMA].trim()));
                        }
                    }
                    lineCount++;
                }
            }
            reader.close();

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                if (!line.contains("Loading models from")) {
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

    private int estimateSize(String data) {
        int count = 0;
        int index = -1;
        while ((index = data.indexOf(' ', index + 1)) != -1) {
            count++;
        }
        return count;
    }

    public void defineReceivedData() {
        addInboundTypeChecker(StringData.class);
    }

    public void definePublishedData() {
        addOutboundTypeChecker(MORFETTE_OUT, GenericTextAnnotation.class);
    }
}
