import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {

	Socket socket;
	
	public Client(Socket socket) {
		this.socket = socket;
		receive();
	}
	
	//반복적으로 클라이언트로부터 메세지를 받는 메소드
	public void receive() {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream(); //입력받음
						byte[] buffer = new byte[512];
						int length = in.read(buffer); //버퍼에 담긴 메세지의 크기
						if(length == -1) throw new IOException(); //메세지 읽었을때 오류 발생
						System.out.println("[Message Receive Success] "  //메세지 읽었을때 오류 발생X
								+ socket.getRemoteSocketAddress() //현재 접속한 클라이언트의 IP 주소 정보
								+ ": " + Thread.currentThread().getName()); //쓰레드의 고유한 정보 이름값 출력
						String message = new String(buffer, 0, length, "UTF-8");
						for(Client client : Main.clients) {
							client.send(message); //전달 받은 메세지를 다른 클라이언트에도 보냄
						}
					}
				} catch(Exception e) {
					try {
						System.out.println("[Message Receive Error]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						Main.clients.remove(Client.this);
						socket.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread); //만들어진 thread를 메인함수 속 threadpool!에 등록
	}
	//해당 클라이언트에게 메시지를 전송하는 메소드
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream(); //보낼때는 받을때와 반대이므로 위와 반대로 생각
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer); //오류 안 발생겼으면 버퍼에 담긴내용을 서버에서 클라이언트로 전송
					out.flush();
				} catch (Exception e) {
					try {
						System.out.println("[Message Send Error]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						Main.clients.remove(Client.this); //오류가 발생했으면 모든 클라이언트들의 정보들의 배열에서 현재 클라이언트 정보를 없앰(접속 끊겼으므로)
						socket.close(); //오류생긴 클라이언트의 소켓을 닫음
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread); //만들어진 thread를 메인함수 속 threadpool!에 등록
	}
	
}
