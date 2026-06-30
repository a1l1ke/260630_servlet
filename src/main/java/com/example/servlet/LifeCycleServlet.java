package com.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

// 2단계
@WebServlet({"/lifecycle", "/lifecycle2"}) // 배열을 넣어서 여러개의 경로를 연결할 수도 있다
public class LifeCycleServlet extends HttpServlet { // 1단계

    @Override
    public void init() throws ServletException {
//        super.init(); // 지워도 된다
        System.out.println("LifeCycleServlet.init");
//        같은 경로로 재접속하거나, 공유하는 경로로 접속 시 다시 init 되지 않음 (초기화되진 않음)
        // lazy 하다
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 매번 호출된다
        // 여기를 통해서 각각 HTTP 메서드/동사 (GET, POST, PUT, DELETE, ...) 로 분배함
        System.out.println("LifeCycleServlet.service");
        super.service(req, resp); // 이건 유지해야함
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 최종적으로 유저에게 무언가를 전달
        System.out.println("LifeCycleServlet.doGet");
        resp.setContentType("text/html; charset=utf-8");
        resp.getWriter().println("GET을 통해 접속했군요");
    }

    @Override
    public void destroy() {
        System.out.println("LifeCycleServlet.destroy");
    }
}
