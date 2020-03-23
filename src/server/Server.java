package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


public class Server {
    private Vector<ClientHandler> clients;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public  Server (){
        clients = new Vector<>();
        authService =new SimpleAuthService();
        ServerSocket server = null;
        Socket socket = null;

        try {
            server = new ServerSocket(19089);
            System.out.println("Сервер работает");

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(socket, this);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void broadcastMsg (String nick, String msg){
        for (ClientHandler c:clients) {
            c.sendMsg(nick + ":" + "\n" + msg);
        }
    }

    public void privatMsg (ClientHandler sender, String receiver, String msg){
        for (ClientHandler c:clients) {
            String message = String.format("[ %s ] private [ %s ] : %s",
                    sender.getNick(), receiver, msg);
            if (sender.getNick().equals(receiver)){
                sender.sendMsg(msg);
                return;
            }
            if (c.getNick().equals(receiver)){
                c.sendMsg(message);
                sender.sendMsg(message);
                return;
            }
            sender.sendMsg("Not found user " + receiver);
        }
    }

    public void subscribe (ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastClientList();
    }
    public void unsubscribe (ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastClientList();
    }
    public boolean isLoginAuthorised(String login){
        for (ClientHandler c:clients) {
            if (c.getLogin().equals(login)){
                return true;
            }
        }
        return false;
    }
    public void broadcastClientList (){
        StringBuilder sb = new StringBuilder("/clientlist ");
        for (ClientHandler c:clients) {
            sb.append(c.getNick()+ " ");
        }
        String msg = sb.toString();
        for (ClientHandler c:clients) {
            c.sendMsg(msg);
        }
    }
}
