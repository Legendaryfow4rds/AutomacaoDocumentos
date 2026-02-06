package BancadaDeTestes;

/**
 * Classe de transferência de dados (DTO).
 * Armazena temporariamente os dados lidos da planilha para uso no processamento.
 */
public class ClienteDTO {
    private String nome;
    private String tipo;
    private String cnpj;
    private String competencia;
    private String matrizFilial;
    private int linhaPlanilha;

    // --- Getters e Setters ---

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getCompetencia() { return competencia; }
    public void setCompetencia(String competencia) { this.competencia = competencia; }

    public String getMatrizFilial() { return matrizFilial; }
    public void setMatrizFilial(String matrizFilial) { this.matrizFilial = matrizFilial; }

    public int getLinhaPlanilha() { return linhaPlanilha; }
    public void setLinhaPlanilha(int linhaPlanilha) { this.linhaPlanilha = linhaPlanilha; }

    /**
     * Retorna o CNPJ contendo apenas os dígitos numéricos.
     * Essencial para comparar com o texto extraído do PDF.
     */
    public String getCnpjApenasNumeros() {
        return cnpj == null ? "" : cnpj.replaceAll("[^0-9]", "");
    }
}
