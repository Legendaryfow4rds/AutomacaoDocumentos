package BancadaDeTestes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class GerenciadorArquivos {

    public static String obterCaminhoDestino(ClienteDTO cliente, String caminhoRaizDrive) {
        String nomeLimpo = cliente.getNome().replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        File pastaCliente = new File(caminhoRaizDrive, nomeLimpo);
        File pastaComp = new File(pastaCliente, cliente.getCompetencia());

        String subTipo = cliente.getTipo().toUpperCase().contains("SERV") ? "SERVICO" : "VIGILANCIA";
        File pastaFinal = new File(pastaComp, subTipo);

        if (!pastaFinal.exists()) {
            pastaFinal.mkdirs();
        }
        return pastaFinal.getAbsolutePath();
    }

    public static void copiarArquivo(String caminhoOrigem, String pastaDestino) {
        try {
            File origem = new File(caminhoOrigem);
            if (!origem.exists()) return;

            File destino = new File(pastaDestino, origem.getName());
            if (!destino.exists()) {
                Files.copy(origem.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao copiar comprovante: " + e.getMessage());
        }
    }
}
