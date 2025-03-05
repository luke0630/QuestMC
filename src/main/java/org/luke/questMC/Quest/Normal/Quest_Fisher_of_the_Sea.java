package org.luke.questMC.Quest.Normal;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.luke.questMC.QuestManager.QuestBase;
import org.luke.questMC.QuestManager.QuestEnum;
import org.luke.questMC.SQL.SQLUtility;

import java.util.*;

import static org.luke.takoyakiLibrary.TakoUtility.getItem;
import static org.luke.takoyakiLibrary.TakoUtility.toColor;

public class Quest_Fisher_of_the_Sea extends QuestBase {
    private final Map<UUID, Byte> progressInfo = new HashMap<>();
    final byte completed_count = 4;

    @Override
    public List<String> getDescription() {
        return List.of(
                "魚を"+ completed_count +"匹 釣る"
        );
    }

    @Override
    public List<ItemStack> getRewardItem() {
        ItemStack item = getItem(Material.SAND, "");
        item.setAmount(50);
        return List.of(
            item
        );
    }

    @Override
    public RewardInfo getRewardCustom() {
        return null;
    }

    @Override
    public String SaveJson() {
        Map<String, String> resultMap = new HashMap<>();

        for(Map.Entry<UUID, Byte> entry : progressInfo.entrySet())  {
            resultMap.put( entry.getKey().toString(), entry.getValue().toString() );
        }
        JSONObject json = SQLUtility.convertMapToJson(resultMap);
        return json.toString();
    }

    @Override
    public void LoadJson(JSONObject json) {
        for(var key : json.keySet()) {
            Byte progress = Byte.valueOf(json.getString(key));
            progressInfo.put(UUID.fromString(key), progress);
        }
    }

    @Override
    public List<String> getProgressInfo(Player player) {
        UUID uuid = player.getUniqueId();
        byte current = progressInfo.get(uuid);
        byte left = (byte) Byte.compare(completed_count, current);
        return List.of(
                "あと" + left + "匹釣ってください。  "+ current +"/"+ completed_count +"匹"
        );
    }

    @Override
    public @NotNull QuestEnum.Quest_Normal getType() {
        return QuestEnum.Quest_Normal.Fisher_of_the_Sea;
    }

    @Override
    public void onComplete(Player player) {
        progressInfo.remove(player.getUniqueId());
    }

    @Override
    protected void onStart(Player player) {
        progressInfo.put(player.getUniqueId(), (byte) 0);
    }

    @EventHandler
    public void onFishEvent(PlayerFishEvent event) {
        Player player = event.getPlayer();

        if(isInProgress(player)) return;

        UUID uuid = player.getUniqueId();
        if(!progressInfo.containsKey(uuid)) {
            onStart(player);
        }

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            // 釣れたものを取得
            if (event.getCaught() instanceof Item caughtItem) {
                Material itemType = caughtItem.getItemStack().getType();

                if (isFish(itemType)) {
                    byte current = progressInfo.get(uuid);
                    byte next = (byte) (current + 1);
                    progressInfo.put(uuid, next);

                    if(next < completed_count) {
                        byte left = (byte) Byte.compare(completed_count, next);
                        player.sendMessage(toColor("&a魚を釣りました！ あと" + left + "匹釣ってください。  "+ next +"/"+ completed_count +"匹"));
                    } else {
                        complete(player);
                    }
                } else {
                    Component component =
                            Component.text("魚以外のものを釣ったためカウントされません。")
                                    .color(NamedTextColor.RED)
                                    .decorate(TextDecoration.BOLD);
                    Component caughtComponent =
                            Component.text("釣ったアイテム: ").append(Component.translatable(Objects.requireNonNull(itemType.getItemTranslationKey())))
                                    .color(NamedTextColor.YELLOW);
                    player.sendMessage( component );
                    player.sendMessage( caughtComponent );
                }
            }
        }
    }

    private boolean isFish(Material material) {
        // Tag.FISHESを使用して魚かどうかを判定
        return Tag.ITEMS_FISHES.isTagged(material);
    }
}
