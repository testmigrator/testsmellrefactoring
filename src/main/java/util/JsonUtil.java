package util;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;


public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();



    public static String objectToJson(Object data) {
        try {
            String string = MAPPER.writeValueAsString(data);
            return string;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T jsonToPojo(String jsonData, Class<T> beanType) {
        try {
            T t = MAPPER.readValue(jsonData, beanType);
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static <T> List<T> jsonToList(String jsonData, Class<T> beanType) {
        try {
            List<T> list = JSONArray.parseArray(jsonData, beanType);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }



}