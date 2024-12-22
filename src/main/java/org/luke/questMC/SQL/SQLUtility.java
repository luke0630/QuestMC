package org.luke.questMC.SQL;

import lombok.experimental.UtilityClass;
import org.json.JSONObject;

import java.util.Map;

@UtilityClass
public class SQLUtility {
    public JSONObject convertMapToJson(Map<String, String> data) {
        JSONObject jsonObject = new JSONObject();

        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            jsonObject.put(key, value);
        }
        return jsonObject;
    }
}
