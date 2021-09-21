package mine.lc.bedjson.commands;

import mine.lc.bedjson.controllers.JSONServer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_Join implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "Only players can use this command");
            return false;
        }

        if(args.length <=0){
            sender.sendMessage(ChatColor.RED + "Not enough arguments");
            return false;
        }
        String mode = args[0];
        if(!JSONServer.getServersMode().containsKey(mode)){
            sender.sendMessage(ChatColor.RED + "No existe ese modo de juego.");
            return false;
        }
        JSONServer server = JSONServer.getBestServerByMode(mode);
        if(server == null){
            sender.sendMessage(ChatColor.RED + "No hay partidas disponibles, intentalo denuevo.");
            return false;
        }
        server.sendPlayerToServer((Player) sender);
        return false;
    }
}
