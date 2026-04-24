#!/bin/bash

echo "Compilando..."
javac -cp "lib/*" -d out $(find src -name "*.java")

echo "Iniciando servidor Java..."
java -cp "out:lib/*" controller.Servidor &

echo "Iniciando frontend..."
cd frontend && npx serve .