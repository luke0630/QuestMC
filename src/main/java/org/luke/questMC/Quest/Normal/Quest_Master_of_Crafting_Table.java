package org.luke.questMC.Quest.Normal;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.luke.questMC.QuestManager.QuestBase;
import org.luke.questMC.QuestManager.QuestEnum;
import org.luke.takoyakiLibrary.TakoUtility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.luke.questMC.QuestManager.QuestEnum.Quest_Normal.Master_of_Crafting_Table;
import static org.luke.takoyakiLibrary.TakoUtility.getItem;
import static org.luke.takoyakiLibrary.TakoUtility.toColor;

public class Quest_Master_of_Crafting_Table extends QuestBase {

    private enum craft_progress {
        CREATE_CRAFT_TABLE(2),
        PUT_CRAFT_TABLE(2),

        ;

        private final int completeCount;

        craft_progress(int i) {
            this.completeCount = i;
        }

        public Integer getCompleteCount() {
            return this.completeCount;
        }
    }

    record Progress(craft_progress progress, Integer count) {}
    private final Map<UUID, Progress> progressInfo = new HashMap<>();

    @Override
    public List<String> getDescription() {
        return List.of(
                "作業台を"+ craft_progress.CREATE_CRAFT_TABLE.getCompleteCount() +"個作った後、",
                "作業台を"+ craft_progress.PUT_CRAFT_TABLE.getCompleteCount() +"個設置する"
        );
    }

    @Override
    public List<ItemStack> getRewardItem() {
        return List.of(
                getItem(Material.IRON_HELMET, ""),
                getItem(Material.IRON_CHESTPLATE, ""),
                getItem(Material.IRON_LEGGINGS, ""),
                getItem(Material.IRON_BOOTS, "")
        );
    }

    @Override
    public RewardInfo getRewardCustom() {
        return null;
    }

    @Override
    public String SaveJson() {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<UUID, Progress> entry : progressInfo.entrySet()) {
            JSONObject progressJson = new JSONObject();
            progressJson.put("progress", entry.getValue().progress.name());
            progressJson.put("count", entry.getValue().count);

            jsonObject.put(entry.getKey().toString(), progressJson);
        }
        return jsonObject.toString();
    }

    @Override
    public void LoadJson(JSONObject json) {
        System.out.println(json.toString());
        for (String key : json.keySet()) {
            Object value = json.get(key);

            if (value instanceof JSONObject progressJson) {
                try {
                    craft_progress progress = craft_progress.valueOf(progressJson.getString("progress"));
                    int count = progressJson.getInt("count");

                    progressInfo.put(UUID.fromString(key), new Progress(progress, count));
                } catch (Exception e) {
                    System.err.println("エラー: " + key + " のデータ処理中に問題が発生しました: " + e.getMessage());
                }
            }
        }

    }

    @Override
    public List<String> getProgressInfo(Player player) {
        UUID uuid = player.getUniqueId();
        if(!progressInfo.containsKey(uuid)) return null;
        if(progressInfo.get(uuid).progress == craft_progress.CREATE_CRAFT_TABLE) {
            final int complete_count = craft_progress.CREATE_CRAFT_TABLE.getCompleteCount();
            int result = progressInfo.get(player.getUniqueId()).count;

            return List.of(
                    "&6作業台を" + craft_progress.CREATE_CRAFT_TABLE.getCompleteCount() + "個クラフトしてください",
                    "&aクラフトした作業台の個数: " + result + "/"+ complete_count +"個",
                    "あと"+ (complete_count - result)  +"個 クラフトしてください"
            );
        } else if(progressInfo.get(uuid).progress == craft_progress.PUT_CRAFT_TABLE) {
            final int complete_count = craft_progress.PUT_CRAFT_TABLE.getCompleteCount();
            int result = progressInfo.get(player.getUniqueId()).count;

            return List.of(
                    "&6作業台を" + craft_progress.PUT_CRAFT_TABLE.getCompleteCount() + "個設置してください",
                    "&a設置したした作業台の個数: " + result + "/"+ complete_count +"個",
                    "あと"+ (complete_count - result)  +"個 設置してください"
            );
        }
        return null;
    }

    @Override
    public QuestEnum.@NotNull Quest_Normal getType() {
        return Master_of_Crafting_Table;
    }

    @Override
    public void onComplete(Player player) {
        progressInfo.remove(player.getUniqueId());
    }

    @Override
    protected void onStart(Player player) {
        progressInfo.put(player.getUniqueId(), new Progress(craft_progress.CREATE_CRAFT_TABLE, 0));
        player.sendMessage(TakoUtility.toColor("&c&l作業台を二つクラフトしてください。"));
    }

    //Events
    @EventHandler
    public void onCraftItemEvent(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getRecipe().getResult();

        if(isInProgress(player)) return;
        if(item.getType() != Material.CRAFTING_TABLE) return;

        UUID uuid = player.getUniqueId();
        if(!progressInfo.containsKey(uuid)) {
            onStart(player);
        }

        if(progressInfo.get(uuid).progress == craft_progress.CREATE_CRAFT_TABLE) {
            int itemResultAmount = TakoUtility.getCraftedItemCount(event);

            final int complete_count = craft_progress.CREATE_CRAFT_TABLE.getCompleteCount();

            int result = progressInfo.get(player.getUniqueId()).count + itemResultAmount;
            boolean completed_craft_table = result >= complete_count;

            progressInfo.put(player.getUniqueId(), new Progress(craft_progress.CREATE_CRAFT_TABLE, result));
            player.sendMessage(TakoUtility.toColor("&a現在クラフトした作業台の個数: " + result + "/"+ complete_count +"個"));
            if(completed_craft_table) {
                player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1 ,1);
                player.sendMessage(toColor("&c次は作業台を" + craft_progress.PUT_CRAFT_TABLE.getCompleteCount() +"個設置してください。"));
                progressInfo.put(player.getUniqueId(), new Progress(craft_progress.PUT_CRAFT_TABLE, 0));
            }
        }
    }
    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if(isInProgress(player)) return;

        UUID uuid = player.getUniqueId();
        if(progressInfo.get(uuid).progress == craft_progress.PUT_CRAFT_TABLE) {
            if(event.getBlock().getType() == Material.CRAFTING_TABLE) {
                final int complete_count = craft_progress.PUT_CRAFT_TABLE.getCompleteCount();
                int result = progressInfo.get(player.getUniqueId()).count + 1;

                progressInfo.put(player.getUniqueId(), new Progress(craft_progress.PUT_CRAFT_TABLE, result));

                player.sendMessage(TakoUtility.toColor("&a現在設置した作業台の個数: " + result + "/"+ complete_count +"個"));

                if(result >= craft_progress.PUT_CRAFT_TABLE.getCompleteCount()) {
                    complete(player);
                }
            }
        }
    }
}
