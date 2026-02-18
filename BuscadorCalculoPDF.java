package BancadaDeTestes;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuscadorCalculoPDF {

    /**
     * Extrai o total de funcionários com dupla checagem.
     * @param caminhoPdf Caminho do arquivo PDF (Vig ou Serv)
     * @param cnpjCliente CNPJ do cliente que estamos buscando
     * @param cnpjGocil CNPJ da filial Gocil obtido via DicionarioFiliais
     */
    public static String extrairTotalFuncionarios(String caminhoPdf, String cnpjCliente, String cnpjGocil) {
        if (caminhoPdf == null || caminhoPdf.isEmpty()) return "0";

        File file = new File(caminhoPdf);
        if (!file.exists()) return "0";

        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();

            for (int i = 1; i <= document.getNumberOfPages(); i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);

                String textoPagina = stripper.getText(document);

                // Limpeza para comparação numérica (ignora pontos, traços e barras do PDF)
                String textoNumerico = textoPagina.replaceAll("[^0-9]", "");

                // DUPLA AUTENTICAÇÃO: Verifica se ambos os CNPJs numéricos estão na página
                if (textoNumerico.contains(cnpjCliente) && textoNumerico.contains(cnpjGocil)) {
                    return extrairNumeroDoTexto(textoPagina);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao ler PDF de cálculo: " + e.getMessage());
        }
        return "0";
    }

    private static String extrairNumeroDoTexto(String texto) {
        // Regex para buscar o padrão "Total de Funcionários: X" ou apenas o número isolado
        // Adaptado para o layout que você costuma usar
        Pattern pattern = Pattern.compile("(?i)(?:Total\\s+de\\s+Funcionários|Total\\s+Trabalhadores|Qtd\\s+Total):?\\s*(\\d+)");
        Matcher matcher = pattern.matcher(texto);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // Fallback: se não achar pelo rótulo, busca o último número significativo do rodapé
        // (Ajustar conforme a necessidade do layout específico)
        return "0";
    }
}
