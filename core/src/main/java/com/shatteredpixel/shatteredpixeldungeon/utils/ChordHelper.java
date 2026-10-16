package com.shatteredpixel.shatteredpixeldungeon.utils;

import java.util.Random;

public final class ChordHelper {

    private static final Random rnd = new Random();

    // 12平均律
    private static float stToPitch(int semitone) {
        return (float) Math.pow(2, semitone / 12.0);
    }

    private static int rootOffset() {
        return rnd.nextInt(7) - 3;
    }

    public static float[] generateMajor7() {
        int root = rootOffset();
        float base = stToPitch(root);

        int[] chord = {0, 4, 7, 11};
        float[] pitches = new float[4];

        for (int i = 0; i < 4; i++) {
            pitches[i] = base * stToPitch(chord[i]);
        }

        return pitches;
    }

    public static void main(String[] args) {
        float[] c = generateMajor7();
        System.out.printf("%.4f %.4f %.4f %.4f%n", c[0], c[1], c[2], c[3]);
    }
}