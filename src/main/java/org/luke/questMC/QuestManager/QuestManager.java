package org.luke.questMC.QuestManager;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.luke.questMC.QuestMC;
import org.luke.questMC.SQL.SQLManager;

import java.util.*;

import static org.luke.takoyakiLibrary.TakoUtility.toColor;


@UtilityClass
public class QuestManager {
    @Getter
    private final Map<Enum<QuestEnum.Quest_Normal>, QuestBase> quests = new HashMap<>();
    @Getter
    private final Map<UUID, List<QuestEnum.Quest_Normal>> clearedQuests = new HashMap<>();
    @Getter
    private final Map<Player, QuestProgressInfo> progressInfo = new HashMap<>();

    @Getter
    public static class QuestProgressInfo {
        private final QuestEnum.Quest_Normal type;
        private List<String> progressInfo = new ArrayList<>();

        public QuestProgressInfo(QuestEnum.Quest_Normal type) {
            this.type = type;
        }
    }

    public void UpdateProgressInfo(Player player, List<String> progressInfo) {
        QuestProgressInfo info = QuestManager.progressInfo.get(player);
        info.progressInfo = progressInfo;
    }

    public void registerQuest(QuestBase quest) {
        quests.put(quest.getType(), quest);
        Bukkit.getPluginManager().registerEvents(quest, QuestMC.getInstance());
    }

    public void unregisterAllQuests() {
        quests.values().forEach(HandlerList::unregisterAll);
        quests.clear();
    }

    // クエストを取得
    public QuestBase getQuest(QuestEnum.Quest_Normal questEnum) {
        for(var quest : quests.entrySet()) {
            if(quest.getKey() == questEnum) {
                return quest.getValue();
            }
        }
        return null;
    }

    public void addClearedQuest(UUID uuid, QuestEnum.Quest_Normal questNormal) {
        SQLManager.addClearedEnum(uuid.toString(), questNormal);
    }

    // クエストを開始
    public void startQuest(QuestEnum.Quest_Normal questType, Player player) {
        QuestBase quest = getQuest(questType);
        if (quest != null) {
            if(!progressInfo.containsKey(player)) {
                progressInfo.put(player, new QuestProgressInfo(questType));
                quest.onStart(player);
                player.sendMessage(toColor("&a&lクエストを開始しました: " + quest.getQuestName()));
            } else {
                if(progressInfo.get(player).type == questType) {
                    player.sendMessage(toColor("&cそのクエストはすでに開始しています。"));
                }
            }
        } else {
            player.sendMessage(toColor("&cクエストが見つかりませんでした: " + questType));
        }
    }
}
