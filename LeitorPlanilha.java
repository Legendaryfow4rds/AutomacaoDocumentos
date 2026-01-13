package BancadaDeTestes;
import org.apache.poi.ss.usermodel.*;
import java.util.ArrayList;
import java.util.List;

public class LeitorPlanilha {

    public static List<ClienteDTO> lerDados(Workbook workbook) {
        List<ClienteDTO> listaClientes = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        // Busca a aba pelo nome exato conforme o seu arquivo real
        Sheet sheet = workbook.getSheet("ENTREGAS");
        if (sheet == null) {
            sheet = workbook.getSheetAt(0); // Caso mudem o nome da aba, pega a primeira
        }

        System.out.println("📖 Lendo aba: " + sheet.getSheetName());

        // Começa da linha 1 (pula cabeçalho)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            // Validações: linha nula ou OCULTA por filtro
            if (row == null || row.getZeroHeight()) continue;

            // Extração das colunas G(6), H(7) e I(8)
            String nome = formatter.formatCellValue(row.getCell(6)).trim();
            String tipo = formatter.formatCellValue(row.getCell(7)).trim();
            String cnpj = formatter.formatCellValue(row.getCell(8)).trim();

            if (!nome.isEmpty() && !cnpj.isEmpty()) {
                ClienteDTO cliente = new ClienteDTO();
                cliente.setNome(nome);
                cliente.setTipo(tipo);
                cliente.setCnpj(cnpj);

                listaClientes.add(cliente);
            }
        }

        return listaClientes;
    }
}
