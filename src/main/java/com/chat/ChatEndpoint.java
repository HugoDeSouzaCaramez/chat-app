package com.chat;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;

@ServerEndpoint("/chat")
public class ChatEndpoint {
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private static final Map<Session, String> userNames = Collections.synchronizedMap(new HashMap<>());

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("Usuário conectado: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        String userName = userNames.get(session);
        sessions.remove(session);
        userNames.remove(session);
        if (userName != null) {
            broadcast("Sistema: " + userName + " saiu do chat.");
        }
        System.out.println("Usuário desconectado: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if (!userNames.containsKey(session)) {
            // Primeira mensagem é o nome do usuário
            userNames.put(session, message);
            broadcast("Sistema: " + message + " entrou no chat.");
        } else {
            // Mensagens subsequentes são mensagens de chat
            String userName = userNames.get(session);
            broadcast(userName + ": " + message);
        }
    }

    private static void broadcast(String message) {
        synchronized (sessions) {
            for (Session s : sessions) {
                if (s.isOpen()) {
                    try {
                        s.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}