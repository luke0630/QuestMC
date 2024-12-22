package org.luke.questMC.QuestManager;

import org.bukkit.inventory.ItemStack;
import org.luke.takoyakiLibrary.TakoUtility;

import java.util.ArrayList;
import java.util.List;

import static org.luke.takoyakiLibrary.TakoUtility.setLore;

public class QuestUtility {
    public static ItemStack getIcon(QuestBase<?> quest) {
        ItemStack item = TakoUtility.getItem(quest.getIcon(), quest.getQuestName());

        List<String> lore = new ArrayList<>();
        lore.add("&c&l----------------------------");
        lore.add("&c&l達成条件");

        for(String  description : quest.getDescription()) {
            lore.add("&a" + description);
        }

        lore.add("&c&l----------------------------");
        lore.add("&c&l報酬");

        boolean noReward = false;

        var rewardItem = quest.getRewardItem();
        var rewardCustom = quest.getRewardCustom();

        if(rewardCustom != null) {
            for(String string : quest.getRewardCustom().description()) {
                lore.add("&a" + string);
            }
        }

        //もしリワードが二つあった場合の中間地点を分ける(見やすくするため)
        if(rewardCustom != null && rewardItem != null) {
            lore.add("&8============================");
        } else if(rewardCustom == null && rewardItem == null) {
            noReward = true;
        }

        if(rewardItem != null) {
            int rewardItemCount = quest.getRewardItem().size();
            if(rewardItemCount > 0) {
                lore.add("&a" + rewardItemCount + "種類のアイテム");
            }
        }

        if(noReward) {
            lore.add("&aなし");
        }

        lore.add("&c&l----------------------------");

        setLore(item, lore);
        return item;
    }
}
