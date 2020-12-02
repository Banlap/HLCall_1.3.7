package hzhl.net.hlcall.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author dana
 * @Description: 存储、读取缓存数据
 * @date 2015/7/28.
 */
public class DataCache {

    private Context context;
    /**
     * sharedPreferences 文件默认使用的文件名
     */
    private static String defaultDataCachePoolName = "DataPool";

    private String dataCachePoolName;
    private SharedPreferences sharedPreferences;

    public DataCache(Context context) {
        this(defaultDataCachePoolName, context);
    }

    public DataCache(String dataCachePoolName, Context context) {
        this.dataCachePoolName = dataCachePoolName;
        this.context = context;
        sharedPreferences = context.getSharedPreferences(this.dataCachePoolName,
                Context.MODE_PRIVATE);
      //  OpenCache();
    }

    /**
     * add a key(String)-value(Serializable object) into SharedPreference
     *
     * @param key
     * @param value
     * @return true if add successfully
     */
    @SuppressLint("NewApi") public boolean put(String key, Serializable value) {
        boolean flag = false;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            String base64String = Base64.encodeToString(bos.toByteArray(),
                    Base64.DEFAULT);
            sharedPreferences.edit().putString(key, base64String).apply();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * add a key(String)-value(String) into SharedPreference
     * @param key
     * @param value
     */
    @SuppressLint("NewApi") public void putString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    /**
     * add a key(String)-value(String) into SharedPreference
     * @param key
     * @param value
     */
       @SuppressLint("NewApi") public void putInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).apply();
    }


    @SuppressLint("NewApi") public void putBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }


    /**
     * add a key(String)-value(long) into SharedPreference
     * @param key
     * @param value
     */
    @SuppressLint("NewApi") public void putLong(String key, long value) {
        sharedPreferences.edit().putLong(key, value).apply();
    }

    /**
     * add a key(String,default is "temp")-value(object) into SharedPreference
     *
     * @param value value of this pair ,with the defalut key="temp"
     * @return true if add successfully
     */
    public boolean put(Serializable value) {
        return put("temp", value);
    }

    /**
     * get value(Serializable Object) from DataPool(SharedPreference) with the
     * given Key
     *
     * @param key key of this pair ,with the defalut key="temp"
     * @return one Serializable Object
     */
    public Serializable get(String key) {
        if (!contains(key))
            return null;
        String base64String = sharedPreferences.getString(key, "");
        byte[] buf = Base64.decode(base64String, Base64.DEFAULT);
        ByteArrayInputStream bis = new ByteArrayInputStream(buf);
        ObjectInputStream ois = null;
        Serializable result = null;
        try {
            ois = new ObjectInputStream(bis);
            result = (Serializable) ois.readObject();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return result;
    }

    /**
     * get value(String) from DataPool(SharedPreference) with the
     * given Key
     *
     * @param key
     * @return String,if key not exist return "" string
     */
    public String getString(String key) {
         return sharedPreferences.getString(key,null);
    }

    /**
     * get value(long) from DataPool(SharedPreference) with the
     * given Key
     *
     * @param key
     * @return String,if key not exist return 0 long
     */
    public long getLong(String key) {
         return sharedPreferences.getLong(key,0);
    }


    public int getInt(String key) {
        return sharedPreferences.getInt(key,99);
    }

    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key,true);
    }

    /**
     * check if DataCache(SharedPreference) contain the given key
     *
     * @param key the given key
     * @return true if it contains
     */
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    /**
     * check if DataCache(SharedPreference) is empty
     *
     * @return true if it's empty
     */
    public boolean isEmpty() {
        return sharedPreferences.getAll().size() == 0;
    }

    /**
     * remove a key-value of this pair
     *
     * @param key the key of this pair
     * @return true if it removes successfully
     */
    @SuppressLint("NewApi") public void remove(String key) {
        if (!contains(key))
            return;
        sharedPreferences.edit().remove(key).apply();
    }

    /**
     * remove all the key-value of this pair
     *
     * @return
     */
    public void removeAll() {
        if (isEmpty())
            return;
        Map<String, ?> map = sharedPreferences.getAll();
        for (String key : map.keySet()) {
            remove(key);
        }
    }

    /**
     * update the key-value
     *
     * @param key   key of this pair
     * @param value value of this pair
     * @return true if set successfully
     */
    public boolean set(String key, Serializable value) {
        if (!contains(key))
            return false;
        return put(key, value);
    }

    /**
     * add a key(String)-set(string) into SharedPreference
     * @param key
     * @param set
     */
    @SuppressLint("NewApi") public void setSharepreferenceSet(String key, Set<String> set) {

        sharedPreferences.edit().putStringSet(key, set).commit();
    }
    /**
     * get value(String) from DataCache(SharedPreference) with the
     * given Key
     *
     * @param key
     * @return String,if key not exist return "" set
     */
    @SuppressLint("NewApi") public Set<String> getSet(String key) {
        Set<String> set = new HashSet<String>();
        return sharedPreferences.getStringSet(key, set);
    }
}
