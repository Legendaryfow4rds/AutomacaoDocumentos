package BancadaDeTestes;

import org.apache.poi.ss.usermodel.*;
import java.util.ArrayList;
import java.util.List;

public class LeitorPlanilha {

    /**
     * Lê a planilha de entregas e converte cada linha válida num objeto ClienteDTO.
     * Focando nas Colunas F (Filial) e I (CNPJ Cliente) para Dupla Autenticação.
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

            // Coluna A (Índice 0) - Competência (Se vazio, pula a linha)
            String compRaw = formatter.formatCellValue(row.getCell(0)).trim();
            if (compRaw.isEmpty()) continue;

            ClienteDTO cliente = new ClienteDTO();

            // === MAPEAMENTO DE COLUNAS ATUALIZADO ===

            // Coluna A (0): Competência (MM.AAAA)
            cliente.setCompetencia(formatarData(compRaw));

            // Coluna F (5): Matriz/Filial -> Usado para buscar o CNPJ da GOCIL no Dicionário
            cliente.setMatrizFilial(formatter.formatCellValue(row.getCell(5)).trim());

            // Coluna G (6): Nome do Cliente
            cliente.setNome(formatter.formatCellValue(row.getCell(6)).trim());

            // Coluna H (7): Tipo de Serviço (VIG ou SERV)
            cliente.setTipo(formatter.formatCellValue(row.getCell(7)).trim());

            // Coluna I (8): CNPJ do Cliente -> Usado para localizar o cliente dentro do PDF
            cliente.setCnpj(formatter.formatCellValue(row.getCell(8)).trim());

            // Guarda a linha real para que o robô saiba onde escrever o resultado depois na Coluna Q
            cliente.setLinhaPlanilha(i + 1);

            // Validação: Adiciona apenas se tiver dados essenciais para a busca
            if (!cliente.getNome().isEmpty() && !cliente.getCnpj().isEmpty()) {
                listaClientes.add(cliente);
            }
        }
        return listaClientes;
    }

    /**
     * Padroniza a data para MM.AAAA
     */
    private static String formatarData(String dataRaw) {
        return dataRaw.replace("/", ".").replace("-", ".");
    }
}
