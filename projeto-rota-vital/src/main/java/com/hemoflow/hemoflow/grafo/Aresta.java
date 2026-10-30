package com.hemoflow.hemoflow.grafo;

public class Aresta {
    private String destino;
    private int peso; // tempo estimado em minutos
    private Double distanciaKm;

    public Aresta(String destino, int peso) {
        this(destino, peso, null);
    }

    public Aresta(String destino, int peso, Double distanciaKm) {
        this.destino = destino;
        this.peso = peso;
        this.distanciaKm = distanciaKm;
    }

    public String getDestino() {
        return destino;
    }

    public int getPeso() {
        return peso;
    }

    public Double getDistanciaKm() {
        return distanciaKm;
    }
}