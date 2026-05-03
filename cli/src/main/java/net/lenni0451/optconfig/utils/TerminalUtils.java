package net.lenni0451.optconfig.utils;

import java.lang.reflect.Method;

public class TerminalUtils {

    public static int getTerminalWidth() {
        try {
            Class<?> ansiConsole = Class.forName("org.jline.jansi.AnsiConsole");
            Method getTerminalWidth = ansiConsole.getDeclaredMethod("getTerminalWidth");
            int width = (int) getTerminalWidth.invoke(null);
            if (width > 0) {
                return width;
            }
        } catch (Throwable ignored) {
        }
        return Integer.MAX_VALUE;
    }

}
