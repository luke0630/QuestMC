package org.luke.questMC.GUI;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.luke.questMC.QuestMC;
import org.luke.questMC.QuestManager.QuestBase;
import org.luke.questMC.QuestManager.QuestEnum;
import org.luke.questMC.QuestManager.QuestUtility;
import org.luke.takoyakiLibrary.TakoUtility;
import org.luke.yakisobaGUILib.Abstract.ListGUIAbstract;
import org.luke.yakisobaGUILib.CustomRunnable;

import java.util.List;

import static org.luke.questMC.GUI.GUITypes.ListGUIEnum.Quest_Detail;
import static org.luke.takoyakiLibrary.TakoUtility.getItem;
import static org.luke.takoyakiLibrary.TakoUtility.setLore;

public class List_QuestDetails extends ListGUIAbstract<GUITypes.ListGUIEnum> {

    QuestBase<QuestEnum.Quest_Normal> quest;
    @Override
    public String getGUITitle() {
        return "達成報酬リスト : " + quest.getQuestName();
    }

    @Override
    public List<ItemStack> getItemList() {
        return quest.getRewardItem();
    }

    @Override
    public ItemStack setCenterItemStack() {
        if(!QuestMC.getQuestManager().getActivePlayers().containsKey(player) ) {
            var item = QuestUtility.getIcon(quest);
            item.setType(Material.REDSTONE_BLOCK);
            var result = item.getLore();
            result.add("&cクリックしてクエストを開始する");
            setLore(item, result);

            return item;
        } else {
            var item = getItem(Material.BARRIER, "&f&l" + quest.getQuestName());
            TakoUtility.setLore(item, List.of(
                    "&cこのクエストは現在進行中です。"
            ));
            return item;
        }
    }

    @Override
    public CustomRunnable.InventoryIndexRunnable whenClickContent() {
        return null;
    }

    @Override
    public CustomRunnable.InventoryRunnable whenClickCenter() {
        return (InventoryClickEvent event) -> {
            if(!QuestMC.getQuestManager().getActivePlayers().containsKey(player) ) {
                QuestMC.getQuestManager().startQuest(quest.getType(), player);
                player.closeInventory();
            } else {
                player.sendMessage("そのクエストはすでに開始しています。");
            }
        };
    }

    @Override
    public CustomRunnable.InventoryRunnable whenClickBack() {
        return (InventoryClickEvent event) -> {
            if(QuestMC.getGuiManager().getOpenQuestDetails().get(player).from_home()) {
                QuestMC.getManager().OpenGUI(player, GUITypes.GUIEnum.Home);
            } else {
                QuestMC.getManager().OpenListGUI(player, GUITypes.ListGUIEnum.Quests);
            }
        };
    }

    @Override
    public void customInventoryClickEvent(InventoryClickEvent inventoryClickEvent) {

    }

    @Override
    public GUITypes.ListGUIEnum getType() {
        return Quest_Detail;
    }

    @Override
    public void onStart() {
        var type = QuestMC.getGuiManager().getOpenQuestDetails().get(player).type();
        this.quest = QuestMC.getQuestManager().getQuest(type);
    }
}
