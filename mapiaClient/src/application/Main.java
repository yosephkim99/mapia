package application;
	
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.print.PrinterJob.JobStatus;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;


public class Main extends Application {
	
	static Socket socket;
	static TextArea textArea;
	static TextField userName = new TextField("O��° �÷��̾�");
	private boolean nameStatus = true;	// �̸��� ����Ǿ�� �ϸ� true ���� �Ǿ����� false
	Button btnReady = new Button("�غ�");
	public static boolean jobStatus = false;	// ������ ���۵Ǹ� true�� �ٲ�� ���� ���� ����
	static TextField jobText = new TextField("");
	public static boolean gameStatus = false;	// ���� ���� �� false ���� �� true
	public static boolean entireChatStatus = false;
	static TextField timeText = new TextField("�ð�");	// �ð� �ʵ�
	static Button sendButton = new Button("������");
	static TextField input = new TextField();
	public static boolean vote = true;
	
	// ���ӿ� ȭ�� ���� �ڵ�
	// ���� ���� �� �޴� ��� �޼����� ó����
	public static void mapiaGameCode(String message) {
		
		// ���� ���� �� ȭ�� ���� ����
		// ���� ���� -> �� Ÿ�̸� ����
		
		// �� 60�� Ÿ�̸� �۵�
		if(message.equals("1Y2O1S4EPHdayTimer")) {
			voteBtn(false);
			dayTimer();
		}
		
		// ���� ���� �ڵ� ����
		if(message.equals("1Y2O1S4EPHjob")) { 
			jobStatus = true;
		}else if(jobStatus) {	// ���� ���� �� false
			jobChange(message);
		}else if(message.equals("1Y2O1S4EPHentireChat")) {	// ��ü �޼��� ä��â�� ������
			entireChatStatus = true;
		}else if(message.equals("1Y2O1S4EPHvote")) {	// ��ǥ ���� �޼��� ����
			vote = true;								// ��ǥ
			voteBtn(vote);								// ��ǥ ���� ��ư���� ��ȯ
			voteTimer();								// ��ǥ Ÿ�̸� ����
		}else if(message.equals("1Y2O1S4EPHvoteE")) {	// ��ǥ ���� ���� �޼��� ����
			vote = false;								// ��ǥ ����
			voteBtn(vote);								// ���� ��ư���� ��ȯ
		}else if(message.equals("1Y2O1S4EPnight")) {
			// �� ���� 13�� �ð� ����
			nightTimer();
			entireChatStatus = false;
			voteBtn(true);
		}
		else if(entireChatStatus) {
			Platform.runLater(() -> { // ��ü ü�� ���� ���� ���¸� ä��â�� �޼��� ������
				textArea.appendText(message);
			});
		}
				
	}
	
	// �� 13�� Ÿ�̸� ����
	public static void nightTimer() {
		Timer night_timer = new Timer();
		TimerTask night_task = new TimerTask() {
			int t = 13;
			@Override
			public void run() {
				if(t>=0) {
					timeText.setText("���� �ð�:"+Integer.toString(t));
					t--;
				}else {
					night_timer.cancel();
				}			
			}
		};
		
		night_timer.schedule(night_task, 1000, 1000);
	}
	
	// ��ǥ ���� ���� ��ư
	public static void voteBtn(boolean vote) {
		if(vote) {
			sendButton.setOnAction(event -> {
				send(input.getText());
				input.setText("");
				input.requestFocus();
			});
		}
		else {
			sendButton.setOnAction(event -> {
				send(userName.getText() + ": " + input.getText() + "\n");
				input.setText("");
				input.requestFocus();
			});
		}
	}
	
	// ��ǥ 8�� �ð� ������
	public static void voteTimer() {
		Timer vote_timer = new Timer();
		TimerTask vote_task = new TimerTask() {
			int t = 8;
			@Override
			public void run() {
				if(t>=0) {
					timeText.setText("���� �ð�:"+Integer.toString(t));
					t--;
				}else {
					vote_timer.cancel();
				}			
			}
		};
		vote_timer.schedule(vote_task, 1000, 1000);
	}
	
	// �� 60�� �ð� ������
	public static void dayTimer() {
		Timer day_timer = new Timer();
		TimerTask day_task = new TimerTask() {
			int t = 60;
			@Override
			public void run() {
				if(t>=0) {
					timeText.setText("���� �ð�:"+Integer.toString(t));
					t--;
				}else {
					day_timer.cancel();
				}			
			}
		};
		
		day_timer.schedule(day_task, 1000, 1000);
	}
	
	// �ڽ��� ���� ǥ��
	public static void jobChange(String message) {
		
		if(message.equals("1Y2O1S4EPHmapia")) {
			jobText.setText("���Ǿ�");
		}else if(message.equals("1Y2O1S4EPHpolice")) {
			jobText.setText("����");
		}else if(message.equals("1Y2O1S4EPHdoctor")) {
			jobText.setText("�ǻ�");
		}else {
			jobText.setText("�ù�");
		}
		
		jobStatus = false;
	}
	
	// Ŭ���̾�Ʈ ���α׷� ���� �޼ҵ��Դϴ�.
	public void startClient(String IP, int port) {
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port);
					receive();
				}catch (Exception e) {
					if(!socket.isClosed()) {
						stopClient();
						System.out.println("[���� ���� ����]");
						Platform.exit();
					}
				}
			}
		};
		thread.start();
	}
	
	// Ŭ���̾�Ʈ ���α׷� ���� �޼ҵ��Դϴ�.
	public static void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// �����κ��� �޼����� ���޹޴� �޼ҵ��Դϴ�.
	public void receive() {
		while(true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[255];
				int length = in.read(buffer);
				if(length == -1) throw new IOException();
				String message = new String(buffer, 0, length, "UTF-8");
				
				if(nameStatus) {	// �̸� ����Ǿ����� Ȯ��
					nameSet(message);
				}else if(message.equals("1Y2O1S4EPHgamestart")) { // ���� ���� �ڵ� ����
					gameStatus = true;		
				}else if(gameStatus) {	// ���� ���۽� ���Ǿ� ���� �ڵ�� �޼��� ���� �޼��� ó��
					mapiaGameCode(message);
				}
				else {
					Platform.runLater(() -> {
						textArea.appendText(message);
					});
				}
				
				
			}catch (Exception e) {
				stopClient();
				break;
			}
		}
	}
	
	// ó�� �ѹ� ���� �г��� ����
	public void nameSet(String message) {
		Platform.runLater(() -> {
			userName.setText(message + "��° �÷��̾�");
		});	
		nameStatus = false;
	}
	
	// ������ �޼����� �����ϴ� �޼ҵ��Դϴ�.
	public static void send(String message) {
		Thread thread = new Thread() {
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				}catch (Exception e) {
					stopClient();
				}
			}
		};
		thread.start();
	}
	
	// ������ ���α׷��� ���۽�Ű�� �޼ҵ��Դϴ�.	
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		HBox hbox = new HBox();
		hbox.setSpacing(5);
		
		userName.setPrefWidth(150);
		userName.setEditable(false);
		HBox.setHgrow(userName, Priority.ALWAYS);
		
		String IPText = "172.30.1.17";
		String portText = "9876";
		
		
		timeText.setEditable(false);
		
		jobText.setEditable(false);
		
		btnReady.setDisable(true);
		btnReady.setPrefWidth(100);
		btnReady.setOnAction(event -> {
			send("1214YOSEPHready");
			btnReady.setDisable(true);
		});
		
		hbox.getChildren().addAll(userName, timeText, jobText, btnReady);
		root.setTop(hbox);
		
		textArea = new TextArea();
		textArea.setEditable(false);
		root.setCenter(textArea);
		
		
		input.setPrefWidth(Double.MAX_VALUE);
		input.setDisable(true);
		
		input.setOnAction(event -> {
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
		
		
		sendButton.setDisable(true);
		
		sendButton.setOnAction(event -> {
			send(userName.getText() + ": " + input.getText() + "\n");
			input.setText("");
			input.requestFocus();
		});
		
		Button connectionButton = new Button("�����ϱ�");
		connectionButton.setOnAction(event -> {
			if(connectionButton.getText().equals("�����ϱ�")) {
				int port = 9876;
				try {
					port = Integer.parseInt(portText);
				} catch (Exception e) {
					e.printStackTrace();
				}
				startClient(IPText, port);
				Platform.runLater(() -> {
					textArea.appendText("[ä�ù� ����]\n");
				});
				connectionButton.setText("�����ϱ�");
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
				btnReady.setDisable(false);
			}else {
				stopClient();
				Platform.runLater(() -> {
					textArea.appendText("[ä�ù� ����]\n");
				});
				connectionButton.setText("�����ϱ�");
				input.setDisable(true);
				sendButton.setDisable(true);
				nameStatus = true;
			}
		});
		
		BorderPane pane = new BorderPane();
		pane.setLeft(connectionButton);
		pane.setCenter(input);
		pane.setRight(sendButton);
		
		root.setBottom(pane);
		Scene scene = new Scene(root, 400, 400);
		primaryStage.setTitle("[ä�� Ŭ���̾�Ʈ]");
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(event -> stopClient());
		primaryStage.show();
		
		connectionButton.requestFocus();
	}
	
	// ���α׷��� �������Դϴ�.
	public static void main(String[] args) {
		launch(args);
	}
}