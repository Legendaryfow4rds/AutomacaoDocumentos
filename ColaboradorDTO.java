package BancadaDeTestes;

public class ColaboradorDTO {
    private String coligada;
    private String nome;
    private String cpf;
    private String tomador;
    private String cnpjTomador;

    // Getters e Setters
    public String getColigada() { return coligada; }
    public void setColigada(String coligada) { this.coligada = coligada; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getTomador() { return tomador; }
    public void setTomador(String tomador) { this.tomador = tomador; }

    public String getCnpjTomador() { return cnpjTomador; }
    public void setCnpjTomador(String cnpjTomador) { this.cnpjTomador = cnpjTomador; }

    // Utilitário para busca no PDF
    public String getCpfApenasNumeros() {
        return cpf == null ? "" : cpf.replaceAll("[^0-9]", "");
    }
}
