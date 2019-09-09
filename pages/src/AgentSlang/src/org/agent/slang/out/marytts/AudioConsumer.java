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

package org.agent.slang.out.marytts;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/20/12
 */
public class AudioConsumer extends Thread {
    private AudioInputStream inputStream;
    private boolean goodState = false;

    private AudioConsumer(AudioInputStream audio) {
        this.inputStream = audio;
    }

    public void run() {
        goodState = false;
        if (inputStream != null) {
            try {
                int waits = 0;
                while (waits < 5 && inputStream.available() == 0) {
                    waits++;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        //--ignore
                    }
                }

                while (inputStream.available() > 0) {
                    byte[] buf = new byte[inputStream.available()];
                    //noinspection ResultOfMethodCallIgnored
                    inputStream.read(buf);
                    goodState = true;
                }
            } catch (IOException e) {
                //-- ignore
            }
        }
    }

    public static boolean consume(AudioInputStream audio, boolean sync) {
        AudioConsumer consumer = new AudioConsumer(audio);
        if (sync) {
            consumer.run();
            return consumer.goodState;
        } else {
            consumer.start();
            return true;
        }
    }
}
