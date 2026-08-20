package sec01.ex01;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/*
InputServlet2 클래스 역할
- input2.html 클라이언트가 보는 화면에서  이름, 비밀번호 입력하고 과목명들을 체크하여
  <input type="submit" value="전송요청"> 을 눌렀을때
  <form action="/pro06/input2"> 태그에 의해 
  http://localhost:8181/pro06/input?user_id=admin&user_pw=1234&subject=c&subject=jsp 요청할 주소가 만들어져 
  TOMCAT 웹애플리케이션 서버에 요청 주소가 전달 되면서 
  요청한 값들을 얻어 처리하는 서버페이지 역할.
*/
@WebServlet("/input2")
public class InputServlet2 extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//순서1. 요청한 데이터들을 HttpServletRequest객체 메모리에서 얻기전에?
		//		HttpServletRequest 객체 메모리에 문자를 처리할수 있는 방식의 값 UTF-8로 설정
		//요약 : 요청한 데이터 문자들 한글처리
		request.setCharacterEncoding("UTF-8");
		
		//순서2. InputServlet2으로 요청한 데이터들을 HttpServletRequest 객체 메모리로 부터 얻는다.
		//요약 : 요청한 데이터들 얻기	
		/*
			HttpServletRequest클래스의 Enumeration<T> getParameterNames() 메소드 
		    - input2.html 에 작성한 <input>태그들의 name 속성 값들을 일일이 기억하지 못할때......
			  input2.html 에서 입력받은 <input>태그들의 name 속성 값들을 모두 Enumeration 배열에 담아 얻기 위한 메소드 
		*/
		Enumeration<String> enu = request.getParameterNames();
		
		//위 Enumeration 배열에  <input> 태그의 name 속성값들이 저장되어 있는 동안만 계속 반복해서 
		while(enu.hasMoreElements()) {
			
			//<input> 의 name 속성값들을 Enumeraction 배열에서 차례대로 얻어 저장
			//==> name 변수에는 "user_id" , "user_pw",  "subject" 가 차례대로 반복되서 얻어 저장될 것이다. 
			String name = enu.nextElement();
			
			//위 name 변수에 저장되어 있는 <input> 의 name 속성 값들을 getParameterValue 메소드 호출시 매개변수로 전달해 
			//입력한 아이디, 비밀번호, 그리고 체크된 과목명들을 모두 String[] 배열에 담아 반환 받는다.
			String[] values = request.getParameterValues(name);
			
			//향상된 for 반복문을 이용해  반복해서 요청한 데이터들 차례로 얻어 출력
			for(String value   : values ) {
				System.out.println("<input>의 name 속성 값 : " + name + ", value=" + value);
			}//for		
		}//while
	}//doGet
	
}//InputServlet2 클래스 











