<%-- form.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Form</title>
</head>
<body>
  <%-- localhost:8080/form --%>
<%--  <form action="${pageContext.request.contextPath}/form">--%>
<%--  <form action="/form" method="get"--%>
<%--  Servlet => doGet --%>
  <form action="/form" method="post"
<%--  Servlet => doPost      --%>
        style="display: flex; flex-direction: column; align-items: start; gap: 10px">
    <input name="username" placeholder="유저 이름을 입력해주세요">
    <input name="password" type="password" placeholder="비밀번호를 입력해주세요">
    <input type="submit">
  </form>
  <section>
    <%-- 데이터 표시 --%>
    <ul>
<%--      request를 객체를 통해서 변형된 데이터를 받을 수 있다 --%>
      <li> <%= request.getAttribute("username") %> </li>
<%--      <li> <%= request.getParameter("username") %> </li>--%>
      <li> <%= request.getAttribute("password") %> </li>
    </ul>
  </section>
</body>
</html>