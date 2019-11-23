package com.sdm.BkParser.SupportClasses;

import java.util.HashMap;
import java.util.Set;

public class HashmapForRest<K,V> extends HashMap {


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        Set keys = keySet();
        for (Object key : keys) {
           sb.append("\"");
           sb.append(key);
           sb.append("\"");
           sb.append(":");
            sb.append(get(key));

            sb.append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append('}');
        return sb.toString();
    }
}
