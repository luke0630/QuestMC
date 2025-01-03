package org.luke.questMC.Quest.Normal;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.luke.questMC.QuestManager.QuestBase;
import org.luke.questMC.QuestManager.QuestEnum;
import org.luke.questMC.SQL.SQLUtility;
import org.luke.takoyakiLibrary.TakoUtility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.luke.questMC.QuestManager.QuestEnum.Quest_Normal.The_Executor_of_Land_Leveling;
import static org.luke.takoyakiLibrary.TakoUtility.toColor;

public class Quest_The_Executor_Of_Land_Leveling extends QuestBase {
    private final Integer completionCondition = 500; //500ブロック
    private final Map<UUID, Integer> destroyedCount = new HashMap<>(); //残り

    @Override
    public List<String> getDescription() {
        return List.of(
                completionCondition + "個のブロックを破壊する"
        );
    }

    @Override
    public List<ItemStack> getRewardItem() {
        return List.of(
                TakoUtility.getItem(Material.DIAMOND_PICKAXE, "")
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
                "残り: " + destroyedCount.get(uuid) + "/"+ completionCondition +" ブロックを破壊してください"
        );
    }

    @Override
    public QuestEnum.@NotNull Quest_Normal getType() {
        return The_Executor_of_Land_Leveling;
    }

    @Override
    public void onComplete(Player player) {
        destroyedCount.remove(player.getUniqueId());
    }
    @Override
    protected void onStart(Player player) {
        destroyedCount.put(player.getUniqueId(), 0);
        player.sendMessage(toColor("&c&lブロックを" + completionCondition + "個破壊してください。"));
    }

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if(destroyedCount.containsKey(uuid)) {
            int leftBlocks = destroyedCount.get(uuid) + 1;
            destroyedCount.replace(uuid ,leftBlocks);
            if(leftBlocks >= completionCondition) {
                player.sendActionBar(Component.text("コンプリート！"));
                complete(player);
            } else {
                player.sendActionBar(Component.text("残り: " + destroyedCount.get(uuid) + "/"+ completionCondition +" 破壊してください"));
            }
        }
    }
}
