package mine.lc.bedjson.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class UtilStack {
    private ItemStack itemStack;

    public UtilStack(String displayName, Material item, List<String> lore){
        ItemStack itemStack = new ItemStack(item);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        List<String> lr = new ArrayList<>();
        lore.forEach(str->{lr.add(ChatColor.translateAlternateColorCodes('&', str));});
        meta.setLore(lr);
        itemStack.setItemMeta(meta);
        setItemStack(itemStack);
    }

    public UtilStack(String displayName, String material, List<String> lore){
        ItemStack itemStack = new ItemStack(Material.getMaterial(material.split(":")[0]),1,
                (short) Integer.parseInt(material.split(":")[1]));
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        List<String> lr = new ArrayList<>();
        lore.forEach(str->{lr.add(ChatColor.translateAlternateColorCodes('&', str));});
        meta.setLore(lr);
        itemStack.setItemMeta(meta);
        setItemStack(itemStack);
    }
    public UtilStack(String displayName, String material){
        ItemStack itemStack = new ItemStack(Material.getMaterial(material.split(":")[0]),1,
                (short) Integer.parseInt(material.split(":")[1]));
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        itemStack.setItemMeta(meta);
        setItemStack(itemStack);
    }
    public UtilStack(String displayName, Material item){
        ItemStack itemStack = new ItemStack(item);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        itemStack.setItemMeta(meta);
        setItemStack(itemStack);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
