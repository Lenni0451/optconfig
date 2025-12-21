package net.lenni0451.optconfig.index;

import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Modifier;

@ApiStatus.Internal
public enum ConfigType {

    STATIC {
        @Override
        public boolean matches(int modifier) {
            return Modifier.isStatic(modifier);
        }
    },
    INSTANCED {
        @Override
        public boolean matches(int modifier) {
            return !Modifier.isStatic(modifier);
        }
    };

    public abstract boolean matches(final int modifier);

}
