package application;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class mapiaGame {
	
	public static int ready = 0;
	static boolean wait = false;
	public static int mapiaP = 2;
	public static int others = 0;
	private static boolean dayEnd = true;	// �� ����� -> ��
	private static boolean nightEnd = false; // �� ����� -> ��
	
	// ���� �帧 ���� �ڵ�
	public static void mapiaGameCode() {
		// ���Ǿ� ���� ȭ�� ���� �ڵ� �۽�
		for(Client c : Main.clients) {
			c.send("1Y2O1S4EPHgamestart");
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		
		// ���� �й� �ڵ� ����
		sendRole();
		
//		 �� -> ��ǥ -> ���� ����(�ù� �� or ���Ǿ� ��) or �� -> �ɷ� -> ���� ����(�ù� ��) or ��
//		 while(Main.clients.size() - 2 > 2)
		day();
	}
	
	// ��
	// �㿡�� �Ϲ� �ù��� ä���� ��ħ
	// ����, �ǻ�� �÷��̾ ���ø� �� �� ���� ���� �ǻ�� ���� ������ ����� ����
	// ���Ǿƴ� ���� �Ǹ� ä�� + �÷��̾� ������ ������
	// ���� ������ ������ �÷��̾ �ǻ簡 ������ �÷��̾ �ƴ϶�� �ش� �÷��̾ ����
	// ���� ������ �ٽ� gameSet()���� Ȯ���ؼ� ���� ���� ���� Ȯ��
	// �� ����� �ٽ� ������ �ѱ�
	public static void night() {
		// �ùε� Ÿ�̸� 13�� ����
		for(Client c: Main.clients) {
			c.send("1Y2O1S4EPnight");
		}
		try {
			Thread.sleep(10);
		} catch (Exception e) {
			System.out.println(e);
		}
		
		// �÷��̾� ���� Ȯ��
		// �ù� ä�� x | ����, �ǻ� �÷��̾� ���� ä�� o | ���Ǿ� ä�� o �÷��̾� ���� ä�� o
		for(Client c : Main.clients) {
			c.send("-------------------------------------\n"
					+"���� �Ǿ����ϴ�!\n" + 
					"-------------------------------------\n");
			if(c.other) {
				c.peopleChat = true;
			}else if(c.mapia) {
				c.mapiaChat = true;
			}else if(c.police || c.doctor) {
				c.PDChat = true;
			}
		}
		try {
			Thread.sleep(10);
		} catch (Exception e) {
			System.out.println(e);
		}
		// ���Ǿ�, ����, �ǻ� ���� ����
		for(Client c: Main.clients) {
			if(c.mapia) {
				c.send("��ŵ��� ���Ǿ� �Դϴ�. ���ݺ��� ���� ���Ǹ� �� ���� �÷��̾ �����ϼ���\n" +
						"�÷��̾ ���̰� ���� ��� �÷��̾� ���ڸ� �Է��ϼ���.\n");
			}else if(c.police) {
				c.send("����� �����Դϴ�. ������ �÷��̾� ���ڸ� �Է����ּ���.\n");
			}else if(c.doctor) {
				c.send("����� �ǻ��Դϴ�. �츮�� ���� �÷��̾� ���ڸ� �Է����ּ���.\n");
			}
		}
		try {
			Thread.sleep(10);
		} catch (Exception e) {
			System.out.println(e);
		}
		
		//	Client.java���� ��� ����
		// timer�� 13(+2)�� �� gaemSet()���� + ���Ǿ� ���� �ǻ簡 ������ �÷��̾� ��� ���� �˷���
		// 13(+2)�� �� �� c.Chat = false ���־�� ��
		Timer night_timer = new Timer();
		TimerTask night_task = new TimerTask() {
			@Override
			public void run() {
				int mapiaP = -1;	// ���Ǿư� ������ ������ �÷��̾�, ���� -1 = null
				int doctorP = -1;	// �ǻ簡 ������ �÷��̾�, ���� -1 = null
				// ����, ���Ǿ�, �ǻ簡 ������ �÷��̾� ����� �ش� �÷��̾�Ը� �˷���
				for(Client c: Main.clients) {
					if(c.police) {
						if(Main.clients.get(c.PDChoicePeople).mapia) {
							c.send("* " + c.PDChoicePeople + "�÷��̾�� ���Ǿ��Դϴ�!\n");
						}else {
							c.send("* " + c.PDChoicePeople + "�÷��̾�� ���Ǿư� �ƴմϴ�!\n");
						}
					}else if(c.mapia) {
						mapiaP = c.mapiaChoicePeople;
					}else if(c.doctor) {
						doctorP = c.PDChoicePeople;
					}
				}
				// ���Ǿư� ������ �÷��̾ �ǻ簡 ������ �÷��̾ �ƴ϶�� kill
				if(mapiaP != doctorP) {
					for(Client c: Main.clients) {
						c.send("-------------------------------------\n" +
								"������ �Ͼ���ϴ�! " + mapiaP + "�÷��̾� ���\n" +
								"-------------------------------------\n");
						if(c.other) {
							c.peopleChat = false;
						}else if(c.mapia) {
							c.mapiaChat = false;
						}else if(c.police || c.doctor) {
							c.PDChat = false;
						}
					}
					Main.clients.get(mapiaP).DOA = false;
					Main.clients.get(mapiaP).send("����� �׾����ϴ�.\n");
				}
				
				try {
					Thread.sleep(10);
				} catch (Exception e) {
					System.out.println(e);
				}
				gameSet();
			}
		};
		night_timer.schedule(night_task, 15000);
	}
	
	// ���� ���� Ȯ��
	public static void gameSet() {
		// ���Ǿư� �ù� ���� �����ϰų� �� ������ ���Ǿ� �� ���� ����
		if(mapiaP >= others) {
			mapiaWin();	// ���Ǿ� ��
		}else if(mapiaP == 0) {
			peopleWin();// �ù� ��
		// ������ ������ �ʾ��� ��� �� or ������ �Ѿ
		}else if(dayEnd){
			night();	// ���� ��������� �� �ڵ� ���� �� ���� �� ���� ����
			dayEnd = false;
			nightEnd = true;
		}else if(nightEnd) {
			day();		// ���� ��������� �� �ڵ� ���� �� ���� �� ���� ����
			dayEnd = true;
			nightEnd = false;
		}
	}
	
	// ��ǥ
	public static void vote() {
		for(Client c : Main.clients) {
			c.send("1Y2O1S4EPHvote");
			c.send("-------------------------------------\n"
					+"���� �������ϴ�!\n8�� ���� �ǽɽ����� �÷��̾ ��ǥ�ϼ���!\n"+
					"�÷��̾��� ���ڸ� �Է��Ͻø� ��ǥ�� �˴ϴ�!\n" +
					"-------------------------------------\n");
			c.voteStatus = true; 	// �÷��̾� ��ǥ ���·� �ѱ�
									// ��ǥ ������ ��� ���� �޼����� ����
		}
		
		// 8��(�÷��̾� ���� +2��) ��ٸ��� ��ǥ ��� ����
		Timer vote_timer = new Timer();
		TimerTask vote_task = new TimerTask() {
			@Override
			public void run() {
				// ���ؽ��� ��ǥ �÷��̾�++
				for(Client c : Main.clients) {
					if(c.DOA && c.votePlayer != -1) {
						Main.MVP(c.votePlayer);
					}				
				}
				// ���ؽ� ����
				try {
					Thread.sleep(20);
				} catch (Exception e) {
					System.out.println(e);
				}
				int MostI = 0;	// ��ǥ�� �÷��̾�
				for(int i=0;i<8;i++) {
					if(Main.mostVotePlayer[MostI] < Main.mostVotePlayer[i])
						MostI = i;
				}
				for(Client c : Main.clients) {
					c.voteStatus = false;
					c.send("-------------------------------------\n"
							+"��ǥ�� ����Ǿ����ϴ�!\n��ǥ�� ���� ���� ���� �÷��̾�: " + 
							MostI + "\n"+
							"-------------------------------------\n");											
				}
				
				try {
					Thread.sleep(20);
				} catch (Exception e) {
					System.out.println(e);
				}
				
				Main.mostVotePlayer = new int[8];	// �ٽ� �ʱⰪ���� ����
				
				for(Client c : Main.clients) {
					c.send("-------------------------------------\n"
							+"�ش� �÷��̾� ó�� ���� ��ǥ�� �����մϴ�!\n " + 
							"����:1 �ݴ�:2\n"+
							"-------------------------------------\n");
					c.voteStatus = false;
					c.ADStatus = true;
					c.send("1Y2O1S4EPHvote");
					try {
						Thread.sleep(10);
					} catch (Exception e) {
						System.out.println(e);
					}
				}
				Main.ADPlayer = new int [3];
				ADVote(MostI);
			}
		};
		vote_timer.schedule(vote_task, 10000);
		
	}
	
	// �÷��̾� ���� ��ǥ
	public static void ADVote(int player) {
		// ���� ��ǥ ��� ���� �� ��ǥ ���� ���� ����� ��
		// ���� ���ο� ���� �ش� �÷��̾� ó��
		
		Timer AD_timer = new Timer();	
		TimerTask AD_task = new TimerTask() {
			@Override
			public void run() {
				String ADmg = "";
				int MostI = 0;
				// ���ؽ��� ���� ��ǥ �����
				for(Client c : Main.clients) {
					if(c.DOA && c.ADPlayer != -1) {
						Main.ADP(c.ADPlayer);
					}
				}
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					System.out.println(e);
				}
				
				for(int i=0;i<3;i++) {
					if(Main.ADPlayer[MostI] < Main.ADPlayer[i])
						MostI = i;
				}
				try {
					Thread.sleep(10);
				} catch (Exception e) {
					System.out.println(e);
				}
				// ���� ��ǥ ��� ����
				if(MostI == 1) {
					ADmg = "�÷��̾ ó�� �Ǿ����ϴ�.";
				}else if(MostI == 2) {
					ADmg = "�÷��̾ ���� �߽��ϴ�.";
				}
				for(Client c : Main.clients) {	
					c.send("-------------------------------------\n"
							+"��ǥ�� ����Ǿ����ϴ�!\n" + 
							ADmg + "\n"+
							"-------------------------------------\n");
					try {
						Thread.sleep(10);
					} catch (Exception e) {
						System.out.println(e);
					}
					c.ADStatus = false;
					c.send("1Y2O1S4EPHvoteE");
					try {
						Thread.sleep(10);
					} catch (Exception e) {
						System.out.println(e);
					}
				}
				if(MostI == 1) {
					Main.clients.get(player).DOA = false;
					Main.clients.get(player).send("����� �׾����ϴ�.\n");
				}
				Main.ADPlayer = new int [3]; // �� �ʱ�ȭ
				try {
					Thread.sleep(10);
				} catch (Exception e) {
					System.out.println(e);
				}
				gameSet();
			}
		};
		AD_timer.schedule(AD_task, 10000);
	}
	
	// �÷��̾� ��ü �޼��� ���� ���� ����
	public static void entrieChat() {
		for(Client c : Main.clients) {
			c.send("1Y2O1S4EPHentireChat");
		}
	}
	
	// ��
	public static void day() {
		
		wait = true;
		// �÷��̾�� �ð� ����
		// Ÿ�̸� �����ؼ� �����ڵ鿡�� �ð� ���� �� �� �ð� ����
		for(Client c : Main.clients) {
			c.send("1Y2O1S4EPHdayTimer");
		}
		// �÷��̾�� ��ǥ ���·� �Ѿ
		Timer day_timer = new Timer();
		TimerTask vote_task = new TimerTask() {
			@Override
			public void run() {
				vote();
			}
		};
		
		// ��ü �÷��̾� ��� �޼��� ���� ���� ���·� �����
		// �÷��̾� �帧 ����� �� �κп��� �ؾ� ��!
		try {
			Thread.sleep(100);
		} catch (Exception e) {
			System.out.println(e);
		}
		entrieChat();
		
		// �޼��� ���� ���� �� �����Ƿ� ��� ����
		
		// �� ���� �޼���
		for(Client c : Main.clients) {
			c.send("���� �Ǿ����ϴ�.\n---���� �÷��̾� ����---\n");
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				System.out.println(e);
			}
			playerStatusSend(c);	
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				System.out.println(e);
			}
			c.send("-------------------------------------\n");
		}
		
		try {
			Thread.sleep(20);
		} catch (Exception e) {
			System.out.println(e);
		}
		
		for(Client c : Main.clients) {
			c.send("���� ���� 60�ʰ� ���� ��ȭ�� �� �� �ֽ��ϴ�.\n");
		}
		
		// 60�� �� �÷��̾�� ��ǥ ���·� ����
		day_timer.schedule(vote_task, 60000);
	}
	
	// �÷��̾� ���� ���� �޼��� ����
	public static void playerStatusSend(Client c) {
		for(Client c2 : Main.clients) {
			if(c2.DOA) {
				c.send(c2.IPlayer + "��° ������(����)" + "\n");
			}else {
				c.send(c2.IPlayer + "��° ������(���)" + "\n");
			}
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
	
	// �ù��� �¸� �޼��� ����
	public static void peopleWin() {
		for(Client c : Main.clients) {
			c.send("������ ���� �Ǿ����ϴ�!" + "\n" +
					"�ù��� ��!" + "\n" + 
					"---�÷��̾� ���---" + "\n" );	
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				System.out.println(e);
			}
				
			for(Client c2 : Main.clients) {
				if(c2.mapia) {
					c.send(c2.IPlayer + "��° ������(���Ǿ�)" + "\n");
				}else if(c2.police) {
					c.send(c2.IPlayer + "��° ������(����)" + "\n");
				}else if(c2.doctor) {
					c.send(c2.IPlayer + "��° ������(�ǻ�)" + "\n");
				}else {
					c.send(c2.IPlayer + "��° ������(�ù�)" + "\n");
				}
				try {
					Thread.sleep(10);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		}
	}
	
	// ���Ǿ��� �¸� �޼��� ����
	public static void mapiaWin() {
		for(Client c : Main.clients) {
			c.send("������ ���� �Ǿ����ϴ�!" + "\n" +
					"���Ǿ��� ��!" + "\n" + 
					"---�÷��̾� ���---" + "\n" );	
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				System.out.println(e);
			}
			
			for(Client c2 : Main.clients) {
				if(c2.mapia) {
					c.send(c2.IPlayer + "��° ������(���Ǿ�)" + "\n");
				}else if(c2.police) {
					c.send(c2.IPlayer + "��° ������(����)" + "\n");
				}else if(c2.doctor) {
					c.send(c2.IPlayer + "��° ������(�ǻ�)" + "\n");
				}else {
					c.send(c2.IPlayer + "��° ������(�ù�)" + "\n");
				}
				try {
					Thread.sleep(10);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		}
	}
	
	// ���� ���� �ڵ�
	// ���� �й� �ڵ�
	public static void sendRole() {
		int TTP = Main.clients.size();	// �� �÷��̾� ��
		int cliN[] = new int[TTP];		// ex) cliN = [3, 1, 2, 0]
		Random r = new Random();
		
		for(int i=0;i<TTP;i++) {
			cliN[i] = r.nextInt(TTP);
			for(int j=0;j<i;j++) {
				if(cliN[i] == cliN[j]) i--;
			}
		}
		
		// ���� ȭ�� ���� �ڵ� �۽�
		for(Client c : Main.clients) {
			c.send("1Y2O1S4EPHjob");
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				System.out.println(e);
			}
		}		
		
		// �� �÷��̾�� �ڽ��� ���� ����
		for(int i=0;i<TTP;i++) {
			if(i<2) {
				Main.clients.get(cliN[i]).send("1Y2O1S4EPHmapia"); 	// ���Ǿ�
				Main.clients.get(cliN[i]).mapia = true;
			}else if(i == 2) {
				Main.clients.get(cliN[i]).send("1Y2O1S4EPHpolice");	// ����
				Main.clients.get(cliN[i]).police = true;
			}else if(i == 3) {
				Main.clients.get(cliN[i]).send("1Y2O1S4EPHdoctor");	// �ǻ�
				Main.clients.get(cliN[i]).doctor = true;
			}else {
				Main.clients.get(cliN[i]).send("1Y2O1S4EPHpeople");	// �ù�
				Main.clients.get(cliN[i]).other = true;
			}
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				System.out.println(e);
			}
		}	
	}
	
	// ��� �÷��̾� �غ� �Ϸ�������� üũ
	// ���� ���� ���� Ȯ��
	public static void allPeopleReady() {
		// ���� ����
		if(ready == Main.clients.size() && Main.clients.size() >= 5) {
			for(Client c : Main.clients) {
				c.isGame = true;		// ��� �÷��̾� ���� ���ӽ������� ����
				c.send("��� �÷��̾ �غ� �߽��ϴ�!" + "\n" + 
						"���ݺ��� ���Ǿ� ������ �����մϴ�!" + "\n" );	
			}
			others = Main.clients.size() - 2;
			mapiaGameCode();
		}
	}
	
}
