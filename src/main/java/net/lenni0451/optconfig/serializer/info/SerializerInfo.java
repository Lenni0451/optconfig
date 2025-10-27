package net.lenni0451.optconfig.serializer.info;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;
import net.lenni0451.optconfig.serializer.TypeSerializerList;

@With
@Getter
@RequiredArgsConstructor
@Accessors(fluent = true)
public class SerializerInfo<C, T> {

    private final TypeSerializerList<C> typeSerializers;
    private final Class<T> type;
    private final T value;

}
