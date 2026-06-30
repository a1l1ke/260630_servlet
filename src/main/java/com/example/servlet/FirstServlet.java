package com.example.servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// Tomcat에 이 서블릿을 인식시키는 작업
// 0. Servlet(HttpServlet)을 상속받아야함
// 1. web.xml에 등록
// 2. @WebServlet 어노테이션을 사용

// Servlet 인터페이스
// ↓
// HttpServlet 추상클래스
// ↓ (최종적으로 개발자가 구현하는 서블릿 클래스)
//@WebServlet("/") // 경로를 매핑
@WebServlet("/first") // 경로를 매핑
public class FirstServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        super.doGet(req, resp); // 지우거나 주석처리하거나...
        // req : 요청 객체 - 사용자/브라우저(..)가 서버로 요청을 보내면서 포함한 정보들을 조회하며,
        // 필요 시 요청 객체를 통해 추가적인 다른 요청을 전달하기 위한 기능이 포함
        // resp(res) : 응답 객체 - 최종적으로 요청을 수행한 사용자/브라우저에게 어떻게 응답을 보낼지, 어떤 내용을 포함할지를 결정하는 객체
        resp.setContentType("text/html; charset=utf-8");
        resp.getWriter().println("안녕하세요 제 첫 Servlet에 오셨군요. 반갑습니다. 완전 멋진 Servlet이죠?");
        resp.getWriter().println("<h1>태그도 작성 가능합니다</h1>");
        resp.getWriter().println("""
                <h1>서블릿 좋죠?</h1>
                <h2>실은 뒤에 더 좋은거 나옴</h2>
                """);
        // 인코딩 문제가 해소됨
    }
}
