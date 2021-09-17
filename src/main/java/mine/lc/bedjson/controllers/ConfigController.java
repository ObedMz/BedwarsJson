package mine.lc.bedjson.controllers;

import mine.lc.bedjson.Bedjson;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * <h2>ConfigController</h2>
 * <br>
 * This class is used to create, write and read the config file
 * of the plugin.
 */
public class ConfigController {
    private final Bedjson plugin = Bedjson.getPlugin(Bedjson.class);
    public FileConfiguration config;
    private File configFile;

    /**
     * Method that return the config object
     * @return FileConfiguration
     */
    public FileConfiguration getConfig() {
        if (this.config == null)
            reloadConfig();
        return this.config;
    }

    /**
     * Method to reload the config.yml file from cache
     */
    public void reloadConfig() {
        if (this.config == null)
            this.configFile = new File(this.plugin.getDataFolder(), "config.yml");
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
        Reader defConfigStream = new InputStreamReader(this.plugin.getResource("config.yml"), StandardCharsets.UTF_8);
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
        this.config.setDefaults(defConfig);
    }

    /**
     * Method to save the config stored in memory.
     */
    public void saveConfig() {
        try {
            this.config.save(this.configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used to register a new Config.yml File
     */
    public void registerConfig() {
        this.configFile = new File(this.plugin.getDataFolder(), "config.yml");
        if (!this.configFile.exists()) {
            getConfig().options().copyDefaults(true);
            saveConfig();
        }
    }
}
