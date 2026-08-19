package sec01.ex01;
/*
[전체 웹 흐름 텍스트 모델링]

  [Client 웹브라우저]                [Tomcat 서버 메모리]
  +------------------+              +---------------------------------+
  | 주소창 입력         |  (1) 요청    | (2) /first 매핑 확인              |
  | localhost:8181   | -----------> |     web.xml 에서 실제 클래스 찾음   |
  | /pro05/first     |              |         |                       |
  |                  |              |         v                       |
  |                  |              | (3) 객체 있나? 없으면 생성          |
  |                  |              |     new FirstServlet()          |
  |                  |              |     -> init() 최초 1회 호출        |
  |                  |              |         |                       |
  |                  |              |         v                       |
  |                  |              | (4) service() -> doGet() 호출    |
  |                  |              |     req, resp 객체를 만들어 전달    |
  |                  |  (5) 응답     |         |                       |
  | (6) HTML 화면 출력 | <----------- |     HTML 응답 데이터 생성          |
  +------------------+              +---------------------------------+

  (7) 재요청 시  : init() 생략, doGet() 만 다시 실행 (객체 재사용)
  (8) 서버 종료 시: destroy() 1회 호출 후 객체 제거

[요청 1회당 메모리 변화]
  요청 도착 -> [Tomcat] req 객체 생성, resp 객체 생성
           -> doGet(req, resp) 실행
           -> resp 에 담긴 HTML 을 브라우저로 전송
           -> 응답 끝나면 req, resp 는 제거됨 (서블릿 객체는 유지)
*/

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//1. 실제 웹 프로그래밍에서 사용되는 사용자정의 서블릿 FirstServlet 는
//   ServletApi.jar 라이브러리에서 제공되는 HttpServlet 클래스를 상속받아 만듭니다.

//2. HttpServlet 클래스에서 제공하는 서블릿 생명주기 관련 메소드들 ( init(), service(), doGet(), destory() )
//
//1.  init()  : FirstServlet 서블릿클래스가 최조로 톰캣 서버 메모리에 로딩 될때  딱 한번만 호출되는 메소드로
//			     멤버변수가 선언되어 있으면 값을 처음 초기화 할 코드를 작성 해 놓을 메소드 입니다.
//
//2.  service() : 클라이언트의 요청을 처리하는 메소드( doGet(), doPost() )를 자동으로 호출하는 메소드.
//
//3.  doGet() :  클라이언트가 GET 요청 방식으로 FirstServlet서블릿 페이지로 요청을 할때 호출되는 콜백메소드로,
//				이 메소드 내부에서는 요청주소에 관한 응답할 데이터를 생성하여 클라이언트의 웹브라우저로 응답할 코드를 작성하는 메소드.
//
//4.  destory() : FirstServlet 서블릿이 톰캣 서버 메모리 영역에서 소멸될때 단 한번만 호출 되는 메소드로,
//				 사용하지 않은 자원 메모리를 제거할코드를 작성하는 메소드 입니다.

public class FirstServlet extends HttpServlet {
	
	//HttpServlet 부모 클래스에 만들어져 있는 서블릿의 생명주기 관련 메소드 오버라이딩 해서 작성!
	//alt + shift  +   s      v

/*		
	init() 메소드는 FirstServlet클래스의 객체(인스턴스)가 처음 생성되면서 톰캣 서버 메모리에 올라가는 시점에
	단 한번만 톰캣에 의해 호출되는 메소드로,  변수의 초기화 작업을 수행하는 코드를 작성해 놓습니다.
	예를 들어, 데이터베이스 서버의 DBMS프로그램과 연결하는 코드를 설정하거나, 설정 파일을 톰캣 서버메모리에 로드하는 동작 등 작업을 수행할수 있음 
*/	
	@Override
	public void init() throws ServletException {
		System.out.println("init() 메소드 호출 - FirstServlet 클래스의 객체 메모리 톰캣서버 메모리 영역에 올림");
	}
/*
 	service() 메소드는 init()메소드의 실행코드가 모두 실행 된 후에  두번째로 호출되는 콜백메소드로,
 	클라이언트의 요청 방식에 따라 doGet() 메소드 또는 doPost() 메소드를 자동으로 호출하는 메소드 입니다.
 	
 	참고.    클라이언트의 요청 방식 중 GET 요청 - 웹브라우저 주소창에 요청주소를 입력해서 엔터를 눌러 FirstServlet을 요청하는 방법
 		    클라이언트의 요청 방식 중 POST 요청 - HTML의 <form method="post"></form> 태그로 FirstServlet을 요청하는 방법

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
		System.out.println("service() 메소드 호출 당함");
	}
*/ 
/*	
	doGet() 메소드는 클라이언트가 Get 요청 방식으로 FirstServlet 서블릿 페이지로 요청을 하면 호출되는 콜백메소드.
	이 메소드 내부에서는 요청주소에 관한 응답할 데이터를 생성하여  클라이언트의 웹브라우저로 반환(응답)할수 있습니다.
	
	req 매개변수  : 클라이언트가 브라우저 주소창에 http://톰캣서버PC의IP:톰캣서버의PORT/컨텍스트/요청할서블릿  <-입력해서 요청하면
				 웹브라우저 프로그램은  http 규칙에 맞게 요청주소에 관한 http 요청메세지를 만들어서  톰캣서버에게 전달합니다.
				 톰캣서버는 http 요청 메세지의 정보를 얻어  HttpServletRequest 객체 메모리를 생성하여 저장 시킵니다.
				 그리고 난 후 doGet메소드의 HttpServletRequest req 매개변수로 생성한 HttpServletRequest객체 주소를 전달해줍니다.
				 
	resp 매개변수 : 톰캣서버가 생성해주는 HttpServletResponse 객체 메모리를 resp 매개변수로 전달 받는데
				  이 객체 메모리에는 클라이언트의 웹브라우저로 응답할 메세지를 설정 하고  이 객체의 메소드를 통해 브라우저로 보내어서 응답합니다.
*/		
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		System.out.println("doGet() 메소드 호출 당함 - GET 요청처리");
		
		System.out.println("요청처리 후  destory() 메소드가 호출되어 서블릿은 종료 될 것임");
		
		//클라이언트의 웹브라우저로 응답할 데이터 종류(MIME-TYPE)을 text기반의 HTML문서의 태그형태로 내보내고
		//한글 깨짐을 방지 하기 위한 응답할 문자 처리 방식을 UTF-8 로  HttpServletResponse객체에 설정
		resp.setContentType("text/html; charset=UTF-8");
		
		//웹브라우저 창과 연결된 출력스트림 통로(PrintWriter객체)를 통해 
		//HTML태그 형태의 응답할 데이터를 만든후 브라우저로 내보낸다.(출력한다.)
		PrintWriter out = resp.getWriter();
					out.println("<html>");
						out.println("<body>");
							out.println("<h1>FirstServlet.class 서블릿이 응답하는 데이터</h1>");
						out.println("</body>");
					out.println("</html>");
	}

/*
	FirstServlet 서블릿 클래스의 객체 메모리가 톰캣 서버 메모리에서 올라가 있다가 제거될때 호출되는 콜백메소드 입니다.
	예를 들어, 데이터베이스연결후 모든 작업이끝나면  데이터베이스 연결 객체 메모리를 닫거나, 파일을 정리하는 등의 작업을 수행할수 있습니다.
*/		
	@Override
	public void destroy() {
		System.out.println("destory() 메소드 호출 -  서블릿 종료");
	}

} //----------------------> class FirstServlet 













