package com.mateuyabar.android.pillownfc;

import java.util.Random;
/**
 * Created by User on 24-Jun-16.
 */
public class idrandom {
    private static Random random = new Random();

    public static String generateActivationCode(int length) {
        String code = new String("");
        for (int i = 0; i < length; i++) {
            code += (char) (random.nextInt(10) + '0');
        }
        return code;
    }
}
