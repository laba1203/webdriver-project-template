package api.objects;

import com.google.gson.Gson;
import lombok.SneakyThrows;

public interface JsonData {

    @SneakyThrows
    default String getAsJsonString(){
        return new Gson().toJson(this);
    }

}
