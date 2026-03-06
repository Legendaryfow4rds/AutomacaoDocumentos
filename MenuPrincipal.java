package BancadaDeTestes;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MenuPrincipalFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Gocil - Central de Automação");

        Label titulo = new Label("AUTOMAÇÃO DE PROCESSOS");
        titulo.getStyleClass().add("menu-titulo");

        // MOTOR 1: Extrator FGTS
        Button btnFGTS = new Button("Extrator FGTS digital");
        btnFGTS.getStyleClass().add("btn-menu");
        btnFGTS.setOnAction(e -> {
            new InterfaceAutomacaoFX().start(new Stage());
            primaryStage.close();
        });

        // MOTOR 2: Cálculo de Funcionários
        Button btnCalculo = new Button("Funcionários por cliente");
        btnCalculo.getStyleClass().add("btn-menu");
        btnCalculo.setOnAction(e -> {
            new InterfaceCalculoFX().start(new Stage());
            primaryStage.close();
        });

        // MOTOR 3: NOVO - Separador de Comprovantes (Motor Python migrado)
        Button btnComprovantes = new Button("Separador de Comprovantes");
        btnComprovantes.getStyleClass().add("btn-menu");
        btnComprovantes.setOnAction(e -> {
            new InterfaceComprovantesFX().start(new Stage());
            primaryStage.close();
        });

        // Adicionando o novo botão ao box (Mantendo o espaçamento de 30)
        VBox boxBotoes = new VBox(30, btnFGTS, btnCalculo, btnComprovantes);
        boxBotoes.setAlignment(Pos.CENTER);

        VBox root = new VBox(50, titulo, boxBotoes); // Aumentei o espaçamento entre título e botões
        root.getStyleClass().add("menu-container");
        root.setAlignment(Pos.CENTER);

        // Ajustei levemente a altura para 550 para acomodar o novo botão com folga
        Scene scene = new Scene(root, 570, 550);
        try {
            scene.getStylesheets().add(getClass().getResource("/estilo.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Erro ao carregar CSS no Menu.");
        }

        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Evita distorção do layout Carbon
        primaryStage.show();
    }

    public static void main(String[] args) { launch(args); }
}
