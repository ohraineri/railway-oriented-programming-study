package me.raineri;

import me.raineri.rop.usecase.RegisterUserUseCase;
import me.raineri.rop.usecase.RegisterUserUseCase.Command;
import me.raineri.rop.usecase.RegisterUserUseCase.RegisterError;

public class Main {
    static void main() {
        var useCase = new RegisterUserUseCase();

        var commands = new Command[]{
                new Command("Ana", "ana@email.com", "senha-forte-123"),   // ok
                new Command("Ana", "ana@email.com", "outra-senha-123"),  // email duplicado
                new Command("", "bob@email.com", "senha-forte-123"),     // nome inválido
                new Command("Bob", "bob-email", "senha-forte-123"),      // email inválido
                new Command("Bob", "bob@email.com", "123"),              // senha fraca
        };

        for (var cmd : commands) {
            String output = useCase.execute(cmd).fold(
                    user -> "✅ criado: " + user.email() + " (id=" + user.id() + ")",
                    Main::describe);
            IO.println(output);
        }
    }

    // Pattern matching exaustivo: o compilador garante que todos os erros são tratados
    private static String describe(RegisterError error) {
        return switch (error) {
            case RegisterError.InvalidName e -> "❌ nome inválido: " + e.reason();
            case RegisterError.InvalidEmail e -> "❌ e-mail inválido: " + e.reason();
            case RegisterError.WeakPassword e -> "❌ senha fraca: " + e.reason();
            case RegisterError.EmailAlreadyUsed e -> "❌ e-mail já cadastrado: " + e.email();
        };
    }
}
