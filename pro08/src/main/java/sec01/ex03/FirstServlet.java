package sec01.ex03;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/*
첫번째 서블릿(톰캣 서버가 실행하는 서버페이지)

순서1. 클라이언트가 웹브라우저 주소창에 첫번째 서블릿FistServlet을 요청할 요청 주소를 입력해서 요청한다.
      요청할 주소 -> http://localhost:8181/pro08/first
           
순서2. FirstServlet 클래스 내부의 doGet메소드 재구현 코드를 작성하는데
      두번째 서블릿인 SecondServlet을 포워딩(재요청기술)할 코드를 작성 함.  
                     
		예)  자바스크립트문법 ->   location.href = "재요청할 두번째 서블릿 매핑 주소 또는  재요청할 .jsp 의 주소";
*/
//@WebServlet("/first")
public class FirstServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*
		포워딩 : 다른 서블릿인 서버페이지를 재요청 하는 기술.
		
		포워딩 방법3. 자바스크립트 언어의 -> location객체의 href속성을 이용하는 방법
		
		->  첫번째 서블릿을 처음 요청했지만 첫번째 서블릿 클래스 내부 코드에서 두번째 서블릿을 재요청할때 
		    웹브라우저 주소창에~~~ 두번째 서블릿의 포워딩(재요청) second 주소가 자동으로 작성되면서        
		    재요청이 이루어 지는 방법이다.
		        	
		-> 작성 방법
				자바스크립트문법 ->   location.href = "재요청할 두번째 서블릿 매핑 주소 또는  재요청할 .jsp 의 주소";
	  */
		//1.  요청한 클라이언트의 웹브라우저에 응답할 데이터 유형 설정하고,
		//	  브라우저로 응답할 문자  깨짐 처리 방식의 값을 UTF-8로  HttpServletResponse 객체  메모리에 설정
		response.setContentType("text/html; charset=UTF-8");
	
		//2. 요청한 클라이언트의 웹브라우저와 연결된 출력 스트림 통로 PrintWriter객체 주소 얻기
		PrintWriter  out = response.getWriter();
		
		//3. 자바스크립트의  location 객체의  href속성에  포워딩(재요청) 할 주소 저장해서 재요청
		out.print("<script type='text/javascript'>");
		
		out.print(" window.alert('회원 가입 성공!'); ");
		
		out.print(" window.alert('메인 화면으로 이동!'); ");
		
		out.print(" location.href = 'second'; ");
		
		out.print("</script>");
	
	}
	
	
}







