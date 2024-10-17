package com.respawnnetwork.respawnlobby.network;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class to ping Minecraft servers to get their status.
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0.1
 */
@Getter
@Setter(AccessLevel.PRIVATE)
public class Ping extends RemoteServer {
    /** The ping timeout */
    private final int timeout;

    /** The Message Of The Day, as specified on the server */
    private String motd;

    /** The amount of users online */
    private int online;

    /** The maximum allowed player amount */
    private int max;


    /**
     * Creates a new ping object.
     *
     * @param address The server address to ping
     * @param port The remote server port
     * @param timeout The maximum timeout
     */
    public Ping(InetAddress address, int port, int timeout) {
        super(address, port);

        this.timeout = timeout;
    }

    /**
     * Fetches the latest
     *
     * @param logger The logger for exception logging
     * @return True if we successfully were able to fetch the latest data, false if not
     */
    public boolean fetch(Logger logger) {/*
        String forServer = " for server " + getAddress() + ":" + getPort();
        Socket socket;
        InputStream in;
        DataOutputStream dataOut;

        // Create socket and set timeout
        try {
            // We need to create a new socket each time, since the socket closes when we close
            // the input / output stream.
            socket = new Socket(getAddress(), getPort());
            socket.setSoTimeout(timeout);

        } catch (IOException io) {
            logger.log(Level.WARNING, "Could not create ping socket" + forServer, io);
            return false;
        }

        // Write the handshake
        try {
            // Create output stream
            OutputStream out = socket.getOutputStream();
            dataOut = new DataOutputStream(out);

            ByteArrayOutputStream b = new ByteArrayOutputStream();

            // Create handshake information
            DataOutputStream handshake = new DataOutputStream(b);
            handshake.writeByte(0);
            writeInt(handshake, 4);
            writeInt(handshake, getAddress().getHostName().length());
            handshake.writeBytes(getAddress().getHostName());
            handshake.writeShort(getPort());
            writeInt(handshake, 1);

            // Write to outgoing packet
            writeInt(dataOut, b.size());
            dataOut.write(b.toByteArray());

            dataOut.writeByte(1);
            dataOut.writeByte(0);

        } catch (IOException io) {
            logger.log(Level.WARNING, "Could not create ping handshake" + forServer, io);
            return false;
        }

        // Read the answer
        try {
            // Create input stream
            in = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(in);

            int size = readInt(dataInputStream);
            int id = readInt(dataInputStream);

            if (id == -1) {
                throw new IOException("Premature end of stream.");
            }

            if (id != 0) {
                throw new IOException("Invalid packetID");
            }

            int length = readInt(dataInputStream);
            if (length == -1) {
                throw new IOException("Premature end of stream.");
            }

            if (length == 0) {
                throw new IOException("Invalid string length.");
            }

            byte[] input = new byte[length];
            dataInputStream.readFully(input);
            String json = new String(input);

            int a = json.length();
            int c = 0;

            while (c + 99 < json.length()) {
                c += 100;
            }

            try {
                long now = System.currentTimeMillis();
                dataOut.writeByte(9);
                dataOut.writeByte(1);
                dataOut.writeLong(now);

                readInt(dataInputStream);
                id = readInt(dataInputStream);
                if (id == -1) {
                    throw new IOException("Premature end of stream.");
                }

                if (id != 1) {
                    throw new IOException("Invalid packetID");
                }

                long pingtime = dataInputStream.readLong();

            } catch (IOException exception) {
                exception.printStackTrace();
            }

            dataOut.close();
            dataInputStream.close();
            socket.close();

            // Now parse.
            JSONParser parser = new JSONParser();
            try {
                JSONObject baseObj = (JSONObject) parser.parse(json);
                JSONObject playerObj = (JSONObject) baseObj.get("players");

                setMotd((String) baseObj.get("description"));
                setOnline((int) ((long) playerObj.get("online")));
                setMax((int) ((long) playerObj.get("max")));

            } catch (ParseException e) {
                e.printStackTrace();
            }

            return true;

        } catch (IOException io) {
            logger.log(Level.WARNING, "Could not read server ping" + forServer, io);
        }

        return false;
    }

    private int readInt(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;

        while (true) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;

            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }

            if ((k & 0x80) != 128) {
                break;
            }
        }

        return i;
    }

    private void writeInt(DataOutputStream out, int value) throws IOException {
        while (true) {
            if ((value & 0xFFFFFF80) == 0) {
                out.writeByte(value);
                return;
            }

            out.writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }

        try {
            Socket sock = new Socket(getAddress(), getPort());

            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
            DataInputStream in = new DataInputStream(sock.getInputStream());

            out.write(0xFE);

            StringBuilder str = new StringBuilder();
            int b;

            while ((b = in.read()) != -1) {
                // Not sure what use the two characters are so I omit them
                if (b == 0 || b <= 16 || b == 255 || b == 23 || b == 24) {
                    continue;
                }

                str.append((char) b);
            }

            String[] data = str.toString().split("§");
            String serverMotd = data[0];
            int onlinePlayers = Integer.parseInt(data[1]);
            int maxPlayers = Integer.parseInt(data[2]);

            setMotd(serverMotd);
            setOnline(onlinePlayers);
            setMax(maxPlayers);

        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not ping remote server", e);
        }*/

        try {
            Socket socket = new Socket(getAddress(), getPort());

            socket.setSoTimeout(timeout);

            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream handshake = new DataOutputStream(b);
            handshake.writeByte(0);
            write(handshake, 4);
            write(handshake, getAddress().getHostName().length());
            handshake.writeBytes(getAddress().getHostName());
            handshake.writeShort(getPort());
            write(handshake, 1);

            write(dataOutputStream, b.size());
            dataOutputStream.write(b.toByteArray());


            dataOutputStream.writeByte(1);
            dataOutputStream.writeByte(0);
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            int size = read(dataInputStream);
            int id = read(dataInputStream);

            if (id == -1) {
                throw new IOException("Premature end of stream.");
            }

            if (id != 0) {
                throw new IOException("Invalid packetID");
            }

            int length = read(dataInputStream);
            if (length == -1) {
                throw new IOException("Premature end of stream.");
            }

            if (length == 0) {
                throw new IOException("Invalid string length.");
            }

            byte[] in = new byte[length];
            dataInputStream.readFully(in);
            String json = new String(in);

            int a = json.length();
            int c = 0;

            while (c + 99 < json.length()) {
                c += 100;
            }

            try {
                long now = System.currentTimeMillis();
                dataOutputStream.writeByte(9);
                dataOutputStream.writeByte(1);
                dataOutputStream.writeLong(now);

                read(dataInputStream);
                id = read(dataInputStream);
                if (id == -1) {
                    throw new IOException("Premature end of stream.");
                }

                if (id != 1) {
                    throw new IOException("Invalid packetID");
                }

                long pingtime = dataInputStream.readLong();

            } catch (IOException exception) {
                logger.log(Level.WARNING, "Could not write response packet", exception);
            }

            dataOutputStream.close();
            outputStream.close();
            inputStreamReader.close();
            inputStream.close();
            socket.close();

            // Now parse.
            JSONParser parser = new JSONParser();

            try {
                JSONObject baseObj = (JSONObject) parser.parse(json);
                JSONObject playerObj = (JSONObject) baseObj.get("players");

                setMotd((String) baseObj.get("description"));
                setOnline((int) ((long) playerObj.get("online")));
                setMax((int) ((long) playerObj.get("max")));

            } catch (ParseException e) {
                logger.log(Level.WARNING, "Could not parse JSON ping response", e);
            }

        } catch (IOException io) {
            // Don't log that
            // logger.log(Level.WARNING, "Could not write / get ping packet, is server running??");
        }

        return true;
    }



    private int read(DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        for (; ; ) {
            int k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
            if ((k & 0x80) != 128) {
                break;
            }
        }
        return i;
    }

    private void write(DataOutputStream out, int paramInt) throws IOException {
        for (;;) {
            if ((paramInt & 0xFFFFFF80) == 0) {
                out.writeByte(paramInt);
                return;
            }
            out.writeByte(paramInt & 0x7F | 0x80);
            paramInt >>>= 7;
        }
    }

}
