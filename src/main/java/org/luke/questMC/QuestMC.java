package org.luke.questMC;

import lombok.Getter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.luke.questMC.Command.CommandManager;
import org.luke.questMC.GUI.*;
import org.luke.questMC.Quest.Normal.Quest_HelloWorld;
import org.luke.questMC.QuestManager.QuestEnum;
import org.luke.questMC.QuestManager.QuestManager;
import org.luke.yakisobaGUILib.YakisobaGUILib;

import java.util.List;
import java.util.Objects;

public final class QuestMC extends JavaPlugin implements Listener {
    @Getter
    private static QuestMC instance;
    @Getter
    private static YakisobaGUILib<GUITypes.GUIEnum, GUITypes.ListGUIEnum> manager;
    @Getter
    private static DataClass guiManager;
    @Getter
    private static QuestManager<QuestEnum.Quest_Normal> questManager;


    @Override
    public void onEnable() {
        instance = this;
        manager = new YakisobaGUILib<>(this, List.of(
                new List_Quests(),
                new List_QuestDetails(),

                new QuestHome()
        ));

        guiManager = new DataClass();

        var command = getCommand("quest");
        Objects.requireNonNull(command).setExecutor(new CommandManager());

        getServer().getPluginManager().registerEvents(this, this);

        questManager = new QuestManager<>();
        questManager.registerQuest(new Quest_HelloWorld());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
