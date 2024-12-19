package org.luke.questMC;

import lombok.Data;
import org.bukkit.entity.Player;
import org.luke.questMC.QuestManager.QuestEnum;

import java.util.HashMap;
import java.util.Map;

@Data
public class DataClass {
    private Map<Player, QuestDetails> openQuestDetails = new HashMap<>();

    public record QuestDetails(QuestEnum.Quest_Normal type, boolean from_home) { }
}
