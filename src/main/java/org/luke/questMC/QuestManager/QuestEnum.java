package org.luke.questMC.QuestManager;

import lombok.Getter;
import org.bukkit.Material;

public class QuestEnum {
    @Getter
    public enum Quest_Normal {
        Hello_World(Material.GRASS_BLOCK, "ハローワールド"),
        Master_of_Crafting_Table(Material.CRAFTING_TABLE, "作業台の鉄人"),
        The_Executor_of_Land_Leveling(Material.IRON_PICKAXE, "整地の遂行者"),
        Manager_of_the_sleeping_quarters(Material.RED_BED, "寝床の管理人"),

        ;

        private final Material icon;
        private final String title;

        Quest_Normal(Material material, String title) {
            this.icon = material;
            this.title = title;
        }
    }
}
