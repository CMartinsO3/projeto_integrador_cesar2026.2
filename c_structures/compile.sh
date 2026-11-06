#!/bin/bash
if ! g++ -Wall -Wextra -std=c++11 lista.cpp fila.cpp pilha.cpp main.cpp -o hemoflow_structures; then
    echo "Erro na compilação das estruturas." >&2
    exit 1
fi
echo "Compilação concluída! Execute ./hemoflow_structures para testar."
