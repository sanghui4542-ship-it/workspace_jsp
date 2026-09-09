package websocket;



//1. 입출력 예외(IOException)를 사용하기 위한 import
import java.io.IOException;

//2. Set을 동기화 처리하는 유틸 클래스(Collections.synchronizedSet)  import
import java.util.Collections;

//3. 실제 저장소로 사용할 HashSet import
import java.util.HashSet;

//4. 중복으로 저장할을 허용하지 않는 컬렉션 인터페이스 Set  import
import java.util.Set;

//5. 클라이언트 연결이 끊길 때 호출되는 메소드를 표시하는 어노테이션
import javax.websocket.OnClose;

//6. 통신 중 예외가 발생했을 때 호출되는 메소드를 표시하는 어노테이션
import javax.websocket.OnError;

//7. 클라이언트가 보낸 메세지를 받을때 호출되는 메소드를 표시하는 어노테이션
import javax.websocket.OnMessage;

//8. 클라이언트가 접속했을때 호출될 메소드를 표시하는 어노테이션
import javax.websocket.OnOpen;

//9. 클라이언트 한명의  연결정보를 담고 있는 객체
import javax.websocket.Session;

//10. 이 파일의 이클래스를 웹소켓 서버페이지로 등록하는 어노테이션 
import javax.websocket.server.ServerEndpoint;

/* 참고1. import 경로가 javax인지 jakarta인지 확인해야 합니다.
- Tomcat 9 이하  : javax.websocket.*  (JDK 8~11, 이 코드 기준)
- Tomcat 10 이상 : jakarta.websocket.*
경로가 다르면 컴파일 에러가 발생합니다.
The import javax.websocket cannot be resolved

또한 Dynamic Web Project로 만든 경우
Tomcat 라이브러리(websocket-api.jar)가 빌드패스에 잡혀 있어야 합니다.
*/

/* 참고2. HTTP와 WebSocket의 차이 (사실 설명)
- HTTP  : 요청 1회 -> 응답 1회 -> 연결 종료. 서버가 먼저 말을 걸 수 없습니다.
- WebSocket : 최초 1회 연결 후 계속 연결 유지. 서버가 먼저 데이터를 보낼 수 있습니다.
그래서 채팅, 실시간 알림, 주식 시세 등에 사용합니다.
*/

//11. @ServerEndpoint : 이 클래스를 웹소켓 서버로 등록하고 요청명을 지정합니다.
//12. 괄호 안의 "/ChatingServer"가 클라이언트가 접속할 주소가 됩니다.
//13. 전체 접속 URL 형식 -> ws://호스트:포트번호/컨텍스트루트/ChatingServer
//14. 예) 				 ws://localhost:8181/WebSocket/ChatingServer
//15. 주의 : http:// 가 아니라 ws:// 프로토콜을 사용합니다.
@ServerEndpoint("/ChatingServer")
public class ChatServer {

	//16. 현재 접속 중인 모든 클라이언트의 Session 객체를 저장하는 저장소
	//17. static : 클라이언트가 몇 명 접속하든 저장소는 딱 1개만 존재해야 합니다.
	//18. Set : 같은 Session이 중복 저장되지 않도록 Set을 사용합니다.
	//19. synchronizedSet : 여러 스레드가 동시에 접근해도 깨지지 않도록 동기화 처리
	private static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());
	
	/* 참고3. 왜 static 이어야 하는가
	   웹소켓 서버는 클라이언트가 접속할 때마다 ChatServer 객체를 새로 생성합니다.
	   따라서 인스턴스 변수로 만들면 접속자마다 각자 다른 Set을 갖게 되어
	   서로의 존재를 알 수 없습니다. static으로 선언해야 모든 접속자가
	   하나의 Set을 공유할 수 있습니다.
	*/
	
	//20. @OnOpen : 클라이언트가 접속에 성공하는 순간 톰캣이 자동 호출합니다.
	//21. 매개변수 Session session : 방금 접속한 클라이언트 1명의 연결 정보
	@OnOpen
	public void onOpen(Session session) {
		
		//22. 접속한 클라이언트의 Session 객체 주소를 위 Set에 추가합니다.
		//23. 여기에 추가되어야 나중에 메세지를 보내줄 수 있습니다.
		clients.add(session);
		
		//24. 세션ID는 톰캣이 접속 순서대로 부여하는 문자열입니다.(예 : 0, 1,  2)
		//25. 이클립스의 console 에 출력하여 누가 접속했는지 세션ID로 확인 가능
		System.out.println("웹 소켓 연결 : " + session.getId());	
	}
	
	
	//26. @OnMessage : 클라이언트가 메세지를 보내오면 톰캣이 자동 호출합니다.
	//27. String message : 클라이언트가 보낸 문자열 내용
	//28. Session session : 그 메세지를 보낸 클라이언트의 연결정보가 저장된 Session객체
	//29. throws IOException : 메세지 전송중 연결이 끊기면 예외가 발생하므로 선언합니다.
	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		
		//30. 누가 무슨 메세지를 보냈는지  서버측페이지(ChatServer.java)로 이클립스에 출력
		System.out.println("메세지 전송 : " + session.getId() + " : " + message);

		//31. synchronized : 반복이 도는 동안 다른스레드(다른 클라이언트 채팅창)가 Set배열의 정보를 수정하지 못하게 잠급니다.
		//32. 잠그지 않으면 반복 도중  접속/종료 가 일어나 예외가 발생할 수 있습니다.
		//33. 발생 가능한 예외 -> java.util.ConcurrentModificationException
		synchronized (clients) {
			
			//34. Set 배열에 저장된 클라이언트(Session 객체)를 하나씩 꺼내 반복합니다.
			//35. client 변수에 접속자 1명의 정보가 있는 Session 객체가 차례대로 담깁니다.
			for(Session client   :  clients) {
				
				//36. 메세지를 보낸 본인(Session)과 같은 대상인지 비교합니다.
				//37. 본인이 아닐때(!)만 전송하여 자기 메세지가 두번 보이는 것을 막습니다.
				if(!client.equals(session)) {
					
					//38. getBasicRemote() : 동기 방식 전송 객체  Basic를 얻습니다.
					//39. sendText() : 상대 브라우저로 문자열을 채팅 메세지로 보냅니다.
					client.getBasicRemote().sendText(message);
					
					/* 참고5. getBasicRemote()와 getAsyncRemote()
					   - getBasicRemote() : Basic 객체는 전송이 끝날 때까지 기다립니다(동기). 순서가 보장됩니다.
					   - getAsyncRemote() : 전송을 맡기고 바로 다음 줄로 넘어갑니다(비동기).
					   접속자가 많을수록 getAsyncRemote()가 유리하지만 순서는 보장되지 않습니다.
					*/
				} //if
				
			}//for
			
		}//synchronized 블럭
		
	}// onMessage 메소드 
	
	
	//40. @OnClose :  채팅창을 닫거나  연결이 끊기면 톰캣이 자동으로 호출합니다.
	@OnClose
	public void onClose(Session session) {
		
		//41. ChatServer.java와 연결이 끊긴 클라이언트의 접속정보인? Session객체를 Set에서 제거합니다.
		//42. 제거 하기 않으면 연결이 끊긴 대상에게 메세지 전송을 시도하면 예외가 발생합니다.
		clients.remove(session);
		
		//43. 종료 로그를 이클립스의 console창에 출력합니다.
		System.out.println("웹소켓 종료 : " + session.getId());		
	}
	
	//44. @OnError : 통신 중 예외가 발생하면 톰캣이 자동 호출합니다.
	//45. 매개변수 Throwable  e  :  발생한 예외 메세지 정보가 담긴 객체가 전달됩니다.
	@OnError
	public  void OnError(Throwable e) {
		
		//46. 에러가 났다는 사실을 이클립스의 console창에 출력
		System.out.println("에러 발생");
		
		//47. 어느 코드 줄에서 에러가 났는지 추적 정보를 이클립스의 console창에 출력
		e.printStackTrace();
	}
	
	/* 참고6. 메서드 호출 순서(생명주기)
	   접속 -> @OnOpen -> @OnMessage(여러 번 반복) -> @OnClose
	   예외 발생 시에는 어느 시점에서든 @OnError가 호출됩니다.
	*/

	/* 예상 출력 (A, B 두 명이 접속 후 A가 "안녕"을 입력한 경우)
	   웹 소켓 연결:0
	   웹 소켓 연결:1
	   메세지 전송 : 0 : 안녕
	   웹소켓 종료 : 0
	   ※ 화면에는 B의 채팅창에만 "안녕"이 표시됩니다.
	*/

	/* 핵심정리
	   1. @ServerEndpoint("/주소")로 웹소켓 서버를 등록하고 ws:// 로 접속합니다.
	   2. 접속자 Session을 static Set에 모아두어야 전체에게 메세지를 뿌릴 수 있습니다.
	   3. @OnOpen -> @OnMessage -> @OnClose 순서로 톰캣이 자동 호출합니다.
	*/
	
}









