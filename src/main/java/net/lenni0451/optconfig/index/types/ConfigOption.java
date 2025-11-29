package net.lenni0451.optconfig.index.types;

import lombok.Getter;
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

@Getter
@ToString
@ApiStatus.Internal
public class ConfigOption {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static String[] getDescription(final String option, @Nullable final Description description, @Nullable final ClassAccess classAccess) {
        if (description == null) return EMPTY_STRING_ARRAY;
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
        return descriptionList.toArray(EMPTY_STRING_ARRAY);
    }


    private final FieldAccess fieldAccess;
    private final String name;
    private final String[] description;
    private final boolean reloadable;
    private final Class<? extends ConfigTypeSerializer<?>> typeSerializer;
    private final boolean hidden;
    private final int order;
    private final String cliName;
    private final String[] cliAliases;
    private final boolean cliIgnored;
    @Nullable
    private final MethodAccess validator;
    private final String[] dependencies;

    public ConfigOption(final FieldAccess fieldAccess, final Option option, @Nullable final Description description, @Nullable final NotReloadable notReloadable, @Nullable final TypeSerializer typeSerializer, @Nullable final Hidden hidden, @Nullable Order order, @Nullable CLI cli, final Map<String, MethodAccess> validatorMethods, @Nullable final ClassAccess classAccess) {
        this.fieldAccess = fieldAccess;
        this.name = option.value().isEmpty() ? fieldAccess.getName() : option.value();
        this.description = getDescription(this.name, description, classAccess);
        this.reloadable = notReloadable == null;
        this.typeSerializer = typeSerializer == null ? null : unsafeCast(typeSerializer.value());
        this.hidden = hidden != null;
        this.order = order == null ? -1 : Math.max(0, order.value());
        this.cliName = (cli == null || cli.name().isEmpty()) ? this.name : cli.name();
        this.cliAliases = cli == null ? EMPTY_STRING_ARRAY : cli.aliases();
        this.cliIgnored = cli != null && cli.ignore();
        this.validator = validatorMethods.remove(this.getName());
        this.dependencies = option.dependencies();
    }

    public <C, T> ConfigTypeSerializer<T> createTypeSerializer(final ConfigLoader<C> configLoader) {
        if (this.typeSerializer == null) {
            return unsafeCast(configLoader.getTypeSerializers().get(this.fieldAccess.getType()));
        } else {
            ClassAccess classAccess = configLoader.getConfigOptions().getClassAccessFactory().create(this.typeSerializer);
            ConstructorAccess constructor = classAccess.tryGetConstructor();
            if (constructor == null) {
                throw new IllegalArgumentException("No void constructor found for type serializer: " + this.typeSerializer.getName());
            }
            return unsafeCast(constructor.newInstance());
        }
    }

}
