package file.controller;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/*
참고. MVC 디자인 개발 패턴에서 Controller(제어자)의 역할

	Controller(서블릿)가 하는 일 3가지
	  1. 브라우저가 보낸 요청 데이터를 request 내장객체 메모리에서 꺼낸다
	  2. Model(DAO)에게 DB작업을 시키고 결과를 받는다
	  3. 결과를 request 내장객체 메모리에 바인딩하고 View(JSP)에게 화면을 그리라고 넘긴다

	Controller 가 하지 않는 일 2가지
	  1. SQL 문장 작성      -> Model(DAO)이 담당
	  2. HTML 태그 출력     -> View(JSP)가 담당
*/

import file.dao.FileDAO;
import file.vo.FileVO;

//업로드한 파일 목록을 DB로 부터 조회 해서 보여주세요 요청주소를 받았을때 처리하는 서블릿 
@WebServlet("/list.do")
public class FileListServlet extends HttpServlet {
	
	/* DB작업을 대신 시킬 FileDAO객체의 주소를 저장할 변수 선언*/
	private FileDAO fileDao;
	
	/* DB작업을 대신 할 FileDAO 객체를 init메소드 내부에서 딱 한번만 생성해서 저장*/
	@Override
	public void init(ServletConfig config) throws ServletException {
		fileDao = new FileDAO();
	}
	
	/* 브라우저가 GET방식으로 요청했을 때 톰캣이 자동 호출하는 메소드

		GET방식이 되는 경우 3가지
		  1. 웹 브라우저 주소창에 직접 주소를 입력한 경우
		  2. <a href="list.do"> 링크를 클릭한 경우
		  3. <form method="get"> 으로 전송한 경우
	*/
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		/*1. 브라우저가 보낸 요청 데이터 중 한글 문자 인코딩 방식 UTF-8로 설정*/
		request.setCharacterEncoding("UTF-8");
		
		/*2. FileDAO객체에게 업로드된 파일의 정보를 DB조회 시켜서 조회된 ArrayList 배열을 얻어 저장*/
		ArrayList<FileVO> list = fileDao.selectAll();
		
		//   ArrayList배열 모습
		//   [ new FileVO(..), new FileVO(..), new FileVO(..) ]
		
		/*3. 조회된 ArrayList배열을 request 내장객체 메모리 영역에 key와 value 한쌍의 형태로 묶어서 바인딩*/
		request.setAttribute("list", list);
		/*
		참고. request내장객체 메모리에 바인딩(저장)한다는 의미

			request.setAttribute("list", list);
			                       key    value

			request내장객체 메모리 모습
			+-----------------------------------+
			|  "list"  ->  ArrayList객체의 주소  |
			+-----------------------------------+

			이 값을 JSP화면에서 꺼내는 방법
			  ${requestScope.list}         <- EL 표기법 (권장)
			  request.getAttribute("list") <- 스크립틀릿 방식

			주의. forward방식으로 이동해야 request메모리가 그대로 공유됩니다.
			     sendRedirect방식으로 이동하면 request메모리가 새로 만들어져
			     위에 저장한 값이 전부 사라집니다.
	    */
		
		//=============================================================================================
		/*4.1.조회결과를 보여줄 JSP화면의 경로를 지정해서 재요청(포워딩)을 담당하는 RequestDispatcher 객체 얻기*/
		RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/fileList.jsp");
		
		/*4.2. 디스패처 방식으로 fileList.jsp를 재요청(포워딩)해서 request와 reponse 내장객체 메모리 공유 */
		dispatcher.forward(request, response);
		//==============================================================================================
		
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}




