package BancadaDeTestes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class GerenciadorArquivos {

    /**
     * Cria a estrutura de pastas necessária e retorna o caminho final.
     * Exemplo: C:/Drive/NOME_CLIENTE/12.2025/VIGILANCIA
     */
    public static String obterCaminhoDestino(ClienteDTO cliente, String caminhoRaizDrive) {
        // Remove caracteres que o Windows não aceita em nomes de pastas
        String nomeLimpo = cliente.getNome().replaceAll("[\\\\/:*?\"<>|]", "_").trim();

        File pastaCliente = new File(caminhoRaizDrive, nomeLimpo);
        File pastaComp = new File(pastaCliente, cliente.getCompetencia());

        String subTipo = cliente.getTipo().toUpperCase().contains("SERV") ? "SERVICO" : "VIGILANCIA";
        File pastaFinal = new File(pastaComp, subTipo);

        // mkdirs() cria toda a hierarquia de pastas de uma vez (pai e filhos)
        if (!pastaFinal.exists()) {
            pastaFinal.mkdirs();
        }
        return pastaFinal.getAbsolutePath();
    }

    /**
     * Copia um arquivo de origem para a pasta de destino de forma segura.
     */
    public static void copiarArquivo(String caminhoOrigem, String pastaDestino) {
        try {
            File origem = new File(caminhoOrigem);
            if (!origem.exists()) return;

            // Define o arquivo de destino mantendo o nome original
            File arquivoDestino = new File(pastaDestino, origem.getName());

            // Só copia se o arquivo ainda não existir na pasta de destino (evita redundância)
            if (!arquivoDestino.exists()) {
                Files.copy(origem.toPath(), arquivoDestino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            // Erros de cópia aparecem no console para não interromper o fluxo principal
            System.err.println("⚠️ Erro ao copiar comprovante: " + e.getMessage());
        }
    }
}
