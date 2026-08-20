package sec01.ex01;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//사장 
/*
MemberServlet 클래스
- http://localhost:8181/pro07/member 요청을 받아
  t_member 테이블의 모든 회원 레코드를 조회해서 HTML 표로 응답하는 서블릿.

[전체 흐름]
[브라우저] --/member 요청--> [MemberServlet] --listMembers()--> [MemberDAO] --SQL--> [t_member]
[브라우저] <--HTML 표 응답-- [MemberServlet] <--ArrayList 반환-- [MemberDAO] <--결과-- [t_member]
*/

@WebServlet("/member")
public class MemberServlet extends HttpServlet{

	// doGet 메소드 오버라이딩 (alt + shift + s 누른후  v)  :  GET 방식 요청이 오면 톰캣이 자동 호출하는 메소드 
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//1. 요청 데이터의 한글 깨짐 방지를 위해 HttpServletRequest 객체의 문자 처리 방식을 UTF-8로 설정
		request.setCharacterEncoding("UTF-8");
		
		//2. 요청한 데이터 얻기
		// -> 이번 요청은 조회 주소( http://localhost:8181/pro07/member ) 만 있고 전송된 데이터가 없으므로 얻는 코드 생략.
		
		// 3. t_member 테이블에 조회 작업을 맡기기 위해 MemberDAO 객체 생성
		MemberDAO dao = new MemberDAO();  //<- DB 연결 작업 + DB작업 하는 사원
		
		// 3.1. listMembers() 호출 -> 조회된 전체 회원(ArrayList배열)을 반환받음
		List  list = dao.listMembers();
		/*
	    반환받은 ArrayList 배열 모습 (조회된 레코드(행) 1개당 MemberVO 객체 1개)
	    [ new MemberVO(), new MemberVO(), new MemberVO() ]
	         		0           1               2          <---- index
		 */		
		//3.2.1. 브라우저로 응답할 데이터의 유형(MIME-TYPE)을 HTML로 , 인코딩을 UTF-8로 설정
		response.setContentType("text/html; charset=utf-8");
		
		//3.2.2. 요청한 클라이언트의 브라우저와 연결된 출려스트림(PrintWriter) 객체 얻기
		PrintWriter  out = response.getWriter();
		
		//3.3. 조회된 회원 정보를 HTML 표 형태로 만들어 브라우저에 출력(응답)
		out.print("<html>");
		out.print("<body>");
			out.print("<table border=1>");
				// 표의 제목 행 출력
				out.print("<tr align='center' bgcolor='lightgreen'>");
					out.print("<th>아이디</th>");
					out.print("<th>비밀번호</th>");
					out.print("<th>이름</th>");
					out.print("<th>이메일</th>");
					out.print("<th>가입일</th>");
				out.print("</tr>");

				/*
			    반환받은 ArrayList 배열 모습 (조회된 레코드(행) 1개당 MemberVO 객체 1개)
			    [ new MemberVO(), new MemberVO(), new MemberVO() ]
			         		0           1               2          <---- index
				 */		
				
				// ArrayList에 저장된 MemberVO 객체 개수(list.size())만큼 반복 출력
				for (int i = 0; i < list.size(); i++) {

					// i번 칸의 MemberVO 객체를 꺼내 변수에 저장
					// (raw 타입 List라서 Object로 반환되므로 MemberVO로 형변환 필요)
					MemberVO memberVO = (MemberVO) list.get(i);

					// 회원 1명 = 표의 행 1개로 출력 (getter로 값을 꺼내 <td>에 삽입)
					out.print("<tr align='center'>");
						out.print("<td>" + memberVO.getId() + "</td>");
						out.print("<td>" + memberVO.getPwd() + "</td>");
						out.print("<td>" + memberVO.getName() + "</td>");
						out.print("<td>" + memberVO.getEmail() + "</td>");
						out.print("<td>" + memberVO.getJoinDate() + "</td>");
					out.print("</tr>");
				}

			out.print("</table>");
		out.print("</body>");
	out.print("</html>");		
		
		
	}
	

	

}











