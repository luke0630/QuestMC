package org.luke.questMC.SQL;

import org.luke.questMC.QuestMC;

import org.json.JSONArray;
import org.luke.questMC.QuestManager.QuestEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLManager {
    private static final Logger log = LoggerFactory.getLogger(SQLManager.class);

    public interface MyCallback {
        void onComplete(); // コールバックが完了したときに呼ばれるメソッド
    }

    static Connection connection = null;
    static final String tableName = "playerQuestData";
    final static String column_uuid = "minecraft_uuid";
    static final String column_quests_cleared = "quests_cleared";

    static final String progress_tableName = "questProgressData";
    final static String progress_column_type = "type";
    final static String progress_column_data = "data";


    public static void ConnectionToDatabase(MyCallback result) {
        String url = "jdbc:mysql://" + SQLData.getURL();
        try {
            // MySQLドライバのロード
            Class.forName("com.mysql.cj.jdbc.Driver");
            // データベースへの接続
            System.out.println(SQLData.getUSERNAME() + "   " + SQLData.getPASSWORD());
            connection = DriverManager.getConnection(url, SQLData.getUSERNAME(), SQLData.getPASSWORD());

            result.onComplete();
        } catch (Exception ignored) {

        }
    }

    public static void CreateDatabase(MyCallback callback) {
        final String dbName = SQLData.getDATABASE_NAME();
        List<String> executes = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();

            // データベース作成を実行
            executes.add("CREATE DATABASE IF NOT EXISTS " + dbName);

            executes.add("USE " + dbName);

            String createTable = "CREATE TABLE IF NOT EXISTS " + tableName + " ( " +
                    column_uuid + " VARCHAR(50) NOT NULL, " +
                    column_quests_cleared + " JSON NULL," +
                    " PRIMARY KEY ( " + column_uuid + " )" +
                    " );";
            executes.add(createTable);

            String createTable_progress = "CREATE TABLE IF NOT EXISTS " + progress_tableName + " ( " +
                    progress_column_type + " VARCHAR(50) NOT NULL, " +
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

    public static void addAndUpdateQuestData(String uuid, List<QuestEnum.Quest_Normal> quests) {
        try {
            List<String> string_quests = new ArrayList<>();
            for(QuestEnum.Quest_Normal quest : quests) {
                string_quests.add(quest.name());
            }
            JSONArray obj = new JSONArray(string_quests);

            if(isExistsKey(uuid)) {
                String query = "UPDATE "+ tableName +" SET "+ column_quests_cleared +" = ? WHERE "+ column_uuid +" = ?;";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, String.valueOf(obj));
                ps.setString(2, uuid);
                ps.executeUpdate();
            } else {
                String query = "INSERT INTO " + tableName + " (" + column_uuid + ", " + column_quests_cleared + ") VALUES (?, ?)";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, uuid);
                ps.setString(2, String.valueOf(obj));
                ps.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        } finally {

        }
    }

    public static void addClearedEnum(String uuid, QuestEnum.Quest_Normal quest) {
        try {
            Statement statement = connection.createStatement();

            statement.executeUpdate("USE " + SQLData.getDATABASE_NAME());
            String query = "SELECT " + column_quests_cleared + " FROM " + tableName + " WHERE " + column_uuid + " = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, uuid);

            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                String result = resultSet.getString(column_quests_cleared);
                JSONArray jsonObj = new JSONArray(result);

                // contains の代替としてループでチェック
                boolean contains = false;
                for (int i = 0; i < jsonObj.length(); i++) {
                    if (jsonObj.getString(i).equals(quest.name())) {
                        contains = true;
                        break;
                    }
                }

                if(!contains) {
                    jsonObj.put(quest.name());

                    String updateQuery = "UPDATE "+ tableName +" SET "+ column_quests_cleared +" = ? WHERE "+ column_uuid +" = ?;";
                    PreparedStatement updatePs = connection.prepareStatement(updateQuery);
                    updatePs.setString(1, String.valueOf(jsonObj));
                    updatePs.setString(2, uuid);
                    updatePs.executeUpdate();
                } else {
                    System.out.println("すでに含まれています");
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }

    public static List<QuestEnum.Quest_Normal> getClearedEnumList(String uuid) {
        var logger = QuestMC.getInstance().getLogger();
        try {
            Statement statement = connection.createStatement();

            statement.executeUpdate("USE " + SQLData.getDATABASE_NAME());
            String query = "SELECT " + column_quests_cleared + " FROM " + tableName + " WHERE " + column_uuid + " = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, uuid);

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
            System.out.println(e);
            throw new RuntimeException(e);
        }
        return null;
    }

    //Utility class
    public static boolean isExistsKey(String uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM "+ tableName +" WHERE "+ tableName +"."+ column_uuid +" = ?) AS exists_flag;");
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) { // カーソルを結果セットの最初の行に移動
                return rs.getBoolean("exists_flag");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
