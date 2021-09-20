package mine.lc.bedjson;

import mine.lc.bedjson.commands.CMD_Join;
import mine.lc.bedjson.controllers.ConfigController;
import mine.lc.bedjson.controllers.JSONServer;
import mine.lc.bedjson.controllers.Status;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/**
 * <H2>BedwarsJson Plugin</H2>
 * BedwarsJson it's a java plugin that get information using the motd info of a server
 * to search for the best server options to teleport the player for a game sorting by
 * modes 4vs4,3vs3,2vs2,1vs1
 *
 * <b>Note:</b> This plugin only work for MBedwars Plugin.
 *
 * @author ObedMz
 * @version 1.0
 * @since 16-09-2021
 */
public final class Bedjson extends JavaPlugin {
    private static Bedjson instance;
    private int start_port;
    private String ip;
    private String prefix;
    private int amount;
    private ConfigController config;
    private final HashMap<String, List<JSONServer>> servers = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        config = new ConfigController();
        config.registerConfig();
        getCommand("join").setExecutor(new CMD_Join());
        setStart_port(config.getConfig().getInt("json.start_port"));
        setIp(config.getConfig().getString("json.ip"));
        setPrefix(config.getConfig().getString("json.prefix"));
        setAmount(config.getConfig().getInt("json.amount"));

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

    private void updateServers() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, ()->{
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
                    e.printStackTrace();
                }
            }
            //Short the List<JsonServer> by the online players. (Check if its revers)
            servers.values().forEach(json-> json.sort(Comparator.comparing(JSONServer::getOnline_players)));

        },20L,20L);


    }

    /**
     * Method to return the best server shorting by the mode.
     * @param mode String
     * @return JSONServer
     */
    public JSONServer getBestServer(String mode){
       return  servers.get(mode).stream().findFirst().orElse(null);
    }

    /**
     * This method is used to send the player to another server
     * around the proxy using the BungeeCord channel.
     *
     * @param player The target Player to send to the server.
     * @param server The proxy server name to teleport the player.
     */
    public void sendPlayerToServer(Player player, String server) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Connect");
            out.writeUTF(server);
            player.sendPluginMessage(getInstance(), "BungeeCord", b.toByteArray());
            b.close();
            out.close();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    /**
     * Method to return the Main Class instance
     * @return Bedjson
     */
    public static Bedjson getInstance(){
        return instance;
    }

    /**
     * Get the starter port from config file
     * @return int
     * @see mine.lc.bedjson.controllers.ConfigController
     */
    public int getStart_port() {
        return start_port;
    }

    /**
     * set the starter port of the config file
     * @param start_port int
     */
    public void setStart_port(int start_port) {
        this.start_port = start_port;
    }

    /**
     * Get the ip of all servers from config file
     * <p>
     *  Note:
     *  all servers will have the same ip.
     * </p>
     * @return String
     */
    public String getIp() {
        return ip;
    }

    /**
     * Set the IP for all the server list from config
     * @param ip String
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * Get the prefix value for bedwars servers.
      * @return String
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set the prefix value for all bedwars servers.
     * @param prefix String
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Get the amount of servers in config.yml
     * @return int
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Set the amount of servers that the plugin
     * is going to looking for
     * @param amount int
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    public FileConfiguration getConfigController(){
        return config.getConfig();
    }
    /**
     * Get a HashMap list where the Key is the mode and Values are a list of JsonServer
     * @see JSONServer
     * @return HashMap
     */
    public HashMap<String, List<JSONServer>> getServers(){
        return servers;
    }
}
