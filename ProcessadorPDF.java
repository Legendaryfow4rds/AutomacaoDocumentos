package BancadaDeTestes;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;

public class ProcessadorPDF {

    public static String extrairPorCnpj(ClienteDTO cliente, String caminhoPdfBase, String pastaDestino) {
        File fileBase = new File(caminhoPdfBase);
        if (!fileBase.exists()) return "ERRO: Base PDF não encontrada";

        try (PDDocument docBase = PDDocument.load(fileBase);
             PDDocument novoDoc = new PDDocument()) {

            PDFTextStripper stripper = new PDFTextStripper();
            String cnpjAlvo = cliente.getCnpjApenasNumeros();
            boolean capturando = false;
            boolean encontrouFim = false;

            for (int p = 0; p < docBase.getNumberOfPages(); p++) {
                stripper.setStartPage(p + 1);
                stripper.setEndPage(p + 1);

                String textoNormalizado = stripper.getText(docBase).replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

                if (textoNormalizado.contains(cnpjAlvo)) {
                    capturando = true;
                }

                if (capturando) {
                    novoDoc.importPage(docBase.getPage(p));
                    if (textoNormalizado.contains("TOTALDOTOMADOR")) {
                        encontrouFim = true;
                        break;
                    }
                }
            }

            if (encontrouFim) {
                String prefixo = cliente.getTipo().toUpperCase().contains("SERV") ? "SERV." : "VIG.";
                String nomeLimpo = cliente.getNome().replaceAll("[\\\\/:*?\"<>|]", "_").trim();

                // Formatação: SERV. Relatório FGTS - Nome do cliente - CNPJ - COMPETENCIA
                String nomeFinal = prefixo + " Relatório FGTS - " + nomeLimpo + " - " +
                        cnpjAlvo + " - " + cliente.getCompetencia() + ".pdf";

                File arquivoSalvo = new File(pastaDestino, nomeFinal);

                // Contador caso existam dois registros idênticos para a mesma competência
                int cont = 1;
                while (arquivoSalvo.exists()) {
                    String nomeAlt = nomeFinal.replace(".pdf", " (" + cont + ").pdf");
                    arquivoSalvo = new File(pastaDestino, nomeAlt);
                    cont++;
                }

                novoDoc.save(arquivoSalvo);
                return "OK:" + arquivoSalvo.getName();
            }
        } catch (Exception e) {
            return "ERRO: " + e.getMessage();
        }
        return "ERRO: CNPJ não encontrado";
    }
}
