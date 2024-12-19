package org.luke.questMC.GUI;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.luke.questMC.DataClass;
import org.luke.questMC.QuestMC;
import org.luke.questMC.QuestManager.QuestBase;
import org.luke.questMC.QuestManager.QuestEnum;
import org.luke.questMC.QuestManager.QuestUtility;
import org.luke.yakisobaGUILib.Abstract.ListGUIAbstract;
import org.luke.yakisobaGUILib.CustomRunnable;

import java.util.ArrayList;
import java.util.List;

import static org.luke.takoyakiLibrary.TakoUtility.setLore;
import static org.luke.takoyakiLibrary.TakoUtility.toColor;

public class List_Quests extends ListGUIAbstract<GUITypes.ListGUIEnum> {
    List<QuestBase<QuestEnum.Quest_Normal>> quests = new ArrayList<>();

    @Override
    public String getGUITitle() {
        return "クエスト一覧";
    }

    @Override
    public List<ItemStack> getItemList() {
        List<ItemStack> items = new ArrayList<>();
        for(var quest : QuestMC.getQuestManager().getQuests().values()) {
            quests.add(quest);

            if(quest.isInProgress(player)) {
                var item = QuestUtility.getIcon(quest);
                var result = new ArrayList<String>();
                result.add("&f" + QuestMC.getQuestManager().getQuest((QuestEnum.Quest_Normal) QuestMC.getQuestManager().getActivePlayers().get(player).getType()).getQuestName());
                result.addAll( item.getLore() );
                result.add("&cクリックして詳細を確認");

                item.setType(Material.BARRIER);

                var meta = item.getItemMeta();
                meta.setDisplayName(toColor("&c&l現在進行中のクエスト"));
                item.setItemMeta(meta);

                setLore(item, result);

                items.add(item);
            } else {
                var item = QuestUtility.getIcon(quest);
                var result = item.getLore();
                result.add("&cクリックして詳細を確認してクエストを開始する");
                setLore(item, result);

                items.add(item);
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

            QuestMC.getGuiManager().getOpenQuestDetails().put(player, new DataClass.QuestDetails(quests.get(index).getType(), false));
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
            QuestMC.getManager().OpenGUI(player, GUITypes.GUIEnum.Home);
        };
    }

    @Override
    public void customInventoryClickEvent(InventoryClickEvent inventoryClickEvent) {

    }

    @Override
    public GUITypes.ListGUIEnum getType() {
        return GUITypes.ListGUIEnum.Quests;
    }
}
