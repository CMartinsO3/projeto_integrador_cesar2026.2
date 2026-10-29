#!/bin/bash
# Compila e executa o teste da tabela hash (Unidade 2)
g++ -Wall -Wextra -std=c++11 fefo.cpp tabela_hash.cpp teste_hash.cpp -o teste_hash && ./teste_hash
