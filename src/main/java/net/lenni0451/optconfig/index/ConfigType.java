package net.lenni0451.optconfig.index;

import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@ApiStatus.Internal
public enum ConfigType {

    STATIC {
        @Override
        public boolean matches(Field field) {
            return Modifier.isStatic(field.getModifiers());
        }
    },
    INSTANCED {
        @Override
        public boolean matches(Field field) {
            return !Modifier.isStatic(field.getModifiers());
        }
    };

    public abstract boolean matches(final Field field);

}
