package sec01.ex01;

import java.io.IOException;
import java.net.Authenticator.RequestorType;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//사장

@WebServlet("/member")
public class MemberServlet extends HttpServlet {

	// doGet 메소드 오버라이딩 (alt + shift + s  v) : GET 방식 요청이 오면 톰캣이 자동 호출하는 메소드
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//1. 요청 데이터의 한글 깨짐 방지를 위해 HttpServletRequest 객체의 문자 처리 방식을 UTF-8로 설정
		request.setCharacterEncoding("UTF-8");
		
		//2. 요청한 데이터 얻기
		//-> 이번 요청은 조회 주소( http://localhost:8181/pro07/member ) 만 있고 전송된 데이터가 없으므로 얻는 코드 생략.
		
		//3. t_member 테이블에 조회 작업을 맡기기 위해 MemberDAO 객체 생성
		MemberDAO dao = new MemberDAO(); //<- DB 연결 작업 + DB작업 하는 사원
		
		
		
		
	}

	
	
}
