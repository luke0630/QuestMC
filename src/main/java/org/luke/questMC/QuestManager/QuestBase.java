package org.luke.questMC.QuestManager;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.luke.questMC.Toast;

import java.util.List;

import static org.luke.questMC.Toast.displayTo;
import static org.luke.takoyakiLibrary.TakoUtility.toColor;

public abstract class QuestBase implements Listener {
    @FunctionalInterface
    public interface RewardRunnable {
        void run(Player player);
    }

    public record RewardInfo(List<String> description, RewardRunnable runnable){}

    // クエスト名を取得
    public abstract Material getIcon();
    public abstract String getQuestName();
    public abstract List<String> getDescription();
    public abstract List<ItemStack> getRewardItem();
    public abstract RewardInfo getRewardCustom();
    public abstract String SaveJson();
    public abstract QuestEnum.Quest_Normal getType();

    // クエスト完了 (基本動作)
    public void complete(Player player) {
        onComplete(player);

        QuestManager.getProgressInfo().remove(player);
        QuestManager.addClearedQuest(player.getUniqueId(), getType());

        player.sendMessage(toColor("&a&lクエストを達成しました: " + getQuestName()));
        player.sendMessage(toColor("&a以下の報酬が渡されました。"));


        if(getRewardItem() != null) {
            Inventory playerInv = player.getInventory();
            for(ItemStack itemStack : getRewardItem()) {
                playerInv.addItem(itemStack);
                player.sendMessage(Component.text("・").append(Component.translatable(itemStack.translationKey()).append(Component.text(" × " + itemStack.getAmount()))) );
            }
        }
        if(getRewardCustom() != null) {
            getRewardCustom().runnable.run(player);
        }

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        displayTo(player , "emerald" , getQuestName(), Toast.Style.GOAL);
    }

    protected abstract void onComplete(Player player);

    // クエスト進行中か確認
    public boolean isInProgress(Player player) {
        if(QuestManager.getProgressInfo().containsKey(player)) {
            return QuestManager.getProgressInfo().get(player).getType() == getType();
        }
        return false;
    }

    protected void onStart(Player player) {

    }
}
