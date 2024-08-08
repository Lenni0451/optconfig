package net.lenni0451.optconfig.utils;

import net.lenni0451.optconfig.ConfigLoader;

public class ReflectionUtils {

    public static <T> T instantiate(final ConfigLoader<?> configLoader, final Class<?> type) {
        return configLoader.getConfigOptions().getClassAccessFactory().create(type).getConstructor().castInstance();
    }

    @Deprecated //Not for removal, but dangerous
    public static <T> T unsafeCast(final Object o) {
        //Not really reflection, but surely fits here
        return (T) o;
    }

}
