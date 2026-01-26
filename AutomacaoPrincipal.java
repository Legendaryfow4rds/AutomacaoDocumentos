package BancadaDeTestes;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import javax.swing.JTextArea;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AutomacaoPrincipalV4 {

    public static void executarComParametros(String planilha, String vig, String serv, String drive, JTextArea log) {
        int contadorSucessos = 0;
        List<String> falhasDetalhadas = new ArrayList<>();
        File pastaMestres = new File(vig).getParentFile();

        try (Workbook workbook = WorkbookFactory.create(new File(planilha))) {
            List<ClienteDTO> clientes = LeitorPlanilha.lerDados(workbook);
            log.append("🚀 Iniciando processamento de " + clientes.size() + " registros...\n\n");

            for (ClienteDTO c : clientes) {
                try {
                    String nomeLimpo = c.getNome().replaceAll("[\\\\/:*?\"<>|]", " ").trim();
                    boolean ehServico = c.getTipo().toUpperCase().contains("SERV");
                    String pdfMestre = ehServico ? serv : vig;

                    String cnpjGocil = DicionarioFiliais.obterCnpjCorreto(c.getMatrizFilial(), c.getTipo());

                    if (cnpjGocil.isEmpty()) {
                        String erro = "Filial '" + c.getMatrizFilial() + "' não mapeada no Dicionário.";
                        log.append("⚠️ " + nomeLimpo + ": " + erro + "\n");
                        falhasDetalhadas.add(nomeLimpo + " [" + c.getMatrizFilial() + "] -> " + erro);
                        continue;
                    }

                    String subPastaTipo = ehServico ? "SERVIÇO" : "VIGILÂNCIA";
                    String caminhoFinalDestino = drive + File.separator + nomeLimpo +
                            File.separator + c.getCompetencia() +
                            File.separator + subPastaTipo;

                    String resultado = ProcessadorPDF.extrairPorCnpj(c, cnpjGocil, pdfMestre, caminhoFinalDestino);

                    if (resultado.equals("OK")) {
                        boolean ehBahia = c.getMatrizFilial().toUpperCase().contains("BAHIA");
                        String termo = ehServico ? "SERV" : "VIG";
                        String guia = localizarGuia(pastaMestres, termo, ehBahia ? "BA" : "");

                        if (guia != null) {
                            GerenciadorArquivos.copiarArquivo(guia, caminhoFinalDestino);
                        }

                        log.append("✅ " + nomeLimpo + " [" + c.getMatrizFilial() + "] - Processado.\n");
                        contadorSucessos++;
                    } else {
                        log.append("❌ " + nomeLimpo + " [" + c.getMatrizFilial() + "] - " + resultado + "\n");
                        falhasDetalhadas.add(nomeLimpo + " [" + c.getMatrizFilial() + "] -> " + resultado);
                    }

                } catch (Exception ex) {
                    falhasDetalhadas.add(c.getNome() + " -> ERRO: " + ex.getMessage());
                }
            }

            // --- RELATÓRIO FINAL ---
            log.append("\n" + "=".repeat(40) + "\n");
            log.append("       RESUMO DA EXECUÇÃO\n");
            log.append("=".repeat(40) + "\n");
            log.append(String.format("✅ SUCESSOS: %d\n", contadorSucessos));
            log.append(String.format("❌ FALHAS:   %d\n", falhasDetalhadas.size()));
            log.append("=".repeat(40) + "\n");

            if (!falhasDetalhadas.isEmpty()) {
                log.append("\nDETALHAMENTO DAS FALHAS:\n");
                for (String falha : falhasDetalhadas) {
                    log.append("• " + falha + "\n");
                }
                log.append("=".repeat(40) + "\n");
            }

        } catch (Exception e) {
            log.append("🚨 ERRO CRÍTICO: " + e.getMessage() + "\n");
        }
    }

    private static String localizarGuia(File pasta, String tipo, String estado) {
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
