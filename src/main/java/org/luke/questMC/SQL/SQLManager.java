package org.luke.questMC.SQL;

import org.json.JSONArray;
import org.json.JSONObject;
import org.luke.questMC.QuestMC;
import org.luke.questMC.QuestManager.QuestEnum;
import org.luke.questMC.QuestManager.QuestManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLManager {
    public interface MyCallback {
        void onComplete();
    }

    static Connection connection = null;
    static final String tableName = "playerQuestData";
    final static String column_uuid = "minecraft_uuid";
    static final String column_quests_cleared = "quests_cleared";
    static final String column_quest_current = "quest_current";

    static final String progress_tableName = "questProgressData";
    final static String progress_column_type = "type";
    final static String progress_column_data = "data";

    public static void ConnectionToDatabase(MyCallback result) {
        String url = "jdbc:mysql://" + SQLData.getURL();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println(SQLData.getUSERNAME() + "   " + SQLData.getPASSWORD());
            connection = DriverManager.getConnection(url, SQLData.getUSERNAME(), SQLData.getPASSWORD());

            result.onComplete();
        } catch (Exception ignored) {

        }
    }

    public static void ExecuteUpdate(String command) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(command);

            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static PreparedStatement GetPrepareStatement(String  query) {
        try {
            return connection.prepareStatement(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static void CreateDatabase(MyCallback callback) {
        final String dbName = SQLData.getDATABASE_NAME();
        List<String> executes = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();

            executes.add("CREATE DATABASE IF NOT EXISTS " + dbName);

            executes.add("USE " + dbName);

            String createTable = "CREATE TABLE IF NOT EXISTS " + tableName + " ( " +
                    column_uuid + " UUID NOT NULL, " +
                    column_quests_cleared + " JSON NULL," +
                    column_quest_current + " VARCHAR(64) NULL," +
                    " PRIMARY KEY ( " + column_uuid + " )" +
                    " );";
            executes.add(createTable);

            String createTable_progress = "CREATE TABLE IF NOT EXISTS " + progress_tableName + " ( " +
                    progress_column_type + " VARCHAR(64) NOT NULL, " +
                    progress_column_data + " JSON NULL," +
                    " PRIMARY KEY ( " + progress_column_type + " )" +
                    " );";
            executes.add(createTable_progress);

            for (String execute : executes) {
                statement.executeUpdate(execute);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            callback.onComplete();
        }
    }
    public static void SaveProgressData(QuestEnum.Quest_Normal type, String json) {
        try {
            PreparedStatement ps = GetPrepareStatement(
                    "INSERT INTO " + progress_tableName + " (" + progress_column_type + ", " + progress_column_data + ") " +
                    "VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE " + progress_column_data + " = VALUES(" + progress_column_data + ")"
            );
            ps.setString(1, type.name());
            ps.setString(2, String.valueOf(json));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONObject LoadProgressData(QuestEnum.Quest_Normal type) {
        try {
            ExecuteUpdate("USE " + SQLData.getDATABASE_NAME());
            PreparedStatement ps = GetPrepareStatement("SELECT " + progress_column_data + " FROM " + progress_tableName + " WHERE " + progress_column_type + " = ?");

            ps.setString(1, type.name());

            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                return new JSONObject(resultSet.getString(progress_column_data));
            }

            resultSet.close();
            ps.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    public static void LoadProgressData() {
        try {
            ExecuteUpdate("USE " + SQLData.getDATABASE_NAME());
            PreparedStatement ps = GetPrepareStatement(
                    "SELECT "+ column_uuid +", "+ column_quest_current +" FROM " + tableName
            );

            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                String uuid = resultSet.getString(column_uuid);
                String type = resultSet.getString(column_quest_current);
                if(uuid !=  null && type != null) {
                    QuestEnum.Quest_Normal quest_current = QuestEnum.Quest_Normal.valueOf( type );
                    QuestManager.getProgressInfo().put(UUID.fromString(uuid), new QuestManager.QuestProgressInfo(quest_current));
                }
            }

            resultSet.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addAndUpdateQuestData(UUID u_uuid, List<QuestEnum.Quest_Normal> quests) {
        String uuid = u_uuid.toString();
        try {
            List<String> string_quests = new ArrayList<>();
            for(QuestEnum.Quest_Normal quest : quests) {
                string_quests.add(quest.name());
            }
            JSONArray obj = new JSONArray(string_quests);

            PreparedStatement ps = GetPrepareStatement(
                    "INSERT INTO " + tableName + " (" + column_uuid + ", " + column_quests_cleared + ") " +
                    "VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE " + column_quests_cleared + " = VALUES(" + column_quests_cleared + ")"
            );
            ps.setString(1, uuid);
            ps.setString(2, String.valueOf(obj));
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateCurrentQuest(UUID uuid, QuestEnum.Quest_Normal type) {
        try {
            PreparedStatement ps = GetPrepareStatement(
                    "INSERT INTO "+ tableName +" ("+ column_uuid +", "+ column_quest_current +") VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE "+ column_quest_current +" = VALUES("+ column_quest_current +")"
            );

            ps.setString(1, uuid.toString());
            if(type == null) {
                ps.setString(2, null);
            } else {
                ps.setString(2, type.name());
            }

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void addClearedEnum(String uuid, QuestEnum.Quest_Normal quest) {
        try {
            ExecuteUpdate("USE " + SQLData.getDATABASE_NAME());
            PreparedStatement ps = GetPrepareStatement(
                    "SELECT " + column_quests_cleared + " FROM " + tableName + " WHERE " + column_uuid + " = ?"
            );
            ps.setString(1, uuid);

            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                String result = resultSet.getString(column_quests_cleared);
                JSONArray jsonObj = new JSONArray();
                if(result != null) {
                    jsonObj = new JSONArray(result);
                }

                if (!jsonObj.toList().contains(quest.name())) {
                    jsonObj.put(quest.name());

                    PreparedStatement updatePs = GetPrepareStatement(
                            "UPDATE " + tableName + " SET " + column_quests_cleared + " = ? WHERE " + column_uuid + " = ?;"
                    );
                    updatePs.setString(1, jsonObj.toString());
                    updatePs.setString(2, uuid);
                    updatePs.executeUpdate();
                }
            } else {
                addAndUpdateQuestData(UUID.fromString(uuid), List.of(quest));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<QuestEnum.Quest_Normal> getClearedEnumList(UUID uuid) {
        var logger = QuestMC.getInstance().getLogger();
        try {
            ExecuteUpdate("USE " + SQLData.getDATABASE_NAME());

            PreparedStatement ps = GetPrepareStatement(
                    "SELECT " + column_quests_cleared + " FROM " + tableName + " WHERE " + column_uuid + " = ?"
            );
            ps.setString(1, uuid.toString());

            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                String result = resultSet.getString(column_quests_cleared);
                logger.info(result);

                JSONArray jsonObj = new JSONArray(result);
                List<QuestEnum.Quest_Normal> types = new ArrayList<>();
                for (int i = 0; i < jsonObj.length(); i++) {
                    String string_type = jsonObj.getString(i);

                    try {
                        QuestEnum.Quest_Normal type = QuestEnum.Quest_Normal.valueOf(string_type);
                        types.add(type);
                    } catch (IllegalArgumentException e) {
                        System.out.println(string_type + "をEnumに変換できませんでした。");
                    }
                }

                return types;
            } else {
                logger.info("No data found for UUID: " + uuid);
            }

            resultSet.close();
            ps.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
