package org.luke.questMC.GUI.Confirm;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.luke.questMC.GUI.GUITypes;
import org.luke.questMC.QuestMC;
import org.luke.questMC.SQL.SQLManager;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class ConfirmManager {
    private Map<Player, confirmInfo> playerconfirmInfoMap = new HashMap<>();
    public static void removePlayerConfirmInfo(Player player) {
        playerconfirmInfoMap.remove(player);
    }
    public static confirmInfo getPlayerConfirmInfo(Player player) {
        if(playerconfirmInfoMap.containsKey(player)) {
            return playerconfirmInfoMap.get(player);
        }
        return null;
    }

    public record confirmInfo(String title, ItemStack yes, ItemStack no, SQLManager.MyCallback yesCallback, SQLManager.MyCallback noCallback) {}
    public void displayConfirm(Player player, confirmInfo info) {
        playerconfirmInfoMap.put(player, info);
        QuestMC.getManager().OpenGUI(player, GUITypes.GUIEnum.Confirm);
        playerconfirmInfoMap.put(player, info);
    }
}
