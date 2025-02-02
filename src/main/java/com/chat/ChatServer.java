package com.chat;

import org.glassfish.tyrus.server.Server;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ChatServer {
    public static void main(String[] args) {
        Server server = new Server("localhost", 8025, "/websocket", null, ChatEndpoint.class);
        try {
            server.start();
            System.out.println("Servidor rodando. Pressione Enter para parar.");
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }
}