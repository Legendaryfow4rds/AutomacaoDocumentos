package BancadaDeTestes;

public class Main {
    public static void main(String[] args) {
        // Esta chamada indireta contorna a verificação de módulos do JavaFX
        // e permite que o JAR seja executado sem parâmetros complexos de VM.
        InterfaceAutomacaoFX.main(args);
    }
}
