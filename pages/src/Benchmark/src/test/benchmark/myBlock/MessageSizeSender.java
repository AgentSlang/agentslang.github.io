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

package test.benchmark.myBlock;

import org.ib.component.base.SourceComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.StringData;
import org.ib.data.LanguageUtils;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 2/12/13
 */
public class MessageSizeSender extends SourceComponent {
    private static final Collection<Integer> MSG_SIZE = Arrays.asList(10, 100, 1000, 10000, 100000, 1000000);
    private static final int MSG_ITERATIONS = 100;

    public MessageSizeSender(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }

    protected void setupComponent(ComponentConfig config) {
        Thread thread = new Thread() {
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    //-- ignore
                }

                for (int msgSize : MSG_SIZE) {
                    publishData("data", new StringData(0, "begin=" + msgSize, LanguageUtils.IDX_NONE));
                    for (int pass = 0; pass < MSG_ITERATIONS; pass++) {
                        String generatedMessage = buildMessage(msgSize);
                        publishData("data", new StringData(pass, "start", LanguageUtils.IDX_NONE));
                        publishData("data", new StringData(pass, generatedMessage, LanguageUtils.IDX_NONE));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            //-- ignore
                        }
                    }
                    publishData("data", new StringData(0, "end", LanguageUtils.IDX_NONE));
                }
            }
        };
        thread.start();
    }

    public void definePublishedData() {
        addOutboundTypeChecker("data", StringData.class);
    }

    private String buildMessage(int size) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < size; i++) {
            sb.append("a");
        }

        return sb.toString();
    }
}
