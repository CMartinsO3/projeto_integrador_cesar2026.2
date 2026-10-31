package com.hemoflow.hemoflow.grafo;

import com.hemoflow.hemoflow.dominio.NoRede;

public final class DistanciaGeografica {
    private static final double RAIO_TERRA_KM = 6371.0088;

    private DistanciaGeografica() {
    }

    public static double calcularKm(NoRede origem, NoRede destino) {
        if (origem.getLatitude() == null || origem.getLongitude() == null
                || destino.getLatitude() == null || destino.getLongitude() == null) {
            throw new IllegalStateException("Os nós da ligação precisam ter latitude e longitude cadastradas");
        }

        double latitudeOrigem = Math.toRadians(origem.getLatitude());
        double latitudeDestino = Math.toRadians(destino.getLatitude());
        double diferencaLatitude = latitudeDestino - latitudeOrigem;
        double diferencaLongitude = Math.toRadians(destino.getLongitude() - origem.getLongitude());

        double a = Math.sin(diferencaLatitude / 2) * Math.sin(diferencaLatitude / 2)
                + Math.cos(latitudeOrigem) * Math.cos(latitudeDestino)
                * Math.sin(diferencaLongitude / 2) * Math.sin(diferencaLongitude / 2);
        double valorLimitado = Math.max(0, Math.min(1, a));
        double angulo = 2 * Math.atan2(Math.sqrt(valorLimitado), Math.sqrt(1 - valorLimitado));
        return RAIO_TERRA_KM * angulo;
    }
}
