package mine.lc.bedjson.controllers;

import com.google.gson.Gson;
import mine.lc.bedjson.Bedjson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.util.*;

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
    private static final HashMap<String, List<JSONServer>> servers = new HashMap<>();
    private static JSONServer random_server;

    /**
     * Load and register modes and servers.
     */
    public static void startup(){
        for(int x=0;x< Bedjson.getInstance().getAmount();x++)
            new JSONServer(Bedjson.getInstance().getPrefix() + x);

        servers.put("4VS4", new ArrayList<>());
        servers.put("3VS3", new ArrayList<>());
        servers.put("2VS2", new ArrayList<>());
        servers.put("1VS1", new ArrayList<>());
        updateServers();
    }

    /**
     * Runnable to update 20L (One second) the information
     * of each server in the List
     * @see JSONServer
     */
    private static void updateServers() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Bedjson.getInstance(), ()->{
            servers.values().forEach(List::clear);
            for(JSONServer sv : JSONServer.getAllServers()) {
                try {
                    sv.ping();

                    if(!sv.isOnline())
                        continue;
                    if(sv.getStatus() != Status.WAITING)
                        continue;
                    if(sv.getOnline_players() >= sv.getMax_players())
                        continue;

                    if(servers.containsKey(sv.getMode().toUpperCase(Locale.ROOT)))
                        servers.get(sv.getMode().toUpperCase(Locale.ROOT)).add(sv);

                } catch (IOException | ParseException e) {
                   Bukkit.getLogger().info("No se pudo obtener información del servidor " + sv.getBungee_name());
                }
            }
            //Short the List<JsonServer> by the online players. (Check if its revers)
            servers.values().forEach(json-> json.sort(Comparator.comparing(JSONServer::getOnline_players)));
            List<JSONServer> best_all = new ArrayList<>();
            servers.values().forEach(best_all::addAll);
            best_all.sort(Comparator.comparing(JSONServer::getOnline_players));
            random_server = best_all.get(0);
        },20L,20L);


    }

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
     * Get a HashMap list where the Key is the mode and Values are a list of JsonServer
     * @see JSONServer
     * @return HashMap
     */
    public static HashMap<String, List<JSONServer>> getServersMode(){
        return servers;
    }
    /**
     * Get a collection of all JsonServer objects from a HashMap
     * @return Collection<JSONServer>
     */
    public static Collection<JSONServer> getAllServers() {
        return server_list.values();
    }

    public static Collection<JSONServer> getServersByMode(String mode){
        List<JSONServer> list = new ArrayList<>(JSONServer.getAllServers());
        list.removeIf(sv-> !sv.getMode().equals(mode));
        return list;
    }
    /**
     * Method to return in-game servers
     * @param jsonServers List of servers
     * @return Servers in game
     */
    public static int getInGame(List<JSONServer> jsonServers) {
        int x =0;
        for(JSONServer sv : jsonServers)
            if(sv.getStatus() == Status.PLAYING)
                x = x+1;
        return x;
    }

    /**
     * Method to return available servers to play
     * @param jsonServers List of servers
     * @return Servers allowed
     */
    public static int getAllowed(List<JSONServer> jsonServers) {
        int x =0;
        for(JSONServer sv : jsonServers)
            if(sv.getStatus() == Status.WAITING)
                x = x+1;
        return x;
    }

    public static JSONServer getRandom_server() {
        return random_server;
    }

    /**
     * This method is used to send the player to another server
     * around the proxy using the BungeeCord channel.
     *
     * @param player The target Player to send to the server.
     */
    public void sendPlayerToServer(Player player) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Connect");
            out.writeUTF(bungee_name);
            player.sendPluginMessage(Bedjson.getInstance(), "BungeeCord", b.toByteArray());
            b.close();
            out.close();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }
    /**
     * Method to return the best server shorting by the mode.
     * @param mode The Mode name
     * @return JSONServer
     */
    public static JSONServer getBestServerByMode(String mode){
        return  servers.get(mode).stream().findFirst().orElse(null);
    }
    /**
     * Method to return the best server shorting by the mode and map name.
     * @param mode The Mode name
     * @param map The Map name
     * @return JSONServer
     */
    public static JSONServer getBestServerByMapMode(String mode, String map){
        for(JSONServer sv : servers.get(mode))
            if(sv.getMap().equalsIgnoreCase(map))
                return sv;
        return null;
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
           Bukkit.getLogger().info("Cant get motd of" + bungee_name);
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
