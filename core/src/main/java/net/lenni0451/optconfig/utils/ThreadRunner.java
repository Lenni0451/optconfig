package net.lenni0451.optconfig.utils;

import lombok.SneakyThrows;

import java.lang.reflect.Method;

public class ThreadRunner {

    private static final ThreadStarter STARTER;

    static {
        ThreadStarter starter;
        try {
            Method ofVirtual = Thread.class.getMethod("ofVirtual");
            Method name = ofVirtual.getReturnType().getMethod("name", String.class);
            Method start = name.getReturnType().getMethod("start", Runnable.class);
            starter = (r, n) -> {
                Object virtualThreadBuilder = ofVirtual.invoke(null);
                Object namedBuilder = name.invoke(virtualThreadBuilder, n);
                return (Thread) start.invoke(namedBuilder, r);
            };
        } catch (Throwable t) {
            t.printStackTrace();
            starter = (runnable, name) -> {
                Thread thread = new Thread(runnable, name);
                thread.setDaemon(true);
                thread.start();
                return thread;
            };
        }
        STARTER = starter;
    }

    @SneakyThrows
    public static Thread startThread(final Runnable runnable, final String name) {
        return STARTER.start(runnable, name);
    }


    @FunctionalInterface
    private interface ThreadStarter {
        Thread start(final Runnable runnable, final String name) throws Throwable;
    }

}
