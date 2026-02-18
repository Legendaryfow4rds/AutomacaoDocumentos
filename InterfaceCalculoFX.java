package BancadaDeTestes;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class InterfaceCalculoFX extends Application {

    private TextField txtPlanilha, txtBasePdf1, txtBasePdf2;
    private TextArea areaLog;
    private Button btnIniciar, btnVoltar;
    private final String CONFIG_FILE = "config_calculo.properties";

    @Override
    public void start(Stage stage) {
        stage.setTitle("Gocil - Cálculo de Funcionários");

        BorderPane mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("menu-container");
        mainLayout.setPadding(new Insets(20));

        // CABEÇALHO
        btnVoltar = new Button("⬅ VOLTAR AO MENU");
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

        Label lblTitulo = new Label("CÁLCULO DE TOTAL DE FUNCIONÁRIOS");
        lblTitulo.getStyleClass().add("menu-titulo-pequeno");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        txtPlanilha = criarLinhaInput(grid, "Planilha de Dados (Excel):", 0);
        txtBasePdf1  = criarLinhaInput(grid, "Base PDF Vigilância:", 1);
        txtBasePdf2  = criarLinhaInput(grid, "Base PDF Serviço:", 2);

        Label lblLog = new Label("STATUS DO PROCESSAMENTO");
        lblLog.getStyleClass().add("label-industrial");

        areaLog = new TextArea();
        areaLog.setEditable(false);
        areaLog.setPrefHeight(300);
        areaLog.setStyle("-fx-text-fill: #D4AF37;");
        areaLog.getStyleClass().add("text-area-industrial");

        btnIniciar = new Button("EXECUTAR ANÁLISE E ATUALIZAR PLANILHA");
        btnIniciar.getStyleClass().add("btn-acao-iniciar");
        btnIniciar.setPrefHeight(60);
        btnIniciar.setMaxWidth(Double.MAX_VALUE);

        // Chamada do método dispararLogicaCalculo
        btnIniciar.setOnAction(e -> dispararLogicaCalculo());

        centerBox.getChildren().addAll(lblTitulo, grid, lblLog, areaLog, btnIniciar);
        mainLayout.setCenter(centerBox);

        // CARREGA AS CONFIGURAÇÕES SALVAS ANTERIORMENTE
        carregarConfiguracoes();

        Scene scene = new Scene(mainLayout, 1000, 600);
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Erro ao carregar estilo.css");
        }

        stage.setScene(scene);
        stage.show();
    }

    private TextField criarLinhaInput(GridPane grid, String rotulo, int linha) {
        Label lbl = new Label(rotulo);
        lbl.getStyleClass().add("label-industrial");
        lbl.setMinWidth(180);

        TextField campo = new TextField();
        campo.getStyleClass().add("input-industrial");
        campo.setPrefWidth(600);
        GridPane.setHgrow(campo, Priority.ALWAYS);

        Button btn = new Button("Procurar");
        btn.getStyleClass().add("btn-procurar");
        btn.setOnAction(e -> {
            File file = new FileChooser().showOpenDialog(null);
            if (file != null) campo.setText(file.getAbsolutePath());
        });

        grid.add(lbl, 0, linha);
        grid.add(campo, 1, linha);
        grid.add(btn, 2, linha);
        return campo;
    }

    // --- MÉTODOS DE PERSISTÊNCIA ---

    private void salvarConfiguracoes() {
        Properties props = new Properties();
        props.setProperty("planilha", txtPlanilha.getText());
        props.setProperty("pdfVig", txtBasePdf1.getText());
        props.setProperty("pdfServ", txtBasePdf2.getText());

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Configuracoes Calculo Funcionarios");
        } catch (IOException e) {
            System.err.println("Erro ao salvar config: " + e.getMessage());
        }
    }

    private void carregarConfiguracoes() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) return;

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
            txtPlanilha.setText(props.getProperty("planilha", ""));
            txtBasePdf1.setText(props.getProperty("pdfVig", ""));
            txtBasePdf2.setText(props.getProperty("pdfServ", ""));
        } catch (IOException e) {
            System.err.println("Erro ao carregar config: " + e.getMessage());
        }
    }

    // --- LÓGICA DE EXECUÇÃO ---

    private void dispararLogicaCalculo() {
        // 1. Salva os caminhos atuais para a próxima vez que abrir
        salvarConfiguracoes();

        // 2. Prepara a interface
        btnIniciar.setDisable(true);
        areaLog.clear();

        // 3. Executa em Thread separada para não travar a tela
        new Thread(() -> {
            try {
                AutomacaoPrincipalV9.executarCalculoFuncionarios(
                        txtPlanilha.getText(),
                        txtBasePdf1.getText(),
                        txtBasePdf2.getText(),
                        areaLog
                );
            } finally {
                Platform.runLater(() -> btnIniciar.setDisable(false));
            }
        }).start();
    }

    public static void main(String[] args) { launch(args); }
}
