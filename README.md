# TP - AEDs III (NutriChef)

Sistema de gerenciamento com cadastro de clientes, alimentos e receitas, com API REST em Java e interface web.

## Tecnologias
- Java (backend)
- Spark Java (servidor HTTP)
- GSON (JSON)
- HTML, CSS e JavaScript (frontend)

## Estrutura principal
- [src/controller/Servidor.java](src/controller/Servidor.java): endpoints da API
- [src/util/Arquivo.java](src/util/Arquivo.java): persistencia dos registros
- [frontend/index.html](frontend/index.html): entrada do frontend
- [frontend/js/scripts.js](frontend/js/scripts.js): logica da interface
- [frontend/css/style.css](frontend/css/style.css): estilos
- [scripts/seed_data.sh](scripts/seed_data.sh): carga de dados para testes

## Pre-requisitos
- Java 11 ou superior
- Bash
- curl
- Navegador

## Como rodar o projeto

O arquivo `run.sh` já vem junto com o projeto. Basta executar estes passos na raiz do repositório:

1. Dê permissão de execução ao script, uma única vez:
```bash
chmod +x run.sh
```

2. Para iniciar o projeto completo, rode:
```bash
./run.sh
```

O backend sobe em `http://localhost:7777` e o frontend abre na porta exibida pelo `serve`.

## Carga de dados para testes (seed)
O script cria clientes, alimentos e receitas automaticamente via API.

Execucao padrao:

```bash
cd /home/.../.../.../TP---AEDs-III
./scripts/seed_data.sh
```

Com quantidades personalizadas:

```bash
./scripts/seed_data.sh http://localhost:7777 100 120 80
```

Parametros:
1. URL da API
2. quantidade de clientes
3. quantidade de alimentos
4. quantidade de receitas

## Funcionalidades implementadas
- Login
- Cadastro de usuario
- Recuperacao de senha com codigo (solicitar e confirmar)
- Dashboard com visao geral
- CRUD de clientes
- CRUD de alimentos
- CRUD de receitas
- Sidebar recolhivel com persistencia de estado
- Busca com botao Pesquisar
- Busca com filtro de campo:
1. Clientes: Todos, ID, Nome, E-mail
2. Alimentos: Todos, ID, Nome, Categoria
3. Receitas: Todos, ID, Titulo, Tempo

## Endpoints principais
- GET /clientes
- POST /clientes
- PUT /clientes/:id
- DELETE /clientes/:id
- POST /clientes/login
- POST /clientes/esqueci-senha/solicitar
- POST /clientes/esqueci-senha/confirmar
- GET /alimentos
- POST /alimentos
- PUT /alimentos/:id
- DELETE /alimentos/:id
- GET /receitas
- POST /receitas
- PUT /receitas/:id
- DELETE /receitas/:id

## Troubleshooting

### erro: no source files
Voce provavelmente usou o padrao errado no find.

Use:

```bash
javac -cp "lib/*" -d out $(find src -name "*.java")
```

Nao use:

```bash
find src -name ".java"
```

### seed_data.sh: comando nao encontrado
Execute com caminho relativo correto a partir da raiz:

```bash
./scripts/seed_data.sh
```

### chmod: nao foi possivel acessar seed_data.sh
O arquivo nao fica na raiz, fica dentro de scripts.

```bash
chmod +x ./scripts/seed_data.sh
./scripts/seed_data.sh
```

### curl: (7) Failed to connect to localhost port 7777
Backend nao esta rodando. Compile e inicie o servidor antes do seed.

## Observacao
Se os dados novos nao aparecerem no navegador, atualize com Ctrl+F5.

## Compactação de arquivos

### LZW

O método de dicionário se localiza na pasta src/util/LZW.java

Assim que os dados das entidades são gravadas na pasta "dados", é necessário realizar a compilação da compactação via terminal. 
(Compilação)
```bash
javac src/util/LZW.java -d src
```
Quando "LZW.class" aparecer no explorer, significa que a compilação foi um sucesso. É possível executar a compactação da pasta dados, que serão armazenadas em um arquivo nomeado "backup.hl" dentro dessa mesma pasta:

![alt text](image.png)

#### Execução de Compactação:
```bash
java -cp src util.LZW c
```
Uma mensagem será imprimida no terminal, indicando o tamanho original (em bytes) dos arquivos da pasta de dados gravados, o tamanho do arquivo após a compactação e a taxa em porcentagem de reaproveitamento.

![alt text](image-1.png)

#### Execução da Descompactação:
```bash
java -cp src util.LZW d
```

Será imprimido uma mensagem dos arquivos localizados na pasta dados e logo depois uma mensagem indicando sucesso na descompactação via LZW.

![alt text](image-2.png)

### Huffman

O algoritmo de compactação por Huffman está localizado na pasta src/util/Huffman.java.

Assim que os dados das entidades são gravados na pasta dados, é necessário realizar a compilação da compactação via terminal.

#### Compilação
```bash
javac src/util/Huffman.java -d src
```
Quando o arquivo "Huffman.class" aparecer no explorer, significa que a compilação foi realizada com sucesso. A partir disso, é possível compactar todos os arquivos da pasta dados, gerando um único arquivo de backup chamado "backup.hf".

#### Execução da Compactação
```bash
java -cp src util.Huffman c
```
Ao executar a compactação, o sistema percorre todos os arquivos presentes na pasta dados, calcula as frequências dos bytes, constrói a árvore de Huffman e gera o arquivo "backup.hf".

Uma mensagem será exibida no terminal informando:

- Tamanho original dos dados (em bytes);
- Tamanho do arquivo compactado;
- Taxa de compressão obtida;
- Confirmação da criação do backup.

Exemplo:

![alt text](Huffmanc.png)

#### Execução da Descompactação
```bash
java -cp src util.Huffman d
```
Durante a descompactação, o sistema lê o arquivo "backup.hf", reconstrói a árvore de Huffman a partir das frequências armazenadas e restaura todos os arquivos originais para suas respectivas pastas dentro de dados.

Uma mensagem semelhante à seguinte será exibida:

- Restaurado: ./dados/alimentos/bucket.dat
- Restaurado: ./dados/alimentos/diretorio.dat
- Restaurado: ./dados/receitas/bucket.dat
- Restaurado: ./dados/receitas/diretorio.dat
- Descompactacao Huffman realizada com sucesso!

#### Funcionamento

O algoritmo Huffman realiza a compactação utilizando codificação por frequência. Os bytes mais frequentes recebem códigos menores, enquanto os menos frequentes recebem códigos maiores. Durante a compactação são armazenadas as frequências dos símbolos, permitindo que a árvore de Huffman seja reconstruída posteriormente para a descompactação.

O resultado é um único arquivo de backup (backup.hf) contendo todos os arquivos utilizados pelo sistema.

## Casamento de Padrões

### KMP (Knuth-Morris-Pratt)

O algoritmo KMP está localizado na pasta src/service/busca/KMP.java, e é utilizado através da classe orquestradora src/service/BuscaService.java. A interação é feita a partir da opção do menu do Principal.java "4 - Pesquisar por padrão (KMP / BM)". É preferível que essa opção seja escolhida após a gravação de dados das entidades envolvidas.

O KMP permite buscar um padrão (substring) dentro dos seguintes campos textuais do sistema:
- Nome do Cliente
- Nome do Alimento
- Título da Receita
  
A busca não exige correspondência exata — basta que o padrão informado esteja contido no texto do campo (busca case-insensitive). Por exemplo, buscar por "ana" encontra um cliente chamado "Ana" ou "Mariana".

### Compilação
```bash
javac service/busca/KMP.java service/BuscaService.java -d .
```
Execute o comando acima a partir da pasta `src`. Quando os arquivos `KMP.class` e `BuscaService.class` aparecerem no explorer, a compilação foi concluída com sucesso.

#### Execução via menu do sistema 
O KMP está integrado diretamente ao menu principal do sistema (console). Para utilizá-lo:
 
1. Compile e execute o `Principal.java` normalmente:
```bash
javac view/*.java service/busca/*.java service/*.java dao/*.java model/*.java util/*.java -d .
java view.Principal
```
 
2. No menu principal, escolha a opção:
```
4 - Pesquisar por padrão (KMP / BM)
```
 
3. Escolha qual entidade deseja buscar (Cliente, Alimento ou Receita).
   
4. Escolha o algoritmo:
```
1 - KMP (Knuth-Morris-Pratt)
2 - Boyer-Moore
```
Digite `1` para utilizar o KMP.
 
5. Digite o padrão (texto) que deseja buscar. O sistema vai retornar todos os registros cujo campo correspondente contém esse padrão.
Exemplo de uso buscando por clientes cujo nome contenha "ana":
```
Digite o padrão a buscar no nome do cliente: ana
 
--- Resultado da busca (KMP) ---
Cliente {id = 1; Nome = ana; Data de Nascimento = ...}
Cliente {id = 3; Nome = mariana; Data de Nascimento = ...}
 
Total: 2 cliente(s) encontrado(s).
```
 
#### Funcionamento
O KMP evita comparações redundantes ao construir, a partir do próprio padrão buscado, uma **tabela de falha** (função de prefixo). Essa tabela indica, para cada posição do padrão, qual o maior prefixo que também é sufixo até aquele ponto.
 
Durante a busca, ao encontrar um caractere que não corresponde (mismatch), o algoritmo usa essa tabela para "pular" diretamente para a próxima posição válida no padrão, sem precisar retroceder no texto. Isso garante complexidade O(n + m), onde `n` é o tamanho do texto e `m` é o tamanho do padrão — evitando o reprocessamento típico de uma busca ingênua.
