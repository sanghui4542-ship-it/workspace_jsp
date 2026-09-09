<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>

	<h2>웹소켓 채팅 - 대화명 입력해서 채팅창 띄워주기</h2>
	
	대화명 : <input type="text" id="chatId">
	
	<button onclick="chatWinOpen();">채팅 참여</button>

	<script type="text/javascript">
	
		function chatWinOpen(){
			
			let id = document.getElementById("chatId");  // 대화명 입력하는 곳 <input id="chatId"> 얻기
			
			//<input>에 대화명 입력하지 않았을 경우, 대화명 입력 요청 알림
			if(id.value == ""){
				alert("대화명을 입력 후 채팅창을 열어 주세요");
				id.focus();
				return;  //chatWinOpen() 함수 종료 
			}
			
			//새로운 채팅창 열기 
			//입력한 대화명을 요청 데이터로 전달한 ChatWindow.jsp를 새롭게 팝업창 형식으로 보여줌
			window.open("ChatWindow.jsp?chatId=" + id.value,"","width=500, height=500");
			
			//새로운 팝업 채팅창이 열리면  입력한 대화명을 <input>에서 없애주자
			id.value = "";
		}
	</script>

</body>
</html>






