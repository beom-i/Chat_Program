
import java.net.ServerSocket;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application{
	public static ExecutorService threadPool; //다양한 클라이언트들이 접속했을때 쓰레드들 관리
	public static Vector<Client> clients = new Vector<Client>(); //접속한 클라이언트들을 관리
	
	ServerSocket serverSocket;
	
	//서버를 구동시켜 클라이언트 연결을 기다리는 메소드
	public void startServer(String IP, int port) { }
	
	//서버의 작동을 중지시키는 메소드
	public void stopServer() { }
	
	//UI생성하고 실질적으로 프로그램 작동시키는 메소드
	@Override
	public void start(Stage primaryStage) {
		
	}
	//프로그램의 시작점
	public static void main(String[] args) {
		launch(args);
	}
}