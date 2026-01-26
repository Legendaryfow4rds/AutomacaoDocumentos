package BancadaDeTestes;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;

public class ProcessadorPDF {
    public static String extrairPorCnpj(ClienteDTO cliente, String cnpjGocil, String caminhoPdfBase, String pastaDestino) {
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

                String textoPagina = stripper.getText(docBase);
                String numerosPagina = textoPagina.replaceAll("[^0-9]", "");

                if (numerosPagina.contains(cnpjGocil) && numerosPagina.contains(cnpjAlvo)) {
                    capturando = true;
                }

                if (capturando) {
                    novoDoc.importPage(docBase.getPage(p));
                    if (textoPagina.toUpperCase().replaceAll("\\s", "").contains("TOTALDOTOMADOR")) {
                        encontrouFim = true;
                        break;
                    }
                }
            }

            if (encontrouFim) {
                File pasta = new File(pastaDestino);
                if (!pasta.exists()) pasta.mkdirs();

                String prefixo = cliente.getTipo().toUpperCase().contains("SERV") ? "SERV." : "VIG.";

                // RESTAURADO: Nome completo com Filial e CNPJ
                String nomeFinal = String.format("%s Relatório FGTS - %s - %s - %s - %s.pdf",
                        prefixo,
                        cliente.getNome().replaceAll("[\\\\/:*?\"<>|]", " "),
                        cliente.getMatrizFilial(),
                        cnpjAlvo,
                        cliente.getCompetencia());

                File arquivoSalvo = new File(pastaDestino, nomeFinal);
                novoDoc.save(arquivoSalvo);
                return "OK";
            }
            return "NÃO LOCALIZADO NO PDF";
        } catch (Exception e) {
            return "ERRO TÉCNICO: " + e.getMessage();
        }
    }
}
