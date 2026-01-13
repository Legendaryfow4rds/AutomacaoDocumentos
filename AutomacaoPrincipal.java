package BancadaDeTestes;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import java.io.File;
import java.util.List;

public class AutomacaoPrincipalV3 {
    public static void main(String[] args) {
        try {
            File arquivoExcel = new File("ENTREGA MENSAL - 2025.xlsx");
            Workbook workbook = WorkbookFactory.create(arquivoExcel);

            // Chama a classe especialista
            List<ClienteDTO> clientes = LeitorPlanilha.lerDados(workbook);

            System.out.println("Total de clientes filtrados para processar: " + clientes.size());

            for (ClienteDTO c : clientes) {
                System.out.println("-> Cliente: " + c.getNome() + " | Tipo: " + c.getTipo());
            }

            workbook.close();
        } catch (Exception e) {
            System.err.println("Erro na leitura: " + e.getMessage());
        }
    }
}
