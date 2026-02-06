package BancadaDeTestes;

import org.apache.poi.ss.usermodel.*;
import java.util.ArrayList;
import java.util.List;

public class LeitorPlanilha {

    /**
     * Lê a planilha de entregas e converte cada linha válida num objeto ClienteDTO.
     */
    public static List<ClienteDTO> lerDados(Workbook workbook) {
        List<ClienteDTO> listaClientes = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        // Tenta buscar a aba "ENTREGAS", se não achar, pega a primeira aba disponível
        Sheet sheet = workbook.getSheet("ENTREGAS");
        if (sheet == null) {
            sheet = workbook.getSheetAt(0);
        }

        // Começa do 1 para pular o cabeçalho
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            // Pula linhas vazias ou ocultas
            if (row == null || row.getZeroHeight()) {
                continue;
            }

            // Coluna A (Índice 0) - Competência
            String compRaw = formatter.formatCellValue(row.getCell(0)).trim();
            if (compRaw.isEmpty()) continue;

            ClienteDTO cliente = new ClienteDTO();

            // Mapeamento das colunas conforme sua estrutura atual:
            cliente.setCompetencia(formatarData(compRaw));              // Coluna A
            cliente.setMatrizFilial(formatter.formatCellValue(row.getCell(5)).trim()); // Coluna F
            cliente.setNome(formatter.formatCellValue(row.getCell(6)).trim());         // Coluna G
            cliente.setTipo(formatter.formatCellValue(row.getCell(7)).trim());         // Coluna H
            cliente.setCnpj(formatter.formatCellValue(row.getCell(8)).trim());         // Coluna I

            // Guarda o número da linha real (i + 1) para o relatório de erros
            cliente.setLinhaPlanilha(i + 1);

            // Validação básica: Só adiciona se tiver Nome e CNPJ
            if (!cliente.getNome().isEmpty() && !cliente.getCnpj().isEmpty()) {
                listaClientes.add(cliente);
            }
        }
        return listaClientes;
    }

    /**
     * Padroniza a data para usar pontos (MM.AAAA) para evitar erros em nomes de pastas.
     */
    private static String formatarData(String dataRaw) {
        return dataRaw.replace("/", ".").replace("-", ".");
    }
}
