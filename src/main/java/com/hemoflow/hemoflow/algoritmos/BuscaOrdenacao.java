package com.hemoflow.hemoflow.algoritmos;

import java.util.Arrays;

/** Implementações didáticas próprias de busca e ordenação para a U2. */
public final class BuscaOrdenacao {
    private BuscaOrdenacao() {
    }

    public static int sequencial(int[] valores, int chave) {
        for (int indice = 0; indice < valores.length; indice++) {
            if (valores[indice] == chave) {
                return indice;
            }
        }
        return -1;
    }

    /** Requer vetor crescente. */
    public static int binaria(int[] valores, int chave) {
        int inicio = 0;
        int fim = valores.length - 1;
        while (inicio <= fim) {
            int meio = inicio + (fim - inicio) / 2;
            if (valores[meio] == chave) {
                return meio;
            }
            if (valores[meio] < chave) {
                inicio = meio + 1;
            } else {
                fim = meio - 1;
            }
        }
        return -1;
    }

    public static void bubble(int[] valores) {
        for (int passagem = 0; passagem < valores.length - 1; passagem++) {
            boolean trocou = false;
            for (int indice = 0; indice < valores.length - passagem - 1; indice++) {
                if (valores[indice] > valores[indice + 1]) {
                    trocar(valores, indice, indice + 1);
                    trocou = true;
                }
            }
            if (!trocou) {
                break;
            }
        }
    }

    public static void selection(int[] valores) {
        for (int inicio = 0; inicio < valores.length - 1; inicio++) {
            int menor = inicio;
            for (int indice = inicio + 1; indice < valores.length; indice++) {
                if (valores[indice] < valores[menor]) {
                    menor = indice;
                }
            }
            trocar(valores, inicio, menor);
        }
    }

    public static void insertion(int[] valores) {
        for (int indice = 1; indice < valores.length; indice++) {
            int chave = valores[indice];
            int anterior = indice - 1;
            while (anterior >= 0 && valores[anterior] > chave) {
                valores[anterior + 1] = valores[anterior];
                anterior--;
            }
            valores[anterior + 1] = chave;
        }
    }

    public static void merge(int[] valores) {
        if (valores.length > 1) {
            mergeRecursivo(valores, 0, valores.length - 1);
        }
    }

    private static void mergeRecursivo(int[] valores, int inicio, int fim) {
        if (inicio >= fim) {
            return;
        }

        int meio = inicio + (fim - inicio) / 2;
        mergeRecursivo(valores, inicio, meio);
        mergeRecursivo(valores, meio + 1, fim);

        int[] esquerda = Arrays.copyOfRange(valores, inicio, meio + 1);
        int[] direita = Arrays.copyOfRange(valores, meio + 1, fim + 1);
        int i = 0;
        int j = 0;
        int destino = inicio;

        while (i < esquerda.length && j < direita.length) {
            valores[destino++] = esquerda[i] <= direita[j] ? esquerda[i++] : direita[j++];
        }
        while (i < esquerda.length) {
            valores[destino++] = esquerda[i++];
        }
        while (j < direita.length) {
            valores[destino++] = direita[j++];
        }
    }

    private static void trocar(int[] valores, int primeiro, int segundo) {
        int temporario = valores[primeiro];
        valores[primeiro] = valores[segundo];
        valores[segundo] = temporario;
    }
}
