package net.lenni0451.optconfig.utils.generics;

import javax.annotation.Nullable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * AI generated class to resolve generic types at runtime.
 */
public class GenericResolver {

    public static Type[] getResolvedGenerics(@Nullable final Type startingType, @Nullable final Class<?> targetClass) {
        if (targetClass == null) return null;
        if (startingType == null) return null;

        Map<TypeVariable<?>, Type> context = new HashMap<>();
        return resolveRecursive(startingType, targetClass, context);
    }

    private static Type[] resolveRecursive(Type currentType, Class<?> targetClass, Map<TypeVariable<?>, Type> context) {
        if (currentType == null) return null; // End of chain

        // 1. Get the raw class for the current type
        Class<?> rawClass = Generics.resolveTypeToClass(currentType);
        if (rawClass == null) return null;

        // 2. If this is a ParameterizedType, update the context map.
        if (currentType instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) currentType;
            Type[] actualArgs = pType.getActualTypeArguments();
            TypeVariable<?>[] typeParams = rawClass.getTypeParameters();

            // Map type parameters to their actual, resolved types
            // e.g., For List<String>, map E -> String.class
            for (int i = 0; i < actualArgs.length && i < typeParams.length; i++) {
                // We must resolve the argument *before* putting it in the map,
                // in case it's a variable from an outer scope (e.g., List<E>)
                context.put(typeParams[i], resolve(actualArgs[i], context));
            }
        }

        // 3. Base Case: We found the target class!
        if (targetClass.equals(rawClass)) {
            // We found it, but it might not be parameterized in this context
            if (!(currentType instanceof ParameterizedType)) {
                // e.g., looking for List.class, found 'class MyList implements List' (not List<E>)
                // Return its defined type parameters if it has any, otherwise empty.
                // Or just return empty, as they are not resolved.
                return rawClass.getTypeParameters().length == 0 ? new Type[0] : rawClass.getTypeParameters();
            }

            // It's parameterized. Resolve its arguments against the context.
            ParameterizedType pTargetType = (ParameterizedType) currentType;
            Type[] args = pTargetType.getActualTypeArguments();
            Type[] resolvedArgs = new Type[args.length];
            for (int i = 0; i < args.length; i++) {
                resolvedArgs[i] = resolve(args[i], context);
            }
            return resolvedArgs;
        }

        // 4. Recursive Step: Check superclass
        Type genericSuper = rawClass.getGenericSuperclass();
        Type[] result = resolveRecursive(genericSuper, targetClass, new HashMap<>(context)); // Use copy of context
        if (result != null) {
            return result;
        }

        // 5. Recursive Step: Check interfaces
        for (Type genericInterface : rawClass.getGenericInterfaces()) {
            result = resolveRecursive(genericInterface, targetClass, new HashMap<>(context)); // Use copy of context
            if (result != null) {
                return result;
            }
        }

        // 6. Not found in this branch
        return null;
    }

    private static Type resolve(final Type type, final Map<TypeVariable<?>, Type> context) {
        if (type instanceof TypeVariable<?>) {
            // This is a variable (e.g., E). See if we've resolved it.
            // Loop to handle chained resolutions (e.g., K -> V, V -> String)
            Type mapped = type;
            while (mapped instanceof TypeVariable<?>) {
                Type next = context.get(mapped);
                if (next == null) {
                    return mapped; // Unresolved variable
                }
                mapped = next;
            }
            return mapped;
        }
        if (type instanceof ParameterizedType) {
            // This is a generic type (e.g., List<E>). Resolve its arguments.
            ParameterizedType pType = (ParameterizedType) type;
            Type[] args = pType.getActualTypeArguments();
            Type[] resolvedArgs = new Type[args.length];
            boolean changed = false;

            for (int i = 0; i < args.length; i++) {
                resolvedArgs[i] = resolve(args[i], context);
                if (resolvedArgs[i] != args[i]) {
                    changed = true;
                }
            }

            // If any argument changed, create a new ParameterizedType
            if (changed) {
                return new ParameterizedTypeImpl(pType.getRawType(), resolvedArgs, pType.getOwnerType());
            }
            return type; // No changes
        }
        if (type instanceof GenericArrayType) {
            // This is a generic array (e.g., E[]). Resolve its component type.
            GenericArrayType gType = (GenericArrayType) type;
            Type comp = gType.getGenericComponentType();
            Type resolvedComp = resolve(comp, context);

            if (resolvedComp != comp) {
                return new GenericArrayTypeImpl(resolvedComp);
            }
            return type; // No changes
        }

        // Other types (Class, WildcardType) are returned as-is
        return type;
    }

}
