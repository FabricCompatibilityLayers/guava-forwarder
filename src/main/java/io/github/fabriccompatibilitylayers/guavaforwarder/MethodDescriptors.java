package io.github.fabriccompatibilitylayers.guavaforwarder;

import java.lang.reflect.Method;

final class MethodDescriptors {
    private MethodDescriptors() {
    }

    static String descriptorOf(Method method) {
        return descriptorOf(method.getParameterTypes(), method.getReturnType());
    }

    static String descriptorOf(Class<?>[] parameterTypes, Class<?> returnType) {
        StringBuilder builder = new StringBuilder("(");
        for (Class<?> parameterType : parameterTypes) {
            builder.append(typeDescriptor(parameterType));
        }
        builder.append(")").append(typeDescriptor(returnType));
        return builder.toString();
    }

    private static String typeDescriptor(Class<?> type) {
        if (type == void.class) return "V";
        if (type == boolean.class) return "Z";
        if (type == byte.class) return "B";
        if (type == char.class) return "C";
        if (type == short.class) return "S";
        if (type == int.class) return "I";
        if (type == long.class) return "J";
        if (type == float.class) return "F";
        if (type == double.class) return "D";
        if (type.isArray()) return "[" + typeDescriptor(type.getComponentType());
        return "L" + type.getName().replace('.', '/') + ";";
    }
}
