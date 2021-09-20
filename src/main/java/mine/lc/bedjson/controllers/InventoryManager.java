package mine.lc.bedjson.controllers;

import mine.lc.bedjson.Bedjson;
import mine.lc.bedjson.util.UtilStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InventoryManager {
    private static final FileConfiguration config = Bedjson.getInstance().getConfigController();

    public static void openGameInventory(Player p, String mode){
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&',
                config.getString("inventory.games.title").replaceAll("%mode%", mode)));
        ConfigurationSection cfg = config.getConfigurationSection("inventory.games.items");
        for(String str: cfg.getKeys(false)){
            ConfigurationSection cg = cfg.getConfigurationSection(str);
            ItemStack item = new UtilStack(cg.getString("name"),
                    cg.getString("material"), cg.getStringList("lore")).getItemStack();
            inv.setItem(cg.getInt("slot"), item);
        }
        p.openInventory(inv);
    }

    public static void openMapMenu(Player p , String mode){
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&',
                config.getString("inventory.maps.title").replaceAll("%mode%", mode)));
        ConfigurationSection cfg = config.getConfigurationSection("inventory.maps.items");
        for(String str: cfg.getKeys(false)){
            ConfigurationSection cg = cfg.getConfigurationSection(str);
            ItemStack item = new UtilStack(cg.getString("name"),
                    cg.getString("material"), cg.getStringList("lore")).getItemStack();
            inv.setItem(cg.getInt("slot"), item);
        }
        /*
         * loader for maps
         */
        int start = config.getInt("inventory.maps.map_list.start");
        int max =  config.getInt("inventory.maps.map_list.max_row");

        List<JSONServer> list = new ArrayList<>(JSONServer.getAllServers());
        list.removeIf(sv-> !sv.getMode().equals(mode));

        HashMap<String, List<JSONServer>> maps = new HashMap<>();
        for(JSONServer sv : list){
            if(maps.containsKey(sv.getMap())){
                maps.get(sv.getMap()).add(sv);
                continue;
            }
            maps.put(sv.getMap(), new ArrayList<>());
            maps.get(sv.getMap()).add(sv);
        }
        ConfigurationSection cfmap = config.getConfigurationSection("inventory.maps.map_list");
        List<ItemStack> fill = new ArrayList<>();
        for(String key : maps.keySet()){
            List<String> lore = new ArrayList<>();
            int allow = getAllowed(maps.get(key));
            int game = getInGame(maps.get(key));
            cfmap.getStringList("lore").forEach(str->{
                str = str.replaceAll("%amount_allowed%", allow +"");
                str = str.replaceAll("%amount_ingame%", game + "");
                lore.add(str);
            });
            ItemStack item = new UtilStack(cfmap.getString("name").replaceAll("%map%", key),
                    cfmap.getString("material"), lore).getItemStack();
            fill.add(item);
        }

        for(int x = 0; x<fill.size();x++){
            ItemStack in = fill.get(x);
            int salt = x+start;
            inv.setItem(salt, in);

            int sum = 0;
            while (salt > 0) {
                sum = sum + salt % 10;
                salt = salt / 10;
            }

            if(sum == max)
                x = x+2;

        }
        p.openInventory(inv);

    }
    private static int getInGame(List<JSONServer> jsonServers) {
        int x =0;
        for(JSONServer sv : jsonServers)
            if(sv.getStatus() == Status.PLAYING)
                x = x+1;
        return x;
    }
    private static int getAllowed(List<JSONServer> jsonServers) {
        int x =0;
        for(JSONServer sv : jsonServers)
            if(sv.getStatus() == Status.WAITING)
                x = x+1;
        return x;
    }
}
