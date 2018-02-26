package com.github.bohnman.core.bean;

import com.github.bohnman.core.collect.CoreIterables;
import com.github.bohnman.core.convert.CoreConversions;
import com.github.bohnman.core.lang.CoreMethods;
import com.github.bohnman.core.lang.array.CoreArrayWrapper;
import com.github.bohnman.core.lang.array.CoreArrays;
import com.github.bohnman.core.range.CoreIntRange;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public class CoreBeans {

    private CoreBeans() {
    }


    public static Stream<PropertyDescriptor> getReadablePropertyDescriptors(Class<?> beanClass) {
        try {
            return Arrays.stream(Introspector.getBeanInfo(beanClass).getPropertyDescriptors())
                    .filter(propertyDescriptor -> propertyDescriptor.getReadMethod() != null)
                    .filter(propertyDescriptor -> !propertyDescriptor.getName().equals("class"));
        } catch (IntrospectionException e) {
            return Stream.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static Object getProperty(Object o, Object key) {
        if (o == null || key == null) {
            return null;
        }

        if (o.getClass().isArray()) {
            CoreArrayWrapper wrapper = CoreArrays.wrap(o);

            if (key instanceof Number) {
                int index = CoreArrays.normalizeIndex(((Number) key).intValue(), wrapper.size(), -1, wrapper.size() - 1);
                return index < 0 ? null : wrapper.get(index);
            }

            if (key instanceof CoreIntRange) {
                CoreIntRange range = ((CoreIntRange) key).toExclusive().normalize(wrapper.size());
                return range.isEmpty() ? null : wrapper.slice(range.getStart(), range.getEnd());
            }
        }

        if (o instanceof Iterable) {
            Iterable iterable = (Iterable) o;
            int size = CoreIterables.size(iterable);

            if (key instanceof Number) {
                int index = CoreArrays.normalizeIndex(((Number) key).intValue(), size, -1, size - 1);
                return index < 0 ? null : CoreIterables.get(iterable, index);
            }

            if (key instanceof CoreIntRange) {
                CoreIntRange range = ((CoreIntRange) key).toExclusive().normalize(size);
                return range.isEmpty() ? null : CoreIterables.slice(iterable, range.getStart(), range.getEnd());
            }
        }

        String keyString = CoreConversions.toString(key);

        if (o instanceof Map) {
            return ((Map) o).get(keyString);
        }


        return getReadablePropertyDescriptors(o.getClass())
                .filter(propertyDescriptor -> propertyDescriptor.getName().equals(keyString))
                .map(propertyDescriptor -> CoreMethods.invoke(propertyDescriptor.getReadMethod(), o))
                .findFirst()
                .orElse(null);

    }

}
