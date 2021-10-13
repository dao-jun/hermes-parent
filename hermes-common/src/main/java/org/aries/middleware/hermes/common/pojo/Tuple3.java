package org.aries.middleware.hermes.common.pojo;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class Tuple3<T, K, V> {
    public final T f1;
    public final K f2;
    public final V f3;

    public static <T, K, V> Tuple3<T, K, V> of(T f1, K f2, V f3) {

        return new Tuple3<>(f1, f2, f3);
    }
}
