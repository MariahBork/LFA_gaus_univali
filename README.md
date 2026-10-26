# Trabalho M2 — Gramática e Interpretador com GALS

**Disciplina:** Linguagens Formais e Autômatos
**Curso:** Ciência da Computação — Univali
**Professor:** Alex Luciano Roesler Rese
**Integrantes:** Mariah Bork e Gabriel Alexandre

Implementação de uma gramática e de um interpretador para uma pequena linguagem de
programação, usando o [WebGALS](https://lia-univali.github.io/Web-GALS/) como gerador
dos analisadores léxico, sintático e semântico.

## Sobre o projeto

O GALS gera o código Java dos três analisadores a partir da descrição da linguagem. O
grupo ficou responsável pela definição dos tokens, pela gramática e pela implementação
das ações semânticas, que é onde fica a lógica do interpretador.

A linguagem trabalha apenas com números **binários inteiros sem sinal** e oferece:

- atribuição de valor ou de expressão a uma variável
- exibição de valor na tela com o comando `Show`
- soma, subtração, multiplicação, divisão, exponenciação e logaritmo

## Tokens

| Token | Expressão | Observação |
|---|---|---|
| (brancos) | `[ \n\t\r\s]+` | descartado pelo analisador |
| `numeros` | `[01]+` | apenas 0 e 1, porque a linguagem é binária |
| `variaveis` | `[A-Za-z][A-Za-z][A-Za-z0-9]+` | inicia sempre com letra |
| `logaritmo` | `variaveis : "log"` | palavra reservada |
| `show` | `variaveis : "Show"` | palavra reservada |
| `soma` | `\+` | |
| `subtracao` | `\-` | |
| `multiplicacao` | `\*` | |
| `divisao` | `/` | |
| `exponenciacao` | `^` | |
| `atribuicao` | `=` | |
| `delimitador` | `;` | |
| `associacao_aberta` | `\(` | |
| `associacao_fechada` | `\)` | |

`log` e `Show` são reconhecidos a partir do padrão de variável e reclassificados como
palavras reservadas. É o mesmo mecanismo que um compilador real usa para distinguir uma
variável chamada `show` do comando `Show`.

## Gramática

Símbolo inicial: `<lista_comandos>`

    <lista_comandos> ::= <comando> <lista_comandos> | <comando>
    <comando> ::= variaveis #1 atribuicao <calculo4> delimitador #2
                | show associacao_aberta <calculo4> associacao_fechada delimitador #3
    <calculo4> ::= <calculo4> soma <calculo3> #4
                 | <calculo4> subtracao <calculo3> #5
                 | <calculo3>
    <calculo3> ::= <calculo3> multiplicacao <calculo2> #6
                 | <calculo3> divisao <calculo2> #7
                 | <calculo2>
    <calculo2> ::= <calculo1> exponenciacao <calculo2> #8
                 | <calculo1>
    <calculo1> ::= logaritmo <valor> #9
                 | <valor>
    <valor> ::= associacao_aberta <calculo4> associacao_fechada
              | numeros #10
              | variaveis #11

A precedência cresce do nível 4 para o nível 1. Soma e multiplicação são recursivas à
esquerda, o que garante associatividade à esquerda. A exponenciação é recursiva à
direita, porque associa da direita para a esquerda. O logaritmo tem a maior precedência
e se aplica apenas ao valor imediatamente à sua direita.

## Ações semânticas

| Ação | O que faz |
|---|---|
| `#1` | guarda o nome da variável de destino |
| `#2` | retira o resultado da pilha e atribui à variável |
| `#3` | retira o resultado da pilha e imprime em binário |
| `#4` a `#8` | desempilham dois operandos, calculam e empilham o resultado |
| `#9` | aplica o logaritmo |
| `#10` | converte o número binário para decimal e empilha |
| `#11` | busca o valor da variável e empilha |

O interpretador usa um mapa para guardar as variáveis e uma pilha para avaliar as
expressões.

## Arquivos

| Arquivo | Função |
|---|---|
| `trabalho2-mariah-bork-gabriel-alexandre.gals` | definição da linguagem, aberta no WebGALS |
| `Main.java` | ponto de entrada, lê os comandos e trata as exceções |
| `Lexico.java` | analisador léxico gerado, executa o autômato |
| `Sintatico.java` | analisador sintático gerado, descida recursiva |
| `Semantico.java` | ações semânticas, com o mapa de variáveis e a pilha |
| `ParserConstants.java` | constantes e tabelas do sintático |
| `ScannerConstants.java` | tabelas do léxico |
| `Constants.java` | constantes gerais, incluindo as palavras reservadas |
| `Token.java` | representa um token com lexema, código e posição |
| `AnalysisError.java` e demais `*Error.java` | hierarquia de exceções de análise |

## Como executar

Requisito: JDK instalado. O projeto foi testado com o JDK 21 da Eclipse Adoptium.
Para conferir, rode `java -version` e `javac -version`. Se o segundo comando não
responder, falta o JDK.

Clone o repositório e entre na pasta:

    git clone https://github.com/USUARIO/REPO.git
    cd REPO

Compile:

    javac -encoding UTF-8 -d . *.java

Execute:

    java gals.Main

Para sair do interpretador, digite `sair`.

### Por que o `-d .` é obrigatório

Todos os arquivos declaram `package gals;` na primeira linha. Em Java, o pacote precisa
espelhar a estrutura de diretórios, então o compilador tem que gravar os `.class` dentro
de uma pasta chamada `gals`. O parâmetro `-d .` faz isso.

Sem ele, os `.class` ficam soltos na raiz e a execução falha com
`ClassNotFoundException: gals.Main`. E sem a pasta, usar apenas `java Main` também não
funciona, porque a classe pertence ao pacote.

Quando a compilação dá certo, o `javac` não imprime nada. Silêncio é sucesso.

### Erros comuns

| Mensagem | Causa | Solução |
|---|---|---|
| `Invalid filename: *.java` | o terminal está na pasta errada e o curinga não encontrou nenhum arquivo | confira com `dir *.java` e entre na pasta correta |
| `ClassNotFoundException: gals.Main` | faltou o `-d .` ou a compilação não rodou | compile de novo e confira a pasta `gals` com `dir gals` |
| `Semantico cannot be resolved to a type` | erro da extensão Java do VS Code, não do código | use o terminal, como descrito acima |

Se o arquivo `.zip` do trabalho for extraído, ele cria uma pasta dentro da outra. Neste
repositório os arquivos estão na raiz, então o problema não acontece no clone.

## Exemplo

Entrada:

    A = 10;
    B = 11;
    B = 111 + A * B;
    Show ( B );

Saída:

    1101

`A` vale 2 em decimal e `B` vale 3, porque os valores são binários. A multiplicação tem
precedência sobre a soma, então a expressão é `7 + (2 * 3) = 13`, e 13 em binário é
`1101`.

## Casos para testar

| Entrada | Saída esperada | O que verifica |
|---|---|---|
| `A = 10 + 11 * 10;` `Show ( A );` | `1000` | precedência da multiplicação |
| `A = ( 10 + 11 ) * 10;` `Show ( A );` | `1010` | parênteses alterando a ordem |
| `A = 10 ^ 11 ^ 10;` `Show ( A );` | `1000000000` | exponenciação associando à direita |
| `Show ( A` | erro sintático | parêntese não fechado |
| `Show ( C );` sem atribuir `C` | erro semântico | variável não definida |

## Decisões e limitações

- Todos os valores são binários inteiros sem sinal, conforme o enunciado.
- A exponenciação associa pela direita.
- O logaritmo tem precedência maior que a exponenciação.
- BASE DO LOGARITMO: preencher conforme a implementação.
- DIVISÃO POR ZERO: preencher conforme a implementação.
- LOGARITMO DE ZERO OU DE VALOR NEGATIVO: preencher conforme a implementação.
- SUBTRAÇÃO COM RESULTADO NEGATIVO: preencher conforme a implementação.
- LIMITE DE TAMANHO DOS VALORES: preencher conforme a implementação.

## Vídeo de apresentação

LINK_DO_VIDEO

## Referências

- WebGALS: https://lia-univali.github.io/Web-GALS/
- GALS original: https://gals.sourceforge.net/
