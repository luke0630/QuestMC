package org.luke.questMC.Quest.Normal;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.json.JSONObject;
import org.luke.questMC.QuestManager.QuestBase;
import org.luke.questMC.QuestManager.QuestEnum;
import org.luke.questMC.QuestManager.QuestManager;
import org.luke.questMC.SQL.SQLUtility;
import org.luke.takoyakiLibrary.TakoUtility;

import java.util.*;

import static org.luke.takoyakiLibrary.TakoUtility.getItem;

public class Quest_HelloWorld extends QuestBase {

    Map<UUID, Double> walkedDistance = new HashMap<>();
    final double completionCondition = 100;

    @Override
    public Material getIcon() {
        return Material.GRASS_BLOCK;
    }

    @Override
    public String getQuestName() {
        return "ハローワールド";
    }

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
            System.out.println(key);
            Double progress = json.getDouble(key);
            walkedDistance.put(UUID.fromString(key), progress);
        }
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
    public QuestEnum.Quest_Normal getType() {
        return QuestEnum.Quest_Normal.Hello_World;
    }


    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // クエスト進行中のプレイヤーのみ処理
        if (!isInProgress(player)) return;

        UUID uuid = player.getUniqueId();
        if(!walkedDistance.containsKey(uuid)) {
            walkedDistance.put(uuid, completionCondition);
        }

        double distance = TakoUtility.getDistanceWithoutY(event.getFrom(), event.getTo());
        double currentDistance = walkedDistance.get(uuid) - distance;
        walkedDistance.put(uuid, currentDistance);

        player.sendActionBar(Component.text("残り:  " + (int)currentDistance + "メートル"));
        QuestManager.UpdateProgressInfo(player, List.of(
                "残り:  " + (int)currentDistance + "メートル"
        ));


        if(currentDistance <= 0) {
            player.sendActionBar(Component.text(" "));
            complete(player);
        }
    }

    @Override
    protected void onComplete(Player player) {
        walkedDistance.remove(player.getUniqueId());
    }
}
