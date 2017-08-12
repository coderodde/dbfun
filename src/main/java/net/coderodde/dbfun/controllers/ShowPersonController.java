package net.coderodde.dbfun.controllers;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller is responsible for viewing persons.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Aug 8, 2017)
 */
@WebServlet(name = "ShowPersonController", urlPatterns = {"/show/*"})
public class ShowPersonController extends HttpServlet {


    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  the servlet request.
     * @param response the servlet response.
     * @throws ServletException if a servlet-specific error occurs.
     * @throws IOException if an I/O error occurs.
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {
            String path = request.getPathInfo();
            
            if (path == null || path.equals("/")) {
                Gson gson = new Gson();
                out.println(
                        gson.toJson(
                                DataAccessObject.instance().getAllUsers()));
                return;
            }
            
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            
            String[] tokens = path.split("/");
            String idString = tokens[0];
            int id = -1;

            try {
                id = Integer.parseInt(idString);
            } catch (NumberFormatException ex) {
                out.println("Error: " + idString + " is not an integer.");
                return;
            }

            Person person = DataAccessObject.instance().getUserById(id);

            if (person == null) {
                out.println("Error: no person with ID = " + id + ".");
                return;
            }

            String matchFirstName = null;

            if (tokens.length == 2) {
                matchFirstName = tokens[1];
            }

            if (!person.getFirstName().equals(matchFirstName)) {
                response.sendRedirect("/show/" + id + "/" + 
                                      person.getFirstName());
                return;
            }

            Gson gson = new Gson();
            out.println(gson.toJson(person));
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Shows the user info via ID/first_name";
    }
}
