package me.raineri;

import java.util.Objects;

public record Failure<S, F>(F error) implements Result<S, F> {

    public Failure {
        Objects.requireNonNull(error, "The Failure error cannot be null.");
    }

    @SuppressWarnings("unchecked")
    <T> Failure<T, F> retypeSuccess() {
        return (Failure<T, F>) this;
    }
}