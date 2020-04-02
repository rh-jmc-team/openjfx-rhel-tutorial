package com.redhat.jfx.tutorials;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.Label;

/*
 * Hello World, the OpenJFX way
 */
public class HelloWorldJFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Label hello = new Label("Hello World from OpenJFX!");
        Scene scene = new Scene(new StackPane(hello), 800, 600);
        stage.setScene(scene);

        stage.show();
    }
}
