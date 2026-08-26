package sec04.ex01;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;


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

@WebListener
/* 위 설명대로HttpSessionBindingListener 에는 이 애노테이션이 필요 없다.
붙어 있어도 오류는 나지 않지만, 다른 리스너용 등록 표식이므로
학습 혼란을 막으려면 지우는 것이 바람직하다. (지워도 동작 동일) */
public class LoginImpl implements HttpSessionBindingListener {
	/* implements HttpSessionBindingListener
	   : 이 클래스의 객체가 세션에 담길 때 이벤트를 받겠다는 자격 선언.
	     아래의 valueBound / valueUnbound 오버라이딩이 강제된다. */
	
	String user_id; /*로그인 요청시 입력받은 아이디 저장할 변수 */
	String user_pw; /*로그인 요청시 입력받은 비밀번호 저장할 변수 */
	
	static int total_user = 0;
	/* 현재 접속자 수를 저장하는 static(정적) 변수.
	   static 이므로 객체마다 따로 생기지 않고 클래스에 딱 1개만 존재한다.
	   -> hong의 객체가 +1, kim의 객체가 +1 해도 "같은 변수"가 늘어나므로
	      모든 사용자의 합계(총 접속자 수)를 셀 수 있다. */
	
	public LoginImpl() {} /*기본 생성자*/
	
	public LoginImpl(String user_id, String user_pw) { /*로그인 요청하여 접속하는 접속자의 아이디, 비밀번호 초기화 생성자*/
		this.user_id = user_id;
		this.user_pw = user_pw;
	}

	/*==================================================================
	  valueBound() : "이  LoginImpl 클래스의 객체"가 HttpSession메모리(세션)에 저장(바인딩)되는 순간 자동 호출

	  호출되는 시점 (LogionTestServlet.java 서블릿 코드 기준)
	    session.setAttribute("loginUser", loginUser);  <- 바로 이 줄 실행 순간
	     
	  매개변수 event : 어느 세션에 어떤 이름으로 담겼는지의 정보.
	                 event.getSession(), event.getName() 으로 확인 가능.
	==================================================================*/
	@Override
	public void valueBound(HttpSessionBindingEvent event) {
		
		System.out.println("사용자 접속");	
		++LoginImpl.total_user; /*접속자 수 1증가 static 변수이므로 클래스명.클래스변수명으로 접근했다.*/
	}

	/*========================================================================================
	  valueUnbound() : "이 LoginImpl 객체"가  HttpSession메모리(세션)에서 제거(언바인딩)되는 순간 자동 호출

	  호출되는 시점 3가지
	    1. session.invalidate()            : 로그아웃 처리로 HttpSession(세션)을  TomCat에서 삭제
	    2. 세션 유효시간 초과               	   : 기본 30분간 요청이 없을 때
	    3. session.removeAttribute("loginUser") : 이 LoginImpl 객체만 골라서  HttpSession(세션) 메모리에서 제거
	  ** 브라우저를 그냥 닫는 것은 서버가 모르므로,
	     2번(유효시간 초과)이 될 때까지 감소하지 않는다. **
	==================================================================*/
	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		
		System.out.println("사용자 접속 해제");
		
		total_user--;  /*접속자 수 1 감소.  위와 달이 클래스명 없이 접근한 형태 - 같은 static 변수다.*/
		
	}
	
	
}











 