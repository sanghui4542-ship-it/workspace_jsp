package sec04.ex02;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


/*
[이 LoginImpl 클래스의 역할 2가지]
1. 로그인 정보 보관 : 입력받은 아이디와 비밀번호를 변수에 저장한다.
2. 이벤트 처리      : 이 객체가 세션에 저장되거나 제거되는 순간을
                      스스로 감지해서 접속자 수를 증감시킨다.

[HttpSessionBindingListener 란?]
- "세션에 담기는 LoginImpl 객체 자신"이 구현하는 리스너 부모 인터페이스다.
- 추상메소드 2개를 반드시 오버라이딩해야 한다.
    valueBound()   : 이 LoginImpl 객체가 session.setAttribute() 로
                     세션에 저장(바인딩)되는 순간 자동 호출
    valueUnbound() : 이 LoginImpl 객체가 세션에서 제거(언바인딩)되는 순간 자동 호출
                     (invalidate, removeAttribute, 유효시간 초과)
- 두 메소드를 부르는 코드는 어디에도 없다.
  저장/제거라는 "사건"이 일어나면 톰캣이 대신 호출해 준다.

[다른 리스너와의 등록 방식 차이 - 중요]
- HttpSessionListener 등 대부분의 리스너 : @WebListener 등록 필수
- HttpSessionBindingListener (이 클래스) : 등록 불필요!
  세션에 담기는 객체 자신이 리스너라서, 톰캣이 setAttribute 순간
  "이 객체가 BindingListener구나"를 알아채고 알아서 호출한다.
*/

//웹 애플리케이션(pro10 프로젝트)에서 사용자의 로그인 상태를 관리하는 클래스 입니다.
//HttpSessionListener인터페이스를 구현하여 HttpSession객체가 생성되거나 소멸될때 동작합니다.

@WebListener
public class LoginImpl implements HttpSessionListener{

	String user_id; /*로그인 요청시 입력받은 아이디 저장할 변수 */
	String user_pw; /*로그인 요청시 입력받은 비밀번호 저장할 변수 */	
	static int total_user = 0;  /*모든 사용자의 합계(총 접속자 수)*/

	public LoginImpl() {} /*기본 생성자*/
	
	public LoginImpl(String user_id, String user_pw) { /*로그인 요청하여 접속하는 접속자의 아이디, 비밀번호 초기화 생성자*/
		this.user_id = user_id;
		this.user_pw = user_pw;
	}

	// 사용자가 로그인 요청 하여 LoginTestServlet2 서블릿 내부에서  HttpSession객체 메모리가 생성되어 TOMCAT에 올라갈떄 실행되는 콜백 메소드입니다.
	@Override
	public void sessionCreated(HttpSessionEvent se) {
		System.out.println("HttpSession 객체 메모리 새로 생성"); //새로운 세션 영역이 생성되었음을 테스트 할 용도로 출력
		++total_user;  //로그인 요청하여 접속한 접속자 수 1 증가 
	}

	// 사용자가 로그아웃 링크를 클릭해서  HttpSession객체 메모리가 TOMCAT에서 만료되어 소멸될때 실행 되는 콜백메소드입니다.
	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		System.out.println("HttpSession객체 메모리 소멸");
		total_user--; //로그인 한 접속자 수 1 감소 
	}

}











 