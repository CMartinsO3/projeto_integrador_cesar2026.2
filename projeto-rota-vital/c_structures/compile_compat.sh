#!/bin/bash
# Compila e executa o teste da matriz ABO/Rh (Unidade 2)
g++ -Wall -Wextra -std=c++11 compat_abo_rh.cpp teste_compat.cpp -o teste_compat && ./teste_compat
