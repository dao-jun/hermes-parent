package org.aries.middleware.hermes.common.pojo;

import lombok.AllArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
public class Tuple2<K, V> {
    public final K f1;
    public final V f2;

    public static <K, V> Tuple2<K, V> of(K f1, V f2) {
        return new Tuple2<>(f1, f2);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
        return f1.equals(tuple2.f1) && f2.equals(tuple2.f2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(f1, f2);
    }
}
