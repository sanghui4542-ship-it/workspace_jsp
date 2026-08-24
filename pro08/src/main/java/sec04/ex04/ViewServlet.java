package sec04.ex04;

// 주제 : HTML 출력 코드를 메소드로 분리하여 가독성을 높인 ViewServlet

/*
    이 서블릿의 전체 처리 흐름

    MemberServlet에서 포워딩됨 (request 안에 조회 결과 배열이 담겨 있음)
        |
        v
    request에서 ArrayList 배열 꺼내기 -> HTML 표 형태로 출력 응답

    가독성 개선 포인트
    1. 표의 제목 행 출력 / 회원 한 명 행 출력을 각각의 메소드로 분리
    2. List<MemberVO> 제네릭 표기 -> (MemberVO) 강제 형변환 코드 삭제
*/

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/viewMembers2")
public class ViewServlet extends HttpServlet {

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
	// 화면 출력 담당 : 조회 결과 배열을 꺼내 HTML 표로 응답
	//================================================================
	@SuppressWarnings("unchecked")
	protected void doHandle(HttpServletRequest request, HttpServletResponse response)
							throws ServletException, IOException {

		// 요청 데이터의 한글 깨짐 방지 설정
		request.setCharacterEncoding("UTF-8");

		// MemberServlet에서 바인딩한 조회 결과 ArrayList 배열 꺼내오기
		List<MemberVO> list = (List<MemberVO>) request.getAttribute("membersList");

		// 응답 데이터 유형(HTML)과 인코딩 방식(UTF-8) 설정
		response.setContentType("text/html; charset=utf-8");

		// 웹브라우저로 데이터를 내보낼 출력스트림 얻기
		PrintWriter out = response.getWriter();

		/*
		    출력할 HTML 표의 완성 모습

		    +--------+----------+------+--------+--------+------+------+
		    | 아이디 | 비밀번호 | 이름 | 이메일 | 가입일 | 삭제 | 수정 |
		    +--------+----------+------+--------+--------+------+------+
		    | hong   | 1234     | ...  | ...    | ...    | 삭제 | 수정 |   <- 회원 수만큼 반복
		    +--------+----------+------+--------+--------+------+------+
		*/
		out.print("<html>");
		out.print("<body>");
		out.print("<table border=1>");

		// 표의 제목 행 1줄 출력
		printTableHeader(out);

		// 배열에 저장된 MemberVO 객체 수만큼 반복하며 회원 한 명씩 행 출력
		for (int i = 0; i < list.size(); i++) {
			printMemberRow(out, list.get(i));
		}

		out.print("</table>");
		out.print("<a href='/pro08/memberForm.html'>회원가입</a>");
		out.print("</body>");
		out.print("</html>");
	}

	//================================================================
	// 1. 표의 제목 행 출력
	//================================================================
	private void printTableHeader(PrintWriter out) {

		out.print("<tr align='center' bgcolor='lightgreen'>");
		out.print("<th>아이디</th>");
		out.print("<th>비밀번호</th>");
		out.print("<th>이름</th>");
		out.print("<th>이메일</th>");
		out.print("<th>가입일</th>");
		out.print("<th>삭제</th>");
		out.print("<th>수정</th>");
		out.print("</tr>");
	}

	//================================================================
	// 2. 회원 한 명의 정보 행 출력 (삭제/수정 요청 링크 포함)
	//================================================================
	private void printMemberRow(PrintWriter out, MemberVO vo) {

		out.print("<tr align='center'>");
		out.print("<td>" + vo.getId() + "</td>");
		out.print("<td>" + vo.getPwd() + "</td>");
		out.print("<td>" + vo.getName() + "</td>");
		out.print("<td>" + vo.getEmail() + "</td>");
		out.print("<td>" + vo.getJoinDate() + "</td>");

		// 클릭 시 MemberServlet(/member4)으로 삭제/수정 요청을 보내는 링크
		out.print("<td><a href='/pro08/member5?command=delMember&id=" + vo.getId() + "'>삭제</a></td>");
		out.print("<td><a href='/pro08/member5?command=modMember&id=" + vo.getId() + "'>수정</a></td>");
		out.print("</tr>");
	}

} // class ViewServlet 끝
