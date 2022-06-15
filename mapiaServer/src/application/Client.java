package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
	// 한명의 클라이언트와 통신하도록 해주는 클라이언트 클레스

	Socket socket;
	public int IPlayer = -1;
	public boolean isGame = false;
	public boolean DOA = true;	// DeadOrAlive 플레이어 생존 여부
	public boolean mapia = false;
	public boolean police = false;
	public boolean doctor = false;
	public boolean other = false;
	public boolean voteStatus = false;	// 플레이어 투표 상태
	public int votePlayer = -1;			// 자기가 투표한 인원
	public boolean ADStatus = false;	// 찬반 투표 상태
	public int ADPlayer = -1;			// 찬반 투표 1 = 처형 2 = 생존
	public boolean peopleChat = false;	// 플레이어 채팅이 활성화되면 채팅을 전송 못함
	public boolean mapiaChat = false;	// 마피아 채팅이 활성화되면 채팅과 선택체팅이 가능함
	public boolean PDChat = false;		// 경찰 의사 채팅이 활성화되면 플레이어 선택 채팅만 가능
	
	public static int PDChoicePeople = 0;		// 경찰, 의사가 밤에 선택한 플레이어
	public static int mapiaChoicePeople = 0;	// 마피아가 밤에 선택한 플레이어
	
	public Client(Socket socket, int i) {
		this.socket = socket;
		IPlayer = i;
		receive();
	}
	
	// 클라이언트로부터 메세지를 전달 받는 메소드입니다.
	public void receive() {
		send(Integer.toString(IPlayer));
		Runnable thread = new Runnable(){
			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream(); // 어떠한 내용을 전달 받음
						byte[] buffer = new byte[512];
						int length = in.read(buffer); 
						while(length == -1) throw new IOException();
						System.out.println("[메세지 수신 성공]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						String message = new String(buffer, 0, length, "UTF-8");
						// 클라이언트에게 전달받은 메세지를 다른 클라이언트들에게 전달
						// 게임 시작 전
						if(!isGame) {
							prevGame(message);
						}else { // 게임 시작 후
							// 상황은 세가지로 나뉘어짐
							// 1. 메세지를 전체 플레이어한테 보낸다. entireChat() mapiaGame.java에서는 사용할 일 없음
							// 2. 자기 자신만 보이는 메세지를 송신한다.	send()
							// 3. 메세지 송신 불가 상태		// 그냥 안보내면 됨
							// 플레이어 제어용 코드 ex(타이머, 등은 어쩔 수 없이 전체 플레이어에게 보낸다.)
							// 투표 상태일 경우 자기 자신한테만 메세지가 보이게 수정
							
							if(DOA) {	// 플레이어는 생존을 해야지 채팅을 할 수 있다.
								// 순서
								// 게임 시작 전 -> 플레이어 투표 상태 -> 찬반 투표 상태
								// ->
								if(voteStatus) {	// 플레이어 투표 상태
									vote(message);
								}else if(ADStatus) {		// 찬반 투표 상태
									ADvote(message);
								}else if(mapiaChat) {
									mapiaC(message);
								}else if(PDChat) {
									PDC(message);
								}else if(peopleChat) {
									// 아무 것도 못함
								}
								else {				
									entireChat(message);
								}
							}
							
						}
					}
				}catch (Exception e) {
					try {
						System.out.println("[메세지 수신 오류]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
					}catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);
	}
	
	// mapiaChat 코드
	public static void mapiaC(String message) {
		mapiaChoicePeople = 0;
		if(message.matches("[+-]?\\d*(\\.\\d+)?")) {
			if(0 <= Integer.parseInt(message) && 8 >= Integer.parseInt(message)) {
				mapiaChoicePeople = Integer.parseInt(message);
				for(Client c: Main.clients) {
					if(c.mapia) {
						c.send("* " + mapiaChoicePeople + "플레이어를 선택했습니다! *\n");
					}
				}
			}
		}else {
			for(Client c: Main.clients) {
				if(c.mapia) {
					c.send(message);
				}
			}
		}
	}
	
	// PDChat 코드
	public void PDC(String message) {
		PDChoicePeople = 0;
		if(!message.matches("[+-]?\\d*(\\.\\d+)?")) {
			send("*숫자를 입력해 주세요!*\n");
		}else {
			int i = Integer.parseInt(message);
			if(i >= 0 && i <= 8) {
				PDChoicePeople = Integer.parseInt(message);
				send("*" + i + "플레이어를 선택했습니다.*\n");
			}else {
				send("*0~8의 플레이어만 선택해주세요!*\n");
			}
		}	
	}
	
	// 찬반 투표
	public void ADvote(String message) {
		ADPlayer = 0;
		if(!message.matches("[+-]?\\d*(\\.\\d+)?")) {
			send("숫자를 입력해 주세요!\n");
		}else {
			int i = Integer.parseInt(message);
			if(i == 1) {
				ADPlayer = Integer.parseInt(message);
				send("찬성하셨습니다.\n");
			}else if(i == 2){
				ADPlayer = Integer.parseInt(message);
				send("반대하셨습니다.\n");
			}else {
				send("1 or 2를 입력해주세요!\n");
			}
		}	
	}
	
	// 플레이어 투표
	public void vote(String message) {
		votePlayer = -1;
		if(!message.matches("[+-]?\\d*(\\.\\d+)?")) {
			send("플레이어의 숫자를 입력해 주세요!\n");
		}else {
			int i = Integer.parseInt(message);
			if(i < Main.clients.size() ) {
				votePlayer = Integer.parseInt(message);
				send("자신이 투표한 플레이어 = " + votePlayer + "\n");
			}else {
				send("없는 플레이어입니다!\n");
			}
		}
		
	}
	
	// 게임 시작 전 코드
	public void prevGame(String message) {		
		// 준비 버튼 코드 수신
		if(message.equals("1214YOSEPHready")) {
			mapiaGame.ready++;
			mapiaGame.allPeopleReady();
		}	
		// 준비 버튼 코드 수신 X -> 전체채팅
		else {
			entireChat(message);
		}
	}
	
	// 자신의 채팅이 전체 클라이언트에게 보냄
	public void entireChat(String message) {
		for(Client client : Main.clients) {
			client.send(message);
		}
	}
	
	// 클라이언트에게 메시지를 전송하는 메소드입니다.
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					try {
						System.out.println("[메세지 송신 오류]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						Main.clients.remove(Client.this);
						socket.close();
					}catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);
	}
	
}