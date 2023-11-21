package api.objects;

import com.google.gson.*;
import io.restassured.response.Response;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractJson implements JsonData {

    protected <T> T parseObjFromList(Response resp, String path, Class<T> classOfT, int index){
        List<HashMap> jList = resp.getBody().jsonPath().get(path);
        if (jList.isEmpty()) {
            return null;
        }
        JSONObject object = new JSONObject(jList.get(index));
        return new Gson().fromJson(object.toJSONString(), classOfT);
    }

    protected <T> ArrayList<T> parseList(Response resp, String path, Class<T> classOfT){
        ArrayList<T> arr = new ArrayList<>();
        List<HashMap> jList = resp.getBody().jsonPath().getList(path);
        for (HashMap map: jList) {
            JSONObject object = new JSONObject(map);
            arr.add(
                    new Gson().fromJson(object.toJSONString(), classOfT)
            );
        }
        return arr;
    }

    protected <T> T parseObject(Response resp, String path, Class<T> classOfT){
        HashMap<String, HashMap> jHash = resp.getBody().jsonPath().get(path);
        JSONObject object = new JSONObject(jHash);
        GsonBuilder builder = deserializationBuilder();
        Gson gson = builder.create();
        return gson.fromJson(object.toJSONString(), classOfT);
    }

    protected GsonBuilder deserializationBuilder(){
        return new GsonBuilder()
//                .serializeNulls()
                .addDeserializationExclusionStrategy(new CustomExclusionStrategy());
    }

    protected GsonBuilder serializationBuilder(){
        return new GsonBuilder();
    }

    @Override
    public String getAsJsonString(){
        return serializationBuilder().create().toJson(this);
    }

    public JsonElement getAsJson(){
        return serializationBuilder().create().toJsonTree(this);
    }

    public JsonObject convertObjectToJsonObject() {
        return JsonParser.parseString(new Gson().toJson(this)).getAsJsonObject();
    }


    protected class CustomExclusionStrategy implements ExclusionStrategy {
        private final Class<?> typeToSkip;

        public CustomExclusionStrategy() {
            typeToSkip = null;
        }

        public CustomExclusionStrategy(Class<?> typeToSkip) {
            this.typeToSkip = typeToSkip;
        }

        public boolean shouldSkipClass(Class<?> clazz) {
            return (typeToSkip != null) && (clazz == typeToSkip);
        }

        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(ExcludeInParsing.class) != null;
        }

    }

}
