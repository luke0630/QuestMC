package org.luke.questMC.GUI;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
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

import static org.luke.takoyakiLibrary.TakoUtility.setLore;

public class List_ClearedQuests extends ListGUIAbstract<GUITypes.ListGUIEnum> {
    @Override
    public String getGUITitle() {
        return "クリア済みクエスト一覧";
    }

    @Override
    public List<ItemStack> getItemList() {
        List<QuestEnum.Quest_Normal> clearedList = SQLManager.getClearedEnumList(player.getUniqueId());
        if(clearedList != null && !clearedList.isEmpty()) {
            List<ItemStack> items = new ArrayList<>();
            for(QuestEnum.Quest_Normal type : clearedList) {
                QuestBase quest = QuestManager.getQuest(type);
                ItemStack item = QuestUtility.getIcon(quest);

                List<String> addLore = new ArrayList<>();
                if(item.getLore() != null) {
                    addLore.addAll(item.getLore());
                }

                addLore.add("&c&lこのクエストはすでにクリア済みです。");
                addLore.add("&cクリックして詳細を確認・もう一度クエストを開始する");
                setLore(item, addLore);

                items.add(item);
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
        return null;
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