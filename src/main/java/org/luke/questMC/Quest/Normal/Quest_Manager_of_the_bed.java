package org.luke.questMC.Quest.Normal;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.luke.questMC.QuestManager.QuestBase;
import org.luke.questMC.QuestManager.QuestEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.luke.takoyakiLibrary.TakoUtility.getItem;
import static org.luke.takoyakiLibrary.TakoUtility.toColor;

public class Quest_Manager_of_the_bed extends QuestBase {
    private final Map<UUID, bedProgress> progressInfo = new HashMap<>();

    private enum bedProgress {
        create("作業台でベッドを1つ作成してください(色は問わない)"),
        put("作成したベッドを設置してください"),
        ;

        private final String text;

        bedProgress(String text) {
            this.text = text;
        }
    }

    final bedProgress init = bedProgress.create;

    @Override
    public List<String> getDescription() {
        return List.of(
                "作業台でベッドを1つ作成する(色は問わない)",
                "作成したベッドを設置する"
        );
    }

    @Override
    public List<ItemStack> getRewardItem() {
        ItemStack item = getItem(Material.COOKED_BEEF, "");
        item.setAmount(12);
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
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<UUID, bedProgress> entry : progressInfo.entrySet()) {
            JSONObject progressJson = new JSONObject();
            progressJson.put("progress", entry.getValue().name());
            jsonObject.put(entry.getKey().toString(), progressJson);
        }
        return jsonObject.toString();
    }

    @Override
    public void LoadJson(JSONObject json) {
        for (String key : json.keySet()) {
            Object value = json.get(key);

            if (value instanceof JSONObject progressJson) {
                try {
                    bedProgress progress = bedProgress.valueOf(progressJson.getString("progress"));
                    progressInfo.put(UUID.fromString(key), progress);
                } catch (Exception e) {
                    System.err.println("エラー: " + key + " のデータ処理中に問題が発生しました: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public List<String> getProgressInfo(Player player) {
        UUID uuid = player.getUniqueId();
        return List.of(
            progressInfo.get(uuid).text
        );
    }

    @Override
    public QuestEnum.@NotNull Quest_Normal getType() {
        return QuestEnum.Quest_Normal.Manager_of_the_bed;
    }

    @Override
    public void onComplete(Player player) {
        progressInfo.remove(player.getUniqueId());
    }

    @Override
    protected void onStart(Player player) {
        progressInfo.put(player.getUniqueId(), init);
        player.sendMessage(toColor("&c&l" + init.text));
    }

    @EventHandler
    public void onCraftItemEvent(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();
        if(isInProgress(player)) return;

        ItemStack item = event.getRecipe().getResult();

        if(!isInProgress(player)) return;
        if(item.getType() != Material.RED_BED) return;
        UUID uuid = player.getUniqueId();

        if(!progressInfo.containsKey(uuid)) {
            onStart(player);
        }

        if(progressInfo.get(uuid) == bedProgress.create) {
            player.closeInventory();
            progressInfo.put(uuid, bedProgress.put);
            player.sendMessage(toColor("&c&l次は、" + bedProgress.put.text));
        }
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if(isInProgress(player)) return;

        UUID uuid = player.getUniqueId();
        if(progressInfo.get(uuid) == bedProgress.put && isBed(event.getBlock().getType())) {
            complete(player);
        }
    }

    public boolean isBed(Material material) {
        return Tag.BEDS.isTagged(material);
    }
}
