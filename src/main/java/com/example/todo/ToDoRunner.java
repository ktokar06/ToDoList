package com.example.todo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ToDoRunner extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/todo/todo-view.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("ToDo List Application");
        primaryStage.setScene(new Scene(root, 1000, 600));
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(500);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}