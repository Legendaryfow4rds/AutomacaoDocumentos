package BancadaDeTestes;

import org.apache.poi.ss.usermodel.*;
import java.util.ArrayList;
import java.util.List;

public class LeitorPlanilha {
    public static List<ClienteDTO> lerDados(Workbook workbook) {
        List<ClienteDTO> listaClientes = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        Sheet sheet = workbook.getSheet("ENTREGAS");
        if (sheet == null) sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || row.getZeroHeight()) continue;

            String compRaw = formatter.formatCellValue(row.getCell(0)).trim();
            if (compRaw.isEmpty()) continue;

            ClienteDTO cliente = new ClienteDTO();
            cliente.setCompetencia(formatarData(compRaw));
            cliente.setMatrizFilial(formatter.formatCellValue(row.getCell(5)).trim()); // Coluna F
            cliente.setNome(formatter.formatCellValue(row.getCell(6)).trim());         // Coluna G
            cliente.setTipo(formatter.formatCellValue(row.getCell(7)).trim());         // Coluna H
            cliente.setCnpj(formatter.formatCellValue(row.getCell(8)).trim());         // Coluna I
            cliente.setLinhaPlanilha(i + 1);

            if (!cliente.getNome().isEmpty() && !cliente.getCnpj().isEmpty()) {
                listaClientes.add(cliente);
            }
        }
        return listaClientes;
    }

    private static String formatarData(String dataRaw) {
        return dataRaw.replace("/", ".").replace("-", ".");
    }
}
