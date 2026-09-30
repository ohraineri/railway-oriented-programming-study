package me.raineri;

import java.util.Objects;

public record Success<S, F>(S value) implements Result<S, F> {

    public Success {
        Objects.requireNonNull(value, "The Success value cannot be null use Result.done() for empty.");
    }

    @SuppressWarnings("unchecked")
    <G> Success<S, G> retypeError() {
        return (Success<S, G>) this;
    }
}