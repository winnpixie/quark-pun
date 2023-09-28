package io.github.winnpixie.quark;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class QuarkServerThread extends Thread {
    private final QuarkServer server;

    public QuarkServerThread(QuarkServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        try (DatagramSocket srvSock = new DatagramSocket(server.getPort())) {
            server.getLogger().info("Quark Server started on port %d".formatted(srvSock.getLocalPort()));

            while (server.isRunning()) {
                runTest(srvSock);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runTest(DatagramSocket srvSock) throws IOException {
        byte[] buffer = new byte[1200];
        DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
        srvSock.receive(inPacket);

        byte[] inPayload = inPacket.getData();
        int inLen = inPacket.getLength();

        System.out.printf("recv from %s (%d byte(s) total)%n", inPacket.getAddress().getHostName(), inLen);

        QuarkCommand commandIn = new QuarkCommand(inPayload, inLen);
        System.out.println("== Packet Data ==");
        System.out.printf("Peer Id: %d%n", commandIn.peerId);
        System.out.printf("CRC: %d%n", commandIn.crcEnabled ? 204 : 0);
        System.out.printf("Cmd Count: %d%n", commandIn.commandCount);
        System.out.printf("Time: %d%n", commandIn.time);
        System.out.printf("Challenge: %d%n", commandIn.challenge);

        System.out.println("== Command Data ==");
        System.out.printf("Type: %d%n", commandIn.type);
        System.out.printf("Channel: %d%n", commandIn.channel);
        System.out.printf("Flags: %d%n", commandIn.flags);
        System.out.printf("Reserved: %d%n", commandIn.reserved);
        System.out.printf("Size: %d%n", commandIn.size);
        System.out.printf("Reliable Sequence Number: %d%n", commandIn.reliableSeqNum);

        System.out.printf("Payload (%d byte(s)%n", commandIn.payload.length);
        for (int i = 0; i < commandIn.payload.length; i++) {
            System.out.printf("%02X, ", commandIn.payload[i]);
        }
        System.out.println();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            QuarkCommand commandOut = new QuarkCommand();
            commandOut.peerId = commandIn.peerId;
            commandOut.time = commandIn.time;
            commandOut.challenge = commandIn.challenge;

            // TODO: Figure out base communication to reach a "Connected" state.
            if (commandIn.peerId == -1) {
                commandOut.type = 3;
                commandOut.writeTo(dos);

                dos.writeShort(42);
            } else if (commandIn.type != 1) {
                commandOut.type = 6;

                commandOut.payload = new byte[]{
                        (byte) 243,
                        (byte) 1
                };
                commandOut.writeTo(dos);
            } else {
                // For now, just disconnect the client with reason ServerLogic.
                commandOut.type = 4;
                commandOut.reserved = 1;
                commandOut.writeTo(dos);
            }

            dos.flush();
        }

        byte[] outPayload = baos.toByteArray();
        DatagramPacket outgoingPacket = new DatagramPacket(outPayload, 0, outPayload.length,
                inPacket.getAddress(), inPacket.getPort());
        srvSock.send(outgoingPacket);
    }
}
