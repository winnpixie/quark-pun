package io.github.winnpixie.quark;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Program {
    public static boolean RUNNING = true;

    public static void main(String[] args) {
        // CONNECTION TESTING

        try {
            DatagramSocket udpSock = new DatagramSocket(5055);
            byte[] buffer = new byte[1200];
            DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);

            while (RUNNING) {
                System.out.println("WAITING");
                udpSock.receive(incomingPacket);

                byte[] incomingPayload = incomingPacket.getData();
                int len = incomingPacket.getLength();

                int time;
                int challenge;
                try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(incomingPayload, 0, len))) {
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

                System.out.printf("Payload (%d bytes)%n", len);
                for (int i = 0; i < len; i++) {
                    System.out.printf("%02X, ", incomingPayload[i]);
                }

                System.out.printf("%nrecv from %s%n", incomingPacket.getAddress().getHostAddress());

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (DataOutputStream dos = new DataOutputStream(baos)) {
                    // PACKET HEADER
                    dos.writeShort(1); // ???
                    dos.writeByte(0); // CRC
                    dos.writeByte(1); // Command Count
                    dos.writeInt(time); // Time
                    dos.writeInt(challenge); // Challenge

                    // TYPES
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
                    dos.writeByte(6); // Command Id
                    dos.writeByte(0); // Channel Id
                    dos.writeByte(0); // Flags
                    dos.writeByte(0); // Reserved
                    dos.writeInt(14); // Size
                    dos.writeInt(0); // Reliable Seq Num

                    // T3
                    // dos.writeShort(42);

                    // T6
                    dos.writeByte(243);
                    dos.writeByte(1);

                    dos.flush();
                }

                byte[] outgoingPayload = baos.toByteArray();
                DatagramPacket outgoingPacket = new DatagramPacket(outgoingPayload, 0, outgoingPayload.length,
                        incomingPacket.getAddress(), incomingPacket.getPort());
                udpSock.send(outgoingPacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
