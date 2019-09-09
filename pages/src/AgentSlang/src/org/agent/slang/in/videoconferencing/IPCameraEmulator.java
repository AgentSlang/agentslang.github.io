package org.agent.slang.in.videoconferencing;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

interface ServerReadyListener extends EventListener {
    void onServerReady();
}

class StreamListener implements Runnable {
    private final int port;
    private EventListenerList listeners = new EventListenerList();
    private List<OutputStream> clients = new CopyOnWriteArrayList<>();

    public StreamListener(int port) {
        this.port = port;
    }

    public void addServerReadyListener(ServerReadyListener listener) {
        listeners.add(ServerReadyListener.class, listener);
    }

    public void removeServerReadyListener(ServerReadyListener listener) {
        listeners.remove(ServerReadyListener.class, listener);
    }

    public List<OutputStream> getClients() {
        return clients;
    }

    @Override
    public void run() {
        ServerSocket listener = null;
        try {
            listener = new ServerSocket(port);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        for (ServerReadyListener serverReadyListener: listeners.getListeners(ServerReadyListener.class)) {
            serverReadyListener.onServerReady();
        }

        if (listener == null) {
            return;
        }

        try {
            Socket socket = listener.accept();
            InputStream stream = socket.getInputStream();
            byte[] buffer = new byte[socket.getReceiveBufferSize()];

            int read;
            while ((read = stream.read(buffer, 0, buffer.length)) >= 0) {
                if (read == 0) {continue;}

                List<OutputStream> bad = new ArrayList<>();

                for (OutputStream output: clients) {
                    try {
                        output.write(buffer, 0, read);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        bad.add(output);
                    }
                }

                clients.removeAll(bad);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * This class provides streaming data on http port
 */
class HTTPStreamer {
    private final int port;
    private final List<OutputStream> streams;
    private HttpServer server;

    public HTTPStreamer(int port, List<OutputStream> streams) {
        this.port = port;
        this.streams = streams;
    }

    /**
     * Starts streaming on http port.
     */
    public void start() {
        server = HttpServer.createSimpleServer(null, port);

        server.getServerConfiguration().addHttpHandler(new HttpHandler() {
            @Override
            public void service(Request request, Response response) throws Exception {
                response.setContentType("multipart/x-mixed-replace; boundary=--wxcvbn");

                streams.add(response.getOutputStream());
                response.suspend();
            }
        });

        try {
            server.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops streaming on http port.
     */
    public void stop() {
        server.shutdownNow();
    }
}

/**
 * This class simulates IP camera using user's webcam.
 * OS Compatibility: Windows and Linux
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @version 1, 6/23/15
 */
class IPCameraEmulator {
    private final int listeningPort, ipCameraPort;
    private HTTPStreamer httpStreamer;

    public IPCameraEmulator(int listeningPort, int ipCameraPort) {
        this.listeningPort = listeningPort;
        this.ipCameraPort = ipCameraPort;
    }

    /**
     * Starts streaming user webcam video on http port. 
     */
    public void start() {
        final CountDownLatch counter = new CountDownLatch(1);

        StreamListener streamListener = new StreamListener(listeningPort);
        streamListener.addServerReadyListener(new ServerReadyListener() {
            @Override
            public void onServerReady() {
                counter.countDown();
            }
        });

        new Thread(streamListener).start();

        httpStreamer = new HTTPStreamer(ipCameraPort, streamListener.getClients());
        httpStreamer.start();

        try {
            counter.await();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops streaming on http port.
     */
    public void stop() {
        httpStreamer.stop();
    }
}
