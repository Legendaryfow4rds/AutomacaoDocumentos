package BancadaDeTestes;

import java.util.HashMap;
import java.util.Map;

public class DicionarioFiliais {
    // Mapa que associa a chave (FILIAL_TIPO) ao CNPJ correspondente
    private static final Map<String, String> mapeamento = new HashMap<>();

    static {
        // --- GRUPO: SERVIÇO ---
        mapeamento.put("SÃO PAULO_VIG", "50844182000155");
        mapeamento.put("BAURU_SERV", "00146889000705");
        mapeamento.put("CAMPINAS_SERV", "00146889000624");
        mapeamento.put("SANTOS_SERV", "00146889000977");
        mapeamento.put("RIBEIRÃO PRETO_SERV", "00146889001000");
        mapeamento.put("PARANÁ_SERV", "00146889000462");
        mapeamento.put("RIO GRANDE DO SUL_SERV", "00146889000543");
        mapeamento.put("SANTA CATARINA_SERV", "00146889001515");
        mapeamento.put("RIO DE JANEIRO_SERV", "00146889000896");
        mapeamento.put("MINAS GERAIS_SERV", "00146889001191");
        mapeamento.put("GOIAS_SERV", "00146889001353");
        mapeamento.put("PERNAMBUCO_SERV", "00146889001434");
        mapeamento.put("MARANHÃO_SERV", "00146889001787");
        mapeamento.put("ESPÍRITO SANTO_SERV", "00146889001604");
        mapeamento.put("BAHIA_SERV", "33931783000186"); // Gocil Nordeste
        mapeamento.put("CEARÁ_SERV", "00146889002163");
        mapeamento.put("MATO GROSSO DO SUL_SERV", "00146889001949");

        // --- GRUPO: VIGILÂNCIA ---
        mapeamento.put("SÃO PAULO_SERV", "00146889000110");
        mapeamento.put("BAURU_VIG", "50844182000236");
        mapeamento.put("CAMPINAS_VIG", "50844182001208");
        mapeamento.put("SANTOS_VIG", "50844182001631");
        mapeamento.put("RIBEIRÃO PRETO_VIG", "50844182001470");
        mapeamento.put("PARANÁ_VIG", "50844182000902");
        mapeamento.put("RIO GRANDE DO SUL_VIG", "50844182001712");
        mapeamento.put("SANTA CATARINA_VIG", "50844182002280");
        mapeamento.put("RIO DE JANEIRO_VIG", "50844182001984");
        mapeamento.put("MINAS GERAIS_VIG", "50844182002018");
        mapeamento.put("GOIAS_VIG", "50844182002360");
        mapeamento.put("PERNAMBUCO_VIG", "50844182002441");
        mapeamento.put("BRASÍLIA_VIG", "50844182002603");
        mapeamento.put("MARANHÃO_VIG", "50844182002522");
        mapeamento.put("CEARÁ_VIG", "50844182002794");
        mapeamento.put("PARÁ_VIG", "50844182002875");
        mapeamento.put("ESPÍRITO SANTO_VIG", "50844182003090");
        mapeamento.put("MATO GROSSO DO SUL_VIG", "50844182002956");
        mapeamento.put("BAHIA_VIG", "06261891000116"); // Gocil Nordeste Vigilância
    }

    /**
     * Retorna o CNPJ formatado apenas com número baseado nos dados da planilha.
     */
    public static String obterCnpjCorreto(String filial, String tipo) {
        // Normaliza a chave para evitar erros de digitação (espaços e maiúsculas)
        String chave = filial.toUpperCase().trim() + "_" + (tipo.toUpperCase().contains("SERV") ? "SERV" : "VIG");

        // Retorna o CNPJ ou uma String vazia caso a filial não exista no mapa
        return mapeamento.getOrDefault(chave, "");
    }
}
