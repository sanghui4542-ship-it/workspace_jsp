
<%@page import="java.util.List"%>
<%@page import="sec02.ex01.MemberDAO"%>
<%@page import="sec02.ex01.MemberVO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
   
 <%-- JSTL 중에서 core태그들을 사용하기 위해 외부 주소로 요청 --%>    
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>  

<% //순서1. memberForm.html 화면에서 가입을 위해 입력한 요청 데이터들 중 한글 문자 인코딩 방식 UTF-8로 설정
 request.setCharacterEncoding("UTF-8"); %>
 
 <%-- 순서2. 순서3. memberForm.html 화면에서 가입을 위해 입력한 요청 데이터들을 request내장객체 메모리 영역에서 얻어!
                  MemberVO클래스의 객체 생성후 각변수에 저장--%>
 <jsp:useBean id="membervo" class="sec02.ex01.MemberVO" />
 <jsp:setProperty  name="membervo" property="*"/>
 
 <%--순서4. memberForm.html화면에서 입력한 가입 정보들을 DB의 t_member테이블에 추가(insert)하기 위해
          DB관련 작업을 담당하는 MemberDAO클래스의 객체를 생성해서 addMember메소드 호출!(MemberVO객체 전달) --%>
 <jsp:useBean id="memberdao" class="sec02.ex01.MemberDAO"  />
 <c:set var="insertResult" value="${memberdao.addMember(membervo)}"/>
 
 
 <%-- 순서5. 순서4.에서  DB의 t_member테이블에  새 회원 레코드 추가에 성공했다면?
                      추가된 새회원정보를 포함해서 t_member테이블에 저장된 모든 회원레코드를 조회 해 오기 위해
            DB관련 작업을 담당하는 MemberDAO객체의 listMembers메소드를 호출해서 조회된 ArrayList배열을 얻어 저장합니다.  --%>
 <c:set var="membersList" value="${memberdao.listMembers()}" />          
 
 <%-- 순서6. 순서5.에서 조회된 회원정보가 저장된 ArrayList배열을!! 
            request내장객체 메모리에 key->"list", value->ArrayList 한쌍의 형태로 묶어서 바인딩(저장)합니다.  --%>
 <c:set var="list" value="${membersList}"  scope="request"/>            
    
 <%-- 순서7. 디스패처 방식으로 membersList.jsp를 재요청(포워딩)해서 request와 response내장객체 메모리 공유! --%>   
 <jsp:forward page="membersList.jsp"/>       
            














