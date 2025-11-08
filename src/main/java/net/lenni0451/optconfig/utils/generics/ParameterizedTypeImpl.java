package net.lenni0451.optconfig.utils.generics;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@EqualsAndHashCode
@RequiredArgsConstructor
class ParameterizedTypeImpl implements ParameterizedType {

    @Getter
    private final Type rawType;
    private final Type[] actualTypeArguments;
    @Getter
    private final Type ownerType;

    @Override
    public Type[] getActualTypeArguments() {
        return this.actualTypeArguments.clone();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.ownerType != null) {
            sb.append(this.ownerType.getTypeName()).append("$");
        }
        sb.append(this.rawType.getTypeName());
        if (this.actualTypeArguments.length > 0) {
            sb.append("<");
            for (int i = 0; i < this.actualTypeArguments.length; i++) {
                sb.append(i == 0 ? "" : ", ").append(this.actualTypeArguments[i].getTypeName());
            }
            sb.append(">");
        }
        return sb.toString();
    }

}
