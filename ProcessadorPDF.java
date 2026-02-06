package BancadaDeTestes;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;

public class ProcessadorPDF {

    /**
     * Localiza as páginas de um cliente específico dentro do PDF mestre e salva em um novo arquivo.
     * @return "OK" em caso de sucesso ou uma mensagem descritiva do erro.
     */
    public static String extrairPorCnpj(ClienteDTO cliente, String cnpjGocil, String caminhoPdfBase, String pastaDestino, String nomeArquivo) {
        File fileBase = new File(caminhoPdfBase);

        // Validação inicial do arquivo mestre
        if (!fileBase.exists()) {
            return "ERRO: O PDF mestre não foi encontrado no caminho especificado.";
        }

        // try-with-resources: Garante que os arquivos sejam fechados mesmo em caso de erro
        try (PDDocument docBase = PDDocument.load(fileBase);
             PDDocument novoDoc = new PDDocument()) {

            PDFTextStripper stripper = new PDFTextStripper();
            String cnpjAlvo = cliente.getCnpjApenasNumeros();
            boolean capturando = false;
            boolean encontrouFim = false;

            // Varredura página por página
            for (int p = 0; p < docBase.getNumberOfPages(); p++) {
                // Configura o stripper para ler apenas a página atual
                stripper.setStartPage(p + 1);
                stripper.setEndPage(p + 1);

                String textoPagina = stripper.getText(docBase);
                // Limpa o texto para facilitar a comparação de números
                String numerosPagina = textoPagina.replaceAll("[^0-9]", "");

                // CRITÉRIO DE INÍCIO: A página deve conter o CNPJ da Gocil E o CNPJ do Cliente
                if (numerosPagina.contains(cnpjGocil) && numerosPagina.contains(cnpjAlvo)) {
                    capturando = true;
                }

                if (capturando) {
                    // Importa a página original mantendo a qualidade e formatação
                    novoDoc.importPage(docBase.getPage(p));

                    // CRITÉRIO DE FIM: O relatório do FGTS termina na linha "TOTAL DO TOMADOR"
                    // Removemos espaços para evitar erros de leitura ("TOTALDOTOMADOR")
                    if (textoPagina.toUpperCase().replaceAll("\\s", "").contains("TOTALDOTOMADOR")) {
                        encontrouFim = true;
                        break;
                    }
                }
            }

            if (encontrouFim) {
                // Cria a estrutura de pastas se não existir
                File pasta = new File(pastaDestino);
                if (!pasta.exists()) {
                    pasta.mkdirs();
                }

                // Salva o novo arquivo PDF recortado
                File arquivoSalvo = new File(pasta, nomeArquivo);
                novoDoc.save(arquivoSalvo);
                return "OK";
            } else {
                return "NÃO LOCALIZADO: CNPJ do cliente não encontrado no PDF mestre.";
            }

        } catch (Exception e) {
            return "ERRO TÉCNICO: " + e.getMessage();
        }
    }
}
