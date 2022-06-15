package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;


public class Main extends Application {
	
	public static int IPlayer = 0;
	public static int[] mostVotePlayer = new int[8];
	public static int[] ADPlayer = new int [3];
	public static int Choice = -1;
	static Semaphore semaphore = new Semaphore(1);
	
	// 뮤텍스
	public static void MVP(int i) {
		try {
			semaphore.acquire();
			System.out.println("가장 많이 투표된 뮤텍스 정상 작동");
			mostVotePlayer[i]++;
			semaphore.release();
		} catch (Exception e) {
			System.out.println("가장 많이 투표된 뮤텍스 오류");
		}
	}
	public static void ADP(int i) {
		try {
			semaphore.acquire();
			System.out.println("찬반 투표 뮤텍스 정상 작동");
			ADPlayer[i]++;
			semaphore.release();
		} catch (Exception e) {
			System.out.println("찬반 투표 뮤텍스 오류");
		}
	}
	
	// 여러개의 쓰레드를 효율적으로 관리하기 위해 사용하는 라이브러리
	public static ExecutorService threadPool;
	// 클라이언트들
	public static Vector<Client> clients = new Vector<Client>();
	
	ServerSocket serverSocket;
	
	// 서버를 구동시켜서 클라이언트의 연결을 기다리는 메소드입니다.
	public void startServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP, port));
		}catch (Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
		
		// 클라이언트가 접속할 때까지 계속 기다리는 쓰레드입니다.
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket, IPlayer));
						System.out.println("[클라이언트 접속]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						IPlayer++;
					}catch (Exception e) {
						if(!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
			}
		};
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);
	}
	
	// 서버의 작동을 중지시키는 메소드입니다.
	public void stopServer() {
		IPlayer = 0;
		mapiaGame.ready = 0;
		try {
			// 현재 작동 중인 모든 소켓 담기
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			// 서버 소켓 객체 닫기
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			// 쓰레드 풀 종료하기
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// UI를 생성하고, 실질적으로 프로그램을 동작시키는 메소드입니다.
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("나눔고딕", 15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("시작하기");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
		root.setBottom(toggleButton);
		
		String IP = "172.30.1.17"; // 자기 자신의 컴퓨터 주소 즉 로컬 주소
//		String IP = "172.17.16.1";
		// 실제로 서버 운영은 아님 우리 컴푸터 안에서 테스트
		int port = 9876;
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("시작하기")) {
				startServer(IP, port);
				Platform.runLater(() -> {
					String message = String.format("[서버 시작]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("종료하기");
				});
			}else {
				stopServer();
				Platform.runLater(() -> {
					String message = String.format("[서버 종료]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("시작하기");
				});
			}	
		});
		
		Scene scene = new Scene(root, 400, 400);
		primaryStage.setTitle("[ 채팅 서버 ]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	// 프로그램의 진입점입니다.	
	public static void main(String[] args) {
		launch(args);
	}
}