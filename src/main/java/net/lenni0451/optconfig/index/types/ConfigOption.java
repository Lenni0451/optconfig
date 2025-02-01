package net.lenni0451.optconfig.index.types;

import lombok.ToString;
import net.lenni0451.optconfig.ConfigLoader;
import net.lenni0451.optconfig.access.types.ClassAccess;
import net.lenni0451.optconfig.access.types.ConstructorAccess;
import net.lenni0451.optconfig.access.types.FieldAccess;
import net.lenni0451.optconfig.access.types.MethodAccess;
import net.lenni0451.optconfig.annotations.*;
import net.lenni0451.optconfig.exceptions.InvalidDescriptionGeneratorException;
import net.lenni0451.optconfig.serializer.ConfigTypeSerializer;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static net.lenni0451.optconfig.utils.ReflectionUtils.unsafeCast;

@ToString
@ApiStatus.Internal
public class ConfigOption {

    private static String[] getDescription(final String option, @Nullable final Description description, @Nullable final ClassAccess classAccess) {
        if (description == null) return new String[0];
        List<String> descriptionList = new ArrayList<>();
        Collections.addAll(descriptionList, description.value());
        if (!description.generator().isEmpty() && classAccess != null) {
            MethodAccess generator = classAccess.tryGetMethod(description.generator(), String[].class);
            if (generator == null) {
                throw new InvalidDescriptionGeneratorException(classAccess.getClazz(), option, description.generator(), "the method does not exist or has the wrong signature");
            }
            if (!Modifier.isStatic(generator.getModifiers())) {
                throw new InvalidDescriptionGeneratorException(classAccess.getClazz(), option, description.generator(), "the method is not static");
            }
            String[] generatedDescription = (String[]) generator.invoke(null);
            if (generatedDescription != null) Collections.addAll(descriptionList, generatedDescription);
        }
        return descriptionList.toArray(new String[0]);
    }


    private final FieldAccess fieldAccess;
    private final String name;
    private final String[] description;
    private final boolean reloadable;
    private final Class<? extends ConfigTypeSerializer<?, ?>> typeSerializer;
    private final boolean hidden;
    private final int order;
    @Nullable
    private final MethodAccess validator;
    private final String[] dependencies;

    public ConfigOption(final FieldAccess fieldAccess, final Option option, @Nullable final Description description, @Nullable final NotReloadable notReloadable, @Nullable final TypeSerializer typeSerializer, @Nullable final Hidden hidden, @Nullable Order order, final Map<String, MethodAccess> validatorMethods, @Nullable final ClassAccess classAccess) {
        this.fieldAccess = fieldAccess;
        this.name = option.value().isEmpty() ? fieldAccess.getName() : option.value();
        this.description = getDescription(this.name, description, classAccess);
        this.reloadable = notReloadable == null;
        this.typeSerializer = typeSerializer == null ? null : unsafeCast(typeSerializer.value());
        this.hidden = hidden != null;
        this.order = order == null ? -1 : Math.max(0, order.value());
        this.validator = validatorMethods.remove(this.getName());
        this.dependencies = option.dependencies();
    }

    public FieldAccess getFieldAccess() {
        return this.fieldAccess;
    }

    public String getName() {
        return this.name;
    }

    public String[] getDescription() {
        return this.description;
    }

    public boolean isReloadable() {
        return this.reloadable;
    }

    public Class<? extends ConfigTypeSerializer<?, ?>> getTypeSerializer() {
        return this.typeSerializer;
    }

    public <C, T> ConfigTypeSerializer<C, T> createTypeSerializer(final ConfigLoader<C> configLoader, final Class<C> configClass, final C configInstance) {
        if (this.typeSerializer == null) {
            return unsafeCast(configLoader.getTypeSerializers().get(configInstance, this.fieldAccess.getType()));
        } else {
            ClassAccess classAccess = configLoader.getConfigOptions().getClassAccessFactory().create(this.typeSerializer);
            Class<?> currentClass = configClass;
            ConstructorAccess constructor;
            do {
                //Try to get the constructor for the config class or its super classes
                constructor = classAccess.tryGetConstructor(currentClass);
            } while (constructor == null && (currentClass = currentClass.getSuperclass()) != null);
            if (constructor == null) constructor = classAccess.tryGetConstructor(); //If no fitting constructor was found try to get the default constructor
            if (constructor == null) {
                //No constructor found
                throw new IllegalArgumentException("No config (" + configClass.getName() + ") constructor found for type serializer: " + this.typeSerializer.getName());
            }
            return unsafeCast(constructor.newInstance(configInstance));
        }
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public int getOrder() {
        return this.order;
    }

    @Nullable
    public MethodAccess getValidator() {
        return this.validator;
    }

    public String[] getDependencies() {
        return this.dependencies;
    }

}
