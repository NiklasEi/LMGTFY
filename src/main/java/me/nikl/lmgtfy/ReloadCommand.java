package me.nikl.lmgtfy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author Niklas Eicker
 */
public class ReloadCommand implements CommandExecutor {
    private Main main;

    ReloadCommand(Main main){
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        main.reload();
        return true;
    }
}
