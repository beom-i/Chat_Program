package application;
	
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class Main extends Application {
	
	Socket socket;
	TextArea textArea; //다양한 내용을 주고 받았을 때 메세지를 출력하는 공간
	int isAdministrator = 0;
	
	//클라이언트 프로그램 동작 메소드
	public void startClient(String IP, int port) {
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port); //소켓 생성 , 소켓 초기화
					receive(); //서버로부터 메시지 전달받음
				} catch (Exception e) {
					if(!socket.isClosed()) { //오류 -> 소켓 열려있으면 클라이언트 종료
						stopClient();
						System.out.println("[Disconnect]");
						Platform.exit(); //프로그램도 종료
					}
				}
			}
		};
		thread.start();
	}
	
	//클라이언트 프로그램 종료 메소드
	public void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) { //소켓이 열려있는 상태라면 소켓을 닫아줌
				socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//서버로부터 메시지를 전달받는 메소드 
	public void receive() { //서버 프로그램과 거의 비슷
		while(true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[512];
				int length = in.read(buffer);
				if(length == -1) throw new IOException();
				String message = new String(buffer, 0, length, "UTF-8");
				Platform.runLater(() -> {
					textArea.appendText(message);
				});
			} catch (Exception e) {
				stopClient();
				break;
			}
		}
	}
	
	//서버로 메시지를 전송하는 메소드
	public void send(String message) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		HBox hbox = new HBox();
		hbox.setSpacing(5);
		
		TextField userName = new TextField();
		userName.setPrefWidth(150);
		userName.setPromptText("Write Name");
		HBox.setHgrow(userName,Priority.ALWAYS);
		
		TextField IPText = new TextField("127.0.0.1");
		TextField portText = new TextField("9876");
		portText.setPrefWidth(80);
		
		hbox.getChildren().addAll(userName,IPText,portText);
		root.setTop(hbox);
		
		textArea = new TextArea();
		textArea.setEditable(false);
		root.setCenter(textArea);
		
		TextField input = new TextField();
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);
		
		input.setOnAction(event -> {
			if(input.getText().equals("/admin on")) {
				isAdministrator = 1;
				input.setText("");
				return;
			}else if(input.getText().equals("/admin off")) {
				isAdministrator = 0;
				input.setText("");
				return;
			}
			
			if(isAdministrator == 1) {
				send("[ADMIN " + userName.getText() + " ]:" + input.getText() + "\n");
			} else {
				send(userName.getText() + " : " + input.getText() + "\n");
			}
			input.setText("");
			input.requestFocus();
		});
		
		Button sendButton = new Button("Send");
		sendButton.setDisable(true);
		sendButton.setOnAction(event -> {
			if(input.getText().equals("/admin on")) {
				isAdministrator = 1;
				input.setText("");
				return;
			}else if(input.getText().equals("/admin off")) {
				isAdministrator = 0;
				input.setText("");
				return;
			}
			
			if(isAdministrator == 1) {
				send("[ADMIN " + userName.getText() + " ]:" + input.getText() + "\n");
			} else {
				send(userName.getText() + " : " + input.getText() + "\n");
			}
		
			input.setText("");
			input.requestFocus();
		});	
		
		Button connectionButton = new Button("Connect");
		connectionButton.setOnAction(event -> {
			if(connectionButton.getText().equals("Connect")) {
				int port = 9876;
				try {
					port = Integer.parseInt(portText.getText());
				} catch(Exception e) {
					e.printStackTrace();
				}
				startClient(IPText.getText(),port);
				Platform.runLater(() -> {
					textArea.appendText("[Chat Room Connect]\n");
				});
				connectionButton.setText("Bye");
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
			} else {
				stopClient();
				Platform.runLater(() -> {
					textArea.appendText("[Chat Room Disconnect]\n");
				});
				connectionButton.setText("Connect");
				input.setDisable(true);
				sendButton.setDisable(true);
			}
		});
		
		BorderPane pane = new BorderPane();
		pane.setLeft(connectionButton);
		pane.setCenter(input);
		pane.setRight(sendButton);
		
		root.setBottom(pane);
		
		Scene scene = new Scene(root,400,400);
		primaryStage.setTitle("[Chat Client]");
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(event -> stopClient());
		primaryStage.show();
		
		connectionButton.requestFocus();
	}
	public static void main(String[] args) {
		launch(args);
	}
}
