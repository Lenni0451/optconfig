package net.lenni0451.optconfig.utils;

import net.lenni0451.optconfig.ConfigLoader;
import net.lenni0451.optconfig.access.ClassAccessFactory;

public class ReflectionUtils {

    public static <T> T instantiate(final ConfigLoader<?> configLoader, final Class<?> type) {
        return instantiate(configLoader.getConfigOptions().getClassAccessFactory(), type);
    }

    public static <T> T instantiate(final ClassAccessFactory classAccessFactory, final Class<?> type) {
        return classAccessFactory.create(type).getConstructor().castInstance();
    }

    @Deprecated //Not for removal, but dangerous
    public static <T> T unsafeCast(final Object o) {
        //Not really reflection, but surely fits here
        return (T) o;
    }

}
