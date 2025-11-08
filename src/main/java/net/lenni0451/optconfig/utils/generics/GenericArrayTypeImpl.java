package net.lenni0451.optconfig.utils.generics;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
class GenericArrayTypeImpl implements GenericArrayType {

    private final Type genericComponentType;

    @Override
    public String toString() {
        return this.genericComponentType.getTypeName() + "[]";
    }

}
