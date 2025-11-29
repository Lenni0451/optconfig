package net.lenni0451.optconfig.index.dummy;

import net.lenni0451.optconfig.annotations.CLI;

import java.lang.annotation.Annotation;

public class DummyCLI implements CLI {

    @Override
    public String name() {
        return "";
    }

    @Override
    public String[] aliases() {
        return new String[0];
    }

    @Override
    public boolean ignore() {
        return true;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return DummyCLI.class;
    }

}
