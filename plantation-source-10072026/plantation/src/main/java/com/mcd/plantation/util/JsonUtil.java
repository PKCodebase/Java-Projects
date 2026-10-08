package com.mcd.plantation.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class JsonUtil implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final ObjectMapper mapper = new ObjectMapper();
	
	static {
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, true);
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Asia/Kolkata")));
		mapper.setDateFormat(dateFormat);
	}
	
	public static String convertObjectToJson(Object object) {
		try {
			String json = mapper.writeValueAsString(object);
			return json;
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static <T> T convertJsonToObject(String json, Class<T> objectClass) {
		try {
			T object = mapper.readValue(json, objectClass);
			return object;
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static List<String> getAllValueByKeyFromJsonArrayOfJosnObjects(String jsonStr, String keyName){
		try {
			JsonNode jsonNode = mapper.readTree(jsonStr);
			ArrayNode arrayField = (ArrayNode) jsonNode;
			List<String> values = new ArrayList<String>();
			arrayField.forEach(node -> {
				String value = node.get(keyName) != null ? node.get(keyName).asText(keyName) : null;
				if(value != null && !value.isBlank()) {
					values.add(value);
				}
	        });
			return values;
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static String getSubJson(String jsonStr, String[] keyNames){
		try {
			JsonNode jsonNode = mapper.readTree(jsonStr);
			ArrayNode arrayField = (ArrayNode) jsonNode;
			ArrayNode parentArray =  mapper.createArrayNode();
			if(keyNames.length > 0) {
				arrayField.forEach(node -> {
					ObjectNode childObject = mapper.createObjectNode();
					for (String key : keyNames) {
						String value = node.get(key) != null ? node.get(key).asText(key) : null;
						if(value != null && !value.isBlank()) {
							childObject.put(key, value);
						}
					}
					if(!childObject.isEmpty()) {
						parentArray.add(childObject);
					}
				});
			}
			return parentArray.toString();
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	public static boolean isValidJson(String json) {
	    try {
	        mapper.readTree(json);
	    } catch (JacksonException e) {
	        return false;
	    }
	    return true;
	}
	
	public static boolean isJsonArray(String json) {
	    try {
	        JsonNode node = mapper.readTree(json);
	        return node.isArray();
	    } catch (JacksonException e) {
	        return false;
	    }
	}
	
	public static boolean isJsonObject(String json) {
	    try {
	        JsonNode node = mapper.readTree(json);
	        return node.isObject();
	    } catch (JacksonException e) {
	        return false;
	    }
	}
	
	public static String createJsonObjectText(Map<String, String> datas){
		try {
			ObjectNode objectNode = mapper.createObjectNode();
			for (Map.Entry<String, String> entry : datas.entrySet()) {
				objectNode.put(entry.getKey(), entry.getValue());
			}
			return objectNode.toString();
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public static String getValueByKey(String jsonString, String key) {
		try {
			JsonNode rootJsonNode = mapper.readTree(jsonString);
			JsonNode jsonNode = rootJsonNode.get(key);
			if(jsonNode != null && jsonNode.isValueNode()) {
				return jsonNode.asText();
			}
			return null;
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}
}
