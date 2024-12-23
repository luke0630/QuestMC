package org.luke.questMC;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.luke.questMC.Command.CommandManager;
import org.luke.questMC.Event.EventManager;
import org.luke.questMC.GUI.GUITypes;
import org.luke.questMC.GUI.List_QuestDetails;
import org.luke.questMC.GUI.List_Quests;
import org.luke.questMC.GUI.QuestHome;
import org.luke.questMC.Quest.Normal.Quest_HelloWorld;
import org.luke.questMC.QuestManager.QuestBase;
import org.luke.questMC.QuestManager.QuestEnum;
import org.luke.questMC.QuestManager.QuestManager;
import org.luke.questMC.SQL.SQLData;
import org.luke.questMC.SQL.SQLManager;
import org.luke.yakisobaGUILib.YakisobaGUILib;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.luke.questMC.SQL.SQLManager.*;

public final class QuestMC extends JavaPlugin implements Listener {
    @Getter
    private static QuestMC instance;
    @Getter
    private static YakisobaGUILib<GUITypes.GUIEnum, GUITypes.ListGUIEnum> manager;
    @Getter
    private static DataClass guiManager;
    @Getter
    private static FileConfiguration settingConfig;

    @Override
    public void onEnable() {
        instance = this;
        manager = new YakisobaGUILib<>(this, List.of(
                new List_Quests(),
                new List_QuestDetails(),

                new QuestHome()
        ));

        guiManager = new DataClass();

        saveDefaultConfig();
        settingConfig = getConfig();
        LoadConfig();

        ConnectionToDatabase(() -> {
            CreateDatabase(() -> {

            });
        });

        var command = getCommand("quest");
        Objects.requireNonNull(command).setExecutor(new CommandManager());

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new EventManager(), this);

        QuestManager.registerQuest(new Quest_HelloWorld());

        LoadAllProgressData();
        LoadProgressData();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for(Map.Entry<Enum<QuestEnum.Quest_Normal>, QuestBase> entry :  QuestManager.getQuests().entrySet()) {
            SQLManager.SaveProgressData((QuestEnum.Quest_Normal) entry.getKey(), entry.getValue().SaveJson());
        }
    }

    private void LoadConfig() {
        String url = settingConfig.getString("mysql-url");
        String username = settingConfig.getString("mysql-username");
        String password = settingConfig.getString("mysql-password");
        String databaseName = settingConfig.getString("mysql-database-name");
        SQLData.Initialization(url, username, password, databaseName);
    }

    private void LoadAllProgressData() {
        for(QuestBase quest :  QuestManager.getQuests().values()) {
            quest.LoadJson(
                    LoadProgressData(quest.getType())
            );
        }
    }
}
