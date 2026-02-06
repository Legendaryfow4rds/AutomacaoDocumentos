package BancadaDeTestes;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AutomacaoPrincipalV7 {

    public static volatile boolean cancelarProcesso = false;

    public static void executarComParametros(String planilha, String vig, String serv, String drive, TextArea log) {
        cancelarProcesso = false;
        int contadorSucessos = 0;
        List<String> falhasDetalhadas = new ArrayList<>();

        // Pasta onde estão os PDFs mestres para buscar as Guias
        File arquivoReferencia = new File(vig.isEmpty() ? serv : vig);
        File pastaMestres = arquivoReferencia.getParentFile();

        try (Workbook workbook = WorkbookFactory.create(new File(planilha))) {
            List<ClienteDTO> clientes = LeitorPlanilha.lerDados(workbook);

            updateLog(log, "🚀 Iniciando processamento de " + clientes.size() + " registros...\n\n");

            for (ClienteDTO c : clientes) {
                if (cancelarProcesso) {
                    updateLog(log, "\n🛑 PROCESSO INTERROMPIDO PELO USUÁRIO.\n");
                    break;
                }

                try {
                    String nomeLimpo = c.getNome().replaceAll("[\\\\/:*?\"<>|]", " ").trim();
                    boolean ehServico = c.getTipo().toUpperCase().contains("SERV");
                    String pdfMestre = ehServico ? serv : vig;

                    // 1. Busca o CNPJ da Gocil (Filial) para localizar no PDF Mestre
                    String cnpjGocil = DicionarioFiliais.obterCnpjCorreto(c.getMatrizFilial(), c.getTipo());

                    if (cnpjGocil.isEmpty()) {
                        String erro = String.format("❌ %s (Linha %d): Filial '%s' não mapeada no Dicionário.",
                                nomeLimpo, c.getLinhaPlanilha(), c.getMatrizFilial());
                        updateLog(log, erro + "\n");
                        falhasDetalhadas.add(erro);
                        continue;
                    }

                    // 2. Prepara caminhos e nomes (Novo padrão encurtado com CNPJ do Cliente)
                    String compFormatada = c.getCompetencia().replace("/", ".");
                    String subPastaTipo = ehServico ? "SERVIÇO" : "VIGILÂNCIA";
                    String cnpjCliente = c.getCnpjApenasNumeros();

                    String pastaDestino = drive + File.separator + nomeLimpo +
                            File.separator + compFormatada +
                            File.separator + subPastaTipo;

                    String prefixo = ehServico ? "Serv" : "Vig";

                    // --- EDIÇÃO SOLICITADA: Retirado espaços e adicionado CNPJ do cliente ---
                    String nomeArquivo = String.format("%s. Relatório FGTS-%s-%s-%s.pdf",
                            prefixo,
                            c.getMatrizFilial().toUpperCase().replace(" ", ""), // Remove espaços do nome da filial também
                            cnpjCliente,
                            compFormatada);

                    // 3. Executa a extração das páginas do PDF
                    String resultado = ProcessadorPDF.extrairPorCnpj(c, cnpjGocil, pdfMestre, pastaDestino, nomeArquivo);

                    if (resultado.equals("OK")) {
                        // 4. Busca e copia a Guia Mestre (comprovante de pagamento)
                        boolean ehBahia = c.getMatrizFilial().toUpperCase().contains("BAHIA");
                        String guia = localizarGuia(pastaMestres, ehServico ? "SERV" : "VIG", ehBahia ? "BA" : "");

                        if (guia != null) {
                            GerenciadorArquivos.copiarArquivo(guia, pastaDestino);
                        }

                        updateLog(log, "✅ " + nomeLimpo + " - OK.\n");
                        contadorSucessos++;
                    } else {
                        String erro = String.format("❌ %s (Linha %d): %s", nomeLimpo, c.getLinhaPlanilha(), resultado);
                        updateLog(log, erro + "\n");
                        falhasDetalhadas.add(erro);
                    }

                } catch (Exception ex) {
                    String erro = "🚨 Erro em " + c.getNome() + " (Linha " + c.getLinhaPlanilha() + "): " + ex.getMessage();
                    updateLog(log, erro + "\n");
                    falhasDetalhadas.add(erro);
                }
            }

            exibirResumoFinal(log, contadorSucessos, falhasDetalhadas);

        } catch (Exception e) {
            updateLog(log, "🚨 ERRO CRÍTICO AO LER PLANILHA: " + e.getMessage() + "\n");
        }
    }

    private static void exibirResumoFinal(TextArea log, int sucessos, List<String> falhas) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(60)).append("\n");
        sb.append("📊 RELATÓRIO FINAL DE PROCESSAMENTO\n");
        sb.append("✅ SUCESSOS: ").append(sucessos).append("\n");
        sb.append("❌ FALHAS: ").append(falhas.size()).append("\n");
        sb.append("=".repeat(60)).append("\n");

        if (!falhas.isEmpty()) {
            sb.append("⚠️ DETALHAMENTO DOS ERROS:\n\n");
            for (String f : falhas) {
                sb.append(f).append("\n");
            }
            sb.append("=".repeat(60)).append("\n");
        }

        updateLog(log, sb.toString());
    }

    private static void updateLog(TextArea log, String msg) {
        if (log != null) {
            Platform.runLater(() -> {
                log.appendText(msg);
                log.setScrollTop(Double.MAX_VALUE);
            });
        }
    }

    private static String localizarGuia(File pasta, String tipo, String estado) {
        if (pasta == null || !pasta.exists()) return null;
        File[] files = pasta.listFiles();
        if (files == null) return null;

        for (File f : files) {
            String n = f.getName().toUpperCase();
            if (n.contains("GUIA") && n.contains(tipo)) {
                if (!estado.isEmpty() && n.contains(estado)) return f.getAbsolutePath();
                if (estado.isEmpty() && !n.contains(" BA")) return f.getAbsolutePath();
            }
        }
        return null;
    }
}
