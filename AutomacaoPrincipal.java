package BancadaDeTestes;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import javax.swing.JTextArea;
import java.io.File;
import java.util.List;

public class AutomacaoPrincipalV4 {

    public static void executarComParametros(String planilha, String vig, String serv, String drive, JTextArea log) {
        int sucessos = 0;
        int erros = 0;

        File pastaBase = new File(vig).getParentFile();

        try (Workbook workbook = WorkbookFactory.create(new File(planilha))) {
            List<ClienteDTO> clientes = LeitorPlanilha.lerDados(workbook);
            escrever(log, "📊 Iniciando processamento de " + clientes.size() + " clientes.");

            for (ClienteDTO c : clientes) {
                boolean eServico = c.getTipo().toUpperCase().contains("SERV");

                // 1. Identifica qual PDF mestre usar (VIG ou SERV)
                String pdfMestreCaminho = eServico ? serv : vig;
                File arquivoMestre = new File(pdfMestreCaminho);
                String nomeMestre = arquivoMestre.getName().toUpperCase();

                // 2. Lógica de Detecção de Estado (ex: BA)
                // Se o nome do PDF mestre contém "BA", a busca do comprovante também usará "BA"
                String sufixoEstado = nomeMestre.contains("BA") ? "BA" : "";
                String termoServico = eServico ? "SERV" : "VIG";

                // 3. Busca o Comprovante que casa com o Tipo E com o Estado
                String caminhoComprovante = localizarComprovanteInteligente(pastaBase, termoServico, sufixoEstado);

                String pastaDestino = GerenciadorArquivos.obterCaminhoDestino(c, drive);

                escrever(log, "• Processando: " + c.getNome() + (sufixoEstado.isEmpty() ? "" : " [" + sufixoEstado + "]"));

                // 4. Copia o Comprovante (Se encontrado)
                if (caminhoComprovante != null) {
                    GerenciadorArquivos.copiarArquivo(caminhoComprovante, pastaDestino);
                } else {
                    escrever(log, "  ⚠️ Alerta: Comprovante " + termoServico + " " + sufixoEstado + " não encontrado.");
                }

                // 5. Extração do PDF Individual
                String res = ProcessadorPDF.extrairPorCnpj(c, pdfMestreCaminho, pastaDestino);

                if (res.startsWith("OK:")) {
                    escrever(log, "  ✅ Kit " + (sufixoEstado.isEmpty() ? "Padrão" : sufixoEstado) + " arquivado.");
                    sucessos++;
                } else {
                    escrever(log, "  ❌ Erro: " + res);
                    erros++;
                }
            }
            escrever(log, "\n========================================");
            escrever(log, "🏁 FINALIZADO: " + sucessos + " Sucessos.");
            escrever(log, "========================================");

        } catch (Exception e) {
            escrever(log, "❌ ERRO: " + e.getMessage());
        }
    }

    /**
     * Localiza o comprovante baseado no tipo (SERV/VIG) e na presença ou não do estado (BA)
     */
    private static String localizarComprovanteInteligente(File pasta, String tipo, String estado) {
        File[] arquivos = pasta.listFiles();
        if (arquivos != null) {
            for (File f : arquivos) {
                String nome = f.getName().toUpperCase();

                boolean ehPdf = nome.endsWith(".PDF");
                boolean ehGuia = nome.contains("GUIA") && nome.contains("COMPROV");
                boolean ehTipo = nome.contains(tipo.toUpperCase());

                // Se 'estado' for "BA", o arquivo PRECISA conter "BA".
                // Se 'estado' for vazio, o arquivo NÃO DEVE conter "BA" (para não pegar o errado).
                boolean condicaoEstado;
                if (!estado.isEmpty()) {
                    condicaoEstado = nome.contains(estado.toUpperCase());
                } else {
                    condicaoEstado = !nome.contains("BA"); // Evita pegar o da Bahia no fluxo padrão
                }

                if (ehPdf && ehGuia && ehTipo && condicaoEstado) {
                    return f.getAbsolutePath();
                }
            }
        }
        return null;
    }

    private static void escrever(JTextArea log, String txt) {
        if (log != null) {
            log.append(txt + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        }
    }
}
