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
        HashMap<String, List<JSONServer>> maps = new HashMap<>();
        for(JSONServer sv : JSONServer.getServersByMode(mode)){
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
            int allow = JSONServer.getAllowed(maps.get(key));
            int game = JSONServer.getInGame(maps.get(key));
            cfmap.getStringList("lore").forEach(str->{
                str = str.replaceAll("%amount_allowed%", allow +"");
                str = str.replaceAll("%amount_ingame%", game + "");
                lore.add(str);
            });
            ItemStack item = new UtilStack(cfmap.getString("name").replaceAll("%map%", key),
                    cfmap.getString("material"), lore).getItemStack();
            fill.add(item);
        }
        /*
         * Method to refill an inventory with the list of servers inside a specific slot.
         * start = the start slot where the first server is going to be set
         * max = the max row of the inventory after make a line break (+1) because the slots start in 0
         * line = stored value that incremented when the for has a new row
         * getSum() = this method return the sum of the digits of a number
         *
         */
        int start = config.getInt("inventory.maps.map_list.start");
        int max =  config.getInt("inventory.maps.map_list.max_row")+1;
        int line = 0;
        for(int x = 0; x<fill.size() && x<inv.getSize();x++){
            ItemStack in = fill.get(x);
            int slot = x+start;
            if(getSum(slot) == max)
                line = line+2;
            inv.setItem(slot + line, in);

        }
        p.openInventory(inv);

    }

    /**
     * This method return the sum of digits of a number
     * @param a number to sum
     * @return the sum digits of a
     */
    private static int getSum(int a){
        int sum = 0;
        while (a > 0) {
            sum = sum + a % 10;
            a = a / 10;
        }
        return sum;
    }
}
