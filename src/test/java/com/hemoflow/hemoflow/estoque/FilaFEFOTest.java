package com.hemoflow.hemoflow.estoque;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.hemoflow.hemoflow.dominio.Bolsa;
import com.hemoflow.hemoflow.dominio.Hemocomponente;
import com.hemoflow.hemoflow.dominio.NoRede;
import com.hemoflow.hemoflow.dominio.TipoNo;
import com.hemoflow.hemoflow.dominio.TipoSanguineo;

class FilaFEFOTest {

    @Test
    void priorizaValidadeMaisProxima() {
        NoRede hc = new NoRede("HC", "Hemocentro", TipoNo.HEMOCENTRO);
        Bolsa maisLonge = new Bolsa(TipoSanguineo.O_POS, Hemocomponente.HEMACIAS, LocalDate.now(), LocalDate.now().plusDays(20), "B", hc);
        Bolsa maisPerto = new Bolsa(TipoSanguineo.O_NEG, Hemocomponente.HEMACIAS, LocalDate.now(), LocalDate.now().plusDays(3), "A", hc);

        FilaFEFO fila = new FilaFEFO();
        fila.adicionar(maisLonge);
        fila.adicionar(maisPerto);

        assertEquals("A", fila.proxima().getLote());
        assertEquals("B", fila.proxima().getLote());
    }
}
