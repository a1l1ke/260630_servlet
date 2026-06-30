package com.example.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/form")
public class FormServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("FormServlet.doGet");
        req.getRequestDispatcher("form.jsp").forward(req, resp);
//        일반적으로 GET은 데이터나 페이지를 주는 방식
        // Servlet & JSP -> 특정한 jsp 페이지를 서블릿(가상 경로)로 감싸서 준다
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Form을 통해서 input들을 받아서 복잡한 처리를 통해 전달
        // -> 지금은 해당 페이지로 이동하면서 'req'에 담아서 줬는데
        // 나중에 가서 '영속성'을 담당하는 Repository 등을 구현하게 되면
        // 새롭게 데이터를 담당하는 페이지를 호출하여 req의 연쇄를 막는다

        System.out.println("FormServlet.doPost");
        // 일반적으로는 다듬거나 다른 호출을 해서 그 값을 주는 것
        req.setAttribute("username",
                req.getParameter("username").substring(0, 3) + "*".repeat(req.getParameter("username").length() - 3)
        );
        req.setAttribute("password",
                req.getParameter("password").substring(0, 3) + "*".repeat(req.getParameter("password").length() - 3)
        );
        req.getRequestDispatcher("form.jsp").forward(req, resp);
    }
}
