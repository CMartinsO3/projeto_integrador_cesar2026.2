#include <stdio.h>
#include "lista.hpp"
#include "fila.hpp"
#include "pilha.hpp"

int main() {
    printf("=========================================\n");
    printf("  HemoFlow - Estruturas de Dados Base (C) \n");
    printf("=========================================\n\n");

    /* TESTE DA LISTA (Estoque) */
    printf("--- Testando Lista Encadeada (Estoque) ---\n");
    Lista estoque;
    lista_inicializar(&estoque);
    
    lista_inserir_fim(&estoque, 101);
    lista_inserir_fim(&estoque, 102);
    lista_inserir_inicio(&estoque, 100);
    
    lista_imprimir(&estoque);
    printf("Removendo do inicio: %d\n", lista_remover_inicio(&estoque));
    lista_imprimir(&estoque);
    printf("Contem bolsa 102? %s\n", lista_contem(&estoque, 102) ? "Sim" : "Nao");
    
    lista_liberar(&estoque);
    printf("\n");

    /* TESTE DA FILA (Requisicoes) */
    printf("--- Testando Fila FIFO (Requisicoes) ---\n");
    Fila requisicoes;
    fila_inicializar(&requisicoes);
    
    fila_enfileirar(&requisicoes, 501);
    fila_enfileirar(&requisicoes, 502);
    fila_enfileirar(&requisicoes, 503);
    
    fila_imprimir(&requisicoes);
    printf("Espiando frente: %d\n", fila_espiar(&requisicoes));
    printf("Desenfileirando: %d\n", fila_desenfileirar(&requisicoes));
    fila_imprimir(&requisicoes);
    
    fila_liberar(&requisicoes);
    printf("\n");

    /* TESTE DA PILHA (Historico) */
    printf("--- Testando Pilha LIFO (Historico) ---\n");
    Pilha historico;
    pilha_inicializar(&historico);
    
    pilha_empilhar(&historico, 901);
    pilha_empilhar(&historico, 902);
    pilha_empilhar(&historico, 903);
    
    pilha_imprimir(&historico);
    printf("Espiando topo: %d\n", pilha_espiar(&historico));
    printf("Desempilhando: %d\n", pilha_desempilhar(&historico));
    pilha_imprimir(&historico);
    
    pilha_liberar(&historico);
    printf("\n=========================================\n");

    return 0;
}
