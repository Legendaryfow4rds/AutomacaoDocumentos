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

            // COMPETENCIA está na Coluna 0 (A)
            String compRaw = formatter.formatCellValue(row.getCell(0)).trim();
            String competencia = formatarData(compRaw);

            // NOME (Coluna 6), TIPO (Coluna 7), CNPJ (Coluna 8)
            String nome = formatter.formatCellValue(row.getCell(6)).trim();
            String tipo = formatter.formatCellValue(row.getCell(7)).trim();
            String cnpj = formatter.formatCellValue(row.getCell(8)).trim();

            if (!nome.isEmpty() && !cnpj.isEmpty()) {
                ClienteDTO cliente = new ClienteDTO();
                cliente.setNome(nome);
                cliente.setTipo(tipo);
                cliente.setCnpj(cnpj);
                cliente.setCompetencia(competencia);
                cliente.setLinhaPlanilha(i + 1);
                listaClientes.add(cliente);
            }
        }
        return listaClientes;
    }

    private static String formatarData(String dataRaw) {
        // Trata 2025-11-01 para 11.2025
        if (dataRaw.contains("-") && dataRaw.length() >= 7) {
            String[] partes = dataRaw.split("-");
            return partes[1] + "." + partes[0];
        }
        return dataRaw.replace("/", ".");
    }
}
