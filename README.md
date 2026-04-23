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

## Como compilar o backend
Execute na raiz do projeto:

```bash
cd /home/.../.../.../TP---AEDs-III
javac -cp "lib/*" -d out $(find src -name "*.java")
```

## Como executar o backend

```bash
cd /home/.../.../.../TP---AEDs-III
javac -cp "lib/*" -d out $(find src -name "*.java")
java -cp "out:lib/*" controller.Servidor
```

API em: http://localhost:7777

## Como executar o frontend
Opcao recomendada:

```bash
cd /home/.../.../..../TP---AEDs-III/frontend
npx serve .
```

Depois, acessar a URL exibida no terminal pelo `serve`.

Opcao alternativa:

- abrir [frontend/index.html](frontend/index.html) diretamente no navegador.

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