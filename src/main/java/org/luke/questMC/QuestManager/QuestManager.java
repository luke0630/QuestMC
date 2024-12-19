package org.luke.questMC.QuestManager;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.luke.questMC.QuestMC;

import java.util.*;

import static org.luke.takoyakiLibrary.TakoUtility.toColor;

@Getter
public class QuestManager<E extends Enum<E>> {
    private final Map<Enum<E>, QuestBase<E>> quests = new HashMap<>();
    private final Map<Player, QuestProgressInfo> activePlayers = new HashMap<>();

    @Getter
    public class QuestProgressInfo {
        private final Enum<E> type;
        private List<String> progressInfo = new ArrayList<>();

        public QuestProgressInfo(Enum<E> type) {
            this.type = type;
        }
    }

    public void UpdateProgressInfo(Player player, List<String> progressInfo) {
        QuestProgressInfo info = activePlayers.get(player);
        info.progressInfo = progressInfo;
    }

    public void registerQuest(QuestBase<E> quest) {
        quest.Init();
        quests.put(quest.getType(), quest);
        Bukkit.getPluginManager().registerEvents(quest, QuestMC.getInstance());
    }

    public void unregisterAllQuests() {
        quests.values().forEach(HandlerList::unregisterAll);
        quests.clear();
    }

    // クエストを取得
    public QuestBase<E> getQuest(E questEnum) {
        for(var quest : quests.entrySet()) {
            if(quest.getKey() == questEnum) {
                return quest.getValue();
            }
        }
        return null;
    }

    // クエストを開始
    public void startQuest(E questType, Player player) {
        QuestBase<?> quest = getQuest(questType);
        if (quest != null) {
            if(!activePlayers.containsKey(player)) {
                activePlayers.put(player, new QuestProgressInfo(questType));
                quest.onStart(player);
                player.sendMessage(toColor("&a&lクエストを開始しました: " + quest.getQuestName()));
            } else {
                if(activePlayers.get(player).type == questType) {
                    player.sendMessage(toColor("&cそのクエストはすでに開始しています。"));
                }
            }
        } else {
            player.sendMessage(toColor("&cクエストが見つかりませんでした: " + questType));
        }
    }
}
