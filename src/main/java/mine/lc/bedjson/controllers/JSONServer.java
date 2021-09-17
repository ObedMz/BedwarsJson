package mine.lc.bedjson.controllers;

import com.google.gson.Gson;
import mine.lc.bedjson.Bedjson;

import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

/**
 * <h1>JSONServer</h1>
 * <br>
 * <p>
 *     JSONServer class made to get some properties of a server
 *     by a json object throw the motd info.
 * </p>
 */
public class JSONServer {
    private static final Gson gson = new Gson();
    private String ip;
    private int port;
    private String motd;
    private int max_players;
    private int online_players;
    private String map;
    private String mode;
    private boolean online;
    private String bungee_name;
    private Status status;
    private static final HashMap<String, JSONServer> server_list = new HashMap<>();

    /**
     * method to get or create a new JSONServer object
     * @param name Server name
     * @return JSONServer
     */
    public static JSONServer getServer(String name){
        if(server_list.containsKey(name))
            return server_list.get(name);

        return null;
    }

    /**
     * Create a new JSONServer object
     * @param name Bungee Name
     */
    public JSONServer(String name){
        setBungee_name(name);
        setIp(Bedjson.getInstance().getIp());
        setPort(Bedjson.getInstance().getStart_port() + Integer.parseInt(name.replaceAll(Bedjson
        .getInstance().getPrefix(), "")));
        server_list.put(name, this);
    }

    /**
     * Get a collection of all JsonServer objects from a HashMap
     * @return Collection<JSONServer>
     */
    public static Collection<JSONServer> getAllServers() {
        return server_list.values();
    }

    /**
     * Get the Status of a server
     * @return Status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Set the Status for the server
     * @param status Status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Get the bungee server name of the server.
     * @return String
     */
    public String getBungee_name() {
        return bungee_name;
    }

    /**
     * Set the proxy server name.
     * @param bungee_name String
     */
    public void setBungee_name(String bungee_name) {
        this.bungee_name = bungee_name;
    }

    /**
     * Return if  the server is online / offline
     * @return boolean
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * Set the online mode of the server
     * @param online boolean
     */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /**
     * Get the mode of the server
     * @return String
     */
    public String getMode() {
        return mode;
    }

    /**
     * Set the Mode from the server
     * @param mode String
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * Get the Map name of the current arena.
     * @return String
     */
    public String getMap() {
        return map;
    }

    /**
     * Set the Map's name from the motd info.
     * @param map String
     */
    public void setMap(String map) {
        this.map = map;
    }

    /**
     * Get the size of players in arena.
     * @return int
     */
    public int getOnline_players() {
        return online_players;
    }

    public void setOnline_players(int online_players) {
        this.online_players = online_players;
    }

    public int getMax_players() {
        return max_players;
    }

    public void setMax_players(int max_players) {
        this.max_players = max_players;
    }

    public String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }


    private static int readVarInt(DataInputStream stream) throws IOException {
        int out = 0;
        int bytes = 0;
        byte in;
        do {
            in = stream.readByte();
            out |= (in & 0x7F) << (bytes++ * 7);
            if (bytes > 5) throw new RuntimeException("VarInt too big");

        } while ((in & 0x80) == 0x80);
        return out;
    }

    private static void writeVarInt(DataOutputStream stream, int value) throws IOException {
        int part;
        do {
            part = value & 0x7F;
            value >>>= 7;
            if (value != 0) part |= 0x80;

            stream.writeByte(part);

        } while (value != 0);
    }
    public void ping() throws IOException, ParseException {
        try (Socket socket = new Socket(getIP(), getPort())) {
            OutputStream outputStream;
            DataOutputStream dataOutputStream;
            InputStream inputStream;
            InputStreamReader inputStreamReader;
            int timeout = 1000;
            socket.setSoTimeout(timeout);
            outputStream = socket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            inputStream = socket.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream handshake = new DataOutputStream(b);
            handshake.writeByte(0x00);
            writeVarInt(handshake, 47); //protocol version 4
            writeVarInt(handshake, getIP().length());
            handshake.writeBytes(getIP());
            handshake.writeShort(getPort());
            writeVarInt(handshake, 1);
            writeVarInt(dataOutputStream, b.size());
            dataOutputStream.write(b.toByteArray());

            dataOutputStream.writeByte(0x01);
            dataOutputStream.writeByte(0x00);
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            readVarInt(dataInputStream);
            int id = readVarInt(dataInputStream);

            if (id == -1) {
                close(dataOutputStream,outputStream,inputStreamReader,inputStream,socket);
                return;
            }

            if (id != 0x00) { //we want a status response
                close(dataOutputStream,outputStream,inputStreamReader,inputStream,socket);
                return;
            }
            int length = readVarInt(dataInputStream); //length of json string

            if (length == -1 || length == 0) {
                close(dataOutputStream,outputStream,inputStreamReader,inputStream,socket);
                return;
            }

            byte[] in = new byte[length];
            dataInputStream.readFully(in);  //read json string
            String json = new String(in);


            long now = System.currentTimeMillis();
            dataOutputStream.writeByte(0x09); //size of packet
            dataOutputStream.writeByte(0x01); //0x01 for ping
            dataOutputStream.writeLong(now); //time!?

            readVarInt(dataInputStream);
            id = readVarInt(dataInputStream);
            if (id == -1)  {
                close(dataOutputStream,outputStream,inputStreamReader,inputStream,socket);
                return;
            }

            if (id != 0x01) {
                close(dataOutputStream,outputStream,inputStreamReader,inputStream,socket);
                return;
            }
            dataInputStream.readLong(); //ping time
            StatusResponse resp = gson.fromJson(json, StatusResponse.class);

            setOnline_players(resp.getPlayers().getOnline());

            setMotd(resp.getDescription());

            if(getMotd() != null && !getMotd().equals("")) setOnline(true);

            if(motd == null) return;

            String status = motd.toUpperCase(Locale.ROOT).split(",")[0].split("=")[1];
            if(status.equalsIgnoreCase("OFF")|| status.equalsIgnoreCase("Reiniciando")){
                setStatus(Status.OFF);
                return;
            }
            if(status.equalsIgnoreCase("Esperando")) setStatus(Status.WAITING);
            if(status.equalsIgnoreCase("Jugando")) setStatus(Status.PLAYING);

            String mode =  motd.toUpperCase(Locale.ROOT).split(",")[4].split("=")[1];
            setMode(mode + "VS" + mode);
            setMap(motd.toUpperCase(Locale.ROOT).split(",")[1].split("=")[1]);
            setMax_players(Integer.parseInt(motd.toUpperCase(Locale.ROOT).split(",")[3].split("=")[1]));
            dataOutputStream.close();
            outputStream.close();
            inputStreamReader.close();
            inputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void close(DataOutputStream d, OutputStream o, InputStreamReader i,InputStream in, Socket socket) throws IOException {
        online = false;
        online_players = 0;
        motd = "";
        d.close();
        o.close();
        i.close();
        in.close();
        socket.close();
    }

    private String getIP() {
        return ip;
    }

    @SuppressWarnings("unused")
    public static class StatusResponse {
        private Object description;
        private Players players;

        public String getDescription() {
            return description.toString();
        }

        public Players getPlayers() {
            return players;
        }
    }

    @SuppressWarnings("unused")
    public static class Players {
        private int max;
        private int online;

        public int getMax() {
            return max;
        }

        public int getOnline() {
            return online;
        }

    }


}
