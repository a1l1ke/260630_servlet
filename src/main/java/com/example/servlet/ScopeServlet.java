package com.example.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/scope")
public class ScopeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        System.out.println("action = " + action);
        if (action == null) {
//            resp.sendRedirect("scope.jsp"); // 주소를 유지하지 않고 = 요청 형식을 유지하지 않고 전달하겠다
            resp.sendRedirect("/scope2");
            return;
        }
//        req.setAttribute("action", action); // parameter, query-sting;
        req.setAttribute("action", action.toUpperCase()); // 변형된 데이터를 요청 객체에 담아서 전달
        HttpSession session = req.getSession();
        // 쿠키를 통해서 사용자/클라이언트를 구별하고, 해당 쿠키에 맞는 유저에 대해 상태 관리
        session.setAttribute("action", action + "라고 할 뻔");
        ServletContext context = req.getServletContext();
        context.setAttribute("action", action + "라고 해버렸네");
        // jsp에 요청 객체를 유지하면서 넘기는 방식 (forward)
        req.getRequestDispatcher("scope.jsp").forward(req, resp);
    }
}
