package sec04.ex04;

// 주제 : 수정 전 회원 조회값을 수정폼에 전달하고, 수정 요청까지 완성한 MemberServlet

/*
    이 서블릿의 전체 처리 흐름

    요청(command 값) 수신
        |
        +-- "addMember"  --> addMember(request)     : 새 회원 INSERT
        +-- "delMember"  --> delMember(request)     : 회원 한 명 DELETE
        +-- "modMember"  --> modMember(request, response)
        |                    : 수정할 회원 한 명 SELECT 후
        |                      조회값을 주소 뒤에 붙여 수정폼으로 이동 (여기서 응답 종료!)
        +-- "modMember2" --> updateMember(request)  : 수정폼 입력값으로 UPDATE
        |
        v
    전체 회원 조회(listMembers) -> request에 바인딩 -> ViewServlet으로 포워딩
    (단, "modMember"는 수정폼 화면으로 이동하므로 이 공통 흐름을 타지 않는다)

    회원 수정의 전체 왕복 흐름 (2번의 요청으로 완성된다)

    [1번째 요청] 회원목록에서 수정 링크 클릭
    /pro08/member5?command=modMember&id=hong
        -> DB에서 hong 조회 -> 조회값 4개를 주소 뒤에 붙여 수정폼으로 이동시킴
        -> /pro08/memberModForm.html?id=hong&pwd=1212&name=%ED%99%8D...&email=...

    [2번째 요청] 수정폼에서 값 고치고 수정요청 버튼 클릭
    /pro08/member5 (post, command=modMember2)
        -> 입력값 4개로 UPDATE 실행 -> 전체 조회 -> 회원목록 화면 응답
*/

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

@WebServlet("/member5")
public class MemberServlet extends HttpServlet {

	// DB 작업을 담당할 MemberDAO 객체 (모든 요청이 공용으로 사용)
	private MemberDAO memberDAO = new MemberDAO();

	// GET, POST 요청 모두 doHandle 메소드 하나로 모아서 처리
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
						throws ServletException, IOException {
		doHandle(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
						throws ServletException, IOException {
		doHandle(request, response);
	}

	//================================================================
	// 요청 접수 창구 : command 값을 판단해 알맞은 처리 메소드 호출
	//================================================================
	protected void doHandle(HttpServletRequest request, HttpServletResponse response)
							throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");// 요청 데이터의 한글 깨짐 방지 설정
		String command = request.getParameter("command"); // 어떤 요청(회원추가? 삭제? 수정화면? 수정완료?)인지 판단할 조건값 얻기

		// command 값에 따라 알맞은 처리 메소드 호출 (해당 없으면 조회만 실행)
		if ("addMember".equals(command)) { addMember(request); }
		else if ("delMember".equals(command)) { delMember(request); }
		else if ("modMember".equals(command)) {				
			modMember(request, response);
					// 수정폼 화면으로 "이동"시키는 응답을 직접 보내므로,
			return; // 아래의 공통 흐름(전체 조회 -> 포워딩)을 타면 안 된다 -> return으로 즉시 종료!
		} 
		else if ("modMember2".equals(command)) updateMember(request);
		forwardListMembers(request, response);// 추가/삭제/수정완료 처리 후 공통 흐름 : 전체 회원 조회 -> 화면 담당 서블릿으로 포워딩
	}

	//================================================================
	// 1. 새 회원 추가 (command=addMember)
	//================================================================
	private void addMember(HttpServletRequest request) {
		
		// 회원가입 폼에서 입력한 값 4개 얻고, 입력값 4개를 MemberVO 객체 1개에 담기
		MemberVO vo = new MemberVO(request.getParameter("id"), 
								   request.getParameter("pwd"), 
								   request.getParameter("name"), 
								   request.getParameter("email"));

		// MemberDAO에 INSERT 요청 -> 성공 1 / 실패 0 반환
		int result = memberDAO.addMember(vo);
		System.out.println("회원가입 성공하면 1 출력, 실패하면 0 출력 = " + result);
	}

	//================================================================
	// 2. 회원 한 명 삭제 (command=delMember)
	//================================================================
	private void delMember(HttpServletRequest request) {
	//회원 한명 삭제 요청 주소 예 : /pro08/member5?command=delMember&id=hong
		// 삭제할 회원의 아이디 얻고, (DELETE의 where 조건으로 사용)
		// MemberDAO에 DELETE 요청
		memberDAO.delMember(request.getParameter("id"));
	}

	//================================================================
	// 3. 수정할 회원 조회 후 수정폼으로 이동 (command=modMember)
	//================================================================
	private void modMember(HttpServletRequest request, HttpServletResponse response)
							throws IOException {

		// 요청 주소 예 : /pro08/member5?command=modMember&id=hong

		// 수정할 회원의 아이디로 DB에서 회원 한 명 조회해 오기
		MemberVO vo = memberDAO.modMember(request.getParameter("id"));

		/*
		    조회값을 수정폼으로 전달하는 방법
		    - memberModForm.html은 정적 화면이라 request.setAttribute로 전달받지 못한다.
		    - 대신 이동할 주소 "뒤에 ?이름=값&이름=값" 형태로 조회값 4개를 붙여 보내면,
		      수정폼의 자바스크립트가 주소 뒤의 값을 꺼내 <input>에 미리 채워 넣는다.

		    [주의] 한글 이름은 주소에 그대로 붙일 수 없다!
		    - 주소(URL)에는 영문/숫자/일부 기호만 허용되므로,
		      "홍길동" 같은 한글은 URLEncoder.encode로 "%ED%99%8D..." 형태로 변환해서 붙인다.
		    - 수정폼의 URLSearchParams가 꺼낼 때 자동으로 원래 한글로 되돌려(디코딩) 준다.
		*/
		String encName = URLEncoder.encode(vo.getName(), "UTF-8");

		// 이동 명령(자바스크립트)을 응답하기 위한 응답 유형/인코딩 설정과 출력스트림 얻기
		response.setContentType("text/html; charset=utf-8");
		PrintWriter out = response.getWriter();

		// location.href : 브라우저에게 "이 주소로 이동하라"고 명령하는 자바스크립트
		// -> 조회값 4개가 붙은 수정폼 주소로 브라우저가 다시 요청(이동)하게 된다
		out.print("<script>");
		out.print("location.href='/pro08/memberModForm.html"
				+ "?id=" + vo.getId()
				+ "&pwd=" + vo.getPwd()
				+ "&name=" + encName
				+ "&email=" + vo.getEmail() + "';");
		out.print("</script>");

		// 완성된 이동 주소 예
		// /pro08/memberModForm.html?id=hong&pwd=1212&name=%ED%99%8D%EA%B8%B8%EB%8F%99&email=hong@gmail.com
	}

	//================================================================
	// 4. 수정폼 입력값으로 회원 정보 수정 (command=modMember2)
	//================================================================
	private void updateMember(HttpServletRequest request) {

		// 수정폼에서 입력(수정)한 값 4개 얻고 (id는 readonly라 조회값 그대로 온다),
		// 수정값 4개를 MemberVO 객체 1개에 담기
		MemberVO vo = new MemberVO(request.getParameter("id"), 
								   request.getParameter("pwd"), 
								   request.getParameter("name"), 
								   request.getParameter("email"));

		// MemberDAO에 UPDATE 요청 -> 성공 1 / 실패 0 반환
		int result = memberDAO.updateMember(vo);
		System.out.println("회원수정 성공하면 1 출력, 실패하면 0 출력 = " + result);
	}

	//================================================================
	// 5. 공통 마무리 : 전체 회원 조회 후 ViewServlet으로 포워딩
	//================================================================
	private void forwardListMembers(HttpServletRequest request, HttpServletResponse response)
									throws ServletException, IOException {

		// 전체 회원 레코드를 조회한 ArrayList 배열 반환받기
		List<MemberVO> list = memberDAO.listMembers();

		// 조회 결과 배열을 HttpServletRequest 객체 메모리에 바인딩
		request.setAttribute("membersList", list);

		// 화면 출력 담당 ViewServlet으로 포워딩 (request, response 메모리 공유)
		RequestDispatcher dispatcher = request.getRequestDispatcher("viewMembers2");
		dispatcher.forward(request, response);
	}

} // class MemberServlet 끝

/*
    핵심 정리 3줄
    1. 회원 수정은 [조회 후 수정폼 이동(modMember)] + [입력값으로 UPDATE(modMember2)]
       2번의 요청으로 완성된다.
    2. 정적 화면(html)에는 setAttribute로 값을 전달할 수 없으므로,
       주소 뒤에 ?이름=값 형태로 조회값을 붙여 보내고 자바스크립트가 꺼내 채운다.
    3. 한글은 URLEncoder.encode로 변환해서 주소에 붙이고,
       modMember는 직접 응답하므로 return으로 공통 포워딩 흐름을 건너뛴다.
*/
