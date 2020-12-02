package hzhl.net.hlcall.utils;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;
import java.util.Map;

/**
 * create by elileo on 2018/5/29
 */
public class WK {
    /**
     * Test if a collection is either NIL or empty
     */
    public static boolean empty(Collection<?> xs) {
        return xs == null || xs.isEmpty();
    }

    /**
     * Test if an array is either NIL or empty
     */
    public static <T> boolean empty(T[] xs) {
        return xs == null || xs.length == 0;
    }

    public static boolean empty(SparseArray<?> xs) {
        return xs == null || xs.size() == 0;
    }

    public static boolean empty(int[] xs) {
        return xs == null || xs.length == 0;
    }

    /**
     * Test if a abstract string is either NIL or empty
     */
    public static boolean empty(CharSequence s) {
        return s == null || s.length() == 0;
    }

    public static boolean empty(Map<?, ?> m) {
        return m == null || m.isEmpty();
    }

    public static boolean eq(Object a, Object b){
        return a == null && b == null ? true : (a == null ? false : a.equals(b));
    }

    public static View inflate(Context context, int layoutId) {
        return inflate(context, layoutId, (ViewGroup)null);
    }

    public static View inflate(Context context, int layoutId, ViewGroup root) {
        return inflate(context, layoutId, root, root != null);
    }

    public static View inflate(Context context, int layoutId, ViewGroup root, boolean attachToRoot) {
        LayoutInflater LayoutInflater = (android.view.LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(LayoutInflater == null) {
            throw new AssertionError("LayoutInflater not found.");
        } else {
            return LayoutInflater.inflate(layoutId, root, attachToRoot);
        }
    }
}
