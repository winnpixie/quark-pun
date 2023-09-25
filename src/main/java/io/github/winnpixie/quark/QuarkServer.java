package io.github.winnpixie.quark;

import java.util.logging.Logger;

public class QuarkServer {
    private final Logger logger = Logger.getLogger(QuarkServer.class.getName());
    private int port;
    private boolean running;
    private final QuarkServerThread serverThread;

    public QuarkServer(int port) {
        this.port = port;

        this.serverThread = new QuarkServerThread(this);
    }

    public Logger getLogger() {
        return logger;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (running) throw new RuntimeException("Can not change port while server is running.");

        this.port = port;
    }

    public boolean isRunning() {
        return running;
    }

    public void start() {
        this.running = true;

        serverThread.start();
    }

    public void stop() {
        this.running = false;

        try {
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
