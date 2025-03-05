package org.luke.questMC.Quest.Normal;

import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

import static org.luke.takoyakiLibrary.TakoUtility.toColor;

public class Quest_Master_of_Trade extends QuestBase {
    private final Map<UUID, Integer> progressInfo = new HashMap<>();
    private final Integer completed_count = 10;
    @Override
    public List<String> getDescription() {
        return List.of(
                String.format("村人と%d回取引する", completed_count)
        );
    }

    @Override
    public List<ItemStack> getRewardItem() {
        return List.of();
    }

    @Override
    public RewardInfo getRewardCustom() {
        return null;
    }

    @Override
    public String SaveJson() {
        Map<String, String> resultMap = new HashMap<>();

        for(Map.Entry<UUID, Integer> entry : progressInfo.entrySet())  {
            resultMap.put( entry.getKey().toString(), entry.getValue().toString() );
        }
        JSONObject json = SQLUtility.convertMapToJson(resultMap);
        return json.toString();
    }

    @Override
    public void LoadJson(JSONObject json) {
        for(var key : json.keySet()) {
            Integer progress = json.getInt(key);
            progressInfo.put(UUID.fromString(key), progress);
        }
    }

    @Override
    public List<String> getProgressInfo(Player player) {
        UUID uuid = player.getUniqueId();
        int current = progressInfo.get(uuid);
        int left = completed_count - current;
        return List.of(
                "あと" + left + "回取引してください。  "+ current +"/"+ completed_count +"回"
        );
    }

    @Override
    public @NotNull QuestEnum.Quest_Normal getType() {
        return QuestEnum.Quest_Normal.Master_Of_Trade;
    }

    @Override
    public void onComplete(Player player) {
        progressInfo.remove(player.getUniqueId());
    }

    @Override
    protected void onStart(Player player) {
        progressInfo.put(player.getUniqueId(), 0);
    }

    @EventHandler
    public void onPlayerTrade(PlayerTradeEvent event) {
        Player player = event.getPlayer();

        if(isNotInProgress(player)) return;

        UUID uuid = player.getUniqueId();
        if(!progressInfo.containsKey(uuid)) {
            onStart(player);
        }

        int count = progressInfo.get(uuid) + 1;
        int left = completed_count - count;

        progressInfo.put(uuid, count);

        if(left <= 0) {
            player.closeInventory();
            complete(player);
        } else {
            player.sendMessage(toColor(String.format(
                    "&aあと%d回村人と引取してください。  %d/%d",
                    left,
                    count,
                    completed_count
            )));
        }
    }
}
