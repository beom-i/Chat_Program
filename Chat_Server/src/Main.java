
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class Main extends Application{
	public static ExecutorService threadPool; //다양한 클라이언트들이 접속했을때 쓰레드들 관리
	public static Vector<Client> clients = new Vector<Client>(); //접속한 클라이언트들을 관리
	
	ServerSocket serverSocket;
	
	//서버를 구동시켜 클라이언트 연결을 기다리는 메소드
	public void startServer(String IP, int port) { 
		try {
			serverSocket = new ServerSocket(); 
			serverSocket.bind(new InetSocketAddress(IP, port)); 
		} catch(Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) { ////서버소켓에 문제가 발생한것이므로
				stopServer(); //서버소켓이 닫혀있는 상태가 아니라면 서버를 종료한다.
			}
			return;
		}
		
		//서버가 소켓을 잘 열었으면 클라이언트가 접속할 때까지 계속 기다리는 쓰레드 
		
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Socket socket = serverSocket.accept(); //클라이언트 접속
						clients.add(new Client(socket)); //클라이언트 배열에 새롭게 접속한 클라이언트 추가
						System.out.println("[클라이언트 접속] "
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
					} catch (Exception e) {
						if(!serverSocket.isClosed()) {  //서버소켓에 문제가 발생한것이므로
							stopServer(); //서버소켓이 닫혀있는 상태가 아니라면 서버를 종료한다.
						}
						break;
					}
				} 
			}
		};
		threadPool = Executors.newCachedThreadPool();//쓰레드 풀을 초기화 해줌
		threadPool.submit(thread); //쓰레드풀에 현재 클라이언트를 기다리는 쓰레드를 담는다
		
	}
	
	//서버의 작동을 중지시키는 메소드 // 자원 할당을 해제하는 메소드
	public void stopServer() {
		try {
			//현재 작동중인 모든 소켓 닫기
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next(); //특정 클라이언트에 접속해서 
				client.socket.close(); //그 클라이언트 소켓을 닫아주고
				iterator.remove(); //iterator에서 연결 끊긴 클라이언트를 제거함
			}
			//모든 클라이언트 연결 끊겼으므로 서버 소켓 객체도 닫는다
			if(serverSocket != null && !serverSocket.isClosed()) { //서버소켓이 널값이 아니고 현재 서버소켓이 열려있는 상태면
				serverSocket.close();
			} 
			//쓰레드 풀 종료하기
			if(threadPool != null && !threadPool.isShutdown()) { 
				threadPool.isShutdown();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//UI생성하고 실질적으로 프로그램 작동시키는 메소드
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();//레이아웃(다른 디자인 요소 담음)
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea(); //문장을 담을 수 있는 공간
		textArea.setEditable(false); //문장을 출력만 하고 그 문장을 수정할 순 없다
		textArea.setFont(new Font("나눔고딕",15)); //문장의 폰트 적용
		root.setCenter(textArea);
		
		Button toggleButton = new Button("start"); //서버를 시작하는 버튼 ,ToggleButton 은 스위치 같은 느낌 시작->종료
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1,0,0,0));
		root.setBottom(toggleButton);
		
		String IP = "127.0.0.1";
		int port = 9876;
		
		toggleButton.setOnAction(event -> { //togglebutton눌렀을때
			if(toggleButton.getText().equals("start")) {
				startServer(IP, port);
				Platform.runLater(() -> { //버튼을 눌렀을 때 gui요소 나오게 하기
					String message = String.format("[Server Start]\n",IP,port);
					textArea.appendText(message);
					toggleButton.setText("End"); //시작 말고 종료하기로 버튼 바뀜
				});
			}else {      //위와반대
				stopServer();
				Platform.runLater(() -> {
					String message = String.format("[Server Finish]\n",IP,port);
					textArea.appendText(message);
					toggleButton.setText("Start");
				});
			}
		});
		
		Scene scene = new Scene(root,400,400); //화면 크기 400x400으로 만듬
		primaryStage.setTitle("[chat server]");
		primaryStage.setOnCloseRequest(event -> stopServer()); //종료버튼을 누르면 서버 종료
		primaryStage.setScene(scene);
		primaryStage.show(); //우리 화면에 보이기
	}
	//프로그램의 시작점
	public static void main(String[] args) {
		launch(args);
	}
}