package sec03.ex01;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
/*
세션 메모리 ?
- 클라이언트가 톰캣서버의 서버페이지와 연결을 유지하는 데이터를 저장하는 HttpSession 객체 메모리 
- HttpSession 객체 메모리는 톰캣서버 측에서 관리 함
- HttpSession 객체 메모리는 웹브라우저창 닫으면 삭제되고, 
  HttpSession 객체 메모리 유지시간을 넘기면 삭제되고, 개발자가 코드 작성해서  로그아웃처리시  삭제됨

세션흐름
클라이언트 -> 요청 -> [톰캣서버] 세션메모리 생성(JSSESSIONID) -> 응답 (JSSESSIONID 웹브라우저의 쿠키저장소 저장)
-> 클라이언트 -> 요청 ->  [톰캣서버] JSESSIONID와 클라이언트가 전달한 JSESSIONID가 같은지 확인해서 클라이언트임을 확인하고 로그인처리함

세션동작흐름
1. 클라이언트가 톰캣서버에 요청을 보냄
2. 톰캣서버는  클라이언트에 대한 HttpSession 객체 메모리를 생성하고, 고유한 JSSESSIONID값을 생성함
3. 톰캣서버는 JSSESSIONID값을 쿠키에 담아 클라이언트에 전송함
4. 클라이언트가 이후 요청을 보낼때, JSESSIONID를 포함하여 톰캣서버로 요청을 보냅니다.
5. 톰캣서버는 JSSESSIONID를 확인하여 해당 클라이언트의 HttpSession 객체의 정보를 가져옴
6. 클라이언트가 로그아웃 하거나 HttpSession 객체 메모리 유지시간을 넘기면 HttpSession 객체 메모리가 삭제됨
 
 
클라이언트가 웹브라우저 주소창에 http://localhost:8181/pro09/sess주소를 입력하여
톰캣 서버가 실행해서 읽어들이는 SessionTest라는 이름의 서블릿페이지를 요청합니다.

요청받으면 서블릿 서버페이지는 새로운 HttpServletRequest객체 메모리의 힘을 빌려서
HttpSession객체 메모리를 하나 만듭니다.
예) request.getSession(); -> HttpSession객체 메모리 반환함 

만들어진 HttpSession객체 메모리의 정보를 웹브라우저로 응답합니다.
*/


@WebServlet("/sess")
public class SessionTest extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//재료
		//1. SessionTest 서블릿이 요청 받은 데이터 중 한글 문자 인코딩 방식 UTF-8 값으로 설정
		request.setCharacterEncoding("UTF-8");
		
		//2. 요청한 클라이언트의 브라우저에 응답할 데이터 유형을 text 기반의 HTML파일에 작성하는 데이터로 설정하고,
		//   응답 할 데이터에 한글 문자가 깨져서 브라우저에 보이는거 방지 하기 위해 응답할 문자 인코딩 방식 UTF-8 값으로 설정
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		//2.1. 요청한 클라이언트의 브라우저 창과 연결된 데이터를 2바이트 단위로 내보낼 출력스트림 PrintWriter생성
		PrintWriter  out = response.getWriter();
		
		//실제 작업
		//1.   세션(HttpSession객체) 메모리 영역 새로 얻어 저장
		//방법 : HttpServletRequest객체의 getSession() 메소드를 호출하면 HttpSession 객체 저장소 메모리를 만들어 반환해준다
		HttpSession  session = request.getSession();
		
		//2.  세션(HttpSession객체) 메모리를 식별 할수 있는 JSSESSIONID 얻기
		//방법 : HttpSession 객체의 getId() 메소드를 호출하면 JSSESSIONID 를 얻을수 있다.
		out.print("클라이언트의 새요청에 의해 새로 생성되어 얻은 HttpSession객체 메모리의 아이디 : " +  session.getId() + "<br>");
		
		//3. 클라이언트가 브라우저로 처음 최초로 요청 했을때 생성되어 얻은 HttpSession객체 메모리를 얻은 시각  출력
		//방법 : HttpSession객체의 getCreationTime()메소드를 호출하면 얻을수 있다. 
		out.println("최초 HttpSession객체 메모리 생성 시각 : " +  new Date(session.getCreationTime()) + "<br>");
		
		//4. 클라이언트가 브라우저로 처음 최초로 요청 했을때 생성된 HttpSession객체 메모리 접근 해서 사용한 시각 얻어 출력
		//방법 : HttpSession객체의 getLastAccessedTime()메소드를 호출하면 얻을수 있다.
		out.println("최근 HttpSession객체 메모리에 접근한 시각 : " +  new Date(session.getLastAccessedTime()) + "<br>");
		
		//5. 생성되어 얻은 HttpSession 객체 메모리가 톰캣서버 메모리에 올라가 있는 유효시간 얻기
		out.println("HttpSession 객체 메모리가 톰캣서버 메모리에 올라가 유지 되는 시간 : " + session.getMaxInactiveInterval() + "<br>");
		
		//6. 클라이언트가 브라우저로 처음 서블릿을 요청했을때 생성되어 얻어진 HttpSession객체 메모리 이냐? 물어보는 isNew()메소드를 사용하여
		//   처음 생성되어 얻어진 HttpSession객체 메모리인지 판별
		//isNew메소드는 true 또는 false를 반환 합니다.
		if(session.isNew()) {
			out.println("처음 생성되어 얻어진 HttpSession객체 메모리 이다~<br>");
		}
		

	}

}









