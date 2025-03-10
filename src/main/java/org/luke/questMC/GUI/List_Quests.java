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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.luke.takoyakiLibrary.TakoUtility.*;

public class List_Quests extends ListGUIAbstract<GUITypes.ListGUIEnum> {
    List<QuestBase> quests = new ArrayList<>();

    @Override
    public String getGUITitle() {
        return "クエスト一覧 (未達成)";
    }

    @Override
    public List<ItemStack> getItemList() {
        List<ItemStack> items = new ArrayList<>();
        for(var quest : QuestManager.getQuests().values()) {
            UUID uuid = player.getUniqueId();
            if(quest.isNotInProgress(player)) {
                List<QuestEnum.Quest_Normal> completedQuests = SQLManager.getCompletedQuestList(uuid);

                if(completedQuests != null && completedQuests.contains(quest.getType())) continue; //達成済みは載せない
                var item = QuestUtility.getIcon(quest);

                var result = new ArrayList<String>();
                if(item.getLore() != null) {
                    result.addAll(item.getLore());
                }

                result.add("&cクリックして詳細を確認してクエストを開始する");

                setLore(item, result);
                items.add(item);
                quests.add(quest);
            }
        }
        return items;
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
        return (InventoryClickEvent event) -> QuestMC.getManager().OpenGUI(player, GUITypes.GUIEnum.Home);
    }

    @Override
    public void customInventoryClickEvent(InventoryClickEvent inventoryClickEvent) {

    }

    @Override
    public GUITypes.ListGUIEnum getType() {
        return GUITypes.ListGUIEnum.Quests;
    }
}
