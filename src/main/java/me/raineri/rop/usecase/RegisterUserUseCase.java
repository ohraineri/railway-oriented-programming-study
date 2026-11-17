package me.raineri.rop.usecase;

import me.raineri.rop.Result;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RegisterUserUseCase {

    // ---- Entrada, saída e erros de domínio ----
    public record Command(String name, String email, String password) {}
    public record User(String id, String name, String email, String passwordHash) {}

    public sealed interface RegisterError {
        record InvalidName(String reason) implements RegisterError {}
        record InvalidEmail(String reason) implements RegisterError {}
        record WeakPassword(String reason) implements RegisterError {}
        record EmailAlreadyUsed(String email) implements RegisterError {}
    }

    // "Banco de dados" em memória, só para o exemplo
    private final Set<String> emailsInUse = new HashSet<>();

    // ---- O trilho: cada passo recebe o resultado do anterior ----
    public Result<User, RegisterError> execute(Command cmd) {
        return validateName(cmd)
                .flatMap(this::validateEmail)
                .flatMap(this::validatePassword)
                .flatMap(this::ensureEmailIsUnique)
                .map(this::toUser)
                .flatMap(this::save);
    }

    private Result<Command, RegisterError> validateName(Command c) {
        return c.name() == null || c.name().isBlank()
                ? Result.fail(new RegisterError.InvalidName("nome é obrigatório"))
                : Result.ok(c);
    }

    private Result<Command, RegisterError> validateEmail(Command c) {
        return c.email() == null || !c.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
                ? Result.fail(new RegisterError.InvalidEmail("formato inválido: " + c.email()))
                : Result.ok(c);
    }

    private Result<Command, RegisterError> validatePassword(Command c) {
        return c.password() == null || c.password().length() < 8
                ? Result.fail(new RegisterError.WeakPassword("mínimo de 8 caracteres"))
                : Result.ok(c);
    }

    private Result<Command, RegisterError> ensureEmailIsUnique(Command c) {
        return emailsInUse.contains(c.email().toLowerCase())
                ? Result.fail(new RegisterError.EmailAlreadyUsed(c.email()))
                : Result.ok(c);
    }

    private User toUser(Command c) {
        // em produção: BCrypt/Argon2 no lugar do hashCode
        return new User(UUID.randomUUID().toString(), c.name(), c.email().toLowerCase(),
                "hash:" + c.password().hashCode());
    }

    private Result<User, RegisterError> save(User u) {
        emailsInUse.add(u.email());
        return Result.ok(u);
    }
}
