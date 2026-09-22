package com.hemoflow.hemoflow.domain.model;

import com.hemoflow.hemoflow.domain.enums.FatorRh;
import com.hemoflow.hemoflow.domain.enums.TipoABO;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Matriz de compatibilidade ABO/Rh para transfusões sanguíneas.
 * Implementa as regras de compatibilidade de tipos sanguíneos.
 */
public class CompatibilidadeABO {
    
    private static final Map<String, Set<String>> matrizCompatibilidade = new HashMap<>();
    
    static {
        // O- é doador universal
        matrizCompatibilidade.put("O-", Set.of("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"));
        
        // O+ pode doar para O+, A+, B+, AB+
        matrizCompatibilidade.put("O+", Set.of("O+", "A+", "B+", "AB+"));
        
        // A- pode doar para A-, A+, AB-, AB+
        matrizCompatibilidade.put("A-", Set.of("A-", "A+", "AB-", "AB+"));
        
        // A+ pode doar para A+, AB+
        matrizCompatibilidade.put("A+", Set.of("A+", "AB+"));
        
        // B- pode doar para B-, B+, AB-, AB+
        matrizCompatibilidade.put("B-", Set.of("B-", "B+", "AB-", "AB+"));
        
        // B+ pode doar para B+, AB+
        matrizCompatibilidade.put("B+", Set.of("B+", "AB+"));
        
        // AB- pode doar para AB-, AB+
        matrizCompatibilidade.put("AB-", Set.of("AB-", "AB+"));
        
        // AB+ é receptor universal (só pode doar para AB+)
        matrizCompatibilidade.put("AB+", Set.of("AB+"));
    }
    
    /**
     * Verifica se uma bolsa de sangue é compatível com um receptor.
     * 
     * @param tipoDoador Tipo ABO do doador
     * @param fatorRhDoador Fator Rh do doador
     * @param tipoReceptor Tipo ABO do receptor
     * @param fatorRhReceptor Fator Rh do receptor
     * @return true se a transfusão é compatível
     */
    public static boolean isCompativel(TipoABO tipoDoador, FatorRh fatorRhDoador,
                                      TipoABO tipoReceptor, FatorRh fatorRhReceptor) {
        String chaveDoador = tipoDoador.getValor() + (fatorRhDoador == FatorRh.POSITIVO ? "+" : "-");
        String chaveReceptor = tipoReceptor.getValor() + (fatorRhReceptor == FatorRh.POSITIVO ? "+" : "-");
        
        Set<String> compatibilidades = matrizCompatibilidade.get(chaveDoador);
        return compatibilidades != null && compatibilidades.contains(chaveReceptor);
    }
    
    /**
     * Retorna todos os tipos sanguíneos compatíveis com um receptor.
     */
    public static Set<String> getTiposCompativeis(TipoABO tipoReceptor, FatorRh fatorRhReceptor) {
        String chaveReceptor = tipoReceptor.getValor() + (fatorRhReceptor == FatorRh.POSITIVO ? "+" : "-");
        Set<String> compativeis = new HashSet<>();
        
        for (Map.Entry<String, Set<String>> entry : matrizCompatibilidade.entrySet()) {
            if (entry.getValue().contains(chaveReceptor)) {
                compativeis.add(entry.getKey());
            }
        }
        
        return compativeis;
    }
}
