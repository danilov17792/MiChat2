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
            server = new ServerSocket(17089);
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
    public void broadcastMsg (String msg, String nick){
        for (ClientHandler c:clients) {
            c.sendMsg(nick + ":" + "\n" + msg);
        }
    }
    public void subscribe (ClientHandler clientHandler){
        clients.add(clientHandler);
    }
    public void unsubscribe (ClientHandler clientHandler){
        clients.remove(clientHandler);
    }
    public void wisperMsg(ClientHandler from, String to, String msg){
        for (ClientHandler c:clients) {
            if(c.getClientName().equals(to)) {
                c.sendMsg("Сообщение от " + from.getClientName() + ":" + "\n" + msg);
                break;
            }
        }
        from.sendMsg("Сообщение для " + to + ":" + "\n" + msg);
    }
}
