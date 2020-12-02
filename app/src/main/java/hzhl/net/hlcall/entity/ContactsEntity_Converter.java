package hzhl.net.hlcall.entity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.ArrayList;
import java.util.List;

public class ContactsEntity_Converter implements PropertyConverter<List<ContactsEntity>, String> {
    @Override
    public List<ContactsEntity> convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return new ArrayList<>();
        }
        // 先得获得这个，然后再typeToken.getType()，否则会异常
        TypeToken<List<ContactsEntity>> typeToken = new TypeToken<List<ContactsEntity>>() {
        };
        return new Gson().fromJson(databaseValue, typeToken.getType());
    }

    @Override
    public String convertToDatabaseValue(List<ContactsEntity> arrays) {
        if (arrays == null || arrays.size() == 0) {
            return "";
        } else {
            return new Gson().toJson(arrays);

        }
    }
}