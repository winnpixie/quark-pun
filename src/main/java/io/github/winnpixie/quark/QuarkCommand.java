package io.github.winnpixie.quark;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// TODO: Re-implement with a proper and maintainable approach
public class QuarkCommand {
    // Header
    public short peerId = -1;
    public boolean crcEnabled = false; // false = 0, true = 204
    public int commandCount = 1; // BYTE
    public int time = 1;
    public int challenge = 0;

    public byte[] payload = new byte[0];

    // Command Types
    // 1 = ACK
    // 3 = Connect
    // 4 = Disconnect
    // 6 = Generic?
    // 7 = Generic (Unreliable)?
    // 8 = Generic (Fragmented)
    public int type;

    public int channel;
    public int flags = 1;

    // RESERVED
    // - Disconnect
    //  1 == Server Logic
    //  3 == Server Full
    public int reserved = 4;

    public int size = 12;
    public int reliableSeqNum = 1;

    public QuarkCommand() {
    }

    public QuarkCommand(byte[] packetData, int length) throws IOException {
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packetData, 0, length))) {
            this.peerId = dis.readShort();
            this.crcEnabled = dis.readByte() == (byte) 204;
            this.commandCount = dis.readUnsignedByte();
            this.time = dis.readInt();
            this.challenge = dis.readInt();

            this.type = dis.readUnsignedByte();
            this.channel = dis.readUnsignedByte();
            this.flags = dis.readUnsignedByte();
            this.reserved = dis.readUnsignedByte();

            this.size = dis.readInt();
            this.reliableSeqNum = dis.readInt();

            if (length - 24 > 0) {
                this.payload = dis.readNBytes(length - 24);
            }
        }
    }

    public void writeTo(DataOutputStream dos) throws IOException {
        dos.writeShort(this.peerId);
        dos.writeByte(this.crcEnabled ? 204 : 0);
        dos.writeByte(this.commandCount);
        dos.writeInt(this.time);
        dos.writeInt(this.challenge);

        dos.writeByte(this.type);
        dos.writeByte(this.channel);
        dos.writeByte(this.flags);
        dos.writeByte(this.reserved);

        this.size = 12 + this.payload.length;
        dos.writeInt(this.size);
        dos.writeInt(this.reliableSeqNum);
    }
}
