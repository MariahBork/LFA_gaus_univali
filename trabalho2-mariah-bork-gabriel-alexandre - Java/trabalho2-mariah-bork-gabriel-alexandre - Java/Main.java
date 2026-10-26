package gals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        Semantico semantico = new Semantico();

        if (args.length > 0)
        {
            // Leitura de arquivo
            executar(Files.readString(Path.of(args[0])), semantico);
            return;
        }

        // Input individual
        System.out.println("Interpretador binário. Digite comandos terminados em ';' ou 'sair' para encerrar.");
        BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in));

        while (true)
        {
            System.out.print("> ");
            String linha = entrada.readLine();

            if (linha == null || linha.trim().equals("sair"))
                break;
            if (linha.isBlank())
                continue;

            executar(linha, semantico);
        }
    }

    private static void executar(String fonte, Semantico semantico)
    {
        try
        {
            new Sintatico().parse(new Lexico(new StringReader(fonte)), semantico);
        }
        catch (LexicalError e)
        {
            System.err.println("Erro léxico na posição " + e.getPosition() + ": " + e.getMessage());
        }
        catch (SyntacticError e)
        {
            System.err.println("Erro sintático na posição " + e.getPosition() + ": " + e.getMessage());
        }
        catch (SemanticError e)
        {
            System.err.println("Erro semântico na posição " + e.getPosition() + ": " + e.getMessage());
        }
        finally
        {
            semantico.limparPilha();
        }
    }
}