package BancadaDeTestes;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class AutomacaoPrincipalV9 {

    public static volatile boolean cancelarProcesso = false;

    /**
     * MOTOR 1: EXTRAÇÃO DE FGTS DIGITAL (PADRÃO V7)
     */
    public static void executarComParametros(String planilha, String vig, String serv, String drive, TextArea log) {
        cancelarProcesso = false;
        int contadorSucessos = 0;
        List<String> falhasDetalhadas = new ArrayList<>();

        File arquivoReferencia = new File(vig.isEmpty() ? serv : vig);
        File pastaMestres = arquivoReferencia.getParentFile();

        try (FileInputStream fis = new FileInputStream(new File(planilha));
             Workbook workbook = WorkbookFactory.create(fis)) {

            List<ClienteDTO> clientes = LeitorPlanilha.lerDadosEntregas(workbook);
            updateLog(log, "🚀 Iniciando Extração FGTS Digital...\n\n");

            for (ClienteDTO c : clientes) {
                if (cancelarProcesso) break;

                try {
                    String nomeLimpo = c.getNome().replaceAll("[\\\\/:*?\"<>|]", " ").trim();
                    boolean ehServico = c.getTipo().toUpperCase().contains("SERV");
                    String pdfMestre = ehServico ? serv : vig;
                    String cnpjGocil = DicionarioFiliais.obterCnpjCorreto(c.getMatrizFilial(), c.getTipo());

                    if (cnpjGocil.isEmpty()) {
                        falhasDetalhadas.add("❌ Filial não mapeada: " + c.getMatrizFilial());
                        continue;
                    }

                    String compFormatada = c.getCompetencia().replace("/", ".");
                    String subPastaTipo = ehServico ? "SERVIÇO" : "VIGILÂNCIA";
                    String pastaDestino = drive + File.separator + nomeLimpo + File.separator + compFormatada + File.separator + subPastaTipo;

                    String prefixo = ehServico ? "Serv" : "Vig";
                    String nomeArquivo = String.format("%s. Relatório FGTS-%s-%s-%s.pdf",
                            prefixo, c.getMatrizFilial().toUpperCase().replace(" ", ""),
                            c.getCnpjApenasNumeros(), compFormatada);

                    String resultado = ProcessadorPDF.extrairPorCnpj(c, cnpjGocil, pdfMestre, pastaDestino, nomeArquivo);

                    if (resultado.equals("OK")) {
                        boolean ehBahia = c.getMatrizFilial().toUpperCase().contains("BAHIA");
                        String guia = localizarGuia(pastaMestres, ehServico ? "SERV" : "VIG", ehBahia ? "BA" : "");
                        if (guia != null) GerenciadorArquivos.copiarArquivo(guia, pastaDestino);

                        updateLog(log, "✅ " + nomeLimpo + " - OK.\n");
                        contadorSucessos++;
                    } else {
                        falhasDetalhadas.add(nomeLimpo + ": " + resultado);
                    }
                } catch (Exception ex) {
                    falhasDetalhadas.add("🚨 Erro em " + c.getNome() + ": " + ex.getMessage());
                }
            }
            exibirResumoFinal(log, contadorSucessos, falhasDetalhadas);
        } catch (Exception e) {
            updateLog(log, "🚨 ERRO AO LER PLANILHA: " + e.getMessage() + "\n");
        }
    }

    /**
     * MOTOR 2: CÁLCULO DE FUNCIONÁRIOS (DUPLA AUTENTICAÇÃO)
     */
    public static void executarCalculoFuncionarios(String planilha, String pdfVig, String pdfServ, TextArea log) {
        cancelarProcesso = false;
        int contadorSucessos = 0;
        int colunaDestino = 16;
        List<String> falhasDetalhadas = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(new File(planilha));
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<ClienteDTO> clientes = LeitorPlanilha.lerDadosEntregas(workbook);

            updateLog(log, "🚀 Iniciando Cálculo com Dupla Autenticação...\n");

            for (ClienteDTO c : clientes) {
                if (cancelarProcesso) break;

                String cnpjGocil = DicionarioFiliais.obterCnpjCorreto(c.getMatrizFilial(), c.getTipo());
                String cnpjAlvo = c.getCnpjApenasNumeros();

                if (cnpjGocil.isEmpty()) {
                    updateLog(log, "⚠️ Filial não reconhecida: " + c.getMatrizFilial() + "\n");
                    falhasDetalhadas.add("❌ Filial Inválida: " + c.getMatrizFilial());
                    continue;
                }

                String total = BuscadorCalculoPDF.extrairTotalFuncionarios(pdfVig, cnpjAlvo, cnpjGocil);
                if (total.equals("0")) {
                    total = BuscadorCalculoPDF.extrairTotalFuncionarios(pdfServ, cnpjAlvo, cnpjGocil);
                }

                Row row = sheet.getRow(c.getLinhaPlanilha() - 1);
                if (row == null) row = sheet.createRow(c.getLinhaPlanilha() - 1);
                Cell cell = row.getCell(colunaDestino);
                if (cell == null) cell = row.createCell(colunaDestino);

                if (!total.equals("0")) {
                    try {
                        cell.setCellValue(Integer.parseInt(total.replaceAll("[^0-9]", "")));
                    } catch (NumberFormatException e) {
                        cell.setCellValue(total);
                    }
                    updateLog(log, "✅ " + c.getNome() + ": " + total + "\n");
                    contadorSucessos++;
                } else {
                    cell.setCellValue("Sem Funcionários");
                    falhasDetalhadas.add("⚠️ Não encontrado: " + c.getNome());
                }
            }

            try (FileOutputStream fos = new FileOutputStream(new File(planilha))) {
                workbook.write(fos);
            }
            exibirResumoFinal(log, contadorSucessos, falhasDetalhadas);

        } catch (Exception e) {
            updateLog(log, "🚨 ERRO CRÍTICO: " + e.getMessage() + "\n");
        }
    }

    /**
     * MOTOR 3: SEPARAÇÃO DE COMPROVANTES (TRI-VALIDAÇÃO E BUSCA DINÂMICA)
     * Atualizado para suportar 2 PDFs e busca linha a linha por nome de coluna.
     */
    public static void executarSeparacaoComprovantes(String planilhaApoio, String pdfVig, String pdfServ, String pastaRaiz, TextArea log) {
        cancelarProcesso = false;
        List<String> falhasDetalhadas = new ArrayList<>();
        int contadorSucessos = 0;

        try (FileInputStream fis = new FileInputStream(new File(planilhaApoio));
             Workbook workbook = WorkbookFactory.create(fis)) {

            updateLog(log, "🚀 Iniciando Motor 3: Separação de Comprovantes...\n");
            updateLog(log, "📊 Mapeando colunas dinamicamente (COL, NOME, CPF, TOMADOR, CNPJ)...\n");

            // 1. Leitura Dinâmica da Planilha (Aba ATIVOS)
            List<ColaboradorDTO> colaboradores = LeitorPlanilha.lerDadosAtivosDinamico(workbook);

            if (colaboradores.isEmpty()) {
                updateLog(log, "⚠️ Nenhuma linha válida encontrada na aba 'ATIVOS'.\n");
                return;
            }

            // 2. Disparo do Processador (Este método faz a ordenação A-Z e a Tripla Validação)
            // Ele retorna o número de sucessos para alimentarmos o relatório final
            contadorSucessos = ProcessadorComprovantes.executar(colaboradores, pdfVig, pdfServ, pastaRaiz, log);

            // 3. Relatório Final (O resumo é gerado dentro do Processador ou aqui no final)
            if (cancelarProcesso) {
                falhasDetalhadas.add("Processo interrompido manualmente pelo usuário.");
            }

            exibirResumoFinal(log, contadorSucessos, falhasDetalhadas);

        } catch (Exception e) {
            updateLog(log, "🚨 ERRO CRÍTICO NO MOTOR 3: " + e.getMessage() + "\n");
            falhasDetalhadas.add("Erro na execução: " + e.getMessage());
            exibirResumoFinal(log, 0, falhasDetalhadas);
        }
    }

    private static void exibirResumoFinal(TextArea log, int sucessos, List<String> falhas) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=" .repeat(60)).append("\n");
        sb.append("         📊 RELATÓRIO FINAL DE EXECUÇÃO\n");
        sb.append("=" .repeat(60)).append("\n");
        sb.append(String.format(" ✅ PROCESSADOS/SUCESSOS: %d\n", sucessos));
        sb.append(String.format(" ❌ FALHAS/PENDÊNCIAS: %d\n", falhas.size()));
        sb.append("-" .repeat(60)).append("\n");

        if (!falhas.isEmpty()) {
            sb.append(" ⚠️ OBSERVAÇÕES:\n");
            for (String erro : falhas) {
                sb.append(" -> ").append(erro).append("\n");
            }
        } else {
            sb.append(" 🎉 PROCESSO CONCLUÍDO COM SUCESSO!\n");
        }
        sb.append("=" .repeat(60)).append("\n");

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
