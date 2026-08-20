package sec01.ex01;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/*
InputServlet 클래스 역할
- input.html 클라이언트가 보는 화면에서  이름, 비밀번호 입력하고 과목명들을 체크하여
  <input type="submit" value="전송요청"> 을 눌렀을때
  <form action="/pro06/input"> 태그에 의해 
  http://localhost:8181/pro06/input?user_id=admin&user_pw=1234&subject=c&subject=jsp 요청할 주소가 만들어져 
  TOMCAT 웹애플리케이션 서버에 요청 주소가 전달 되면서 
  요청한 값들을 얻어 처리하는 서버페이지 역할.
*/
@WebServlet("/input")
public class InputServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//순서1. 요청한 데이터들을 HttpServletRequest객체 메모리에서 얻기전에?
		//		HttpServletRequest 객체 메모리에 문자를 처리할수 있는 방식의 값 UTF-8로 설정
		//요약 : 요청한 데이터 문자들 한글처리
		request.setCharacterEncoding("UTF-8");
		
		//순서2. InputServlet으로 요청한 데이터들을 HttpServletRequest 객체 메모리로 부터 얻는다.
		//요약 : 요청한 데이터들 얻기
		//순서2-1. 입력한 아이디, 비밀번호 얻기
		String user_id = request.getParameter("user_id");
		String user_pw = request.getParameter("user_pw");
		/*
		  순서2-2. <input type="checkbox" name="subject" value="체크된 실제값"> 들에 체크된 value 속성에 설정된 값들만 얻기
		  
		  참고. <input>태그들은 하나의 공통된 name="subject"로 여러 값을 한꺼번에 전송하여 받을 경우
		       HttpServletRequest객체 메모리의 getParameterValues("<input>태그의 name 속성값 subject 전달"); 메소드 호출!
		       String[] getParameterValues 메소드는 체크된 <input type="checkbox">들의 value 속성에 설정된 체크값들만
		       모두~~~~ String[] 배열에 담아 String[] 배열 메모리 주소 자체를 반환 해줍니다.
		*/
		String[] subject = request.getParameterValues("subject");
		//		["c", "jsp"]
		
		//순서3. InputServlet(톰캣서버가 실행할수 있는 서버페이지)로 요청한 데이터들을 확인하기 위해 출력
		System.out.println("요청시 입력한 아이디 : " + user_id);
		System.out.println("요청시 입력한 비밀번호 : " + user_pw);
		
		//<input type="checkbox"> 들 중에  체크되어 있는 과목의 value 문자열값만 출력
		for(String str  : subject ) {
			System.out.println("체크된 과목명 : " + str);
		}
		
		
	}
	
	
}











