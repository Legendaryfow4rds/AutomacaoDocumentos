package BancadaDeTestes;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.io.IOException;

public class ProcessadorPDF {

    public static boolean extrairRelatorioCompleto(ClienteDTO cliente, String caminhoPdfBase) {
        File fileBase = new File(caminhoPdfBase);
        if (!fileBase.exists()) return false;

        try (PDDocument docBase = PDDocument.load(fileBase);
             PDDocument novoDoc = new PDDocument()) {

            PDFTextStripper stripper = new PDFTextStripper();
            String cnpjLimpo = cliente.getCnpj().replaceAll("[^0-9]", "");
            String nomeCliente = cliente.getNome().toUpperCase();

            boolean capturando = false;
            boolean encontrouFim = false;

            for (int p = 0; p < docBase.getNumberOfPages(); p++) {
                stripper.setStartPage(p + 1);
                stripper.setEndPage(p + 1);
                String textoPagina = stripper.getText(docBase).toUpperCase();

                // 1. Identifica se o cliente está nesta página
                boolean temCliente = textoPagina.contains(nomeCliente) || textoPagina.contains(cnpjLimpo);

                // 2. Identifica se é a página de fechamento
                boolean temTotal = textoPagina.contains("TOTAL DO TOMADOR");

                // LOGICA DE CAPTURA:
                // Se encontramos o cliente, começamos a capturar as páginas.
                if (temCliente) {
                    capturando = true;
                }

                if (capturando) {
                    novoDoc.importPage(docBase.getPage(p));

                    // Se além de ter o cliente, encontramos o "Total do Tomador", encerramos aqui.
                    if (temTotal) {
                        encontrouFim = true;
                        break;
                    }
                }
            }

            if (encontrouFim) {
                String prefixo = cliente.getTipo().toUpperCase().contains("SERVIÇO") ? "SERV." : "Vig.";
                String nomeSanitizado = cliente.getNome().replaceAll("[^a-zA-Z0-9]", "_");
                String nomeFinal = prefixo + " Relatório FGTS - 11.2025 - " + nomeSanitizado + ".pdf";

                novoDoc.save(nomeFinal);
                return true;
            }

        } catch (IOException e) {
            System.err.println("❌ Erro ao processar PDF do cliente " + cliente.getNome() + ": " + e.getMessage());
        }
        return false;
    }
}
