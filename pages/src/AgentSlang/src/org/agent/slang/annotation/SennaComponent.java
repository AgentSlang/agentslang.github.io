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
import java.util.HashMap;
import java.util.Map;

/**
 * This class uses in order to annotate the input text data.
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/9/12
 */
@ConfigureParams(mandatoryConfigurationParams = {"sennaPath"}, optionalConfigurationParams = {"sennaParams"},
        outputChannels = "senna.data", outputDataTypes = GenericTextAnnotation.class,
        inputDataTypes = StringData.class)
public class SennaComponent extends MixedComponent {
    private static final String SENNA_PATH = "sennaPath";
    private static final String SENNA_PARAMS = "sennaParams";
    private static final String SENNA_OUT = "senna.data";
    private static final Map<String, String> sennaParamMappings = new HashMap<String, String>();
    private static final Map<String, Integer> sennaParamIndexMappings = new HashMap<String, Integer>();

    static {
        sennaParamMappings.put("-pos", TextAnnotationConstants.POS);
        sennaParamMappings.put("-chk", TextAnnotationConstants.CHK);
        sennaParamMappings.put("-ner", TextAnnotationConstants.NER);
//        sennaParamMappings.put("-srl", TextAnnotationConstants.SRL);
//        sennaParamMappings.put("-psg", TextAnnotationConstants.PSG);

        sennaParamIndexMappings.put("-pos", 0);
        sennaParamIndexMappings.put("-chk", 1);
        sennaParamIndexMappings.put("-ner", 2);
//        sennaParamIndexMappings.put("-srl", 3);
//        sennaParamIndexMappings.put("-psg", 4);
    }

    private Map<Integer, String> paramMappings;
    private File sennaPath;
    private String sennaCommand;

    public SennaComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    /**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
    protected void setupComponent(ComponentConfig config) {
        paramMappings = new HashMap<Integer, String>();

        final File sennaDirectory = config.getFileProperty(SENNA_PATH, true);

        final String operatingSystem = System.getProperty("os.name"),
                     architecture    = System.getProperty("os.arch");
        if ("Linux".equals(operatingSystem) && architecture != null && architecture.endsWith("64")) {
            sennaPath = new File(sennaDirectory, "senna-linux64");
        } else if ("Mac OS X".equals(operatingSystem) && architecture != null && architecture.endsWith("64")) {
            sennaPath = new File(sennaDirectory, "senna-osx");
        } else if (operatingSystem != null && operatingSystem.startsWith("Windows")) {
            sennaPath = new File(sennaDirectory, "senna-win32.exe");
        }

        if (sennaPath == null) {
            throw new RuntimeException(String.format("Platform not supported by Senna: %s on %s", operatingSystem, architecture));
        }

        FileUtils.checkExecutableFile(sennaPath, true);

        sennaCommand = sennaPath.getAbsolutePath();

        String sennaParametersString = config.getProperty(SENNA_PARAMS);
        if (sennaParametersString != null) {
            sennaParametersString = sennaParametersString.trim();
            if (sennaParametersString.contains(",")) {
                sennaParametersString = sennaParametersString.replace(",", " ");
            }
            String[] params = sennaParametersString.split(" ");

            String[] orderedParams = new String[sennaParamIndexMappings.size()];
            for (String param : params) {
                param = param.trim();
                Integer index = sennaParamIndexMappings.get(param);
                String textAnnotationParam = sennaParamMappings.get(param);
                if (textAnnotationParam != null && index != null && index >= 0) {
                    orderedParams[index] = sennaParamMappings.get(param);
                } else {
                    throw new IllegalArgumentException("Invalid senna option:" + param);
                }
            }

            int index = 1;
            for (String param : orderedParams) {
                if (param != null) {
                    paramMappings.put(index, param);
                    index++;
                }
            }

            sennaCommand = sennaCommand + " " + sennaParametersString;
        }
    }

    /**
     * Managing input and output data in the class.
     * @param data input data
     */
    protected void handleData(GenericData data) {
    	System.out.println("\n%%%%%%%%%%%%%%ZJ, Senna preprocessed data: "+((StringData) data).getData() +"\n*******\n");
    	//if something recieved from TextComponent
    	if (data instanceof StringData) {
			
    		if (!((StringData) data).getData().equals("SpeechStarted") && !((StringData) data).getData().equals("SpeechEnd")) {
    			//System.out.println("\nZJ, Senna recieved data: "+((StringData) data).getData() +"\n*******\n");
    			publishData(SENNA_OUT, processData(data.getId(), ((StringData) data).getData()));
    			//System.out.println("\nZJ, Senna published data"+((StringData) data).getData()+"\n*******\n");
    		}
    	}
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
        for (String annotationLevels : paramMappings.values()) {
            result.addAnnotation(TextAnnotationConstants.getLevel(annotationLevels), new Annotation());
        }

        try {
            Process process = Runtime.getRuntime().exec(sennaCommand, new String[0], sennaPath.getParentFile());

            PrintWriter pw = new PrintWriter(process.getOutputStream());
            pw.println(data);
            pw.flush();
            pw.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String[] items = line.split("\t");
                int index = -1;
                for (int i = 0; i < items.length; i++) {
                    if (i == 0) {
                        //text token
                        if (items[i].trim().length() > 0) {
                            TextToken token = new TextToken(items[i].trim());
                            index = result.addTextToken(token);
                        } else {
                            break;
                        }
                    } else {
                        int level = TextAnnotationConstants.getLevel(paramMappings.get(i));
                        byte label = TextAnnotationConstants.transformAnnotationLabel(level, items[i].trim());
                        if (label >= 0) {
                            result.addAnnotationToken(level, new AnnotationToken(index, label));
                        }
                    }
                }
                lineCount++;
            }
            reader.close();

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder sb = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            errorReader.close();
            if (sb.length() > 0) {
                Logger.log(this, Logger.CRITICAL, sb.toString());
            }

            process.destroy();
        } catch (IOException e) {
        	e.printStackTrace();            
        }
		
		//System.out.println("\nZJ, Senna data: "+(result).getData() +"\n*******\n");
    		
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
        addOutboundTypeChecker(SENNA_OUT, GenericTextAnnotation.class);
    }
}
