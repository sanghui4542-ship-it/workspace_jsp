package sec01.ex1;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
  [어노테이션 @ 이란?]
  - 클래스나 메소드 위에 붙이는 추가 정보 표시이다. (메타데이터 라고 부른다)
  - 컴파일러나 톰캣 같은 프로그램이 이 표시를 읽고 특정 동작을 수행한다.
  - @WebServlet 은 " 이 클래스를 서블릿 객체로 톰캣에 등록하라"는 표시 이다.
 */

//이 한줄이 web.xml의 <servlet> + <servlet-mapping> 두 태그를 대신한다.
//브라우저에서 http://localhost:8181/pro05/second 로 요청하면 톰캣이 이클래스의 객체를 실행한다.
@WebServlet("/second")
public class SecondServlet extends HttpServlet {
	

	//호출 순위 1 : SecondServlet 객체가 톰캣 메모리에 처음 올라갈 때 딱 1회만 호출되는 메소드로
	//			 DB 연결, 변수 설정 값 로딩 같은 준비 작업을 여기에 작성한다.
	@Override
	public void init(ServletConfig config) throws ServletException {
		System.out.println("init 메소드 호출>>>>");
	}
	//호출 순위 2 : 클라이언트의 요청 방식에 따라 doGet 또는 doPost 메소드 중 하나를 실행하는 역할을 하는 메소드
	//public void service(....){}

	//호출 순위 3 : 클라이언트가 GET 요청 할 때마다 매번 호출되는 메소드로, service()메소드가 요청 방식을 보고 자동으로 호출해 준다. 
	//request 매개변수 -> 브라우저의 요청 정보를 담는 객체 (톰캣이 만들어 전달 해줌)
	//respose 매개변수 -> 브라우저로 보낼 응답을 담는 객체 (톰캣이 만들어 전달 해줌)
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	//호출 순위 마지막 : 톰캣 서버 종료 등으로 secondServlet 객체(스레드)가 톰캣에서 제거될 때 딱 1회 호출된다.
	//				DB 연결 객체 닫기 같은 정리 작업을 여기에 작성한다.
	@Override
	public void destroy() {
		System.out.println("destory 메소드 호출>>>>");
	}
	
}
