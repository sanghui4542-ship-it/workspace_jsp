package sec01.ex01;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
/*

주제 :  각 서블릿 관련 객체 메모리 들에  바인딩 된  속성(변수,key)의 스코프(접근범위)에 대해 알아보자.

ServletContext 서블릿 관련 객체 메모리 영역 접근범위
- 전체 웹 애플리케이션(pro10) 내부에 만들어 놓은  모든 서블릿 페이지에서 값을 공유하고 자 할때
     값을 속성(key)과 함꼐 묶어서 바인딩 해 놓고 공유하는 객체 메모리.

ServletContext 서블릿 관련 객체 메모리 소멸 시점
- 톰캣 서버가 종료 될때 소멸
- 또는 해당 웹 애플리케이션이 톰캣에서 내려갈때 소멸
- 즉, 톰캣서버가 살아있는 동안 계속 유지됨 

 예) 전체 방문자 수, 공통 설정값, 전역 데이터 등 
---------------------------------------------------------------

HttpSession 서블릿 관련 객체 메모리 영역 접근범위
- 하나의 웹브라우저 창이 닫히기 전까지는 모든 서블릿 페이지에서 값을 공유 하고자 할때 
     값을 속성(key)과 함께 묶어서 바인딩 해 놓고 공유하는 객체 메모리.
     
HttpSession 서블릿 관련 객체 메모리 소멸 시점
- 웹브라우저 창을 닫았을때 소멸
- 설정된 세션 유효시간이 초과(기본 30분)되었을때 소멸
- session.invalidate() 메소드 호출시 소멸      
     
 예) 로그인 정보, 장바구니 , 사용자 상태 정보 
---------------------------------------------------------------

HttpServletRequest 서블릿 관련 객체 메모리 영역 접근범위
- 클라이언트가 웹브라우저를 이용해 서블릿 페이지를 요청하면 톰캣서버에 의해 생성되는 객체 메모리로....
  클라이언트의 요청 값을 속성(key)과 함께 묶어서 바인딩 해 놓고 공유하는 객체 메모리.
     
HttpServletRequest 서블릿 관련 객체 소멸 시점 
- 톰캣 서버가  클라이언트의 요청에 대한 응답 을 완료 하는 순간 소멸
- forward  / include 가 끝나고 나면 즉시 소멸     
     
예) 폼 입력값,  일회성 처리 결과,  에러 메세지 
*/
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//GET 요청 주소  :  http://localhost:8181/pro10/set

@WebServlet("/set")
public class SetAttribute extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//재료
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		
		//실제 작업 : 각각의 서블릿 관련 객체 메모리들에 특정 문자열 값을 속성과 함께 바인딩
		String ctxMesg = "ServletContext 객체 메모리에 바인딩할 특정 문자열 값";
		String sesMesg = "HttpSession 객체 메모리에 바인딩할 특정 문자열 값";
		String reqMesg = "HttpServletRequest 객체 메모리에 바인딩할 특정 문자열 값";
		
		//1. ServletContext 객체 메모리 (웹 프로젝트 pro10 하나당 생성되는 하나의 메모리) 얻기 
		ServletContext servletContext = this.getServletContext();
		
		//2. HttpSession 객체 메모리 (요청한 클라이언트 브라우저창 하나당 생성되는 하나의 메모리) 얻기
		HttpSession  httpSession = request.getSession();
		
		//3. HttpServletRequest 객체 메모리(클라이언트가 서블릿을 톰캣에 요청하는 순간 톰캣이 생성주는 하나의 메모리) 얻기
		//얻는방법 : doGet 메소드의 매개변수 request 로 전달 받습니다.
		
		//각각의 서블릿관련 객체 메모리 영역들에 ~~~~ 바인딩(key 와 value 를 한쌍의 형태로 묶어서 저장) 하는 방법
		//====> setAttribute("key", "value");
		
		//1.1. ServletContext에 바인딩
		servletContext.setAttribute("context", ctxMesg);
		
		//2.1. HttpSession에 바인딩
		httpSession.setAttribute("session", sesMesg);
		
		//3.1. HttpServletRequest에 바인딩
		request.setAttribute("request", reqMesg);
		
		out.print("각각의 서블릿 관련 객체 메모리 영역들에 바인딩 했습니다.");
		
	}
	
	

}























