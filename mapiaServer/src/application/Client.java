package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
	// �Ѹ��� Ŭ���̾�Ʈ�� ����ϵ��� ���ִ� Ŭ���̾�Ʈ Ŭ����

	Socket socket;
	public int IPlayer = -1;
	public boolean isGame = false;
	public boolean DOA = true;	// DeadOrAlive �÷��̾� ���� ����
	public boolean mapia = false;
	public boolean police = false;
	public boolean doctor = false;
	public boolean other = false;
	public boolean voteStatus = false;	// �÷��̾� ��ǥ ����
	public int votePlayer = -1;			// �ڱⰡ ��ǥ�� �ο�
	public boolean ADStatus = false;	// ���� ��ǥ ����
	public int ADPlayer = -1;			// ���� ��ǥ 1 = ó�� 2 = ����
	public boolean peopleChat = false;	// �÷��̾� ä���� Ȱ��ȭ�Ǹ� ä���� ���� ����
	public boolean mapiaChat = false;	// ���Ǿ� ä���� Ȱ��ȭ�Ǹ� ä�ð� ����ü���� ������
	public boolean PDChat = false;		// ���� �ǻ� ä���� Ȱ��ȭ�Ǹ� �÷��̾� ���� ä�ø� ����
	
	public static int PDChoicePeople = 0;		// ����, �ǻ簡 �㿡 ������ �÷��̾�
	public static int mapiaChoicePeople = 0;	// ���Ǿư� �㿡 ������ �÷��̾�
	
	public Client(Socket socket, int i) {
		this.socket = socket;
		IPlayer = i;
		receive();
	}
	
	// Ŭ���̾�Ʈ�κ��� �޼����� ���� �޴� �޼ҵ��Դϴ�.
	public void receive() {
		send(Integer.toString(IPlayer));
		Runnable thread = new Runnable(){
			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream(); // ��� ������ ���� ����
						byte[] buffer = new byte[512];
						int length = in.read(buffer); 
						while(length == -1) throw new IOException();
						System.out.println("[�޼��� ���� ����]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						String message = new String(buffer, 0, length, "UTF-8");
						// Ŭ���̾�Ʈ���� ���޹��� �޼����� �ٸ� Ŭ���̾�Ʈ�鿡�� ����
						// ���� ���� ��
						if(!isGame) {
							prevGame(message);
						}else { // ���� ���� ��
							// ��Ȳ�� �������� ��������
							// 1. �޼����� ��ü �÷��̾����� ������. entireChat() mapiaGame.java������ ����� �� ����
							// 2. �ڱ� �ڽŸ� ���̴� �޼����� �۽��Ѵ�.	send()
							// 3. �޼��� �۽� �Ұ� ����		// �׳� �Ⱥ����� ��
							// �÷��̾� ����� �ڵ� ex(Ÿ�̸�, ���� ��¿ �� ���� ��ü �÷��̾�� ������.)
							// ��ǥ ������ ��� �ڱ� �ڽ����׸� �޼����� ���̰� ����
							
							if(DOA) {	// �÷��̾�� ������ �ؾ��� ä���� �� �� �ִ�.
								// ����
								// ���� ���� �� -> �÷��̾� ��ǥ ���� -> ���� ��ǥ ����
								// ->
								if(voteStatus) {	// �÷��̾� ��ǥ ����
									vote(message);
								}else if(ADStatus) {		// ���� ��ǥ ����
									ADvote(message);
								}else if(mapiaChat) {
									mapiaC(message);
								}else if(PDChat) {
									PDC(message);
								}else if(peopleChat) {
									// �ƹ� �͵� ����
								}
								else {				
									entireChat(message);
								}
							}
							
						}
					}
				}catch (Exception e) {
					try {
						System.out.println("[�޼��� ���� ����]"
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
	
	// mapiaChat �ڵ�
	public static void mapiaC(String message) {
		mapiaChoicePeople = 0;
		if(message.matches("[+-]?\\d*(\\.\\d+)?")) {
			if(0 <= Integer.parseInt(message) && 8 >= Integer.parseInt(message)) {
				mapiaChoicePeople = Integer.parseInt(message);
				for(Client c: Main.clients) {
					if(c.mapia) {
						c.send("* " + mapiaChoicePeople + "�÷��̾ �����߽��ϴ�! *\n");
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
	
	// PDChat �ڵ�
	public void PDC(String message) {
		PDChoicePeople = 0;
		if(!message.matches("[+-]?\\d*(\\.\\d+)?")) {
			send("*���ڸ� �Է��� �ּ���!*\n");
		}else {
			int i = Integer.parseInt(message);
			if(i >= 0 && i <= 8) {
				PDChoicePeople = Integer.parseInt(message);
				send("*" + i + "�÷��̾ �����߽��ϴ�.*\n");
			}else {
				send("*0~8�� �÷��̾ �������ּ���!*\n");
			}
		}	
	}
	
	// ���� ��ǥ
	public void ADvote(String message) {
		ADPlayer = 0;
		if(!message.matches("[+-]?\\d*(\\.\\d+)?")) {
			send("���ڸ� �Է��� �ּ���!\n");
		}else {
			int i = Integer.parseInt(message);
			if(i == 1) {
				ADPlayer = Integer.parseInt(message);
				send("�����ϼ̽��ϴ�.\n");
			}else if(i == 2){
				ADPlayer = Integer.parseInt(message);
				send("�ݴ��ϼ̽��ϴ�.\n");
			}else {
				send("1 or 2�� �Է����ּ���!\n");
			}
		}	
	}
	
	// �÷��̾� ��ǥ
	public void vote(String message) {
		votePlayer = -1;
		if(!message.matches("[+-]?\\d*(\\.\\d+)?")) {
			send("�÷��̾��� ���ڸ� �Է��� �ּ���!\n");
		}else {
			int i = Integer.parseInt(message);
			if(i < Main.clients.size() ) {
				votePlayer = Integer.parseInt(message);
				send("�ڽ��� ��ǥ�� �÷��̾� = " + votePlayer + "\n");
			}else {
				send("���� �÷��̾��Դϴ�!\n");
			}
		}
		
	}
	
	// ���� ���� �� �ڵ�
	public void prevGame(String message) {		
		// �غ� ��ư �ڵ� ����
		if(message.equals("1214YOSEPHready")) {
			mapiaGame.ready++;
			mapiaGame.allPeopleReady();
		}	
		// �غ� ��ư �ڵ� ���� X -> ��üä��
		else {
			entireChat(message);
		}
	}
	
	// �ڽ��� ä���� ��ü Ŭ���̾�Ʈ���� ����
	public void entireChat(String message) {
		for(Client client : Main.clients) {
			client.send(message);
		}
	}
	
	// Ŭ���̾�Ʈ���� �޽����� �����ϴ� �޼ҵ��Դϴ�.
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
						System.out.println("[�޼��� �۽� ����]"
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