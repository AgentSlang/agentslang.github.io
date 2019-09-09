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
/**
 * this package used to annotate
 */
package org.agent.slang.annotation;

import org.agent.slang.data.annotation.GenericTextAnnotation;
import org.apache.commons.codec.language.DoubleMetaphone;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.StringData;
import org.syn.n.bad.annotation.TextToken;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/20/12
 */
@ConfigureParams(outputChannels = "metaphone.data", outputDataTypes = GenericTextAnnotation.class,
        inputDataTypes = {StringData.class, GenericTextAnnotation.class})
public class MetaphoneEncodingComponent extends MixedComponent {
    private static final String outboundChannel = "metaphone.data";
    private static final DoubleMetaphone dm = new DoubleMetaphone();

    public MetaphoneEncodingComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    protected void setupComponent(ComponentConfig config) {
    }

    protected void handleData(GenericData data) {
        if (data instanceof GenericTextAnnotation) {
            GenericTextAnnotation result = encode((GenericTextAnnotation) data);
            if (result != null) {
                publishData(outboundChannel, result);
            }
        } else if (data instanceof StringData) {
            GenericTextAnnotation result = encode(processText((StringData) data));
            if (result != null) {
                publishData(outboundChannel, result);
            }
        }
    }

    private GenericTextAnnotation processText(StringData data) {
        String[] items = data.getData().split(" ");
        GenericTextAnnotation result = new GenericTextAnnotation(data.getId(), items.length);

        for (String item : items) {
            result.addTextToken(new TextToken(item));
        }
        return result;
    }

    private GenericTextAnnotation encode(GenericTextAnnotation annotation) {
        for (int i = 0; i < annotation.size(); i++) {
            annotation.setEncoding(i, new TextToken(dm.encode(annotation.getToken(i).getText())));
        }
        return annotation;
    }

    public void defineReceivedData() {
        addInboundTypeChecker(GenericTextAnnotation.class);
        addInboundTypeChecker(StringData.class);
    }

    public void definePublishedData() {
        addOutboundTypeChecker(outboundChannel, GenericTextAnnotation.class);
    }
}
