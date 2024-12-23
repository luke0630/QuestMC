package org.luke.questMC;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.luke.questMC.Command.CommandManager;
import org.luke.questMC.GUI.GUITypes;
import org.luke.questMC.GUI.List_QuestDetails;
import org.luke.questMC.GUI.List_Quests;
import org.luke.questMC.GUI.QuestHome;
import org.luke.questMC.Quest.Normal.Quest_HelloWorld;
import org.luke.questMC.QuestManager.QuestManager;
import org.luke.questMC.SQL.SQLData;
import org.luke.yakisobaGUILib.YakisobaGUILib;

import java.util.List;
import java.util.Objects;

import static org.luke.questMC.SQL.SQLManager.ConnectionToDatabase;
import static org.luke.questMC.SQL.SQLManager.CreateDatabase;

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

        QuestManager.registerQuest(new Quest_HelloWorld());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void LoadConfig() {
        String url = settingConfig.getString("mysql-url");
        String username = settingConfig.getString("mysql-username");
        String password = settingConfig.getString("mysql-password");
        String databaseName = settingConfig.getString("mysql-database-name");
        SQLData.Initialization(url, username, password, databaseName);
    }
}
