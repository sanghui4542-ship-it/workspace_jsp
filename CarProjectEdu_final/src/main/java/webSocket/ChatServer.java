package webSocket;   // 이 파일이 속한 패키지(=폴더) 이름. 실제 폴더 구조와 반드시 같아야 한다

import java.io.IOException;   // 파일/네트워크 입출력이 실패했을 때의 예외
import java.util.Collections;   // 목록을 정렬·뒤집기 등 할 때 쓰는 도우미 모음
import java.util.HashSet;   // Set 을 실제로 만들 때 쓰는 구현체
import java.util.Set;   // 중복을 허용하지 않는 모음. 같은 사람이 두 번 들어가지 않는다

import javax.websocket.OnClose;   // 웹소켓 연결이 닫힐 때 실행할 메소드 표시
import javax.websocket.OnError;   // 웹소켓에서 오류가 났을 때 실행할 메소드 표시
import javax.websocket.OnMessage;   // 웹소켓으로 메시지가 왔을 때 실행할 메소드 표시
import javax.websocket.OnOpen;   // 웹소켓 연결이 열릴 때 실행할 메소드 표시
import javax.websocket.Session;   // 웹소켓 연결 하나를 나타내는 객체
import javax.websocket.server.ServerEndpoint;   // 웹소켓 주소를 정하는 표시
/*
	@ServerEndpoint 에너테이션 으로  웹 소켓 서버의 요청명을 지정하여,
	해당 요청명으로 접속하는 클라이언트를  이서버페이지가 처리하게 합니다.
	
	참고.  요청 주소명이  /ChatingServer 이므로 이 웹소켓에 접속하기 위한 전체 URL은 다음과 같습니다.
		
			ws://호스트:포트번호/컨텍스트명/ChatingServer
	
	참고. 웹소켓은 http프로토콜이 아닌 ws프로토콜을 사용합니다.
*/	

@ServerEndpoint("/ChatingServer")
public class ChatServer {
	
	//현재  연결된 클라이언트 들이 저잘될  세션영역을 저장할 Set배열을 만들어 줍니다.
	private static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());
	
	//클라이언트가 접속 요청할때 자동으로 호출되는 메소드 정의
	@OnOpen
	public void onOPen(Session session) {
		
		//접속한 클라이언트의  Session 메모리 영역을  위 Set에 추가 
		clients.add(session);
		
		//접속한 클라이언트를 판단할  Session ID를 출력
		System.out.println("웹 소켓 연결 : " + session.getId());
		
	}
	
	//클라이언트로 부터 메세지를 받았을때 자동으로 호출되는 메소드 정의 
	@OnMessage
	public void onMessage(String message,  Session session) throws IOException {
		
		System.out.println("전송한 메세지  : " +  session.getId() + " : " + message );   // 누가 무슨 말을 보냈는지 콘솔에 남긴다 (수업 중 확인용)

		/*
		 ============================================================================
		   [3단계 작업 - 잠금 대상과 예외 처리를 고친 부분]

		   [Before] synchronized (session) { for (Session client : clients) { ... } }

		     1) 잠그는 대상이 틀렸다.
		        지키려는 자원은 clients(전체 목록)인데 session(내 접속)을 잠갔다.
		        접속자 A와 B가 동시에 메시지를 보내면 서로 다른 자물쇠를 잡으므로
		        둘 다 동시에 clients 를 순회한다. 잠금이 아무 역할을 못 한다.
		        그 사이 다른 사람이 접속/종료하면 목록이 바뀌어
		        ConcurrentModificationException 이 난다.
		        synchronizedSet 은 add/remove 한 건씩만 안전하고,
		        "순회 전체"는 직접 잠가줘야 한다. (자바 API 문서에 명시된 규칙)

		     2) 한 명에게 전송이 실패하면 IOException 이 그대로 던져져
		        뒤에 있는 사람들은 메시지를 아예 못 받았다.
		        브라우저를 강제 종료한 세션이 목록에 남아있으면
		        그 뒤 모든 사람의 채팅이 멈추는 셈이다.

		   [After] clients 를 잠그고, 전송 실패한 세션은 모아서 목록에서 제거한다.

		   [참고] 보낸 사람(session)을 제외하는 것은 버그가 아니라 채팅방 설계다.
		          내가 쓴 말은 내 화면에서 이미 보여주고 있으므로 되돌려받을 필요가 없다.
		 ============================================================================
		*/
		Set<Session> broken = new HashSet<Session>();

		synchronized (clients) {   // synchronized = 여러 사람이 동시에 접속해도 목록이 꼬이지 않게 한 번에 한 명만 들어오게 잠근다

			for (Session client : clients) {   // 접속해 있는 사람을 하나씩 훑는다

				//메아리 로  메세지를 보낸 클라이언트는 제외하고  다른 클라이언트 창 화면에 메세지 전송!
				if (client.equals(session)) {
					continue;
				}

				try {
					//메아리 처럼 받은 메시지를 다시 클라이언트 화면으로 전송!
					client.getBasicRemote().sendText(message);

				} catch (Exception e) {
					//이미 끊어진 세션 : 한 명 때문에 전체 전송이 멈추면 안 된다
					System.out.println("[ChatServer] 전송 실패로 목록에서 제거 : " + client.getId());
					broken.add(client);   // 지금 지우면 순회가 깨지므로, 일단 "끊어진 목록" 에 담아 둔다
				}
			}

			//순회가 끝난 뒤에 제거한다 (순회 중 제거는 ConcurrentModificationException)
			clients.removeAll(broken);
		}
	}//-------------
	
	//클라이언트와의 연결이 종료되었을때 자동으로 호출되는 메서드 정의 
	@OnClose
	public void onClose(Session session) {
		
		//클라이언트의 연결이 종료 된  클라이언트의 Session영역을  HashSet에서 제거
		clients.remove(session);
		//클라이언트 접속 종료 출력
		System.out.println("웹 소켓 종료  : " + session.getId());
	
	}//-----------
	
	//클라이언트가의 연결 에러가 발생 했을때.. 자동으로 호출되는 메서드 정의
	/*
	  [3단계 작업 - 에러 난 세션을 목록에서 제거하도록 고친 부분]

	    [Before] public void OnError(Throwable e)
	      에러 내용만 출력하고 끝냈다. 어느 접속에서 난 에러인지 알 수 없고
	      끊어진 세션이 clients 에 그대로 남았다.
	      남은 세션은 다음 메시지 전송 때마다 실패하며 쓰레기처럼 쌓인다.

	    [After] 매개변수에 Session 을 추가해 문제가 난 접속을 목록에서 제거한다.
	      @OnError 는 (Session, Throwable) 형태로도 선언할 수 있다.
	 */
	@OnError
	public void OnError(Session session, Throwable e) {

		if (session != null) {   // 어떤 접속에서 오류가 났는지 알 수 있으면
			clients.remove(session);   // 그 접속을 목록에서 빼낸다 (안 빼면 다음 전송마다 계속 실패한다)
			System.out.println("연결 에러로 목록에서 제거 : " + session.getId());   // 누구를 제거했는지 콘솔에 남긴다
		} else {
			System.out.println("연결 에러");   // 누구인지 알 수 없는 오류는 사실만 남긴다
		}

		e.printStackTrace();//에러 메세지 출력
	}
	
}








