<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>


<%   
	//순서1. 요청한 데이터 한글처리
	request.setCharacterEncoding("UTF-8");
%>
<%-- Address 클래스의 객체를 생성해서 인스턴스변수 city 와 zipcode 에 값을 모두 저장합니다. --%>
<jsp:useBean id="address" class="sec01.ex02.Address" scope="page" />
<jsp:setProperty  name="address" property="city"    value="서울" />
<jsp:setProperty  name="address" property="zipcode" value="07654"/>

<%-- 순서2. 순서3. memberForm.html에서 입력한 요청한 데이터들을 request에서 얻어 MemberVO의 변수에 모두 저장하기 위해 모든 setter호출 
				 ------------------------------------------------------------------------------------------------
				 MemberVO 객체에 만들어 놓은 
				 
				 public void setAddress(Address address){
				 	this.address = address;
				 }
				 
				 위 메소드를 호출하여  private Address address; 인스턴스변수에  new Address();객체 주소를 저장 하여 포함시키자.	 
--%>
<jsp:useBean id="vo" class="sec01.ex02.MemberVO" scope="page" />
<jsp:setProperty name="vo" property="*"/>
<jsp:setProperty name="vo" property="address" value="<%=address%>" />

	<table width="100%" align="center">
		<tr align="center" bgcolor="green">
			<td width="7%">아이디</td>
			<td width="7%">비밀번호</td>
			<td width="7%">이름</td>
			<td width="7%">이메일</td>	
			<td width="7%">도시이름</td>
			<td width="7%">우편번호</td>
		</tr>	
<%-- 순서4. MemberVO객체의 인스턴스변수값 얻어 ${ } EL태그로 출력 --%>
		<tr align="center">
			<td width="7%">${ pageScope.vo.id }</td>
			<td width="7%">${ pageScope.vo.pwd }</td>
			<td width="7%">${ pageScope.vo.name }</td>
			<td width="7%">${           vo.email }</td>	
			
<%-- MemberVO객체의 address 인스턴스변수에 저장된 new Address();객체의 주소번지를 얻고,
	 얻은 new Address();객체의 getter 메소드들을 호출하여  도시이름 , 우편번호 를 얻어  자바코드로 출력!(자바 버전!)
 				
			<td width="7%"><%=vo.getAddress().getCity()%></td>
			<td width="7%"><%=vo.getAddress().getZipcode() %></td>
--%>			
			
<%-- MemberVO객체의 address 인스턴스변수에 저장(포함)된 new Address();객체의 주소번지를 얻고,
	 얻은 new Address();객체의 getter 메소드들을 호출하여  도시이름 , 우편번호 를 얻어  EL ${} 태그안에 출력!
--%>					
			<td width="7%">${vo.address.city}</td>
			<td width="7%">${vo.address.zipcode}</td>	
		</tr>
		<tr height="4" bgcolor="pink">
			<td colspan="6"></td>
		</tr>
	</table>















