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

        Button btnFGTS = new Button("Extrator FGTS digital");
        btnFGTS.getStyleClass().add("btn-menu");
        btnFGTS.setOnAction(e -> {
            new InterfaceAutomacaoFX().start(new Stage());
            primaryStage.close();
        });

        Button btnCalculo = new Button("Funcionários por cliente");
        btnCalculo.getStyleClass().add("btn-menu");
        btnCalculo.setOnAction(e -> {
            new InterfaceCalculoFX().start(new Stage());
            primaryStage.close();
        });

        VBox boxBotoes = new VBox(30, btnFGTS, btnCalculo);
        boxBotoes.setAlignment(Pos.CENTER);

        VBox root = new VBox(titulo, boxBotoes);
        root.getStyleClass().add("menu-container");
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 570, 500);
        try {
            scene.getStylesheets().add(getClass().getResource("/estilo.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Erro ao carregar CSS no Menu.");
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) { launch(args); }
}
