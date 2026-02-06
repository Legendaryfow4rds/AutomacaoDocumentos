package BancadaDeTestes;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.util.Properties;

public class InterfaceAutomacaoFX extends Application {

    private TextField txtPlanilha, txtPdfVig, txtPdfServ, txtDrive;
    private TextArea areaLog;
    private Button btnIniciar, btnParar;
    private final String CONFIG_FILE = "config_extrator.properties";

    @Override
    public void start(Stage stage) {
        stage.setTitle("Gocil - Extrator FGTS Digital");

        // --- LAYOUT PRINCIPAL ---
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.getStyleClass().add("root"); // Define a classe para o CSS

        // --- PAINEL DE ENTRADAS ---
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        txtPlanilha = criarLinhaInput(grid, "Planilha Excel:", 0, false);
        txtPdfVig   = criarLinhaInput(grid, "PDF Vigilância:", 1, false);
        txtPdfServ  = criarLinhaInput(grid, "PDF Serviço:", 2, false);
        txtDrive    = criarLinhaInput(grid, "Pasta do Drive:", 3, true);

        // --- ÁREA DE LOG ---
        Label lblLog = new Label("Status do Processamento:");

        areaLog = new TextArea();
        areaLog.setEditable(false);
        areaLog.setPrefHeight(350);

        VBox logBox = new VBox(10, lblLog, areaLog);

        // --- BOTÕES DE AÇÃO ---
        HBox boxBotoes = new HBox(25);
        boxBotoes.setAlignment(Pos.CENTER);

        btnIniciar = new Button("INICIAR PROCESSO");
        btnIniciar.setPrefWidth(250);
        btnIniciar.setId("btn-iniciar"); // Referência para o CSS

        btnParar = new Button("PARAR PROCESSO");
        btnParar.setPrefWidth(250);
        btnParar.setDisable(true);
        btnParar.setId("btn-parar"); // Referência para o CSS

        btnIniciar.setOnAction(e -> dispararProcesso());
        btnParar.setOnAction(e -> pararProcesso());

        boxBotoes.getChildren().addAll(btnIniciar, btnParar);

        root.getChildren().addAll(grid, logBox, boxBotoes);

        // Carrega configurações salvas
        carregarConfiguracoes();

        // --- SCENE E CSS ---
        Scene scene = new Scene(root, 820, 730);
        try {
            // Tenta carregar o CSS da pasta de resources
            String css = getClass().getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.out.println("⚠️ Aviso: Arquivo estilo.css não encontrado em src/main/resources.");
        }

        stage.setScene(scene);
        stage.show();
    }

    private TextField criarLinhaInput(GridPane grid, String rotulo, int linha, boolean apenasPasta) {
        Label lbl = new Label(rotulo);
        lbl.setPrefWidth(120);

        TextField campo = new TextField();
        campo.setPrefWidth(600);

        Button btn = new Button("Procurar");
        btn.setPrefWidth(100);
        btn.setOnAction(e -> acaoProcurar(campo, apenasPasta));

        grid.add(lbl, 0, linha);
        grid.add(campo, 1, linha);
        grid.add(btn, 2, linha);

        return campo;
    }

    private void acaoProcurar(TextField campo, boolean apenasPasta) {
        File selecionado;
        if (apenasPasta) {
            DirectoryChooser dc = new DirectoryChooser();
            selecionado = dc.showDialog(null);
        } else {
            FileChooser fc = new FileChooser();
            selecionado = fc.showOpenDialog(null);
        }

        if (selecionado != null) {
            campo.setText(selecionado.getAbsolutePath());
            salvarConfiguracoes();
        }
    }

    private void dispararProcesso() {
        if (txtPlanilha.getText().isEmpty() || txtDrive.getText().isEmpty()) {
            exibirAlerta("Campos Vazios", "Selecione a planilha e o destino.");
            return;
        }

        salvarConfiguracoes();
        btnIniciar.setDisable(true);
        btnParar.setDisable(false);
        areaLog.clear();

        Thread thread = new Thread(() -> {
            try {
                AutomacaoPrincipalV7.executarComParametros(
                        txtPlanilha.getText(), txtPdfVig.getText(),
                        txtPdfServ.getText(), txtDrive.getText(), areaLog
                );
            } finally {
                Platform.runLater(() -> {
                    btnIniciar.setDisable(false);
                    btnParar.setDisable(true);
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void pararProcesso() {
        AutomacaoPrincipalV7.cancelarProcesso = true;
        btnParar.setDisable(true);
    }

    private void salvarConfiguracoes() {
        Properties prop = new Properties();
        prop.setProperty("planilha", txtPlanilha.getText().trim());
        prop.setProperty("pdfVig", txtPdfVig.getText().trim());
        prop.setProperty("pdfServ", txtPdfServ.getText().trim());
        prop.setProperty("drive", txtDrive.getText().trim());

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            prop.store(out, null);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void carregarConfiguracoes() {
        File f = new File(CONFIG_FILE);
        if (!f.exists()) return;
        Properties prop = new Properties();
        try (FileInputStream in = new FileInputStream(f)) {
            prop.load(in);
            txtPlanilha.setText(prop.getProperty("planilha", ""));
            txtPdfVig.setText(prop.getProperty("pdfVig", ""));
            txtPdfServ.setText(prop.getProperty("pdfServ", ""));
            txtDrive.setText(prop.getProperty("drive", ""));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void exibirAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
