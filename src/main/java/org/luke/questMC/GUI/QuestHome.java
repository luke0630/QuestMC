package org.luke.questMC.GUI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.luke.questMC.DataClass;
import org.luke.questMC.GUI.Confirm.ConfirmManager;
import org.luke.questMC.QuestMC;
import org.luke.questMC.QuestManager.QuestBase;
import org.luke.questMC.QuestManager.QuestEnum;
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
            result.add("&f" + quest.getType().getTitle());
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
            result.add("&c右クリックで中断");

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


        //******USER INFO******
        var userInfo = TakoUtility.getPlayerHead(player.getUniqueId());
        ItemMeta meta = userInfo.getItemMeta();
        meta.setDisplayName(toColor("&a" + player.getName() + "の情報"));
        userInfo.setItemMeta(meta);

        StringBuilder status = new StringBuilder();
        float percent_of_completed = (float) completed / QuestEnum.Quest_Normal.values().length;

        float percent_of_completed_twenty = percent_of_completed * 40;
        for(int i=0;i < 40;i++) {
            if(i <= percent_of_completed_twenty) {
                status.append("&c|");
            } else {
                status.append("&f|");
            }
        }


        setLore(userInfo, List.of(
                "&7====================",
                "&b達成済みのクエスト: " + completed + "個",
                "&c未達成済みのクエスト: " + notCompleted + "個",
                "&6達成率: "+ (percent_of_completed*100) +"%  "+ status
        ));

        inv.setItem(6+2*9, userInfo);
        //******USER INFO******

        return inv;
    }

    @Override
    public void InventoryClickListener(InventoryClickEvent inventoryClickEvent) {
        Player player = (Player) inventoryClickEvent.getWhoClicked();
        switch(inventoryClickEvent.getSlot()) {
            case 4+2*9 -> QuestMC.getManager().OpenListGUI(player, GUITypes.ListGUIEnum.Quests);
            case 2+2*9 -> QuestMC.getManager().OpenListGUI(player, GUITypes.ListGUIEnum.Quests_Cleared);
            case 4 -> {
                if(inventoryClickEvent.isRightClick()) {
                    ItemStack item_stopQuest = getItem(Material.REDSTONE_BLOCK, "&c&l中断します");
                    ItemStack item_cancelStopQuest = getItem(Material.BARRIER, "&a&l中断しません");

                    QuestBase quest = QuestManager.getQuest( QuestManager.getProgressInfo().get(player.getUniqueId()).getType() );

                    setLore(item_stopQuest, List.of(
                            "&a---------------------------",
                            "&c&l注意: これをクリックすると、",
                            "&c&l進行中のクエストを中断します。",
                            "&c&l進行状況はすべて消えます。",
                            "&a&l-----進行中のクエスト-----",
                            "&a" + quest.getType().getTitle()
                    ));

                    SQLManager.MyCallback stopQuest = () -> {
                        var currentType = QuestManager.getProgressInfo().get(player.getUniqueId()).getType();
                        QuestManager.getQuest(currentType).onComplete(player);
                        QuestManager.getProgressInfo().remove(player.getUniqueId());
                        QuestMC.getManager().OpenGUI(player, GUITypes.GUIEnum.Home);
                        SQLManager.updateCurrentQuest(player.getUniqueId(), null);
                        SQLManager.SaveProgressData(quest.getType(), quest.SaveJson());

                        player.sendMessage(toColor("&c&l" + quest.getType().getTitle() + " を中断しました。"));
                    };
                    SQLManager.MyCallback cancelStopQuest = () -> QuestMC.getManager().OpenGUI(player, GUITypes.GUIEnum.Home);

                    ConfirmManager.displayConfirm(player, new ConfirmManager.confirmInfo(
                            "&c&l確認画面: クエストを中断",
                            item_stopQuest, //はい
                            item_cancelStopQuest, //いいえ
                            stopQuest, //はい
                            cancelStopQuest //いいえ
                    ));
                    return;
                }
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
