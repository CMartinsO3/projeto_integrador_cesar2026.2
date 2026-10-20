package com.hemoflow.hemoflow.grafo;

public class Aresta {

    private final Long destinoId;
    private final int pesoMinutos;

    public Aresta(Long destinoId, int pesoMinutos) {
        this.destinoId = destinoId;
        this.pesoMinutos = pesoMinutos;
    }

    public Long getDestinoId() {
        return destinoId;
    }

    public Long getDestino() {
        return destinoId;
    }

    public int getPesoMinutos() {
        return pesoMinutos;
    }

    public int getTempoMinutos() {
        return pesoMinutos;
    }

    public int getPeso() {
        return pesoMinutos;
    }
}