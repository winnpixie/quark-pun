package io.github.winnpixie.quark;

import java.io.*;
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
        byte[] buffer = new byte[2048];
        DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
        srvSock.receive(inPacket);

        byte[] inPayload = inPacket.getData();
        int inLen = inPacket.getLength();

        int time;
        int challenge;
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(inPayload, 0, inLen))) {
            short peerId = dis.readShort();
            System.out.printf("PEER_ID: %d%n", peerId);

            byte crc = dis.readByte();
            System.out.printf("CRC: %d%n", crc);

            byte cmdCount = dis.readByte();
            System.out.printf("CMD_C: %d%n", cmdCount);

            time = dis.readInt();
            System.out.printf("TIME: %d%n", time);

            challenge = dis.readInt();
            System.out.printf("CHALLENGE: %d%n", challenge);
        }

        System.out.printf("Payload (%d bytes)%n", inLen);
        for (int i = 0; i < inLen; i++) {
            System.out.printf("%02X, ", inPayload[i]);
        }

        System.out.printf("%nrecv from %s%n", inPacket.getAddress().getHostAddress());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            // PACKET HEADER
            dos.writeShort(1); // ???
            dos.writeByte(0); // CRC
            dos.writeByte(1); // Command Count
            dos.writeInt(time); // Time
            dos.writeInt(challenge); // Challenge

            // Commands
            // 1 = ACK
            // 3 = Connect
            // 4 = Disconnect
            // 6 = Generic?
            // 7 = Generic (Unreliable)?
            // 8 = Generic (Fragmented)

            // RESERVED
            // Disconnect
            //  1 == Server Logic
            //  3 == Server Full

            // COMMAND
            dos.writeByte(4); // Command Id
            dos.writeByte(0); // Channel Id
            dos.writeByte(0); // Flags
            dos.writeByte(3); // Reserved
            dos.writeInt(14); // Size
            dos.writeInt(0); // Reliable Seq Num

            // Type 3
            // dos.writeShort(42);

            // Type 6
            dos.writeByte(243);
            dos.writeByte(1);

            dos.flush();
        }

        byte[] outPayload = baos.toByteArray();
        DatagramPacket outgoingPacket = new DatagramPacket(outPayload, 0, outPayload.length,
                inPacket.getAddress(), inPacket.getPort());
        srvSock.send(outgoingPacket);
    }
}
