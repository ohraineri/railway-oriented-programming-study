package me.raineri;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public sealed interface Result<S, F> permits Success, Failure {
    @FunctionalInterface
    interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    static <S, F> Result<S, F> success(S value) {
        return new Success<>(value);
    }

    static <S, F> Result<S, F> failure(F error) {
        return new Failure<>(error);
    }


    static <F> Result<Unit, F> done() {
        return new Success<Unit, F>(Unit.INSTANCE);
    }

    static <S, F> Result<S, F> attempt(CheckedSupplier<S> block, Function<Exception, F> onThrow) {
        try {
            return new Success<>(block.get());
        } catch (Exception exception) {
            return new Failure<>(onThrow.apply(exception));
        }
    }

    default <T> Result<T, F> map(Function<? super S, ? extends T> mapper) {
        return switch (this) {
            case Success<S, F> success -> new Success<>(mapper.apply(success.value()));
            case Failure<S, F> failure -> failure.retypeSuccess();
        };
    }

    default <T> Result<T, F> flatMap(Function<? super S, ? extends Result<T, F>> binder) {
        return switch (this) {
            case Success<S, F> success -> binder.apply(success.value());
            case Failure<S, F> failure -> failure.retypeSuccess();
        };
    }


    default <G> Result<S, G> mapError(Function<? super F, ? extends G> mapper) {
        return switch (this) {
            case Success<S, F> success -> success.retypeError();
            case Failure<S, F> failure -> new Failure<>(mapper.apply(failure.error()));
        };
    }


    default Result<S, F> ensure(Predicate<? super S> invariant, Function<? super S, ? extends F> onViolation) {
        return switch (this) {
            case Success<S, F> success -> invariant.test(success.value())
                    ? this
                    : new Failure<S, F>(onViolation.apply(success.value()));
            case Failure<S, F> _ -> this;
        };
    }


    default Result<S, F> peek(Consumer<? super S> action) {
        if (this instanceof Success<S, F> success) {
            action.accept(success.value());
        }
        return this;
    }


    default Result<S, F> peekError(Consumer<? super F> action) {
        if (this instanceof Failure<S, F> failure) {
            action.accept(failure.error());
        }
        return this;
    }


    default Result<S, F> recover(Function<? super F, ? extends Result<S, F>> fallback) {
        return switch (this) {
            case Success<S, F> _ -> this;
            case Failure<S, F> failure -> fallback.apply(failure.error());
        };
    }

    default <T> T fold(Function<? super S, ? extends T> onSuccess, Function<? super F, ? extends T> onFailure) {
        return switch (this) {
            case Success<S, F> success -> onSuccess.apply(success.value());
            case Failure<S, F> failure -> onFailure.apply(failure.error());
        };
    }

    default boolean isSuccess() {
        return this instanceof Success<S, F>;
    }

    @SafeVarargs
    static <F> Result<Unit, List<F>> combine(Result<?, F>... results) {
        List<F> errors = new ArrayList<>();
        for (Result<?, F> result : results) {
            result.peekError(errors::add);
        }
        return errors.isEmpty()
                ? new Success<Unit, List<F>>(Unit.INSTANCE)
                : new Failure<Unit, List<F>>(List.copyOf(errors));
    }

    static <T, S, F> Result<List<S>, F> traverse(Collection<T> items, Function<? super T, ? extends Result<S, F>> step) {
        List<S> collected = new ArrayList<>(items.size());
        for (T item : items) {
            Result<S, F> result = step.apply(item);
            switch (result) {
                case Success<S, F> success -> collected.add(success.value());
                case Failure<S, F> failure -> {
                    return new Failure<>(failure.error());
                }
            }
        }
        return new Success<>(List.copyOf(collected));
    }
}