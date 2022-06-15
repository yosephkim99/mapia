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
	static TextField userName = new TextField("O번째 플레이어");
	private boolean nameStatus = true;	// 이름이 변경되어야 하면 true 변경 되었으면 false
	Button btnReady = new Button("준비");
	public static boolean jobStatus = false;	// 게임이 시작되면 true로 바뀌어 직업 수시 받음
	static TextField jobText = new TextField("");
	public static boolean gameStatus = false;	// 게임 시작 전 false 시작 후 true
	public static boolean entireChatStatus = false;
	static TextField timeText = new TextField("시간");	// 시간 필드
	static Button sendButton = new Button("보내기");
	static TextField input = new TextField();
	public static boolean vote = true;
	
	// 게임용 화면 제어 코드
	// 게임 시작 후 받는 모든 메세지를 처리함
	public static void mapiaGameCode(String message) {
		
		// 게임 시작 후 화면 제어 순서
		// 직업 수신 -> 낮 타이머 수신
		
		// 낮 60초 타이머 작동
		if(message.equals("1Y2O1S4EPHdayTimer")) {
			voteBtn(false);
			dayTimer();
		}
		
		// 직업 변경 코드 수신
		if(message.equals("1Y2O1S4EPHjob")) { 
			jobStatus = true;
		}else if(jobStatus) {	// 직업 변경 후 false
			jobChange(message);
		}else if(message.equals("1Y2O1S4EPHentireChat")) {	// 전체 메세지 채팅창에 보여줌
			entireChatStatus = true;
		}else if(message.equals("1Y2O1S4EPHvote")) {	// 투표 상태 메세지 수신
			vote = true;								// 투표
			voteBtn(vote);								// 투표 상태 버튼으로 전환
			voteTimer();								// 투표 타이머 시작
		}else if(message.equals("1Y2O1S4EPHvoteE")) {	// 투표 종료 상태 메세지 수신
			vote = false;								// 투표 종료
			voteBtn(vote);								// 원래 버튼으로 전환
		}else if(message.equals("1Y2O1S4EPnight")) {
			// 밤 모드시 13초 시간 설정
			nightTimer();
			entireChatStatus = false;
			voteBtn(true);
		}
		else if(entireChatStatus) {
			Platform.runLater(() -> { // 전체 체팅 수신 가능 상태면 채팅창에 메세지 보여줌
				textArea.appendText(message);
			});
		}
				
	}
	
	// 밤 13초 타이머 설정
	public static void nightTimer() {
		Timer night_timer = new Timer();
		TimerTask night_task = new TimerTask() {
			int t = 13;
			@Override
			public void run() {
				if(t>=0) {
					timeText.setText("남은 시간:"+Integer.toString(t));
					t--;
				}else {
					night_timer.cancel();
				}			
			}
		};
		
		night_timer.schedule(night_task, 1000, 1000);
	}
	
	// 투표 모드시 전송 버튼
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
	
	// 투표 8초 시간 보여줌
	public static void voteTimer() {
		Timer vote_timer = new Timer();
		TimerTask vote_task = new TimerTask() {
			int t = 8;
			@Override
			public void run() {
				if(t>=0) {
					timeText.setText("남은 시간:"+Integer.toString(t));
					t--;
				}else {
					vote_timer.cancel();
				}			
			}
		};
		vote_timer.schedule(vote_task, 1000, 1000);
	}
	
	// 낮 60초 시간 보여줌
	public static void dayTimer() {
		Timer day_timer = new Timer();
		TimerTask day_task = new TimerTask() {
			int t = 60;
			@Override
			public void run() {
				if(t>=0) {
					timeText.setText("남은 시간:"+Integer.toString(t));
					t--;
				}else {
					day_timer.cancel();
				}			
			}
		};
		
		day_timer.schedule(day_task, 1000, 1000);
	}
	
	// 자신의 직업 표시
	public static void jobChange(String message) {
		
		if(message.equals("1Y2O1S4EPHmapia")) {
			jobText.setText("마피아");
		}else if(message.equals("1Y2O1S4EPHpolice")) {
			jobText.setText("경찰");
		}else if(message.equals("1Y2O1S4EPHdoctor")) {
			jobText.setText("의사");
		}else {
			jobText.setText("시민");
		}
		
		jobStatus = false;
	}
	
	// 클라이언트 프로그램 동작 메소드입니다.
	public void startClient(String IP, int port) {
		Thread thread = new Thread() {
			public void run() {
				try {
					socket = new Socket(IP, port);
					receive();
				}catch (Exception e) {
					if(!socket.isClosed()) {
						stopClient();
						System.out.println("[서버 접속 실패]");
						Platform.exit();
					}
				}
			}
		};
		thread.start();
	}
	
	// 클라이언트 프로그램 종료 메소드입니다.
	public static void stopClient() {
		try {
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 서버로부터 메세지를 전달받는 메소드입니다.
	public void receive() {
		while(true) {
			try {
				InputStream in = socket.getInputStream();
				byte[] buffer = new byte[255];
				int length = in.read(buffer);
				if(length == -1) throw new IOException();
				String message = new String(buffer, 0, length, "UTF-8");
				
				if(nameStatus) {	// 이름 변경되었는지 확인
					nameSet(message);
				}else if(message.equals("1Y2O1S4EPHgamestart")) { // 게임 시작 코드 수신
					gameStatus = true;		
				}else if(gameStatus) {	// 게임 시작시 마피아 게임 코드로 메세지 보내 메세지 처리
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
	
	// 처음 한번 실행 닉네임 설정
	public void nameSet(String message) {
		Platform.runLater(() -> {
			userName.setText(message + "번째 플레이어");
		});	
		nameStatus = false;
	}
	
	// 서버로 메세지를 전송하는 메소드입니다.
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
	
	// 실제로 프로그램을 동작시키는 메소드입니다.	
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
		
		Button connectionButton = new Button("접속하기");
		connectionButton.setOnAction(event -> {
			if(connectionButton.getText().equals("접속하기")) {
				int port = 9876;
				try {
					port = Integer.parseInt(portText);
				} catch (Exception e) {
					e.printStackTrace();
				}
				startClient(IPText, port);
				Platform.runLater(() -> {
					textArea.appendText("[채팅방 접속]\n");
				});
				connectionButton.setText("종료하기");
				input.setDisable(false);
				sendButton.setDisable(false);
				input.requestFocus();
				btnReady.setDisable(false);
			}else {
				stopClient();
				Platform.runLater(() -> {
					textArea.appendText("[채팅방 퇴장]\n");
				});
				connectionButton.setText("접속하기");
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
		primaryStage.setTitle("[채팅 클라이언트]");
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(event -> stopClient());
		primaryStage.show();
		
		connectionButton.requestFocus();
	}
	
	// 프로그램의 진입점입니다.
	public static void main(String[] args) {
		launch(args);
	}
}