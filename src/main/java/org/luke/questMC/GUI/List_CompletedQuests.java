package org.luke.questMC.GUI;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.luke.questMC.DataClass;
import org.luke.questMC.QuestMC;
import org.luke.questMC.QuestManager.QuestBase;
import org.luke.questMC.QuestManager.QuestEnum;
import org.luke.questMC.QuestManager.QuestManager;
import org.luke.questMC.QuestManager.QuestUtility;
import org.luke.questMC.SQL.SQLManager;
import org.luke.yakisobaGUILib.Abstract.ListGUIAbstract;
import org.luke.yakisobaGUILib.CustomRunnable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.luke.takoyakiLibrary.TakoUtility.setLore;

public class List_CompletedQuests extends ListGUIAbstract<GUITypes.ListGUIEnum> {
    List<QuestBase> quests = new ArrayList<>();

    @Override
    public String getGUITitle() {
        return "達成済みクエスト一覧";
    }

    @Override
    public List<ItemStack> getItemList() {
        List<QuestEnum.Quest_Normal> clearedList = SQLManager.getCompletedQuestList(player.getUniqueId());
        if(clearedList != null && !clearedList.isEmpty()) {
            List<ItemStack> items = new ArrayList<>();
            for(QuestEnum.Quest_Normal type : clearedList) {
                QuestBase quest = QuestManager.getQuest(type);
                ItemStack item = QuestUtility.getIcon(quest);

                List<String> addLore = new ArrayList<>();
                if(item.getLore() != null) {
                    addLore.addAll(item.getLore());
                }

                addLore.add("&cクリックして詳細を確認・もう一度クエストを開始する");
                addLore.add("&c&lこのクエストはすでに達成済みです。");

                LocalDateTime date = SQLManager.getCompletedDate(player.getUniqueId(), type);
                if(date != null) {
                    addLore.add("&c&l初達成日: "+ date.getYear() +"-"+ date.getMonthValue() +"-"+ date.getMinute() +"  "+ date.getHour() +":" + date.getMinute());
                }
                setLore(item, addLore);

                items.add(item);
                quests.add(QuestManager.getQuest(type));
            }
            return items;
        }
        return null;
    }

    @Override
    public ItemStack setCenterItemStack() {
        return null;
    }

    @Override
    public CustomRunnable.InventoryIndexRunnable whenClickContent() {
        return (InventoryClickEvent event, Integer index) -> {
            Player player = (Player) event.getWhoClicked();

            QuestMC.getGuiManager().getOpenQuestDetails().put(player, new DataClass.QuestDetails(quests.get(index).getType(), getType()));
            QuestMC.getManager().OpenListGUI(player, GUITypes.ListGUIEnum.Quest_Detail);
        };
    }

    @Override
    public CustomRunnable.InventoryRunnable whenClickCenter() {
        return null;
    }

    @Override
    public CustomRunnable.InventoryRunnable whenClickBack() {
        return (InventoryClickEvent event) -> {
            Player player = (Player) event.getWhoClicked();
            QuestMC.getManager().OpenGUI(player, GUITypes.GUIEnum.Home);
        };
    }

    @Override
    public void customInventoryClickEvent(InventoryClickEvent inventoryClickEvent) {

    }

    @Override
    public GUITypes.ListGUIEnum getType() {
        return GUITypes.ListGUIEnum.Quests_Cleared;
    }
}
