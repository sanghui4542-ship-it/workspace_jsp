package sec04.ex03;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
   두번쨰 서블릿 ViewServlet 역할
   - 첫번째 서블릿인 MemberServlet이 조회 작업한 정보를 HttpServletRequest에 바인딩 한후 
     공유 받아 브라우저로 응답(출력) 하는 서블릿.
*/

@WebServlet("/viewMembers")
public class ViewServlet extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//요청한 데이터 한글처리
		request.setCharacterEncoding("UTF-8");
			
		//첫번쨰 서블릿 MemberServlet에서 HttpServletRequest객체 메모리에 바인딩한
		//조회된 MemberVO객체들이 저장된 ArrayList배열 꺼내오기 
		List list = (List)request.getAttribute("membersList");
				
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
					out.print("<th>삭제</th>");
					out.print("<th>수정</th>");				
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
						out.print("<td><a href='/pro08/member4?command=delMember&id="+memberVO.getId()+"'>삭제</a></td>");
						out.print("<td><a href='/pro08/member4?command=modMember&id="+memberVO.getId()+"'>수정</a></td>");
					out.print("</tr>");
				}

			out.print("</table>");
			
			out.print("<a href='/pro08/memberForm.html'>회원가입</a>");
			
		out.print("</body>");
	out.print("</html>");		
		
	}
		
}



