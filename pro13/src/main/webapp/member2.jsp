

<%@page import="java.util.List"%>
<%@page import="sec01.ex01.MemberDAO"%>
<%@page import="sec01.ex01.MemberVO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%

	//1. 요청한 데이터 request 내장객체에 저장된 한글문자 인코딩 방식 UTF-8로 설정
	request.setCharacterEncoding("UTF-8");
	
	//2. 요청한 데이터들 request 내장객체로부터 얻어 각 변수에 저장
	String id = request.getParameter("id");
	String pwd = request.getParameter("pwd");
	String name = request.getParameter("name");
	String email = request.getParameter("email");
/*	
	<jsp:useBean> 액션태그는  자바코드 객체 생성하는 구문을 대채해서 작성할 수 있는 태그 이다.
	
	문법
		<jsp:useBean id="생성한 객체를 식별할 고유ID값(참조변수명 설정)" 
					 class="객체 생성시 사용될 클래스파일이 저장된 경로"
					 scope="생성한 객체는 어떤 내장객체 메모리영역에 바인딩할지 종류 작성"
					 />	
	참고. scope속성= page 또는 request 또는 session 또는 application 중 하나 작성
*/
	
	//3.1. MemberVO클래스의 객체하나를 생성해서 각 인스턴스변수에  요청한 데이터들 모두 각각 저장
	//MemberVO vo = new MemberVO(id, pwd, name, email);
%>
	<%-- jsp:useBean 액션태그를 사용해 MemberVO클래스의 기본생성자를 호출해 객체를 생성한 후 page 내장객체 영역에 바인딩 --%>
	<jsp:useBean   id="vo"   class="sec01.ex01.MemberVO"  scope="page"  />
<%	
	//3.1.1. 바로 위 jsp:useBean 액션태그를 사용해 생성한 MemberVO클래스의 객체 메모리의 각 인스턴스변수에 요청한 데이터들 저장
	vo.setId(id);  vo.setPwd(pwd);  vo.setName(name);  vo.setEmail(email);


	//3.2. 요청한 데이터들을  오라클 DBMS서버의 XE데이터베이스 내부에 만들어진 t_member테이블에 추가(insert) 하기 위해
	//     MemberDAO객체의 addMember()메소드를 호출하여 실행 해야 하기 때문에
	//     MemberDAO클래스의 객체 하나를 생성해서  addMember()메소드 호출시~~ 매개변수로 MemberVO객체주소 전달!
	MemberDAO dao = new MemberDAO();
			  dao.addMember(vo); 
	
	//3.3. t_member테이블에 가입할 회원정보가  추가 되었다면?
	//     t_member테이블에 저장된 회원레코드들을 다시 조회(select) 해서 가져 오기 위해
	//	     위 이미 생성 했었던  MemberDAO객체의 listMembers()메소드를 호출해서 조회된 정보가 저장된 ArrayList배열을 반환 받아 저장합니다.		  
	List membersList  = dao.listMembers(); 
	
	//ArrayList가변길이 배열 모습
	//[ MemberVO,  MemberVO,  MemberVO, MemberVO, MemberVO, MemberVO, MemberVO, MemberVO ]
	//	    0          1            2       3          4        5        6         7       index	

%>     
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%-- 4. 위3.3.에서 조회한 t_member테이블의 정보를 클라이언트의 웹브라우저로 보여주자(응답해 주자) --%>
	<table width="100%" align="center">
		<tr align="center" bgcolor="pink">
			<td width="7%">아이디</td>
			<td width="7%">비밀번호</td>
			<td width="7%">이름</td>
			<td width="7%">이메일</td>
			<td width="7%">가입일</td>		
		</tr>
<%
	//t_member테이블에서 조회한 회원정보가 없으면?
	//(ArrayList배열에 DB의 t_member테이블로부터 조회된 MemberVO객체들이 저장되어 있지 않으면?)
	if(membersList.size() == 0){
%>		
		<tr>
			<td colspan="5">등록된 회원이 없습니다.</td>
		</tr>	
<%	
	}else{ //t_member테이블에서 조회한 회원정보가 있으면?
		   //(ArrayList배열에 DB의 t_member테이블로 부터 조회된 MemberVO객체들이 저장되어 있으면?)
		
		//ArrayList배열에 저장된 MemberVO객체들의 정보를 웹브라우저 화면에 행단위로 반복해서 출력
		for(int i=0;  i<membersList.size();  i++){
			
			//ArrayList배열의 특정 index위치 칸에 저장되어 있는 MemberVO객체들을 차례로 반복해서 얻어 저장
			MemberVO memberVO = (MemberVO)membersList.get(i);
%>			
			<tr align="center">
				<td width="7%"><%=memberVO.getId()%></td>
				<td width="7%"><%=memberVO.getPwd()%></td>
				<td width="7%"><%=memberVO.getName()%></td>
				<td width="7%"><%=memberVO.getEmail()%></td>
				<td width="7%"><%=memberVO.getJoinDate()%></td>		
			</tr>		
<%			
		} //for
	}
%>	
	</table>



</body>
</html>











