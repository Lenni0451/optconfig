package net.lenni0451.optconfig.utils.generics;

import javax.annotation.Nullable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for generics handling.
 */
public class Generics {

    /**
     * Resolve a Type to its raw Class, if possible.
     *
     * @param type The type to resolve
     * @return The raw class, or null if it cannot be resolved
     */
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

    /**
     * Get the component generic type of an array generic type.
     *
     * @param arrayGenericType The array generic type
     * @return The component generic type, or null if not applicable
     */
    @Nullable
    public static Type getArrayComponentGenericType(@Nullable final Type arrayGenericType) {
        if (arrayGenericType instanceof GenericArrayType genericArrayType) {
            return genericArrayType.getGenericComponentType();
        }
        return null;
    }

    /**
     * Get the entry generic type of a list generic type.
     *
     * @param listGenericType The list generic type
     * @return The entry generic type, or null if not applicable
     */
    @Nullable
    public static Type getListEntryGenericType(@Nullable final Type listGenericType) {
        Type[] resolvedGenerics = GenericResolver.getResolvedGenerics(listGenericType, List.class);
        if (resolvedGenerics != null && resolvedGenerics.length == 1) {
            return resolvedGenerics[0];
        }
        return null;
    }

    /**
     * Get the key generic type of a map generic type.
     *
     * @param mapGenericType The map key generic type
     * @return The key generic type, or null if not applicable
     */
    @Nullable
    public static Type getMapKeyGenericType(@Nullable final Type mapGenericType) {
        Type[] resolvedGenerics = GenericResolver.getResolvedGenerics(mapGenericType, Map.class);
        if (resolvedGenerics != null && resolvedGenerics.length == 2) {
            return resolvedGenerics[0];
        }
        return null;
    }

    /**
     * Get the value generic type of a map generic type.
     *
     * @param mapGenericType The map value generic type
     * @return The value generic type, or null if not applicable
     */
    @Nullable
    public static Type getMapValueGenericType(@Nullable final Type mapGenericType) {
        Type[] resolvedGenerics = GenericResolver.getResolvedGenerics(mapGenericType, Map.class);
        if (resolvedGenerics != null && resolvedGenerics.length == 2) {
            return resolvedGenerics[1];
        }
        return null;
    }

}
