package sec06.ex02;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
@WebServlet 어노테이션
------------------------------------------------------------------------
- 이 클래스가 "서블릿(웹에서 동작하는 자바 프로그램)" 임을 톰캣 서버에게 알려주는 표식
- 예전에는 web.xml 파일에 직접 등록했지만,
  지금은 @WebServlet 어노테이션으로 간단히 등록한다.
- 즉, "이 클래스는 웹에서 실행될 서블릿 프로그램이다" 라고 선언하는 부분


   // name 속성
   // ----------------------------------------------------------------
   // - 서블릿의 내부 이름(별명)을 지정
   // - 톰캣 서버가 관리할 때 사용하는 이름
   // - 브라우저 주소에는 직접 보이지 않음
    
    // urlPatterns 속성
   // ----------------------------------------------------------------
   // - 브라우저 주소창에서 요청할 URL 주소(매핑 경로)
   // - 사용자가 /loadConfig 로 접속하면 이 서블릿이 실행됨
   // - 여러 개 주소도 배열로 등록 가능
   //   예: {"/a", "/b"} 

   // loadOnStartup 속성
   // ----------------------------------------------------------------
   // - 톰캣 서버가 "시작될 때" 이 서블릿을 미리 메모리에 로드할지 결정
   // - 숫자가 있을 경우 → 서버 시작 시 즉시 로드
   // - 숫자가 없으면 → 사용자가 처음 요청할 때 로드됨
   // - 숫자가 "작을수록 먼저 로드"
   //   1 → 가장 먼저
   //   2 → 그 다음
   // - 주로 설정값 읽는 서블릿에서 사용
*/
@WebServlet(urlPatterns = {"/loadAppConfig"}, name = "loadConfig", loadOnStartup = 1 )
public class LoadAppConfig extends HttpServlet {

	//웹프로젝트 pro08 내부의 모든 서블릿 페이지에서 공유할 데이터를 보관할 ServletContext 객체 주소 저장할 변수 
	private ServletContext context;
	
	private String menu_member;
	private String menu_order;
	private String menu_goods;
	
	//1. init메소드는 LoadAppConfig 서블릿 클래스가 톰캣서버에메모리에 로드될때 호출되며, 변수 초기화 작업을 담당하는 메소드입니다.
	//  : LoadAppConfig 서블릿 클래스의 context 변수값 초기화
	@Override
	public void init(ServletConfig config) throws ServletException {		
		System.out.println("LoadAppConfig 서블릿의 init 메소드가 호출을 당함");
		
		//매개변수 config로 전달받는 ServletConfig객체의 getServletContext()메소드를 호출하면
		//ServletContext 객체 메모리 주소를 반환 해 줍니다.
		context = config.getServletContext();
		
		//web.xml 또는 @WebServlet 어노테이션에 정의된 초기변수 값을 가져와서 사용할수 있습니다.
		//방법 : getInitParameter("변수명") 메소드를 사용하여, 해당 초기 변수 값들을 가져와 사용할수 있습니다.
		menu_member = context.getInitParameter("menu_member"); //"회원등록 회원조회 회원수정"
		menu_order =  context.getInitParameter("menu_order"); //"주문조회 주문등록 주문취소"
		menu_goods =  context.getInitParameter("menu_goods"); //"상품조회 상품등록 상품수정 상품삭제"
		
		//ServletContext 공유 객체 메모리 내부에서 얻은 메뉴 정보들을  다른 서블릿페이지에서 공유 받아 사용할수 있도록
		//ServletContext 객체 메모리에 바인딩 합시다.
		context.setAttribute("menu_member", menu_member);
		context.setAttribute("menu_order", menu_order);
		context.setAttribute("menu_goods", menu_goods);
	} //============ init 
	
	//2. 클라이언트가  브라우저 주소창에 주소를 입력하여 Get전송 요청을 하면 호출되는 콜백메소드로 
	//역할 : 클라이언트의 요청을 받아 응답 메세지를 생성해서 브라우저로 응답 하는 코드를 작성하는 기능 
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//순서1. 클라이언트가 요청한 데이터 한글 깨짐 방지 처리(인코딩 처리) 값 UTF-8로 설정
		request.setCharacterEncoding("UTF-8");
		
		//순서2. 클라이언트의 웹브라우저로 응답할 데이터 유형 설정하고 응답할 문자 인코딩 처리 값 UTF-8로 설정
		response.setContentType("text/html;  charset=UTF-8");
		
		//순서3. 클라이언트의 웹브라우저와 연결된 출력스트림 생성
		PrintWriter  out  = response.getWriter();
		
		//실제 작업
		//- ServletContext 공유 객체 메모리에 바인딩 했었던 menu_member, menu_order, menu_goods 변수의 값을 모두 꺼내와 얻자
		String menu_member = (String)context.getAttribute("menu_member");
	//  String menu_member = "회원등록 회원조회 회원수정"
		
		String menu_order = (String)context.getAttribute("menu_order");
	//  String menu_order = "주문조회 주문등록 주문취소"
		
		String menu_goods = (String)context.getAttribute("menu_goods");
	//  String menu_goods = "상품조회 상품등록 상품수정 상품삭제"
		
		//- 요청한 클라이언트의 웹브라우저로 PrintWriter 출력 스트림 통로를 통해 응답메세지를 내보내어 출력(응답)
		out.print("<html><body>");
		out.print("<table border='1' cellspacing='0'>");
		out.print("<tr><td>메뉴명</td></tr>");
		out.print("<tr><td>" + menu_member + "</td></tr>");
		out.print("<tr><td>" + menu_order + "</td></tr>");
		out.print("<tr><td>" + menu_goods + "</td></tr>");
		out.print("</table>");
		out.print("</body></html>");
			
	}

}












