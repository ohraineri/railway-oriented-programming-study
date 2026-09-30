package me.raineri;

import java.util.Optional;

public class Main {
    public static String f(String s) {
        return (s.charAt(0) == 'a') ? null : s;
    }

    public static char g(String s) {
        return s.charAt(0);
    }

    static void main() {
        var test = Optional.ofNullable("big")
                .map(Main::f)
                .map(Main::g);

        System.out.println(test);
    }
}
