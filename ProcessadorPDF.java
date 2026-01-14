package BancadaDeTestes;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;

public class ProcessadorPDF {

    public static String extrairPorCnpj(ClienteDTO cliente, String caminhoPdfBase, String pastaDestino) {
        File fileBase = new File(caminhoPdfBase);
        if (!fileBase.exists()) return "ERRO: Arquivo base não encontrado";

        try (PDDocument docBase = PDDocument.load(fileBase);
             PDDocument novoDoc = new PDDocument()) {

            PDFTextStripper stripper = new PDFTextStripper();
            String cnpjAlvo = cliente.getCnpjApenasNumeros();

            if (cnpjAlvo.isEmpty()) return "ERRO: CNPJ vazio na planilha";

            boolean capturando = false;
            boolean encontrouFim = false;

            for (int p = 0; p < docBase.getNumberOfPages(); p++) {
                stripper.setStartPage(p + 1);
                stripper.setEndPage(p + 1);

                String textoOriginal = stripper.getText(docBase);
                // Removemos TUDO que não for letra ou número para a busca ficar imune a espaços/pontos
                String textoNormalizado = textoOriginal.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();

                // 1. Procura o CNPJ (sem pontos/traços) dentro do texto normalizado
                if (textoNormalizado.contains(cnpjAlvo)) {
                    capturando = true;
                }

                if (capturando) {
                    novoDoc.importPage(docBase.getPage(p));

                    // 2. Procura a âncora de encerramento
                    if (textoNormalizado.contains("TOTALDOTOMADOR")) {
                        encontrouFim = true;
                        break;
                    }
                }
            }

            if (encontrouFim) {
                String prefixo = cliente.getTipo().toUpperCase().contains("SERV") ? "SERV." : "Vig.";
                String nomeLimpo = cliente.getNome().replaceAll("[^a-zA-Z0-9]", "_");
                String nomeFinal = prefixo + " Relatório FGTS - " + nomeLimpo + ".pdf";

                File arquivoSalvo = new File(pastaDestino, nomeFinal);
                novoDoc.save(arquivoSalvo);
                return "OK:" + nomeFinal;
            }

        } catch (Exception e) {
            return "ERRO TECNICO: " + e.getMessage();
        }
        return "ERRO: CNPJ " + cliente.getCnpj() + " não localizado ou sem 'Total do Tomador'";
    }
}
