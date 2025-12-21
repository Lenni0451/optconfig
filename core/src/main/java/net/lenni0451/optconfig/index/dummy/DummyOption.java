package net.lenni0451.optconfig.index.dummy;

import net.lenni0451.optconfig.annotations.Option;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.Annotation;

@ApiStatus.Internal
public class DummyOption implements Option {

    private final String value;

    public DummyOption(final String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String[] dependencies() {
        return new String[0];
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return DummyOption.class;
    }

}
