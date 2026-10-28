#!/bin/bash
# Compila e executa o teste da lista FEFO (Unidade 2)
g++ -Wall -Wextra -std=c++11 fefo.cpp teste_fefo.cpp -o teste_fefo && ./teste_fefo
