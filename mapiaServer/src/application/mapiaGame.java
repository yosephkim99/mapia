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
	private static boolean dayEnd = true;	// 낮 종료시 -> 밤
	private static boolean nightEnd = false; // 밤 종료시 -> 낮
	
	// 게임 흐름 제어 코드
	public static void mapiaGameCode() {
		// 마피아 전용 화면 제어 코드 송신
		for(Client c : Main.clients) {
			c.send("1Y2O1S4EPHgamestart");
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		
		// 역할 분배 코드 전송
		sendRole();
		
//		 낮 -> 투표 -> 게임 종료(시민 승 or 마피아 승) or 밤 -> 능력 -> 게임 종료(시민 승) or 낮
//		 while(Main.clients.size() - 2 > 2)
		day();
	}
	
	// 밤
	// 밤에는 일반 시민은 채팅을 못침
	// 경찰, 의사는 플레이어를 선택만 할 수 있음 경찰 의사는 밤이 끝나면 결과를 받음
	// 마피아는 밤이 되면 채팅 + 플레이어 선택이 가능함
	// 밤이 끝나면 선택한 플레이어가 의사가 선택한 플레이어가 아니라면 해당 플레이어를 죽임
	// 밤이 끝나면 다시 gameSet()으로 확인해서 게임 종료 여부 확인
	// 밤 종료시 다시 낮으로 넘김
	public static void night() {
		// 시민들 타이머 13초 설정
		for(Client c: Main.clients) {
			c.send("1Y2O1S4EPnight");
		}
		try {
			Thread.sleep(10);
		} catch (Exception e) {
			System.out.println(e);
		}
		
		// 플레이어 여부 확인
		// 시민 채팅 x | 경찰, 의사 플레이어 선택 채팅 o | 마피아 채팅 o 플레이어 선택 채팅 o
		for(Client c : Main.clients) {
			c.send("-------------------------------------\n"
					+"밤이 되었습니다!\n" + 
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
		// 마피아, 경찰, 의사 전용 공지
		for(Client c: Main.clients) {
			if(c.mapia) {
				c.send("당신들은 마피아 입니다. 지금부터 서로 상의를 해 죽일 플레이어를 선택하세요\n" +
						"플레이어를 죽이고 싶을 경우 플레이어 숫자를 입력하세요.\n");
			}else if(c.police) {
				c.send("당신은 경찰입니다. 수상한 플레이어 숫자를 입력해주세요.\n");
			}else if(c.doctor) {
				c.send("당신은 의사입니다. 살리고 싶은 플레이어 숫자를 입력해주세요.\n");
			}
		}
		try {
			Thread.sleep(10);
		} catch (Exception e) {
			System.out.println(e);
		}
		
		//	Client.java에서 기능 구현
		// timer로 13(+2)초 후 gaemSet()실행 + 마피아 경찰 의사가 선택한 플레이어 결과 각각 알려줌
		// 13(+2)초 후 각 c.Chat = false 해주어야 함
		Timer night_timer = new Timer();
		TimerTask night_task = new TimerTask() {
			@Override
			public void run() {
				int mapiaP = -1;	// 마피아가 살인을 선택한 플레이어, 숫자 -1 = null
				int doctorP = -1;	// 의사가 선택한 플레이어, 숫자 -1 = null
				// 경찰, 마피아, 의사가 선택한 플레이어 결과를 해당 플레이어에게만 알려줌
				for(Client c: Main.clients) {
					if(c.police) {
						if(Main.clients.get(c.PDChoicePeople).mapia) {
							c.send("* " + c.PDChoicePeople + "플레이어는 마피아입니다!\n");
						}else {
							c.send("* " + c.PDChoicePeople + "플레이어는 마피아가 아닙니다!\n");
						}
					}else if(c.mapia) {
						mapiaP = c.mapiaChoicePeople;
					}else if(c.doctor) {
						doctorP = c.PDChoicePeople;
					}
				}
				// 마피아가 선택한 플레이어가 의사가 선택한 플레이어가 아니라면 kill
				if(mapiaP != doctorP) {
					for(Client c: Main.clients) {
						c.send("-------------------------------------\n" +
								"살인이 일어났습니다! " + mapiaP + "플레이어 사망\n" +
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
					Main.clients.get(mapiaP).send("당신은 죽었습니다.\n");
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
	
	// 게임 종료 확인
	public static void gameSet() {
		// 마피아가 시민 수와 동일하거나 더 많으면 마피아 승 게임 종료
		if(mapiaP >= others) {
			mapiaWin();	// 마피아 승
		}else if(mapiaP == 0) {
			peopleWin();// 시민 승
		// 게임이 끝나지 않았을 경우 낮 or 밤으로 넘어감
		}else if(dayEnd){
			night();	// 낮이 종료됐으면 밤 코드 실행 후 낮과 밤 상태 변경
			dayEnd = false;
			nightEnd = true;
		}else if(nightEnd) {
			day();		// 밤이 종료됐으면 낮 코드 실행 후 낮과 밤 상태 변경
			dayEnd = true;
			nightEnd = false;
		}
	}
	
	// 투표
	public static void vote() {
		for(Client c : Main.clients) {
			c.send("1Y2O1S4EPHvote");
			c.send("-------------------------------------\n"
					+"낮이 끝났습니다!\n8초 동안 의심스러운 플레이어를 투표하세요!\n"+
					"플레이어의 숫자를 입력하시면 투표가 됩니다!\n" +
					"-------------------------------------\n");
			c.voteStatus = true; 	// 플레이어 투표 상태로 넘김
									// 투표 상태의 경우 개인 메세지로 수신
		}
		
		// 8초(플레이어 오류 +2초) 기다리고 투표 모드 종료
		Timer vote_timer = new Timer();
		TimerTask vote_task = new TimerTask() {
			@Override
			public void run() {
				// 뮤텍스로 투표 플레이어++
				for(Client c : Main.clients) {
					if(c.DOA && c.votePlayer != -1) {
						Main.MVP(c.votePlayer);
					}				
				}
				// 뮤텍스 종료
				try {
					Thread.sleep(20);
				} catch (Exception e) {
					System.out.println(e);
				}
				int MostI = 0;	// 투표된 플레이어
				for(int i=0;i<8;i++) {
					if(Main.mostVotePlayer[MostI] < Main.mostVotePlayer[i])
						MostI = i;
				}
				for(Client c : Main.clients) {
					c.voteStatus = false;
					c.send("-------------------------------------\n"
							+"투표가 종료되었습니다!\n투표를 제일 많이 받은 플레이어: " + 
							MostI + "\n"+
							"-------------------------------------\n");											
				}
				
				try {
					Thread.sleep(20);
				} catch (Exception e) {
					System.out.println(e);
				}
				
				Main.mostVotePlayer = new int[8];	// 다시 초기값으로 세팅
				
				for(Client c : Main.clients) {
					c.send("-------------------------------------\n"
							+"해당 플레이어 처형 찬반 투표를 시작합니다!\n " + 
							"찬성:1 반대:2\n"+
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
	
	// 플레이어 찬반 투표
	public static void ADVote(int player) {
		// 찬반 투표 결과 전송 및 투표 상태 종료 해줘야 함
		// 찬반 여부에 따라 해당 플레이어 처형
		
		Timer AD_timer = new Timer();	
		TimerTask AD_task = new TimerTask() {
			@Override
			public void run() {
				String ADmg = "";
				int MostI = 0;
				// 뮤텍스로 찬반 투표 계산중
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
				// 찬반 투표 계산 종료
				if(MostI == 1) {
					ADmg = "플레이어가 처형 되었습니다.";
				}else if(MostI == 2) {
					ADmg = "플레이어가 생존 했습니다.";
				}
				for(Client c : Main.clients) {	
					c.send("-------------------------------------\n"
							+"투표가 종료되었습니다!\n" + 
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
					Main.clients.get(player).send("당신은 죽었습니다.\n");
				}
				Main.ADPlayer = new int [3]; // 값 초기화
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
	
	// 플레이어 전체 메세지 수신 가능 상태
	public static void entrieChat() {
		for(Client c : Main.clients) {
			c.send("1Y2O1S4EPHentireChat");
		}
	}
	
	// 낮
	public static void day() {
		
		wait = true;
		// 플레이어에게 시간 전송
		// 타이머 생성해서 참가자들에게 시간 전송 및 낮 시간 제어
		for(Client c : Main.clients) {
			c.send("1Y2O1S4EPHdayTimer");
		}
		// 플레이어들 투표 상태로 넘어감
		Timer day_timer = new Timer();
		TimerTask vote_task = new TimerTask() {
			@Override
			public void run() {
				vote();
			}
		};
		
		// 전체 플레이어 모든 메세지 수신 가능 상태로 만들기
		// 플레이어 흐름 제어는 위 부분에서 해야 함!
		try {
			Thread.sleep(100);
		} catch (Exception e) {
			System.out.println(e);
		}
		entrieChat();
		
		// 메세시 수신 늦을 수 있으므로 잠깐 슬립
		
		// 낮 공지 메세지
		for(Client c : Main.clients) {
			c.send("낮이 되었습니다.\n---현재 플레이어 상태---\n");
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
			c.send("지금 부터 60초간 서로 대화를 할 수 있습니다.\n");
		}
		
		// 60초 후 플레이어들 투표 상태로 만듬
		day_timer.schedule(vote_task, 60000);
	}
	
	// 플레이어 생존 여부 메세지 전송
	public static void playerStatusSend(Client c) {
		for(Client c2 : Main.clients) {
			if(c2.DOA) {
				c.send(c2.IPlayer + "번째 참가자(생존)" + "\n");
			}else {
				c.send(c2.IPlayer + "번째 참가자(사망)" + "\n");
			}
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
	
	// 시민팀 승리 메세지 전송
	public static void peopleWin() {
		for(Client c : Main.clients) {
			c.send("게임이 종료 되었습니다!" + "\n" +
					"시민팀 승!" + "\n" + 
					"---플레이어 목록---" + "\n" );	
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				System.out.println(e);
			}
				
			for(Client c2 : Main.clients) {
				if(c2.mapia) {
					c.send(c2.IPlayer + "번째 참가자(마피아)" + "\n");
				}else if(c2.police) {
					c.send(c2.IPlayer + "번째 참가자(경찰)" + "\n");
				}else if(c2.doctor) {
					c.send(c2.IPlayer + "번째 참가자(의사)" + "\n");
				}else {
					c.send(c2.IPlayer + "번째 참가자(시민)" + "\n");
				}
				try {
					Thread.sleep(10);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		}
	}
	
	// 마피아팀 승리 메세지 전송
	public static void mapiaWin() {
		for(Client c : Main.clients) {
			c.send("게임이 종료 되었습니다!" + "\n" +
					"마피아팀 승!" + "\n" + 
					"---플레이어 목록---" + "\n" );	
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				System.out.println(e);
			}
			
			for(Client c2 : Main.clients) {
				if(c2.mapia) {
					c.send(c2.IPlayer + "번째 참가자(마피아)" + "\n");
				}else if(c2.police) {
					c.send(c2.IPlayer + "번째 참가자(경찰)" + "\n");
				}else if(c2.doctor) {
					c.send(c2.IPlayer + "번째 참가자(의사)" + "\n");
				}else {
					c.send(c2.IPlayer + "번째 참가자(시민)" + "\n");
				}
				try {
					Thread.sleep(10);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
		}
	}
	
	// 역할 전송 코드
	// 역할 분배 코드
	public static void sendRole() {
		int TTP = Main.clients.size();	// 총 플레이어 수
		int cliN[] = new int[TTP];		// ex) cliN = [3, 1, 2, 0]
		Random r = new Random();
		
		for(int i=0;i<TTP;i++) {
			cliN[i] = r.nextInt(TTP);
			for(int j=0;j<i;j++) {
				if(cliN[i] == cliN[j]) i--;
			}
		}
		
		// 직업 화면 제어 코드 송신
		for(Client c : Main.clients) {
			c.send("1Y2O1S4EPHjob");
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				System.out.println(e);
			}
		}		
		
		// 각 플레이어에게 자신의 직업 전송
		for(int i=0;i<TTP;i++) {
			if(i<2) {
				Main.clients.get(cliN[i]).send("1Y2O1S4EPHmapia"); 	// 마피아
				Main.clients.get(cliN[i]).mapia = true;
			}else if(i == 2) {
				Main.clients.get(cliN[i]).send("1Y2O1S4EPHpolice");	// 경찰
				Main.clients.get(cliN[i]).police = true;
			}else if(i == 3) {
				Main.clients.get(cliN[i]).send("1Y2O1S4EPHdoctor");	// 의사
				Main.clients.get(cliN[i]).doctor = true;
			}else {
				Main.clients.get(cliN[i]).send("1Y2O1S4EPHpeople");	// 시민
				Main.clients.get(cliN[i]).other = true;
			}
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				System.out.println(e);
			}
		}	
	}
	
	// 모든 플레이어 준비 완료상태인지 체크
	// 게임 시작 여부 확인
	public static void allPeopleReady() {
		// 게임 시작
		if(ready == Main.clients.size() && Main.clients.size() >= 5) {
			for(Client c : Main.clients) {
				c.isGame = true;		// 모든 플레이어 상태 게임시작으로 변경
				c.send("모든 플레이어가 준비 했습니다!" + "\n" + 
						"지금부터 마피아 게임을 시작합니다!" + "\n" );	
			}
			others = Main.clients.size() - 2;
			mapiaGameCode();
		}
	}
	
}
