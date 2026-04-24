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

1. **Abra o VS Code na pasta raiz do projeto**

2. **Crie um novo arquivo chamado `run.sh`**

3. **Cole o conteúdo:**
```bash
#!/bin/bash

echo "Compilando..."
javac -cp "lib/*" -d out $(find src -name "*.java")

echo "Iniciando servidor Java..."
java -cp "out:lib/*" controller.Servidor &

echo "Iniciando frontend..."
cd frontend && npx serve .
```

4. **Salve o arquivo**

5. **Abra o terminal na raiz do projeto e rode uma única vez:**
```bash
chmod +x run.sh
```

6. **Daí em diante, para rodar o projeto inteiro é só:**
```bash
./run.sh
```

O backend iniciará em http://localhost:7777 e o frontend em http://localhost:3000 (ou a porta exibida pelo `serve`).

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