package BancadaDeTestes;

import org.apache.poi.ss.usermodel.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeitorPlanilha {

    /**
     * MOTOR 1 & 2: Lê a planilha de ENTREGAS.
     * Mantido conforme o padrão original para não afetar o Extrator FGTS.
     */
    public static List<ClienteDTO> lerDadosEntregas(Workbook workbook) {
        List<ClienteDTO> listaClientes = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        Sheet sheet = workbook.getSheet("ENTREGAS");
        if (sheet == null) {
            sheet = workbook.getSheetAt(0);
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || row.getZeroHeight()) continue;

            String compRaw = formatter.formatCellValue(row.getCell(0)).trim();
            if (compRaw.isEmpty()) continue;

            ClienteDTO cliente = new ClienteDTO();
            cliente.setCompetencia(compRaw.replace("/", ".").replace("-", "."));
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

    /**
     * MOTOR 3: Lê a planilha de ATIVOS com mapeamento DINÂMICO.
     * Busca as colunas pelos nomes, ignorando a ordem em que aparecem.
     */
    public static List<ColaboradorDTO> lerDadosAtivosDinamico(Workbook workbook) {
        List<ColaboradorDTO> lista = new ArrayList<>();
        Sheet sheet = workbook.getSheet("ATIVOS");
        if (sheet == null) return lista;

        // 1. MAPEAMENTO DINÂMICO DE COLUNAS (Nomes das colunas)
        Row header = sheet.getRow(0);
        Map<String, Integer> colMap = new HashMap<>();
        for (Cell cell : header) {
            String val = cell.getStringCellValue().toUpperCase().trim();
            colMap.put(val, cell.getColumnIndex());
        }

        // 2. LEITURA DAS LINHAS RESPEITANDO O FILTRO
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // VERIFICAÇÃO DE FILTRO:
            // Se a linha estiver escondida (filtrada), a altura será 0 ou isZeroHeight será true
            if (row.getZeroHeight() || row.getHeight() <= 0) {
                continue; // Pula essa linha, pois está filtrada/escondida
            }

            try {
                ColaboradorDTO dto = new ColaboradorDTO();

                // Atribuição baseada no mapeamento das colunas
                dto.setColigada(getCellValue(row, colMap.get("COL")));
                dto.setNome(getCellValue(row, colMap.get("NOME COLABORADOR")));
                dto.setCpf(getCellValue(row, colMap.get("CPF")));
                dto.setTomador(getCellValue(row, colMap.get("TOMADOR")));
                dto.setCnpjTomador(getCellValue(row, colMap.get("CNPJ TOMADOR")));

                // Só adiciona se houver CPF e Nome
                if (dto.getCpf() != null && !dto.getCpf().isEmpty()) {
                    lista.add(dto);
                }
            } catch (Exception e) {
                // Log de erro de linha se necessário
            }
        }
        return lista;
    }

    // Método auxiliar para evitar NullPointerException nas células
    private static String getCellValue(Row row, Integer colIndex) {
        if (colIndex == null) return "";
        Cell cell = row.getCell(colIndex);
        if (cell == null) return "";

        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }
}
