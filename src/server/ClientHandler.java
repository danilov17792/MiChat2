package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

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
            System.out.println("Сокет " + socket.getRemoteSocketAddress()+ " занят");
            this.server = server;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {

                try {
                    socket.setSoTimeout(3000);
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/reg ")) {
                            String[] token = str.split(" ");
                            boolean b =server.getAuthService().registration(token[1],token[2],token[3]);
                            if (b){
                              sendMsg("Регистрация прошла успешно");
                            }else {
                                sendMsg("Логин/ник уже занят");
                            }

                        }
                        if (str.equals("/end")) {
                            throw new RuntimeException("Клиент вышел по крестику");
                        }

                        if (str.startsWith("/auth ")) {
                            String[] token = str.split(" ");
                            String newNick = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);

                            login = token[1];

                            if (newNick != null) {
                                if (!server.isLoginAuthorised(login)) {
                                    sendMsg("/authok " + newNick);
                                    nick = newNick;
                                    socket.setSoTimeout(0);
                                    server.subscribe(this);
                                    System.out.println("Клиент " + newNick + " авторизовался");
                                    break;
                                } else {
                                    sendMsg("Логин занят");
                                }
                            } else {
                                sendMsg("Неверный логин/пароль");
                            }
                        }
                    }

                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                out.writeUTF("/end");
                                break;
                            }
                            if (str.startsWith("/w")) {
                                String[] token = str.split(" ", 3);
                                if (token.length == 3) {
                                    server.privatMsg(this, token[1], token[2]);
                                }
                            }
                        } else {
                            server.broadcastMsg(nick, str);
                        }
                    }
                }catch (SocketTimeoutException exception){
                    try {
                        out.writeUTF("/end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Сокет " + socket.getRemoteSocketAddress()+ " свободен");
                    System.out.println("Истек Timeout подключения");
                }catch (RuntimeException e){
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    server.unsubscribe(this);
                    System.out.println("Клиент вышел");
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

    public void sendMsg (String msg){
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }

    public String getLogin() {
        return login;
    }
}
