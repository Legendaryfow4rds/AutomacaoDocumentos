package BancadaDeTestes;

public class ClienteDTO {
    private String nome;
    private String tipo;
    private String cnpj;
    private String competencia; // Ex: 01.2025
    private int linhaPlanilha;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getCompetencia() { return competencia; }
    public void setCompetencia(String competencia) { this.competencia = competencia; }

    public int getLinhaPlanilha() { return linhaPlanilha; }
    public void setLinhaPlanilha(int linhaPlanilha) { this.linhaPlanilha = linhaPlanilha; }

    public String getCnpjApenasNumeros() {
        return cnpj == null ? "" : cnpj.replaceAll("[^0-9]", "");
    }
}
