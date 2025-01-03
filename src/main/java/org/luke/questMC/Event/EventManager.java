package org.luke.questMC.Event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.luke.questMC.GUI.Confirm.ConfirmManager;
import org.luke.questMC.QuestMC;
import org.luke.questMC.QuestManager.QuestEnum;
import org.luke.questMC.QuestManager.QuestManager;
import org.luke.questMC.SQL.SQLManager;

import java.util.UUID;

public class EventManager implements Listener {
    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if(QuestManager.isProgressPlayer(player.getUniqueId())) {
            QuestEnum.Quest_Normal quest = QuestManager.getProgressInfo().get(player.getUniqueId()).getType();
            String json_saveData = QuestManager.getQuest(quest).SaveJson();

            SQLManager.SaveProgressData(quest, json_saveData);
        }
    }
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        QuestManager.QuestProgressInfo progressInfo = QuestManager.getProgressInfo().get(uuid);
        if(progressInfo != null) {
            QuestManager.UpdateProgressInfo(player, QuestManager.getQuest(progressInfo.getType()).getProgressInfo(player));
            QuestMC.SendFirstMessage(progressInfo.getType(), player);
        }
    }

    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        ConfirmManager.removePlayerConfirmInfo(player);
    }
}
