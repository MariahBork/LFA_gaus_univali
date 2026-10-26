package gals;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Semantico implements Constants {
    private final Stack<BigInteger> pilha = new Stack<>();
    private final Map<String, BigInteger> variaveis = new HashMap<>();
    private String variavelAlvo;

    public void limparPilha() {
        pilha.clear();
    }

    public void executeAction(int action, Token token) throws SemanticError {
        switch (action) {
            case 1: // variaveis #1 atribuicao ...
                variavelAlvo = token.getLexeme();
                break;

            case 2: // ... delimitador #2 -> conclui a atribuição
                variaveis.put(variavelAlvo, pilha.pop());
                break;

            case 3: // Show ( <calculo4> ) ; #3 -> exibe em binário
                System.out.println(pilha.pop().toString(2));
                break;

            case 4: // soma
            {
                BigInteger direita = pilha.pop(); // topo = operando da direita
                BigInteger esquerda = pilha.pop();
                pilha.push(esquerda.add(direita));
                break;
            }

            case 5: // subtração (sem sinal: resultado não pode ser negativo)
            {
                BigInteger direita = pilha.pop();
                BigInteger esquerda = pilha.pop();
                BigInteger resultado = esquerda.subtract(direita);
                if (resultado.signum() < 0)
                    throw new SemanticError("Subtração com resultado negativo: "
                            + esquerda.toString(2) + " - " + direita.toString(2), token.getPosition());
                pilha.push(resultado);
                break;
            }

            case 6: // multiplicação
            {
                BigInteger direita = pilha.pop();
                BigInteger esquerda = pilha.pop();
                pilha.push(esquerda.multiply(direita));
                break;
            }

            case 7: // divisão inteira
            {
                BigInteger direita = pilha.pop();
                BigInteger esquerda = pilha.pop();
                if (direita.signum() == 0)
                    throw new SemanticError("Divisão por zero", token.getPosition());
                pilha.push(esquerda.divide(direita));
                break;
            }

            case 8: // exponenciação (associativa à direita)
            {
                BigInteger expoente = pilha.pop();
                BigInteger base = pilha.pop();
                // BigInteger.pow só aceita expoente do tipo int
                if (expoente.bitLength() > 31)
                    throw new SemanticError("Expoente grande demais: " + expoente.toString(2), token.getPosition());
                pilha.push(base.pow(expoente.intValue()));
                break;
            }

            case 9: // log na base 2, parte inteira (unário: um operando só)
            {
                BigInteger valor = pilha.pop();
                if (valor.signum() == 0)
                    throw new SemanticError("Logaritmo de zero é indefinido", token.getPosition());
                // para valor >= 1, floor(log2(valor)) = número de bits - 1
                pilha.push(BigInteger.valueOf(valor.bitLength() - 1));
                break;
            }

            case 10: // literal binário
                pilha.push(new BigInteger(token.getLexeme(), 2));
                break;

            case 11: // uso de variável dentro de expressão
            {
                BigInteger valor = variaveis.get(token.getLexeme());
                if (valor == null)
                    throw new SemanticError("Variável não atribuída: " + token.getLexeme(), token.getPosition());
                pilha.push(valor);
                break;
            }
        }
    }
}