#!/usr/bin/env bash
set -euo pipefail

API_BASE="${1:-http://localhost:7777}"
TOTAL_CLIENTES="${2:-30}"
TOTAL_ALIMENTOS="${3:-40}"
TOTAL_RECEITAS="${4:-30}"

if ! command -v curl >/dev/null 2>&1; then
  echo "Erro: curl nao encontrado."
  exit 1
fi

post_json() {
  local path="$1"
  local payload="$2"

  local status
  status=$(curl -sS -o /dev/null -w "%{http_code}" \
    -X POST "${API_BASE}${path}" \
    -H "Content-Type: application/json" \
    -d "${payload}")

  if [[ "$status" -lt 200 || "$status" -ge 300 ]]; then
    echo "Falha em ${path}. HTTP ${status}"
    return 1
  fi
}

echo "Iniciando carga de dados..."
echo "API: ${API_BASE}"
echo "Clientes: ${TOTAL_CLIENTES}, Alimentos: ${TOTAL_ALIMENTOS}, Receitas: ${TOTAL_RECEITAS}"

# Clientes
for ((i = 1; i <= TOTAL_CLIENTES; i++)); do
  ano=$((1970 + (i % 30)))
  mes=$((1 + (i % 12)))
  dia=$((1 + (i % 28)))

  data_nasc=$(printf "%04d-%02d-%02d" "$ano" "$mes" "$dia")
  data_add=$(date +%F)

  payload=$(cat <<EOF
{
  "nome": "Cliente Teste ${i}",
  "dataNascimento": "${data_nasc}",
  "dataAdicao": "${data_add}",
  "email": ["cliente${i}@teste.com", "cliente${i}@mail.com"],
  "senha": "123456"
}
EOF
)

  post_json "/clientes" "$payload"
done

# Alimentos
categorias=("Proteina" "Carboidrato" "Vegetal" "Fruta" "Gordura Boa")
for ((i = 1; i <= TOTAL_ALIMENTOS; i++)); do
  c1="${categorias[$((i % ${#categorias[@]}))]}"
  c2="${categorias[$(((i + 2) % ${#categorias[@]}))]}"

  payload=$(cat <<EOF
{
  "nome": "Alimento Teste ${i}",
  "categoria": ["${c1}", "${c2}"]
}
EOF
)

  post_json "/alimentos" "$payload"
done

# Receitas
for ((i = 1; i <= TOTAL_RECEITAS; i++)); do
  tempo=$((10 + (i % 90)))
  porcao=$((1 + (i % 6)))

  payload=$(cat <<EOF
{
  "titulo": "Receita Teste ${i}",
  "informacoes": "Misture os ingredientes da receita ${i} e cozinhe por ${tempo} minutos.",
  "tempoPreparo": ${tempo},
  "porcao": "${porcao} porcoes"
}
EOF
)

  post_json "/receitas" "$payload"
done

echo "Carga concluida com sucesso."
echo "Verifique na interface ou pelas rotas GET:"
echo "  ${API_BASE}/clientes"
echo "  ${API_BASE}/alimentos"
echo "  ${API_BASE}/receitas"
