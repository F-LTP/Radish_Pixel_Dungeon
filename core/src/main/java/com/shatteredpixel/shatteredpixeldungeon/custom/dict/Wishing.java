package com.shatteredpixel.shatteredpixeldungeon.custom.dict;

import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.HandAxe;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Quarterstaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shortsword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Spear;

import java.util.HashMap;

public class Wishing {

    public static HashMap<Float, Class<?>> hashMap;
    public static float min;
    public static boolean full;

    public static float getSimilarityRatio(String str, String target) {
//see https://blog.csdn.net/JavaReact/article/details/82144732
        int d[][]; // 矩阵
        int n = str.length();
        int m = target.length();
        int i; // 遍历str的
        int j; // 遍历target的
        char ch1; // str的
        char ch2; // target的
        int temp; // 记录相同字符,在某个矩阵位置值的增量,不是0就是1
        if (n == 0 || m == 0) {
            return 0;
        }
        d = new int[n + 1][m + 1];
        for (i = 0; i <= n; i++) { // 初始化第一列
            d[i][0] = i;
        }

        for (j = 0; j <= m; j++) { // 初始化第一行
            d[0][j] = j;
        }

        for (i = 1; i <= n; i++) { // 遍历str
            ch1 = str.charAt(i - 1);
            // 去匹配target
            for (j = 1; j <= m; j++) {
                ch2 = target.charAt(j - 1);
                if (ch1 == ch2 || ch1 == ch2 + 32 || ch1 + 32 == ch2) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                // 左边+1,上边+1, 左上角+temp取最小
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + temp);
            }
        }

        return (1 - (float) d[n][m] / Math.max(str.length(), target.length())) * 100F;
    }

    public static void checkSimilarity(String target) {
        min = 0.01f;
        full = false;

        float a;
        hashMap = new HashMap<>();

        for (String str : wishToCompare.keySet()) {
            a = getSimilarityRatio(str, target);

            if (a >= min) {
                if (full) {
                    hashMap.remove(min);
                } else {
                    if (hashMap.size() == 7) {
                        full = true;
                    }
                }
                hashMap.put(a,wishToCompare.get(str));
                min = a;
            }
        }
    }



    private static HashMap<String,Class<?>> wishToCompare = new HashMap();
    static {
//        wishToCompare.put("","");
        wishToCompare.put("短剑", Shortsword.class);
        wishToCompare.put("手斧", HandAxe.class);
        wishToCompare.put("长矛", Spear.class);
        wishToCompare.put("铁头棍", Quarterstaff.class);
        wishToCompare.put("长匕首", Dict.class);
    }

}

