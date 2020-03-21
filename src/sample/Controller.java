package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;
    @FXML
    public HBox authPanel;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox msgPanel;
    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    final String IP_ADRESS = "localhost";
    final int PORT = 17089;
    final String TITLE = "MiChat";

    private boolean authenticated;
    String nickname;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        if (!authenticated){
            nickname = "";
            setTitle(TITLE);
        }
        textArea.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authenticated = false;
    }
    public void connect (){
        try {
            socket = new Socket(IP_ADRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true){
                        String str = in.readUTF();
                        if (str.startsWith("/authok")) {
                            nickname = str.split(" ")[1];
                            setAuthenticated(true);
                            break;
                        }
                        textArea.appendText(str + "\n");
                    }

                    setTitle(TITLE + ": " + nickname);

                    // цикл работы
                    while (true) {
                        String str = in.readUTF();
                        if (str.equals("/end")) {
                            break;
                        }
                        textArea.appendText(str + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setAuthenticated(false);
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendMsg(ActionEvent actionEvent) {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if(socket == null || socket.isClosed()){
            connect();
        }
        try {
            out.writeUTF("/auth " + loginField.getText().trim() + " " + passwordField.getText().trim());
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void setTitle (String title){
        Platform.runLater(()->{
            ((Stage)textArea.getScene().getWindow()).setTitle(title);
        });
    }

}
