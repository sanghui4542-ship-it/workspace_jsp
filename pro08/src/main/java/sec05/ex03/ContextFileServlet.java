package sec05.ex03;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
주제 : getServletContext() 메소드를 호출하여 ServletContext공유 객체 메모리를 얻어 접근합니다.
      getResourceAsStream() 메소드를 이용해 읽어들일 데이터가 저장된 파일 위치 경로를 지정한 후 
      InputStream 입력스트림 통로를 반환 받아  파일에서 데이터를 바이트 단위로 읽어들여 웹브라우저로 응답합니다.

클라이언트 가 브라우저 주소창에 입력해서 요청하는 주소URL -> http://localhost:8181/pro08/cfile
*/


@WebServlet("/cfile")
public class ContextFileServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//재료
		//1. 브라우저로 응답할 데이터 유형 설정 및 한글처리
		response.setContentType("text/html; charset=utf-8");
		
		//2. 출력스트림 얻기
		PrintWriter  out = response.getWriter();
		
		//실제작업
		//1. 웹프로젝트(pro08)의 모든 서블릿 페이지에서 공유할 데이터가 저장되어 있는 ServletContext 공유 객체 메모리 주소 얻기
		ServletContext  servletContext = getServletContext();
		
		//2. 웹프로젝트(pro08)내부에 만들어 놓은 init.txt 텍스트 파일의 데이터들을  
		//   바이트 단위로 읽어들이기 위한 init.txt파일과 연결된 InputStream 입력스트림 통로 얻기 
		//요약 : init.txt 파일의 데이터를 읽어들이기 위한 InputStream 얻기 
							      //servletContext.getResourceAsStream("init.txt파일이 저장되어 있는 경로 작성");
		InputStream inputStream =   servletContext.getResourceAsStream("/WEB-INF/bin/init.txt");
								 
		//InputStream 입력스트림 통로          ->  InputStreamReader로 전환 후   -> BufferedReader 입력스트림 통로로 전환
		//역할 :  1바이트 단위로 읽어들이는 통로->   2바이트 단위로 읽어들이는 통로       ->  문자열 단위로 읽어들이는 통로 
		
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		
		//3. init.txt 파일에서 읽어온 메뉴 데이터 정보들을 저장시킬 변수들 선언
		String menu=null;       //"회원등록 회원조회 회원수정, 주문조회 주문수정 주문취소, 상품조회 상품등록 상품수정 상품삭제"
		
		String menu_member=null;//"회원등록 회원조회 회원수정"
		
		String menu_order=null; //"주문조회 주문수정 주문취소"
		
		String menu_goods=null; //"상품조회 상품등록 상품수정 상품삭제"		
		
		//4. init.txt 파일에 저장된 데이터들을 끝까지 읽어들이면서 각 메뉴 정보를 반복해서 분리합니다.
		//init.txt 파일은 한 줄에 "회원등록 회원조회 회원수정, 주문조회 주문수정 주문취소, 상품조회 상품등록 상품수정 상품삭제" 저장됨
		while(  (menu = bufferedReader.readLine()) != null ) {
		    //   menu = "회원등록 회원조회 회원수정, 주문조회 주문수정 주문취소, 상품조회 상품등록 상품수정 상품삭제"	
			
			//콤마 , 구분자 기호를 기준으로 앞뒤 문자열을 잘라내오자
			//사용되는 클래스 : StringTokenizer
			StringTokenizer stringTokenizer = new StringTokenizer(menu, ",");
			
			menu_member = stringTokenizer.nextToken(); //"회원등록 회원조회 회원수정"
			menu_order  = stringTokenizer.nextToken(); //"주문조회 주문수정 주문취소"
			menu_goods  = stringTokenizer.nextToken(); //"상품조회 상품등록 상품수정 상품삭제"
			
		} //<-- while
		
		//5. 출력스트림 통로인? PrintWriter의 print 메소드를 이용해 
		//   요청한 클라이언트의 웹브라우저 창으로 init.txt 파일에서 읽어들인 메뉴 데이터들을 응답(출력)
		out.print(menu_member + "<br>");//"회원등록 회원조회 회원수정<br>"
		out.print(menu_order + "<br>"); //"주문조회 주문수정 주문취소<br>"
		out.print(menu_goods + "<br>"); ///"상품조회 상품등록 상품수정 상품삭제<br>"
		
		
	}

}









