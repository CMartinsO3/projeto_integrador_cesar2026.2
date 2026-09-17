package com.hemoflow.hemoflow.estoque;

import java.time.LocalDate;

public class Bolsa {
    private String id;
    private LocalDate dataValidade;

    public Bolsa(String id, LocalDate dataValidade) {
        this.id = id;
        this.dataValidade = dataValidade;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public String getId() {
        return id;
    }
}
