package net.lenni0451.optconfig.index.dummy;

import net.lenni0451.optconfig.annotations.Description;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.Annotation;

@ApiStatus.Internal
public class DummyDescription implements Description {

    private final String[] value;

    public DummyDescription(final String... value) {
        this.value = value;
    }

    @Override
    public String[] value() {
        return this.value;
    }

    @Override
    public String generator() {
        return "";
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return DummyDescription.class;
    }

}
