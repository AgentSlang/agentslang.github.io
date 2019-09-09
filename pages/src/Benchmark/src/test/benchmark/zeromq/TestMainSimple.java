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

package test.benchmark.zeromq;

import org.zeromq.ZMQ;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 2/20/13
 */
public class TestMainSimple {
    private static final int MSG_ITERATIONS = 100;

    public static class Sender extends Thread {
        private ZMQ.Socket socket;
        private int msgSize;

        public Sender(int msgSize) {
            ZMQ.Context context = ZMQ.context(1);
            socket = context.socket(ZMQ.PUB);
            socket.bind("tcp://*:" + 1235);
            this.msgSize = msgSize;
        }

        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //-- ignore
            }
            for (int i = 0; i < MSG_ITERATIONS + 1; i++) {
                byte[] msg = buildMessage(msgSize);
                socket.send(msg, 0);
            }
        }

        protected void finalize() throws Throwable {
            super.finalize();
            if (socket != null) {
                socket.close();
            }
        }

        private static byte[] buildMessage(int size) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < size; i++) {
                sb.append("a");
            }

            return sb.toString().getBytes();
        }
    }

    public static class Receiver extends Thread {
        private ZMQ.Socket socket;
        private int msgSize;

        public Receiver(int msgSize) {
            ZMQ.Context context = ZMQ.context(1);
            socket = context.socket(ZMQ.SUB);
            socket.connect("tcp://localhost:" + 1235);
            socket.subscribe(new byte[]{});
            this.msgSize = msgSize;
        }

        public void run() {
            double summ = 0;
            socket.recv(0);
            long timestamp = System.currentTimeMillis();

            for (int i = 0; i < MSG_ITERATIONS; i++) {
                socket.recv(0);
                summ += (System.currentTimeMillis() - timestamp);
                timestamp = System.currentTimeMillis();
            }
            System.out.printf("Avg(%d)=%f%n", msgSize, (summ / MSG_ITERATIONS));
        }

        protected void finalize() throws Throwable {
            super.finalize();
            if (socket != null) {
                socket.close();
            }
        }
    }

    private static final Collection<Integer> MSG_SIZE = Arrays.asList(10, 100, 1000, 10000, 100000, 1000000);

    public static void main(String[] args) {
        for (int msgSize : MSG_SIZE) {
            Sender sender = new Sender(msgSize);
            Receiver receiver = new Receiver(msgSize);

            sender.start();
            receiver.start();

            try {
                sender.join();
                receiver.join();
            } catch (InterruptedException e) {
                //-- ignore
            }

            try {
                sender.finalize();
                receiver.finalize();
            } catch (Throwable throwable) {
                //-- ignore
            }
        }
    }
}
