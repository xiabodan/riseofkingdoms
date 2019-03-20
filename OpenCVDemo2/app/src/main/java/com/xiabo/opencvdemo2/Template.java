package com.xiabo.opencvdemo2;

import java.util.HashMap;
import java.util.Map;

public class Template {
    private static final Map<String, String> matchs = new HashMap<String, String>();

    static {
        matchs.put("_1_1.png", "1.png");
        matchs.put("_2_1.png", "2.png");
        matchs.put("_3_1.png", "3.png");
        matchs.put("_3_2.png", "3.png");
        matchs.put("_3_3.png", "3.png");
        matchs.put("_3_4.png", "3.png");
        matchs.put("_3_5.png", "3.png");
        matchs.put("_3_6.png", "3.png");
        matchs.put("_3_7.png", "3.png");
        matchs.put("_3_8.png", "3.png");
        matchs.put("_4_1.png", "4.png");
        matchs.put("_4_2.png", "4.png");
        matchs.put("_5_1.png", "5.png");
        matchs.put("_5_2.png", "5.png");
        matchs.put("_6_1.png", "6.png");
        matchs.put("_7_1.png", "7.png");
        matchs.put("_8_1.png", "8.png");
        matchs.put("_9_1.png", "9.png");
    }

    public static Map<String, String> getMatchs() {
        return matchs;
    }
}
