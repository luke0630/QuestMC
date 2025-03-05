package org.luke.questMC.Quest.Normal;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.luke.questMC.QuestManager.QuestBase;
import org.luke.questMC.QuestManager.QuestEnum;
import org.luke.questMC.SQL.SQLUtility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.luke.takoyakiLibrary.TakoUtility.getItem;
import static org.luke.takoyakiLibrary.TakoUtility.toColor;

public class Quest_Iron_Man extends QuestBase {
    private final Integer completionCondition = 20; //20ブロック
    private final Map<UUID, Integer> destroyedCount = new HashMap<>(); //残り
    @Override
    public List<String> getDescription() {
        return List.of(
                String.format("鉄鉱石を%d個破壊する", completionCondition)
        );
    }

    @Override
    public List<ItemStack> getRewardItem() {
        return List.of(
                getItem(Material.DIAMOND_PICKAXE, "")
        );
    }

    @Override
    public RewardInfo getRewardCustom() {
        return null;
    }

    @Override
    public String SaveJson() {
        Map<String, String> resultMap = new HashMap<>();

        for(Map.Entry<UUID, Integer> entry : destroyedCount.entrySet())  {
            resultMap.put( entry.getKey().toString(), entry.getValue().toString() );
        }
        JSONObject json = SQLUtility.convertMapToJson(resultMap);
        return json.toString();
    }

    @Override
    public void LoadJson(JSONObject json) {
        for(var key : json.keySet()) {
            Integer count = json.getInt(key);
            destroyedCount.put(UUID.fromString(key), count);
        }
    }

    @Override
    public List<String> getProgressInfo(Player player) {
        UUID uuid = player.getUniqueId();
        return List.of(
                String.format("残り: %d/%d 鉄鉱石を破壊してください",
                        destroyedCount.get(uuid),
                        completionCondition
                )
        );
    }

    @Override
    public @NotNull QuestEnum.Quest_Normal getType() {
        return QuestEnum.Quest_Normal.Iron_Man;
    }

    @Override
    public void onComplete(Player player) {
        destroyedCount.remove(player.getUniqueId());
    }

    @Override
    protected void onStart(Player player) {
        destroyedCount.put(player.getUniqueId(), 0);
        player.sendMessage(toColor(String.format(
                "&c&l鉄鉱石(あるいは深層鉄鉱石)を%d個破壊してください",
                completionCondition
        )));
    }

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if(isInProgress(player)) return;
        if(!Tag.IRON_ORES.isTagged(event.getBlock().getType())) return; //鉄鉱石以外だったらreturnする

        if(!destroyedCount.containsKey(uuid)) {
            destroyedCount.put(uuid, 0);
        }

        int leftBlocks = destroyedCount.get(uuid) + 1;
        destroyedCount.put(uuid ,leftBlocks);
        if(leftBlocks >= completionCondition) {
            complete(player);
            player.sendActionBar(Component.text("  "));
        } else {
            player.sendActionBar(Component.text(String.format(
                    "残り: %d/%d 破壊してください",
                    leftBlocks,
                    completionCondition
            )));
        }
    }
}
