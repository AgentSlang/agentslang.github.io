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

package test.benchmark.zeromq;

import org.zeromq.ZMQ;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

/**
 * Hello world! - extracted from the ActiveMQ examples
 */
public class TestMain {
    private static final Collection<Integer> MSG_SIZE = Arrays.asList(10, 100, 1000, 10000, 100000, 1000000);
    private static final int MSG_ITERATIONS = 100;

    public static void main(String[] args) throws Exception {
        thread(new Producer(), false);
        thread(new Consumer(), false);
    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

    public static class Producer implements Runnable {
        private ZMQ.Socket socket;
        private Random random = new Random();

        public void run() {
            try {
                ZMQ.Context context = ZMQ.context(1);
                socket = context.socket(ZMQ.PUB);
                socket.bind("tcp://*:" + 1235);

                for (int msgSize : MSG_SIZE) {
                    publishData("begin=" + msgSize);
                    for (int pass = 0; pass < MSG_ITERATIONS; pass++) {
                        String generatedMessage = buildMessage(msgSize);
                        publishData("start");
                        publishData(generatedMessage);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            //-- ignore
                        }
                    }
                    publishData("end");
                }
                publishData("finish");

                // Clean up
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }

        private void publishData(String msg) {
            socket.send(msg.getBytes(), 0);
        }

        private String buildMessage(int size) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < size; i++) {
                sb.append("" + random.nextInt(10));
            }

            return sb.toString();
        }
    }

    public static class Consumer implements Runnable {
        boolean running = true;
        long timestamp;
        double avg;
        int size;
        boolean started;
        String label;

        public void run() {
            try {
                ZMQ.Context context = ZMQ.context(1);
                ZMQ.Socket socket = context.socket(ZMQ.SUB);
                socket.connect("tcp://localhost:" + 1235);
                socket.subscribe(new byte[]{});

                while (running) {
                    handleMessage(new String(socket.recv(0)));
                }

                socket.close();
            } catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }

        private void handleMessage(String stringData) {
            if (started) {
                size++;
                avg = ((size - 1) * avg + (System.nanoTime() - timestamp)) / size;
                started = false;
            } else {
                if (stringData.equals("start")) {
                    timestamp = System.nanoTime();
                    started = true;
                } else if (stringData.startsWith("begin")) {
                    timestamp = -1;
                    avg = 0;
                    size = 0;
                    started = false;
                    label = stringData.substring(stringData.indexOf('=') + 1);
                } else if (stringData.equals("end")) {
                    started = false;
                    System.out.printf("[%s] Avg=%f%n", label, (avg / 1000000));
                } else if (stringData.equals("finish")) {
                    running = false;
                }
            }
        }
    }
}