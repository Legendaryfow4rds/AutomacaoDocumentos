package BancadaDeTestes;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import java.io.File;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessadorComprovantes {

    private static Map<Integer, String> cacheTextoVig = new HashMap<>();
    private static Map<Integer, String> cacheTextoServ = new HashMap<>();

    public static int executar(List<ColaboradorDTO> colaboradores, String pathVig, String pathServ, String pastaRaiz, TextArea log) {
        int contadorSucessos = 0;

        try {
            String compVig = extrairCompetencia(pathVig);
            String compServ = extrairCompetencia(pathServ);

            // Mapeando PDFs
            cacheTextoVig = carregarTextoParaMemoria(pathVig);
            cacheTextoServ = carregarTextoParaMemoria(pathServ);

            // O MAPA agora guarda uma lista de "PaginaOrdenada", que contém o Nome para podermos ordenar depois
            Map<String, List<PaginaOrdenada>> acoesVig = new TreeMap<>();
            Map<String, List<PaginaOrdenada>> acoesServ = new TreeMap<>();

            updateLog(log, "🔎 Localizando e organizando por ordem alfabética...\n");

            for (ColaboradorDTO colab : colaboradores) {
                if (AutomacaoPrincipalV9.cancelarProcesso) break;

                String codigoCol = colab.getColigada() != null ? colab.getColigada().trim() : "";
                boolean ehVig = (codigoCol.equals("1") || codigoCol.equals("5"));

                Map<Integer, String> baseBusca = ehVig ? cacheTextoVig : cacheTextoServ;
                String prefixo = ehVig ? "Vig" : "Serv";
                String competencia = ehVig ? compVig : compServ;

                String cpfBusca = colab.getCpfApenasNumeros();
                String nomeBusca = normalizarTexto(colab.getNome());

                for (Map.Entry<Integer, String> pagina : baseBusca.entrySet()) {
                    if (pagina.getValue().contains(cpfBusca) && pagina.getValue().contains(nomeBusca)) {

                        String tomadorPasta = colab.getTomador().replaceAll("[\\\\/:*?\"<>|]", " ").trim().toUpperCase();
                        String pathFinal = pastaRaiz + File.separator + tomadorPasta + File.separator + "COMPROVANTES"
                                + File.separator + String.format("%s-Comprovante-%s.pdf", prefixo, competencia);

                        // Adiciona a página vinculada ao nome do funcionário para permitir ordenação
                        PaginaOrdenada po = new PaginaOrdenada(colab.getNome(), pagina.getKey());

                        if (ehVig) {
                            acoesVig.computeIfAbsent(pathFinal, k -> new ArrayList<>()).add(po);
                        } else {
                            acoesServ.computeIfAbsent(pathFinal, k -> new ArrayList<>()).add(po);
                        }
                    }
                }
            }

            // GRAVAÇÃO COM ORDENAÇÃO INTERNA
            contadorSucessos += gravarArquivosOrdenados(pathVig, acoesVig, log);
            contadorSucessos += gravarArquivosOrdenados(pathServ, acoesServ, log);

            updateLog(log, "\n✨ Finalizado! PDFs gerados com páginas em ordem alfabética.\n");

        } catch (Exception e) {
            updateLog(log, "🚨 Erro: " + e.getMessage() + "\n");
        }
        return contadorSucessos;
    }

    private static int gravarArquivosOrdenados(String pathOrigem, Map<String, List<PaginaOrdenada>> mapaAcoes, TextArea log) {
        if (pathOrigem == null || pathOrigem.isEmpty() || mapaAcoes.isEmpty()) return 0;

        int gerados = 0;
        try (PDDocument docOriginal = Loader.loadPDF(new File(pathOrigem))) {
            for (Map.Entry<String, List<PaginaOrdenada>> entrada : mapaAcoes.entrySet()) {

                List<PaginaOrdenada> paginasDoArquivo = entrada.getValue();

                // --- O PULO DO GATO: Ordena as páginas pelo nome do colaborador antes de salvar ---
                paginasDoArquivo.sort(Comparator.comparing(p -> normalizarTexto(p.nomeColaborador)));

                File arquivoFinal = new File(entrada.getKey());
                if (!arquivoFinal.getParentFile().exists()) arquivoFinal.getParentFile().mkdirs();

                try (PDDocument novoDoc = new PDDocument()) {
                    for (PaginaOrdenada p : paginasDoArquivo) {
                        novoDoc.importPage(docOriginal.getPage(p.indicePagina));
                    }
                    novoDoc.getDocumentCatalog().setViewerPreferences(docOriginal.getDocumentCatalog().getViewerPreferences());
                    novoDoc.save(arquivoFinal);
                    gerados++;
                }
            }
        } catch (Exception e) {
            updateLog(log, "⚠️ Erro na gravação ordenada: " + e.getMessage() + "\n");
        }
        return gerados;
    }

    // Classe auxiliar para manter o vínculo Nome -> Página
    private static class PaginaOrdenada {
        String nomeColaborador;
        int indicePagina;

        PaginaOrdenada(String nome, int indice) {
            this.nomeColaborador = nome;
            this.indicePagina = indice;
        }
    }

    // --- MÉTODOS AUXILIARES (IGUAIS) ---
    private static String extrairCompetencia(String path) {
        if (path == null || path.isEmpty()) return "DATA";
        Matcher m = Pattern.compile("\\d{2}[.\\-]\\d{4}").matcher(new File(path).getName());
        return m.find() ? m.group().replace("-", ".") : "COMPETENCIA";
    }

    private static Map<Integer, String> carregarTextoParaMemoria(String p) {
        Map<Integer, String> m = new HashMap<>();
        File f = new File(p);
        if (!f.exists()) return m;
        try (PDDocument d = Loader.loadPDF(f)) {
            PDFTextStripper s = new PDFTextStripper();
            for (int i = 0; i < d.getNumberOfPages(); i++) {
                s.setStartPage(i + 1); s.setEndPage(i + 1);
                m.put(i, normalizarTexto(s.getText(d)));
            }
        } catch (Exception e) {}
        return m;
    }

    private static String normalizarTexto(String t) {
        if (t == null) return "";
        return Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toUpperCase().replaceAll("[\\.\\-/]", "").trim();
    }

    private static void updateLog(TextArea log, String msg) {
        Platform.runLater(() -> { log.appendText(msg); log.setScrollTop(Double.MAX_VALUE); });
    }
}
