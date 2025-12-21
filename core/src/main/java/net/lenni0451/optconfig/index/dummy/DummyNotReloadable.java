package net.lenni0451.optconfig.index.dummy;

import net.lenni0451.optconfig.annotations.NotReloadable;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.Annotation;

@ApiStatus.Internal
public class DummyNotReloadable implements NotReloadable {

    @Override
    public Class<? extends Annotation> annotationType() {
        return DummyNotReloadable.class;
    }

}
