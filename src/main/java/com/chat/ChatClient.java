package com.chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

public class ChatClient extends Application {
    private TextArea messageArea;
    private TextField inputField;
    private Session session;
    private String userName;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chat Cliente");

        // Solicitar o nome do usuário
        TextInputDialog dialog = new TextInputDialog("Usuário");
        dialog.setTitle("Nome de Usuário");
        dialog.setHeaderText("Bem-vindo ao Chat");
        dialog.setContentText("Por favor, insira seu nome:");
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && !result.get().trim().isEmpty()) {
            userName = result.get().trim();
            initializeUI(primaryStage);
            connectToServer();
        } else {
            System.out.println("Nome de usuário não fornecido. Encerrando...");
            Platform.exit();
        }
    }

    private void initializeUI(Stage primaryStage) {
        BorderPane root = new BorderPane();

        messageArea = new TextArea();
        messageArea.setEditable(false);
        root.setCenter(messageArea);

        HBox bottomBox = new HBox();
        inputField = new TextField();
        Button sendButton = new Button("Enviar");
        bottomBox.getChildren().addAll(inputField, sendButton);
        root.setBottom(bottomBox);

        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void connectToServer() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            String uri = "ws://localhost:8025/websocket/chat";
            container.connectToServer(new ClientEndpoint(), URI.create(uri));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            try {
                if (session != null && session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                    inputField.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientEndpoint extends Endpoint {
        @Override
        public void onOpen(Session session, EndpointConfig config) {
            ChatClient.this.session = session;

            // Enviar o nome do usuário como primeira mensagem
            try {
                session.getBasicRemote().sendText(userName);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Configurar o handler de mensagens
            session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    Platform.runLater(() -> messageArea.appendText(message + "\n"));
                }
            });
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}