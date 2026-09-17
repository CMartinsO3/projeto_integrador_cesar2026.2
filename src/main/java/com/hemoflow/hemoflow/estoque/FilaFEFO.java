package com.hemoflow.hemoflow.estoque;

import java.util.PriorityQueue;
import java.util.Comparator;

public class FilaFEFO {
    private PriorityQueue<Bolsa> fila =
        new PriorityQueue<>(Comparator.comparing(Bolsa::getDataValidade));

    public void adicionar(Bolsa bolsa) {
        fila.add(bolsa);
    }

    public Bolsa proxima() {
        return fila.poll(); // retorna a bolsa com validade mais próxima
    }
}
