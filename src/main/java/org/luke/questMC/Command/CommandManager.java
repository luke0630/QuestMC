package org.luke.questMC.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.luke.questMC.GUI.GUITypes;
import org.luke.questMC.QuestMC;

public class CommandManager implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        if(commandSender instanceof Player player) {
            if (command.getName().equalsIgnoreCase("quest")) {
                QuestMC.getManager().OpenGUI(player, GUITypes.GUIEnum.Home);
            }
        }
        return false;
    }
}
