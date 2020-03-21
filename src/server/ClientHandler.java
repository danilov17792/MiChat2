package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Server server;
    private String nick;
    private String login;

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            System.out.println("RemoteSocketAddress: " + socket.getRemoteSocketAddress());
            this.server = server;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true){
                        String str = in.readUTF();
                        if (str.startsWith("/auth ")) {
                            String[] token = str.split(" ");
                            String newNick = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            if (newNick != null) {
                                sendMsg("/authok " + newNick);
                                nick = newNick;
                                login = token[1];
                                server.subscribe(this);
                                System.out.println("Клиент "+ newNick + " авторизовался");
                                break;
                            }else {
                                sendMsg("Неверный логин/пароль");
                            }

                        }
                    }

                    while (true) {
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            out.writeUTF("/end");
                            break;
                        }
                        if(str.startsWith("/w")){
                            String to = str.split(" ")[1];
                            String msg = str.substring(9);
                            server.wisperMsg(this, to, msg); // сообщение участниками диалога
                        } else
                            server.broadcastMsg(str, nick);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    server.unsubscribe(this);
                    System.out.println("Клиент" + nick + "вышел");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getClientName() {
        return this.nick;
    }

    public void sendMsg (String msg){
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
