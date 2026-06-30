<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Scope</title>
</head>
<body>
    <h1>My Scope</h1>
    <p>
        servlet을 통해서 요청객체 attribute로 받은 것 : <%= request.getAttribute("action") %>
        <%-- forward 방식으로 전달할 경우 직접 설정한 요청 객체의 값(속성)을 호출해서 쓸 수 있음 --%>
    </p>
    <p>
        session 저장 값 : <%= session.getAttribute("action") %>
        <%-- forward 방식으로 전달할 경우 직접 설정한 요청 객체의 값(속성)을 호출해서 쓸 수 있음 --%>
    </p>
    <p>
        application 저장 값 : <%= application.getAttribute("action") %>
        <%-- forward 방식으로 전달할 경우 직접 설정한 요청 객체의 값(속성)을 호출해서 쓸 수 있음 --%>
    </p>
    <p>
        ?key=value (쿼리스트링) : <%= request.getParameter("action") %>
        <%-- jsp도 요청객체를 받아서 쓰는 servlet의 일정 --%>
        <%-- 이것이 과잉되면 jsp가 화면을 표현하는 로직보다 실제 데이터 로직처리가 많아짐 --%>
        <%-- 유지보수가 많이 힘들어짐 => Servlet과 나눠가짐 --%>
    </p>
</body>
</html>
