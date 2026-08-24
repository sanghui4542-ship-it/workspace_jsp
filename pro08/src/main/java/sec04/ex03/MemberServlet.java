package sec04.ex03;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/*
   MemberServlet 첫번쨰 서블릿 클래스 역할
    - 모든회원 조회 요청 받아  조회한 회원정보를 List 배열에 저장한 후 
      ViewServlet 두번째 서블릿 클래스로 공유하기 위해  다시 ~~~~~~ 
      HttpServletRequest 객체 메모리에 바인딩 하여  두번째 서블릿을 포워딩(재요청)해서 공유합니다.  
*/

@WebServlet("/member4")
public class MemberServlet extends HttpServlet{

	
	//Get 요청 방식으로 요청 들어오면 응답하는 콜백메소드 
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doHandle(request, response);
	}
	//Post 요청 방식으로 요청 들어오면 응답하는 콜백메소드 
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doHandle(request, response);
	}
	
	//Get 또는 Post 요청 방식으로 요청 들어오면 모든 응답을 처리하는 일반 메소드 
	protected void doHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// 요청 데이터의 한글 깨짐 방지를 위해 HttpServletRequest 객체의 문자 처리 방식을 UTF-8로 설정
		request.setCharacterEncoding("UTF-8");
		
		//t_member 테이블에 조회 작업을 맡기기 위해 MemberDAO 객체 생성
		MemberDAO dao = new MemberDAO();  //<- DB 연결 작업 + DB작업 하는 사원
		
		
		// listMembers() 호출 -> 조회된 전체 회원(ArrayList배열)을 반환받음
		List  list = dao.listMembers();
		
		//조회된 모든 회원정보들(MemberVO객체들)이 저장된 ArrayList배열 자체를 HttpServletRequest객체 메모리 안에! 바인딩 (저장)
		request.setAttribute("membersList", list);
		
		//RequestDispatcher객체의 forward메소드 호출 방법으로  
		//두번째 서블릿 ViewServlet으로  포워딩(재요청)시~~
		//ArrayList배열이 값 형태로 저장된 HttpServletRequest객체 메모리 전달해서 공유!
		RequestDispatcher dispatcher = request.getRequestDispatcher("viewMembers");			
		dispatcher.forward(request, response); //실제 포워딩시 HttpServletRequest객체 메모리와 HttpServletResponse 객체 메모리 공유!
			
	}
	

	

}











