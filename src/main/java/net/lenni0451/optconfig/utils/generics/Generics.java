package net.lenni0451.optconfig.utils.generics;

import javax.annotation.Nullable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class Generics {

    @Nullable
    public static Class<?> resolveTypeToClass(final Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
        // Cannot get a raw class from a TypeVariable or Wildcard
        return null;
    }

    @Nullable
    public static Type getArrayComponentGenericType(@Nullable final Type arrayGenericType) {
        if (arrayGenericType instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) arrayGenericType;
            return genericArrayType.getGenericComponentType();
        }
        return null;
    }

    @Nullable
    public static Type getListEntryGenericType(@Nullable final Type listGenericType) {
        Type[] resolvedGenerics = GenericResolver.getResolvedGenerics(listGenericType, List.class);
        if (resolvedGenerics != null && resolvedGenerics.length == 1) {
            return resolvedGenerics[0];
        }
        return null;
    }

    @Nullable
    public static Type getMapKeyGenericType(@Nullable final Type mapGenericType) {
        Type[] resolvedGenerics = GenericResolver.getResolvedGenerics(mapGenericType, Map.class);
        if (resolvedGenerics != null && resolvedGenerics.length == 2) {
            return resolvedGenerics[0];
        }
        return null;
    }

    @Nullable
    public static Type getMapValueGenericType(@Nullable final Type mapGenericType) {
        Type[] resolvedGenerics = GenericResolver.getResolvedGenerics(mapGenericType, Map.class);
        if (resolvedGenerics != null && resolvedGenerics.length == 2) {
            return resolvedGenerics[1];
        }
        return null;
    }

}
