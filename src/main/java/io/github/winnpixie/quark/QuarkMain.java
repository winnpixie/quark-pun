package io.github.winnpixie.quark;

public class QuarkMain {
    public static void main(String[] args) {
        QuarkServer server = new QuarkServer(5055);
        server.start();
    }
}
