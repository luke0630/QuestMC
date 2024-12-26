package org.luke.questMC.GUI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.luke.questMC.DataClass;
import org.luke.questMC.QuestMC;
import org.luke.questMC.QuestManager.QuestBase;
import org.luke.questMC.QuestManager.QuestManager;
import org.luke.questMC.QuestManager.QuestUtility;
import org.luke.questMC.SQL.SQLManager;
import org.luke.takoyakiLibrary.TakoUtility;
import org.luke.yakisobaGUILib.Abstract.GUIAbstract;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.luke.takoyakiLibrary.TakoUtility.*;

public class QuestHome extends GUIAbstract<GUITypes.GUIEnum> {
    private QuestBase quest;

    @Override
    public GUITypes.GUIEnum getType() {
        return GUITypes.GUIEnum.Home;
    }

    @Override
    public Inventory getInventory() {
        var inv = TakoUtility.getInitInventory(9*3, "クエスト : ホーム画面");
        for(int i=0;i < inv.getSize();i++) {
            inv.setItem(i, getItem(Material.BLACK_STAINED_GLASS_PANE, " "));
        }

        UUID uuid = player.getUniqueId();
        if(QuestManager.getProgressInfo().containsKey(uuid)) {
            QuestManager.UpdateProgressInfo(
                    player,
                    QuestManager.getQuest( QuestManager.getProgressInfo().get(uuid).getType() ).getProgressInfo(player)
            );

            var item = QuestUtility.getIcon(quest);

            var result = new ArrayList<String>();
            result.add("&f" + quest.getQuestName());
            result.addAll( item.getLore() );
            result.add("&c&l現在の進行状況");

            //初期では色付けする
            var progressInfoList = QuestManager.getProgressInfo().get(uuid).getProgressInfo();
            if(progressInfoList != null && !progressInfoList.isEmpty()) {
                for(var list : progressInfoList) {
                    result.add("&a" + list);
                }
            }

            result.add("&c&l----------------------------");
            result.add("&cクリックして詳細を確認");

            var meta = item.getItemMeta();
            meta.setDisplayName(toColor("&c&l現在進行中のクエスト"));
            item.setItemMeta(meta);

            setLore(item, result);

            inv.setItem(4, item);
        }

        var completedQuests = SQLManager.getCompletedQuestList(player.getUniqueId());

        int notCompleted = QuestManager.getQuests().size();
        int completed = 0;
        if(completedQuests != null && !completedQuests.isEmpty()) {
            completed = completedQuests.size();
            notCompleted -= completed;
        }

        var start = getItem(Material.COMPASS, "&aクエスト一覧 (未達成) &f| &cクエストを開始する");
        setLore(start, List.of(
                "&c&lクリックして一覧を見る",
                "&a&l*"+ notCompleted +"個の未達成のクエスト"
        ));
        inv.setItem(4+2*9, start);

        var cleared = getItem(Material.REPEATER, "&a達成済みクエスト一覧");

        setLore(cleared, List.of(
                "&c&lクリックして一覧を見る",
                "&a&l*"+ completed +"個の達成済みのクエスト"
        ));
        inv.setItem(2+2*9, cleared);
        return inv;
    }

    @Override
    public void InventoryClickListener(InventoryClickEvent inventoryClickEvent) {
        Player player = (Player) inventoryClickEvent.getWhoClicked();
        switch(inventoryClickEvent.getSlot()) {
            case 4+2*9 -> QuestMC.getManager().OpenListGUI(player, GUITypes.ListGUIEnum.Quests);
            case 2+2*9 -> QuestMC.getManager().OpenListGUI(player, GUITypes.ListGUIEnum.Quests_Cleared);
            case 4 -> {
                QuestMC.getGuiManager().getOpenQuestDetails().put(player, new DataClass.QuestDetails(quest.getType(), getType()));
                QuestMC.getManager().OpenListGUI(player, GUITypes.ListGUIEnum.Quest_Detail);
            }
        }
    }

    @Override
    public void onStart() {
        try {
            quest = QuestManager.getQuest(QuestManager.getProgressInfo().get(player.getUniqueId()).getType());
        } catch (Exception ignored) {

        }
    }
}
