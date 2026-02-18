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

        BorderPane mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("menu-container");
        mainLayout.setPadding(new Insets(20));

        // CABEÇALHO
        Button btnVoltar = new Button("⬅ VOLTAR");
        btnVoltar.getStyleClass().add("btn-voltar");
        btnVoltar.setOnAction(e -> {
            new MenuPrincipalFX().start(new Stage());
            stage.close();
        });
        mainLayout.setTop(new HBox(btnVoltar));

        // CONTEÚDO CENTRAL
        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.TOP_LEFT);
        centerBox.setPadding(new Insets(20, 10, 10, 10));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.TOP_LEFT);

        txtPlanilha = criarLinhaInput(grid, "Planilha Excel:", 0, false);
        txtPdfVig   = criarLinhaInput(grid, "PDF Vigilância:", 1, false);
        txtPdfServ  = criarLinhaInput(grid, "PDF Serviço:", 2, false);
        txtDrive    = criarLinhaInput(grid, "Pasta do Drive:", 3, true);

        Label lblLog = new Label("CONSOLE DE OPERAÇÃO");
        lblLog.getStyleClass().add("menu-titulo-pequeno");

        areaLog = new TextArea();
        areaLog.setEditable(false);
        areaLog.setPrefHeight(350);
        areaLog.setStyle("-fx-text-fill: #D4AF37;");
        areaLog.getStyleClass().add("text-area-industrial");

        // BOTÕES DE AÇÃO LADO A LADO
        btnIniciar = new Button("INICIAR PROCESSAMENTO");
        btnIniciar.getStyleClass().add("btn-acao-iniciar");
        btnIniciar.setPrefHeight(80);
        btnIniciar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnIniciar, Priority.ALWAYS);
        btnIniciar.setOnAction(e -> dispararProcesso());

        btnParar = new Button("PARAR AGORA");
        btnParar.getStyleClass().add("btn-acao-parar");
        btnParar.setPrefHeight(80);
        btnParar.setMaxWidth(Double.MAX_VALUE);
        btnParar.setDisable(true);
        HBox.setHgrow(btnParar, Priority.ALWAYS);
        // Vincula o cancelamento à sua classe de lógica
        btnParar.setOnAction(e -> AutomacaoPrincipalV9.cancelarProcesso = true);

        HBox boxBotoes = new HBox(20, btnIniciar, btnParar);
        boxBotoes.setAlignment(Pos.CENTER);

        centerBox.getChildren().addAll(grid, lblLog, areaLog, boxBotoes);
        mainLayout.setCenter(centerBox);

        carregarConfiguracoes();

        Scene scene = new Scene(mainLayout, 800, 600);
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Erro ao carregar style.css");
        }

        stage.setScene(scene);
        stage.show();
    }

    private TextField criarLinhaInput(GridPane grid, String rotulo, int linha, boolean apenasPasta) {
        Label lbl = new Label(rotulo);
        lbl.getStyleClass().add("label-industrial");
        lbl.setMinWidth(150);

        TextField campo = new TextField();
        campo.getStyleClass().add("input-industrial");
        campo.setPrefWidth(700);
        GridPane.setHgrow(campo, Priority.ALWAYS);

        Button btn = new Button("Procurar");
        btn.getStyleClass().add("btn-procurar");
        btn.setMinWidth(100);

        btn.setOnAction(e -> {
            File selecionado = apenasPasta ? new DirectoryChooser().showDialog(null) : new FileChooser().showOpenDialog(null);
            if (selecionado != null) {
                campo.setText(selecionado.getAbsolutePath());
                salvarConfiguracoes();
            }
        });

        grid.add(lbl, 0, linha);
        grid.add(campo, 1, linha);
        grid.add(btn, 2, linha);
        return campo;
    }

    // --- RESTAURAÇÃO DA LÓGICA DE EXECUÇÃO ---

    private void dispararProcesso() {
        salvarConfiguracoes();
        btnIniciar.setDisable(true);
        btnParar.setDisable(false);
        areaLog.clear();
        AutomacaoPrincipalV9.cancelarProcesso = false; // Reseta o cancelamento

        // Thread para não travar a interface visual enquanto a automação roda
        new Thread(() -> {
            try {
                AutomacaoPrincipalV9.executarComParametros(
                        txtPlanilha.getText(),
                        txtPdfVig.getText(),
                        txtPdfServ.getText(),
                        txtDrive.getText(),
                        areaLog
                );
            } catch (Exception e) {
                Platform.runLater(() -> areaLog.appendText("\nErro crítico: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> {
                    btnIniciar.setDisable(false);
                    btnParar.setDisable(true);
                });
            }
        }).start();
    }

    private void salvarConfiguracoes() {
        Properties prop = new Properties();
        prop.setProperty("planilha", txtPlanilha.getText());
        prop.setProperty("pdfVig", txtPdfVig.getText());
        prop.setProperty("pdfServ", txtPdfServ.getText());
        prop.setProperty("drive", txtDrive.getText());
        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            prop.store(out, null);
        } catch (IOException e) {
            System.err.println("Erro ao salvar configurações.");
        }
    }

    private void carregarConfiguracoes() {
        File f = new File(CONFIG_FILE);
        if (!f.exists()) return;
        Properties prop = new Properties();
        try (InputStream in = new FileInputStream(f)) {
            prop.load(in);
            txtPlanilha.setText(prop.getProperty("planilha", ""));
            txtPdfVig.setText(prop.getProperty("pdfVig", ""));
            txtPdfServ.setText(prop.getProperty("pdfServ", ""));
            txtDrive.setText(prop.getProperty("drive", ""));
        } catch (IOException e) {
            System.err.println("Erro ao carregar configurações.");
        }
    }

    public static void main(String[] args) { launch(args); }
}
