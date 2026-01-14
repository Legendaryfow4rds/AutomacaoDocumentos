package BancadaDeTestes;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import java.io.File;
import java.util.List;

public class AutomacaoPrincipalV3 {

    public static void main(String[] args) {
        // AJUSTE ESTES CAMINHOS
        String pastaRaiz = "C:\\Users\\abner.silva\\Documents\\ProjetoGocil\\";
        String planilhaNome = "ENTREGA MENSAL - 2025.xlsx";

        try (Workbook workbook = WorkbookFactory.create(new File(pastaRaiz + planilhaNome))) {
            List<ClienteDTO> clientes = LeitorPlanilha.lerDados(workbook);
            System.out.println("🚀 Processando " + clientes.size() + " clientes via CNPJ...");

            for (ClienteDTO cliente : clientes) {
                // Determina o arquivo PDF base
                String arquivoBase = cliente.getTipo().toUpperCase().contains("SERV")
                        ? "SERV. - Relatório de Tomador GFD - 11.2025.pdf"
                        : "VIG. - Relatório de Tomador GFD - 11.2025.pdf";

                String caminhoCompleto = pastaRaiz + arquivoBase;

                System.out.println("⏳ Buscando CNPJ: " + cliente.getCnpj() + " (" + cliente.getNome() + ")");

                // Chama o novo método focado em CNPJ
                String resultado = ProcessadorPDF.extrairPorCnpj(cliente, caminhoCompleto, pastaRaiz);

                if (resultado.startsWith("OK:")) {
                    System.out.println("   ✅ Arquivo Gerado: " + resultado.substring(3));
                } else {
                    System.err.println("   ❌ " + resultado);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro fatal: " + e.getMessage());
        }
    }
}
