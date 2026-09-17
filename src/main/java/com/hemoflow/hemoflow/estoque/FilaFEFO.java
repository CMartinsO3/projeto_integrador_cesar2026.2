package com.hemoflow.hemoflow.estoque;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import com.hemoflow.hemoflow.dominio.Bolsa;

public class FilaFEFO {
    private final PriorityQueue<Bolsa> fila = new PriorityQueue<>(
            Comparator.comparing(Bolsa::getDataValidade)
                    .thenComparing(Bolsa::getId, Comparator.nullsLast(Long::compareTo))
    );

    public FilaFEFO() {
    }

    public FilaFEFO(List<Bolsa> bolsas) {
        fila.addAll(bolsas);
    }

    public void adicionar(Bolsa bolsa) {
        fila.add(bolsa);
    }

    public Bolsa proxima() {
        return fila.poll();
    }

    public boolean isEmpty() {
        return fila.isEmpty();
    }

    public int size() {
        return fila.size();
    }
}
