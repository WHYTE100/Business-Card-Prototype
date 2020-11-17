package com.pridetechnologies.businesscard;

import android.graphics.Color;

public enum PasswordStrength {

    // we use some color in green tint =>
    //more secure is the password, more darker is the color associated
    WEAK(R.string.weak, Color.parseColor("#FC1908")),
    MEDIUM(R.string.good, Color.parseColor("#FCB001")),
    STRONG(R.string.strong, Color.parseColor("#74FF64")),
    VERY_STRONG(R.string.very_strong, Color.parseColor("#00BE00"));

    public int msg;
    public int color;
    private static int MIN_LENGTH = 8;
    private static int MAX_LENGTH = 15;

    PasswordStrength(int msg, int color) {
        this.msg = msg;
        this.color = color;
    }

    public static PasswordStrength calculate(String password) {
        int score = 0;
        // boolean indicating if password has an upper case
        boolean upper = false;
        // boolean indicating if password has a lower case
        boolean lower = false;
        // boolean indicating if password has at least one digit
        boolean digit = false;
        // boolean indicating if password has a leat one special char

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);

            if (!digit  &&  Character.isDigit(c)) {
                score++;
                digit = true;
            } else {
                if (!upper || !lower) {
                    if (Character.isUpperCase(c)) {
                        upper = true;
                    } else {
                        lower = true;
                    }

                    if (upper && lower) {
                        score++;
                    }
                }
            }
        }

        int length = password.length();

        if (length > MAX_LENGTH) {
            score++;
        } else if (length < MIN_LENGTH) {
            score = 0;
        }

        // return enum following the score
        switch(score) {
            case 0 : return WEAK;
            case 1 : return MEDIUM;
            case 2 : return STRONG;
            case 3 : return VERY_STRONG;
            default:
        }

        return VERY_STRONG;
    }
}
