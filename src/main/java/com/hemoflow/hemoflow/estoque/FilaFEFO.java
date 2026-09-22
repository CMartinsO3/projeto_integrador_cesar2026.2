package com.hemoflow.hemoflow.estoque;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import com.hemoflow.hemoflow.dominio.Bolsa;

public class FilaFEFO {

    private final PriorityQueue<Bolsa> fila =
            new PriorityQueue<>(Comparator.comparing(Bolsa::getDataValidade));

    public void adicionar(Bolsa bolsa) {
        fila.add(bolsa);
    }

    public void adicionarTodas(List<Bolsa> bolsas) {
        fila.addAll(bolsas);
    }

    public Bolsa proxima() {
        return fila.poll();
    }

    public boolean estaVazia() {
        return fila.isEmpty();
    }

    public int tamanho() {
        return fila.size();
    }
}