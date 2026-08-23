package sec02.ex02;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
memberForm.html(회원가입을 위해 가입할 정보를 입력후 DB의 테이블에 INSERT 요청 하는 화면)
에서 가입할 정보를 입력하고 가입요청버튼을 클릭하면 입력된 정보들을 
모두 HttpServletRequest객체 메모리에  저장된후 공유받아 사용하는 서블릿으로 
회원 추가후 추가된 정보를 확인 하기 위해
다시~~~ 모든 회원정보들을 조회해서 클라이언트의 웹브라우저로 출력(응답) 해서 보여줍니다.

순서1. input type="hidden" name="command" value="addMember" 의 
      addMember값을  HttpServletRequest객체 메모리에서 얻습니다.
	  
	  얻는 코드 :   request.getParameter("command"); -> "addMember"
	  
	  얻는 이유 : 어떤 요청을 했는지 서블릿은 판단하기 위해서 입니다. 
	  			여기서는 addMember 값 자체가 회원가입 요청임을 판단하는 값이 됩니다. 
	  			
순서2. addMember <- 요청한 값은 t_member테이블에 회원추가 요청임을 판단해
	  가입시 입력한 회원정보들을 HttpServletRquest객체 메모리로부터 얻어서
	  MemeberVO객체 생성후 각인스턴스변수에 저장 

순서3. 실제 t_member테이블에 insert하기 위해 MemberDAO의 addMember메소드 호출시~
  	  매개변수로 MemberVO객체의 주소를 전달하여 MemberDAO의 addMember메소드 내부에서
  	  insert문장을 만들고 insert문을 실행할수 있도록 합니다.
  	  
순서4. insert에 성공하면 다시~모든 회원정보 조회 요청을 하기위해
  	  MemberDAO객체의 listMembers메소드를 호출하여 ArrayList배열을 받고
  	  웹브라우저 화면에 조회된 정보들을 응답합니다.
  	  
	  			
*/
//모든 회원 정보 조회 GET요청 주소 :  http://localhost:8181/pro07/member3   

//회원 가입 양식 디자인 입력 화면 GET요청 주소  :http://localhost:8181/pro07/memberForm.html

//새 회원 추가 POST요청 주소 : http://localhost:8181/pro07/member3
//					회원추가 요청 조건 값 : addMember 

//조회된 모든 회원정보를 보여주는 화면에서  삭제 <a>를 클릭하여 삭제 GET요청 하는 주소 : 
//http://localhost:8181/pro07/member3?command=delMember&id=hong
//							  회원삭제 요청 조건값 : delMember	

@WebServlet("/member3")
public class MemberServlet extends HttpServlet{

	
	//Get 요청 방식으로 요청 들어오면 응답하는 콜백메소드 
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doHandle(request, response);
	}
	//Post 요청 방식으로 요청 들어오면 응답하는 콜백메소드 
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doHandle(request, response);
	}
	
	//Get 또는 Post 요청 방식으로 요청 들어오면 모든 응답을 처리하는 일반 메소드 
	protected void doHandle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// 요청 데이터의 한글 깨짐 방지를 위해 HttpServletRequest 객체의 문자 처리 방식을 UTF-8로 설정
		request.setCharacterEncoding("UTF-8");
		
		//t_member 테이블에 조회 작업을 맡기기 위해 MemberDAO 객체 생성
		MemberDAO dao = new MemberDAO();  //<- DB 연결 작업 + DB작업 하는 사원
		
		
		// ----->  MemberServlet이 어떤 요청(회원 조회?  회원추가?  회원수정? 중 회원추가요청)을 받았는지 판단
		String command = request.getParameter("command");
		//회원 추가 요청한 조건값  "addMember"얻기 
		//회원 삭제 요청한 조건값  "delMember"얻기 
		
		//----->  DB의  t_member 테이블에 새회원 추가 요청("addMember")을 서블릿이 받았다면?
		if(command != null  &&  command.equals("addMember")) {
			
			//----> 회원가입(t_member 테이블에 클라이언트가 입력한 회원정보 추가)을 위해서 요청한 데이터들 얻기
			//=============== 회원가입 폼에서 입력한 값들을 HttpServletRequest객체 메모리에서 얻기=========
			String _id = request.getParameter("id");
			String _pwd = request.getParameter("pwd");
			String _name = request.getParameter("name");
			String _email = request.getParameter("email");
			
			//----> 회원가입을 위해 입력한 정보들을 한꺼번에 MemberDAO.java로 전달 하기 위해 
			//      MemberVO클래스의 객체를 생성하여 각 인스턴스변수에 저장
			MemberVO vo = new MemberVO(_id, _pwd, _name, _email);
			
			//-----> MemberDAO 에 작성해 놓은 addMember 메소드 호출시! 매개변수로 가입시 입력한 정보가 저장된 MemberVO 객체 주소하나 전달해서
			//		 INSERT 명령!  INSERT에 성공하면 1을 반환, INSERT에 실패하면 0을 반환 받게 하자.
			int result = dao.addMember(vo);
			
			System.out.println("회원가입(추가)에 성공하면 1 출력, 회원가입(추가)에 실패하면 0 출력 = " + result);
		
		// 회원 한사람의 삭제 요청을 서블릿이 받았다면?
		// command 변수에 저장된 요청 조건 값이 "delMember" 과 같다면? 	
		} else if(command != null &&  command.equals("delMember")) {
			
		// http://localhost:8181/pro07/member3?command=delMember&id=hong  <- 삭제 요청을 받은 요청 주소 
			
			//요청한 삭제할 회원 아이디 얻기
				//삭제할 회원의 아이디를 HttpservletRequest객체 메모리에서 꺼내오기
				//얻는 이유 : DELETE 구문 작성시 어떤 아이디를 가진 회원 레코드를 삭제 할것인지 판단해서 회원 레코드 하나만 삭제 시키기 위함
				//		예 : delete from t_member where id='hong';
			String id = request.getParameter("id"); //'hong'
			
			//요청 받은 삭제할 회원 아이디를 이용해  DB의 t_member테이블에 저장된 하나의 회원 레코드 삭제하기 위해
			//MemberDAO에 만들어 놓은 delMember메소드 호출!
			dao.delMember(id);
			
			
		}
		
		
		
		// listMembers() 호출 -> 조회된 전체 회원(ArrayList배열)을 반환받음
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
						out.print("<td><a href='/pro07/member3?command=delMember&id="+memberVO.getId()+"'>삭제</a></td>");
						out.print("<td><a href='/pro07/member3?command=delMember&id="+memberVO.getId()+"'>수정</a></td>");
					out.print("</tr>");
				}

			out.print("</table>");
			
			out.print("<a href='/pro07/memberForm.html'>회원가입</a>");
			
		out.print("</body>");
	out.print("</html>");		
		
		
	}
	

	

}











