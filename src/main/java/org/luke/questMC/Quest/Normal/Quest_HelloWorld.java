package org.luke.questMC.Quest.Normal;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.luke.questMC.QuestManager.QuestBase;
import org.luke.questMC.QuestManager.QuestEnum;
import org.luke.questMC.SQL.SQLUtility;
import org.luke.takoyakiLibrary.TakoUtility;

import java.util.*;

import static org.luke.takoyakiLibrary.TakoUtility.getItem;

public class Quest_HelloWorld extends QuestBase {

    Map<UUID, Double> walkedDistance = new HashMap<>();
    final double completionCondition = 100;

    @Override
    public List<String> getDescription() {
        return List.of(
                completionCondition + "メートル歩き回る"
        );
    }

    @Override
    public RewardInfo getRewardCustom() {
        return null;
    }

    @Override
    public String SaveJson() {
        Map<String, String> resultMap = new HashMap<>();

        for(Map.Entry<UUID, Double> entry : walkedDistance.entrySet())  {
            resultMap.put( entry.getKey().toString(), entry.getValue().toString() );
        }
        JSONObject json = SQLUtility.convertMapToJson(resultMap);
        return json.toString();
    }

    @Override
    public void LoadJson(JSONObject json) {
        for(var key : json.keySet()) {
            Double progress = json.getDouble(key);
            walkedDistance.put(UUID.fromString(key), progress);
        }
    }

    @Override
    public List<String> getProgressInfo(Player player) {
        if(walkedDistance.containsKey(player.getUniqueId())) {
            double currentDistance = walkedDistance.get(player.getUniqueId());
            return List.of(
                    "残り:  " + (int)currentDistance + "メートル"
            );
        }
        return List.of(
            "残り:  " + completionCondition + "メートル"
        );
    }

    @Override
    public List<ItemStack> getRewardItem() {
        var oak = getItem(Material.OAK_LOG, "");
        oak.setAmount(10);
        return List.of(
            oak
        );
    }

    @Override
    public QuestEnum.@NotNull Quest_Normal getType() {
        return QuestEnum.Quest_Normal.Hello_World;
    }


    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // クエスト進行中のプレイヤーのみ処理
        if (isNotInProgress((player))) return;

        UUID uuid = player.getUniqueId();
        if(!walkedDistance.containsKey(uuid)) {
            walkedDistance.put(uuid, completionCondition);
        }

        double distance = TakoUtility.getDistanceWithoutY(event.getFrom(), event.getTo());
        double currentDistance = walkedDistance.get(uuid) - distance;
        walkedDistance.put(uuid, currentDistance);

        player.sendActionBar(Component.text("残り:  " + (int)currentDistance + "メートル"));

        if(currentDistance <= 0) {
            player.sendActionBar(Component.text(" "));
            complete(player);
        }
    }

    @Override
    public void onComplete(Player player) {
        walkedDistance.remove(player.getUniqueId());
    }

    @Override
    protected void onStart(Player player) {
        player.sendMessage(TakoUtility.toColor("&c&l" + completionCondition + "メートル歩いてください。"));
    }
}
