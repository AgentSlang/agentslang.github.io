/*
 * Copyright (c) MIU, LITIS, INSA de Rouen, France
 *
 * All Rights Reserved. Use is subject to license terms.
 *
 * This file is part of Benchmark.
 *
 * Benchmark is free software: you can redistribute it and/or modify
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
 * it could be obtained at  <http://www.cecill.info/>
 */

package test.benchmark.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
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
        public void run() {
            try {
                // Create a ConnectionFactory
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();

                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create the destination (Topic or Queue)
                Destination destination = session.createQueue("TEST.FOO");

                // Create a MessageProducer from the Session to the Topic or Queue
                MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

                for (int msgSize : MSG_SIZE) {
                    publishData(session, producer, "begin=" + msgSize);
                    for (int pass = 0; pass < MSG_ITERATIONS; pass++) {
                        String generatedMessage = buildMessage(msgSize);
                        publishData(session, producer, "start");
                        publishData(session, producer, generatedMessage);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            //-- ignore
                        }
                    }
                    publishData(session, producer, "end");
                }
                publishData(session, producer, "finish");

                // Clean up
                session.close();
                connection.close();
            } catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }

        private void publishData(Session session, MessageProducer producer, String msg) {
            try {
                TextMessage message = session.createTextMessage(msg);
                producer.send(message);
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

        private Random random = new Random();

        private String buildMessage(int size) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < size; i++) {
                sb.append("" + random.nextInt(10));
            }

            return sb.toString();
        }
    }

    public static class Consumer implements Runnable, ExceptionListener {
        public void run() {
            try {

                // Create a ConnectionFactory
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

                // Create a Connection
                Connection connection = connectionFactory.createConnection();
                connection.start();

                connection.setExceptionListener(this);

                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                // Create the destination (Topic or Queue)
                Destination destination = session.createQueue("TEST.FOO");

                // Create a MessageConsumer from the Session to the Topic or Queue
                MessageConsumer consumer = session.createConsumer(destination);

                // Wait for a message

                while (running) {
                    Message message = consumer.receive(1000);

                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        String text = textMessage.getText();
                        handleMessage(text);
                    }
                }

                consumer.close();
                session.close();
                connection.close();
            } catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }

        boolean running = true;

        long timestamp;
        double avg;
        int size;
        boolean started;
        String label;

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

        public synchronized void onException(JMSException ex) {
            System.out.println("JMS Exception occured.  Shutting down client.");
        }
    }
}