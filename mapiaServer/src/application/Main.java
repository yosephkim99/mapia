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
	
	// ���ؽ�
	public static void MVP(int i) {
		try {
			semaphore.acquire();
			System.out.println("���� ���� ��ǥ�� ���ؽ� ���� �۵�");
			mostVotePlayer[i]++;
			semaphore.release();
		} catch (Exception e) {
			System.out.println("���� ���� ��ǥ�� ���ؽ� ����");
		}
	}
	public static void ADP(int i) {
		try {
			semaphore.acquire();
			System.out.println("���� ��ǥ ���ؽ� ���� �۵�");
			ADPlayer[i]++;
			semaphore.release();
		} catch (Exception e) {
			System.out.println("���� ��ǥ ���ؽ� ����");
		}
	}
	
	// �������� �����带 ȿ�������� �����ϱ� ���� ����ϴ� ���̺귯��
	public static ExecutorService threadPool;
	// Ŭ���̾�Ʈ��
	public static Vector<Client> clients = new Vector<Client>();
	
	ServerSocket serverSocket;
	
	// ������ �������Ѽ� Ŭ���̾�Ʈ�� ������ ��ٸ��� �޼ҵ��Դϴ�.
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
		
		// Ŭ���̾�Ʈ�� ������ ������ ��� ��ٸ��� �������Դϴ�.
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket, IPlayer));
						System.out.println("[Ŭ���̾�Ʈ ����]"
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
	
	// ������ �۵��� ������Ű�� �޼ҵ��Դϴ�.
	public void stopServer() {
		IPlayer = 0;
		mapiaGame.ready = 0;
		try {
			// ���� �۵� ���� ��� ���� ���
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			// ���� ���� ��ü �ݱ�
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			// ������ Ǯ �����ϱ�
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// UI�� �����ϰ�, ���������� ���α׷��� ���۽�Ű�� �޼ҵ��Դϴ�.
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("�������", 15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("�����ϱ�");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
		root.setBottom(toggleButton);
		
		String IP = "172.30.1.17"; // �ڱ� �ڽ��� ��ǻ�� �ּ� �� ���� �ּ�
//		String IP = "172.17.16.1";
		// ������ ���� ��� �ƴ� �츮 ��Ǫ�� �ȿ��� �׽�Ʈ
		int port = 9876;
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("�����ϱ�")) {
				startServer(IP, port);
				Platform.runLater(() -> {
					String message = String.format("[���� ����]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
				});
			}else {
				stopServer();
				Platform.runLater(() -> {
					String message = String.format("[���� ����]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
				});
			}	
		});
		
		Scene scene = new Scene(root, 400, 400);
		primaryStage.setTitle("[ ä�� ���� ]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	// ���α׷��� �������Դϴ�.	
	public static void main(String[] args) {
		launch(args);
	}
}