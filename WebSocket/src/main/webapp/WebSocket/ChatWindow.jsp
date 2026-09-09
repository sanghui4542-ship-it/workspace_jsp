<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>

	<style>
		/*채팅 UI요소 스타일 지정*/
		#chatWindow{
   			 border: 1px solid black;    /* 대화창 테두리를 검은색 실선으로 설정 */
			 width : 270px;				 /* 대화창 너비 설정*/
			 height : 310px;             /* 대화창 높이 설정*/
			 overflow: scroll;			 /* 내용이 넘칠 때 스크롤바 표시 */
			 padding : 5px;				 /* 대화창 내부의 안쪽 여백 설정*/
		}
		#chatMessage { 
		    width: 236px;               /* 메시지 입력창 너비 설정 */
		    height: 30px;               /* 메시지 입력창 높이 설정 */
		}
		#sendBtn { 
		    height: 30px;               /* 전송 버튼 높이 설정 */
		    position: relative;         /* 전송 버튼 위치 조정을 위해 relative 포지션 설정 */
		    top: 2px;                   /* 버튼을 약간 아래로 이동 */
		    left: -2px;                 /* 버튼을 약간 왼쪽으로 이동 */
		}
		#closeBtn { 
		    margin-bottom: 3px;         /* 종료 버튼의 하단 여백 설정 */
		    position: relative;         /* 종료 버튼 위치 조정을 위해 relative 포지션 설정 */
		    top: 2px;                   /* 종료 버튼을 약간 아래로 이동 */
		    left: -2px;                 /* 종료 버튼을 약간 왼쪽으로 이동 */
		}
		#chatId { 
		    width: 158px;               /* 대화명 입력창 너비 설정 */
		    height: 24px;               /* 대화명 입력창 높이 설정 */
		    border: 1px solid #AAAAAA;  /* 대화명 입력창 테두리 설정 */
		    background-color: #EEEEEE;  /* 대화명 입력창 배경색 설정 */
		}
		.myMsg { 
		    text-align: right;          /* 내 메시지를 오른쪽 정렬로 설정 */
		}	
	</style>

	<script type="text/javascript">
	/*
	### WebSocket 클래스의 역할

	JavaScript의 WebSocket 클래스는 웹소켓 통신을 위한 API를 제공하며, 
	주요 역할과 기능은 다음과 같습니다.

	1. **서버와의 양방향 통신 채널 생성**:
	   - WebSocket 객체는 브라우저와 서버 간에 양방향 통신 채널을 만들어, **실시간 통신**을 가능하게 합니다.
	   - HTTP와 달리 웹소켓은 한 번 연결되면, 클라이언트와 서버 간에 메시지를 주고받는 데 추가 요청이 필요 없습니다.

	2. **이벤트 핸들러**:
	   - 웹소켓은 통신 상태에 따라 여러 이벤트 핸들러를 제공합니다. 
	   - onopen, onmessage, onclose, onerror 등 이벤트가 있어, 
	         서버 연결 시 발생할 동작을 코드로 정의할 수 있습니다.
	     - onopen : 웹소켓 연결이 열릴 때 발생하여 초기 설정 등을 처리할 수 있습니다.
	     - onmessage: 서버에서 메시지를 받을 때 발생하여, 메시지 데이터를 처리할 수 있습니다.
	     - onclose: 연결이 닫혔을 때 발생하여, 연결 종료 후 동작을 처리할 수 있습니다.
	     - onerror: 통신 중 오류가 발생할 때 호출되어 오류 처리를 할 수 있습니다.

	3. **메시지 전송 및 수신**:
	   - send() 메서드를 통해 클라이언트가 서버로 메시지를 보낼 수 있으며, 
	          서버에서 수신한 메시지를 `onmessage` 이벤트 핸들러에서 처리할 수 있습니다.
	   - 이 양방향 메시지 기능을 통해 채팅, 알림, 게임 데이터 등의 실시간 통신을 지원합니다.

	4. **상태 유지**:
	   - 웹소켓은 브라우저와 서버 간의 연결 상태를 readyState 속성으로 유지하여, 
	     현재 연결 상태를 확인하고 조작할 수 있습니다.
	   - 연결 상태는 CONNECTING, OPEN, CLOSING, CLOSED의 네 가지 상태로 표현됩니다.

	이와 같이 WebSocket 객체는 실시간 웹 애플리케이션에서 서버와 브라우저 간에 효율적인 양방향 통신을 관리하고, 
	대화형 기능을 구현하는 데 중요한 역할을 합니다.

	
	
		WebSocket 객체 생성
		-  new WebSocket(요청주소) 를 통해 웹소켓 객체를 생성하면,
		   지정된 요청주소의 서버페이지로 즉시 연결 요청을 시도 합니다.
		-  이 연결은 서버페이지와 양방향 통신 채널을 형성합니다.
	*/
	
	//웹 소켓 객체 생성 : JSP의 application내장객체를 통해 요청할 채팅 서버페이지 요청 주소로 웹소켓 통르를 만들어 연결
	let webSocket = new WebSocket("<%=application.getInitParameter("CHAT_ADDR")%>/ChatingServer");
									//				ws://localhost:8181/WebSocket/ChatingServer
									
	/* 참고4. web.xml에 아래 설정이 있어야 CHAT_ADDR 값을 읽을 수 있습니다.
	   <context-param>
	     <param-name>CHAT_ADDR</param-name>
	     <param-value>ws://localhost:8181/WebSocket</param-value>
	   </context-param>
	   설정이 없으면 null이 출력되어 주소가 "null/ChatingServer"가 되고 연결에 실패합니다.
	*/								
	
	// 웹 소켓 채팅에 사용할 HTML(DOM) 요소들(대화창, 메세지 입력창,  대화명)을 저장할 변수들 선언	
	let chatWindow, chatMessage, chatId;
	   
	//웹브라우저가 ChatWindow.jsp 파일의 모든 HTML태그들(DOM)을 로딩했을때
	window.onload = function(){
		
		 //대화 내용이 표시될 대화창 영역 얻기 <div id="chatWindow"></div>
		 chatWindow = document.getElementById("chatWindow");
		 
		 //메세지 입력 창 요소 얻기 <input type="text" id="chatMessage" onkeyup="enterKey();">
		 chatMessage = document.getElementById("chatMessage");
		 
		 //채팅하는 사용자의 대화명 요소에서 입력된 대화명 얻기  <input type="text" id="chatId"  value="${param.chatId}" readonly>
		 chatId = document.getElementById("chatId").value;
		 
	}
	//메세지 전송 함수  : 채팅 사용자가  메세지 전송 버튼을 클릭하거나 엔터 키를 눌렀을때 호출됨
	function sendMessage(){
		
		//사용자가 입력한 메세지를 대화창에서 얻어 오른쪾 정렬로 다자인 추가
		chatWindow.innerHTML += "<div class='myMsg'>" + chatMessage.value  + "</div>";
		
		//웹 소켓 통로(new WebSocket(....))를 통해 메세지를 서버페이지(ChatServer.java)로 전송
		//'대화명 | 메세지' 로 구분 (대화명과 입력한 채팅메세지를 | 기호로 구분하여 전송)
		webSocket.send(chatId + '|' + chatMessage.value);
		
		//메세지 입력창 내용 비우기 위해 빈 문자열"" 넣자
		chatMessage.value = "";
		
		//대화창 스크롤 막대바를 맨 아래로 강제 이동하여  새로운 메세지가 나타나면 보이게 설정
		chatWindow.scrollTop = chatWindow.scrollHeight;
	}
	
	//서버페이지와 웹 소켓로 연결을 종료하는 함수  :  사용자가 '채팅종료' 버튼을 클릭했을때 호출됨
	function disconnect(){
		webScoket.close();  //웹 소켓 통로 와 서버페이지와 연결 끊기 ==순간==> (웹브라우저, 서버페이지 연결 끊김)
	}
	
	//메세지 입력창(<input>)에서 Enter키를 누르고 뗏을때
	//자동으로 sendMessage함수를 호출하도록 처리하는 enterKey 함수 정의
	function enterKey(){
		//참고. Enter 키의 키코드 값은 13으로 정해져 있다.
		if(window.event.keyCode == 13){ //Enter 키를 눌렀다면
			sendMessage();
		}
	}
	//=================================================================================================
		
	//WebSocket 웹소켓 객체 통로에 여러 이벤트가 발생했을때... 자동으로 처리하는 이벤트 핸들러(function) 설정
	
	//1. 서버페이지에 웹소켓 통로 연결이 성공적으로 이루어진 이벤트가 발생했을때  호출되는 이벤트 핸들러(function) 설정
	webSocket.onopen = function(event){
		
		//대화창 	<div id="chatWindow"></div> 에 연결 성공 메세지를 보여주기 위해 출력
		chatWindow.innerHTML += "웹소켓 통로를 통한 서버페이지에 연결되었습니다.<br>";
	};
	
	//2. 웹 소켓 통로와 연결된 서버페이지와의 연결이 종료 될때의 이벤트가 발생하면 호출되는 이벤트 핸들러(function) 설정
	webSocket.onclose = function(event){
		
		//대화창 	<div id="chatWindow"></div> 에 연결 종료 메세지를 보여주기 위해 출력
		chatWindow.innerHTML += "웹소켓 통로를 통한 서버페지이와의 연결이 종료되었습니다.<br>";
	}
	
	//3. 웹 소켓 통로와 통신중  오류가 발생하는 이벤트가 발생하면 호출되는 핸들러(function)설정
	webSocket.onerror = function(event){
		
		alert(event.data);  //오류 발생시 알림창에 오류메세지 표시 
		
		//대화창 	<div id="chatWindow"></div> 에  오류 메세지를 보여주기 위해 출력
		chatWindow.innerHTML += "채팅 중에 오류가 발생하였습니다.<br>";
		
	}
	
	//4. 서버페이지에서 웹소켓 통로를 통해 클라이언트가 보낸 메세지를 보내서 여기로 수신했을때(메아리처럼 울러퍼지는 특징)
	webSocket.onmessage = function(event){
		
		//수신된 데이터 '대화명 | 메세지' 문자열을  '|' 기준으로 분리하여  배열전체에 담아 저장
		let message = event.data.split("|"); 
		//['대화명','메세지']
		//   0       1     index
		
		//수신된 데이터 중에서 '대화명' 얻어 변수에 저장
		let sender = message[0];
		//'대화명'

		//수신된 데이터 중에서 '메세지' 얻어 변수에 저장
		let content = message[1];
		//'메세지'
		
		if(content != ""){ //수신된 메세지가 있으면?						
			if(content.match("/")){//수신된 메세지가 귓속말인지 확인(귓속말 메세지에 '/' 문자가 포함되어 있음)	
				if(content.match( ("/" + chatId) )){ 	//귓속말 사용자를 대상으로 한것인지 확인(예:  '/사용자ID' )
					
					//귓속말 대상이 맞으면 귓속말임을 표시하고 메세지를 대화창에 표시 
					let temp = content.replace( ("/" + chatId) , "[귓속말] : ");
					chatWindow.innerHTML += "<div>" + sender + "" + temp + "</div>";
				}
			}else{//수신된 메세지 전체가 귓속말 메세지가 아니면?
				
				//일반 메세지인 경우 대화창 <div id="chatWindow"></div> 에 '대화명 : 메세지' 형식으로 표시 
				chatWindow.innerHTML += "<div>" + sender + " : " + content + "</div>";	
			}			
		} // 가장 바깥 if
		
		//새 메세지가 대화창에 추가되면 대화창의 스크롤 막대바를 가장 아래로 이동시켜 
		//사용자가 최신 새 메세지를 볼수 있도록 설정
		chatWindow.scrollTop = chatWindow.scrollHeight;
	};
		
		
							
	</script>
</head>
<body>
  	<%-- 현재 채팅하는 사람의 대화명을 input태그에 표시, 읽기전용으로 설정하여 수정 불가 --%>
  	대화명 : <input type="text" id="chatId"  value="${param.chatId}" readonly>
  	
  	<%--채팅 종료 버튼 클릭시 disconnect() 함수 호출 --%>
  	<button id="closeBtn" onclick="disconnect();">채팅 종료</button>
  	
  	<%-- 대화창, 수신된 메세지와 전송한 메세지가 표시되는 영역 --%>
  	<div id="chatWindow"></div>
  	
  	<div>
  		<%-- 메세지 입력공간, 키보드의 keyUp이벤트 발생시 enterKey()함수 호출 --%>
  		<input type="text" id="chatMessage" onkeyup="enterKey();">
  	
  		<%-- 입력한 메세지 전송 버튼, 클릭시(click이벤트) sendMessage()함수 호출 --%>
  		<button id="sendBtn" onclick="sendMessage();">전송</button>
  	</div>
  	
  	
  
</body>
</html>




