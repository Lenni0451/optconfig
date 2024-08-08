package net.lenni0451.optconfig.annotations.internal;

import net.lenni0451.optconfig.annotations.NotReloadable;

import java.lang.annotation.Annotation;

public class DummyNotReloadable implements NotReloadable {

    @Override
    public Class<? extends Annotation> annotationType() {
        return DummyNotReloadable.class;
    }

}
