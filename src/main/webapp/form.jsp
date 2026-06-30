<%-- form.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Form</title>
</head>
<body>
  <%-- localhost:8080/form --%>
<%--  <form action="${pageContext.request.contextPath}/form">--%>
  <form action="/form" style="display: flex; flex-direction: column; align-items: start; gap: 10px">
    <input name="username" placeholder="유저 이름을 입력해주세요">
    <input name="password" type="password" placeholder="비밀번호를 입력해주세요">
    <input type="submit">
  </form>
</body>
</html>