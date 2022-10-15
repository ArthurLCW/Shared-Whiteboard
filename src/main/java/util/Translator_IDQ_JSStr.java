package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import util.ID;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingDeque;

public class Translator_IDQ_JSStr {
    public String IDQueueToString(LinkedBlockingDeque<ID> idQ) throws JsonProcessingException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("MsgName", "UpdateSendUsers");
        JSONArray jsArray = new JSONArray();
        ObjectMapper mapper = new ObjectMapper();

        Iterator<ID> iterator = idQ.iterator();
        while (iterator.hasNext()){
            ID id = iterator.next();
            jsArray.add(mapper.writeValueAsString(id));
        }
        jsonObject.put("JsonArray", jsArray);
        String jsStr = jsonObject.toJSONString();
        System.out.println("IDQueueJSONString jsStr: "+jsStr);
        return jsStr;
    }


    public Vector<ID> StrToVec(String jsonStr) throws ParseException, UnknownHostException {
        Vector<ID> vecID = new Vector<ID>();
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(jsonStr);
        JSONArray retArray = (JSONArray) jsonObject.get("JsonArray");
        for (int i=0; i<retArray.size(); i++){
            String str = (String) retArray.get(i);
            JSONObject jsID = (JSONObject) parser.parse(str);
            ID id = new ID((String)jsID.get("username"), InetAddress.getByName((String) jsID.get("ip")),
                    ((Long) jsID.get("port")).intValue());
            vecID.add(id);
        }
        System.out.println("IDQueueJSONString vecID: "+vecID);
        return vecID;
    }
}
