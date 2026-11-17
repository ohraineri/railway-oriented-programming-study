package me.raineri.rop;

import java.util.function.Function;

public sealed interface Result<T, E> {

    record Success<T, E>(T value) implements Result<T, E> {}
    record Failure<T, E>(E error) implements Result<T, E> {}

    static <T, E> Result<T, E> ok(T value) { return new Success<>(value); }
    static <T, E> Result<T, E> fail(E error) { return new Failure<>(error); }

    // map: transforma o valor se estiver no trilho de sucesso
    default <U> Result<U, E> map(Function<T, U> fn) {
        return switch (this) {
            case Success<T, E> s -> ok(fn.apply(s.value()));
            case Failure<T, E> f -> fail(f.error());
        };
    }

    // flatMap (bind): encadeia um passo que também pode falhar
    default <U> Result<U, E> flatMap(Function<T, Result<U, E>> fn) {
        return switch (this) {
            case Success<T, E> s -> fn.apply(s.value());
            case Failure<T, E> f -> fail(f.error());
        };
    }

    default <R> R fold(Function<T, R> onSuccess, Function<E, R> onFailure) {
        return switch (this) {
            case Success<T, E> s -> onSuccess.apply(s.value());
            case Failure<T, E> f -> onFailure.apply(f.error());
        };
    }
}
