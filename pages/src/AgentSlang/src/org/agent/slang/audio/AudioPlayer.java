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

package org.agent.slang.audio;

import javax.sound.sampled.AudioInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/4/13
 */
public class AudioPlayer extends Thread {
    public enum AudioPlayerEvent {
        start,
        stop
    }

    public interface AudioPlayerListener {
        public void audioPlayerStatusUpdated(String audioLabel, AudioPlayerEvent event);
    }

    private class LabeledAudioSequence {
        private String label;
        private AudioInputStream audioInputStream;

        private LabeledAudioSequence(String label, AudioInputStream audioInputStream) {
            this.label = label == null ? "default" : label;
            this.audioInputStream = audioInputStream;
        }
    }

    private final Queue<LabeledAudioSequence> streamList = new LinkedList<LabeledAudioSequence>();
    private final List<AudioPlayerListener> listeners = new LinkedList<AudioPlayerListener>();

    private boolean running = true;

    public AudioPlayer() {
        setName("AudioPlayer Thread");
        start();
    }

    public void run() {
        while (running) {
            LabeledAudioSequence sample;
            synchronized (streamList) {
                sample = streamList.poll();
                if (sample == null) {
                    try {
                        streamList.wait();
                        sample = streamList.poll();
                    } catch (InterruptedException e) {
                        running = false;
                    }
                }
            }

            if (sample != null) {
                marytts.util.data.audio.AudioPlayer player = new marytts.util.data.audio.AudioPlayer(sample.audioInputStream);
                fireEvent(sample.label, AudioPlayerEvent.start);
                player.start();
                try {
                    player.join();
                } catch (InterruptedException e) {
                    //-- ignore
                }
                player.interrupt();
                fireEvent(sample.label, AudioPlayerEvent.stop);
            }
        }
    }

    public void addStatusListener(AudioPlayerListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeStatusListener(AudioPlayerListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private void fireEvent(String audioLabel, AudioPlayerEvent event) {
        synchronized (listeners) {
            for (AudioPlayerListener listener : listeners) {
                listener.audioPlayerStatusUpdated(audioLabel, event);
            }
        }
    }

    public void playAudio(String audioLabel, AudioInputStream sample) {
        synchronized (streamList) {
            streamList.add(new LabeledAudioSequence(audioLabel, sample));
            streamList.notifyAll();
        }
    }

    public void playAudio(AudioInputStream sample) {
        playAudio(null, sample);
    }

    public void killPlayer() {
        running = false;
        interrupt();
    }
}
