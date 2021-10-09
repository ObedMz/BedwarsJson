package mine.lc.bedjson;

import mine.lc.bedjson.commands.CMD_Join;
import mine.lc.bedjson.controllers.ConfigController;
import mine.lc.bedjson.controllers.JSONServer;
import mine.lc.bedjson.events.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

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
        JSONServer.startup();
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

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

}
