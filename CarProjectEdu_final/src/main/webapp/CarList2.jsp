<%@page import="Vo.CarListVo"%>
<%@page import="java.util.Vector"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%
request.setCharacterEncoding("UTF-8");
String contextPath = request.getContextPath();
request.setAttribute("contextPath", contextPath);

//request.getAttribute() 는 Object 를 돌려주므로 형변환 검사를 할 수 없다(문법상 어쩔 수 없는 경고를 끈다)
@SuppressWarnings("unchecked")
Vector<CarListVo> carList = (Vector<CarListVo>) request.getAttribute("v");

/*
 [버그 수정] 조회 결과가 없을 때 500 에러가 났다.

   이 화면은 CarMain.jsp 가 include 하는 "조각(fragment)" 이지만,
   주소창에 /CarProject/CarList2.jsp 를 직접 입력해도 열린다.
   그때는 컨트롤러를 거치지 않으므로 request 에 "v"(차량 목록)가 없어
   아래 for 문에서 NullPointerException 이 발생했다.

       for (CarListVo vo : carList)   <- carList 가 null

   다른 조각들은 JSTL(${...}) 을 쓰거나 null 검사를 해서 빈 화면만 나오는데,
   이 파일만 스크립틀릿으로 바로 반복하고 있어 유일하게 500 이 났다.

   "값이 없을 수도 있다"는 전제를 두는 것이 기본이다.
   null 이면 빈 목록으로 바꿔 화면이 깨지지 않게 한다.
*/
if (carList == null) {
	carList = new Vector<CarListVo>();
}

int j = 0;
%>


<style>
/* =============================================
   [차량 목록] 반응형 스타일
   ============================================= */

/* 전체 컨테이너 */
.carlist2-container {
    max-width: 1000px;
    width: 100%;
    margin: 0 auto;
    padding: 20px 10px;
    box-sizing: border-box;
}

/* 페이지 제목: 이미지(cis.jpg) → CSS 텍스트 제목으로 대체 */
.carlist2-title {
    text-align: center;
    margin-bottom: 20px;
}

.carlist2-title .title-text {
    display: inline-block;
    font-size: 1.2rem;
    font-weight: bold;
    color: #333;
    padding: 8px 20px;
    border-left: 4px solid #cc0000;
    background-color: #f5f5f5;
    letter-spacing: 2px;
}

/* =============================================
   [차량 카드 그리드] 반응형
   - PC(1000px 이상): 4열
   - 태블릿(768px): 2열
   - 모바일(480px 이하): 1열
   ============================================= */
.car-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    justify-content: center;
    margin-bottom: 20px;
}

/* 차량 카드 */
.car-card {
    flex: 0 0 calc(25% - 15px);
    max-width: 240px;
    min-width: 200px;
    text-align: center;
    border: 1px solid #ddd;
    border-radius: 8px;
    padding: 10px;
    background-color: #fff;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    box-sizing: border-box;
    transition: transform 0.2s;
}

.car-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

/* 차량 이미지 플레이스홀더 (이미지 vo.getCarimg() 제거 → CSS 대체) */
.car-img-placeholder {
    width: 100%;
    height: 150px;
    background: linear-gradient(135deg, #2c2c2c 0%, #555555 100%);
    border-radius: 6px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin-bottom: 8px;
}

.car-img-placeholder .car-icon {
    font-size: 2.5rem;
    line-height: 1;
}

.car-img-placeholder .car-name-tag {
    font-size: 0.8rem;
    color: rgba(255,255,255,0.8);
}

/* 차량 링크 */
.car-card a {
    text-decoration: none;
    color: #333;
    display: block;
    font-size: 13px;
    line-height: 1.8;
}

.car-card a:hover {
    color: #cc0000;
}

/* 검색 폼 */
.carlist2-search {
    max-width: 500px;
    margin: 20px auto;
    padding: 15px;
    background-color: #f5f5f5;
    border-radius: 8px;
    text-align: center;
}

.carlist2-search form {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    align-items: center;
    justify-content: center;
}

.carlist2-search select {
    padding: 5px 10px;
    border: 1px solid #ccc;
    border-radius: 4px;
    font-size: 14px;
}

.carlist2-search input[type="submit"] {
    padding: 6px 16px;
    background-color: #cc0000;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
}

.carlist2-search input[type="submit"]:hover {
    background-color: #aa0000;
}

/* =============================================
   [반응형] 태블릿 (768px 이하) - 2열
   ============================================= */
@media (max-width: 768px) {
    .car-card {
        flex: 0 0 calc(50% - 10px);
        max-width: calc(50% - 10px);
        min-width: 150px;
    }
}

/* =============================================
   [반응형] 모바일 (480px 이하) - 1열
   ============================================= */
@media (max-width: 480px) {
    .car-card {
        flex: 0 0 100%;
        max-width: 100%;
    }
    .carlist2-search form {
        flex-direction: column;
    }
    .carlist2-search select,
    .carlist2-search input[type="submit"] {
        width: 100%;
    }
}
/* 모바일 반응형 끝 */
</style>


    <!-- ==========================================
         차량 목록 전체 컨테이너
         ========================================== -->
    <div class="carlist2-container">

        <!-- 페이지 제목: 이미지(cis.jpg) → CSS 텍스트 제목으로 대체 -->
        <div class="carlist2-title">
            <h3 class="title-text">차량 정보 목록</h3>
        </div>

        <%-- 목록이 비었을 때 안내 (기존에는 아무것도 없는 빈 화면이 나왔다) --%>
        <% if (carList.isEmpty()) { %>
            <div class="alert alert-info text-center">
                조회된 차량이 없습니다.<br>
                <a href="<%= contextPath %>/Car/CarList.do">전체 차량 보기</a>
            </div>
        <% } %>

        <!-- 차량 카드 그리드 -->
        <div class="car-grid">

        <% for (CarListVo vo : carList) { %>
            <!-- 차량 카드 1개 -->
            <div class="car-card">
                <a href="<%= contextPath %>/Car/CarInfo.do?carno=<%= vo.getCarno() %>">
                    <%-- 차량 이미지 플레이스홀더 (이미지 vo.getCarimg() 제거 → CSS 대체) --%>
                    <div class="car-img-placeholder">
                        <span class="car-icon">🚗</span>
                        <%-- [보안] DB 값도 이스케이프한다 (관리자가 넣은 값이라도 예외를 두지 않는다) --%>
                        <span class="car-name-tag"><%= util.HtmlUtil.escape(vo.getCarname()) %></span>
                    </div>
                    <%-- 차량명 --%>
                    차량명 : <%= util.HtmlUtil.escape(vo.getCarname()) %><br>
                    <%-- 한대당 금액 --%>
                    한대당 금액 : <%= vo.getCarprice() %>
                </a>
            </div>
        <%
            j++;
        }
        %>

        </div>

        <!-- 차량 유형별 검색 폼 -->
        <div class="carlist2-search">
            <form action="<%= contextPath %>/Car/carcategory.do">
                <select name="carcategory">
                    <option value="Small">소형</option>
                    <option value="Mid">중형</option>
                    <option value="Big">대형</option>
                </select>
                &nbsp;&nbsp;&nbsp;
                <input type="submit" value="차량검색">
            </form>
        </div>

    </div>

