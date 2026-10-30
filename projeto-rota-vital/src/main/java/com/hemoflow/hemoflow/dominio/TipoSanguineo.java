package com.hemoflow.hemoflow.dominio;

public enum TipoSanguineo {
    A_POS("A+"),
    A_NEG("A−"),
    B_POS("B+"),
    B_NEG("B−"),
    AB_POS("AB+"),
    AB_NEG("AB−"),
    O_POS("O+"),
    O_NEG("O−");

    private final String rotulo;

    TipoSanguineo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public static String rotuloDe(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return "—";
        }
        String limpo = codigo.trim();
        if ("Todos".equalsIgnoreCase(limpo) || "Todos".equals(limpo)) {
            return "Todos";
        }
        try {
            return valueOf(limpo).getRotulo();
        } catch (IllegalArgumentException e) {
            return limpo.replace("_POS", "+").replace("_NEG", "−");
        }
    }

    public static String rotuloLista(String csv) {
        if (csv == null || csv.isBlank()) {
            return "—";
        }
        if ("Todos".equalsIgnoreCase(csv.trim())) {
            return "Todos";
        }
        String[] partes = csv.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < partes.length; i++) {
            if (i > 0) {
                sb.append(" · ");
            }
            sb.append(rotuloDe(partes[i]));
        }
        return sb.toString();
    }
}
