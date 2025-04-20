package ru.academits.gerasimenko.todolistservlet.servlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.text.StringEscapeUtils;
import ru.academits.gerasimenko.todolistservlet.data.TodoItem;
import ru.academits.gerasimenko.todolistservlet.data.TodoItemsInMemoryRepository;
import ru.academits.gerasimenko.todolistservlet.data.TodoItemsRepository;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("")
public class TodoListServlet extends HttpServlet {
    private TodoItemsRepository todoItemsRepository;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        todoItemsRepository = new TodoItemsInMemoryRepository();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        String baseUrl = req.getContextPath() + "/";
        StringBuilder todoItemsStringBuilder = new StringBuilder();

        HttpSession session = req.getSession();

        String createError = getErrorMessage(session, "createError");
        String findError = getErrorMessage(session, "findError");

        List<TodoItem> todoItems = todoItemsRepository.getAll();

        for (TodoItem todoItem : todoItems) {
            todoItemsStringBuilder.append(String.format("""
                            <li style="margin-bottom: 1em;">
                                <form action="%s" method="POST">
                                    <input type="text" name="text" value="%s">
                                    <button type="submit" name="action" value="save">Сохранить</button>
                                    <button type="submit" name="action" value="delete">Удалить</button>
                                    <div style="color: red;">%s</div>
                                    <input type="hidden" name="id" value="%s">
                                </form>
                            </li>
                            """,
                    baseUrl,
                    StringEscapeUtils.escapeHtml4(todoItem.getText()),
                    getErrorMessage(session, "updateError", "updateErrorItemId", todoItem.getId()),
                    todoItem.getId()
            ));
        }

        PrintWriter writer = resp.getWriter();
        writer.printf("""
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <title>TODO List Servlet</title>
                        </head>
                        <body>
                            <h1>TODO List</h1>
                        
                            <form action="%s" method="POST">
                                <label>
                                    <span>Введите заметку:</span>
                                    <input type="text" name="text">
                                </label>
                        
                                <button type="submit" name="action" value="create">Создать</button>
                                <div style="color: red;">%s</div>
                            </form>
                        
                            <ul>%s</ul>
                        
                            <div style="color: red;">%s</div>
                        </body>
                        """,
                baseUrl,
                createError,
                todoItemsStringBuilder,
                findError
        );
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        HttpSession session = req.getSession();


        switch (action) {
            case "create" -> {
                String text = req.getParameter("text");

                if (text == null || text.trim().isEmpty()) {
                    session.setAttribute("createError", "Необходимо задать текст заметки");
                } else {
                    todoItemsRepository.create(new TodoItem(text.trim()));
                }
            }

            case "save" -> {
                int id = Integer.parseInt(req.getParameter("id"));
                String text = req.getParameter("text");


                if (text == null || text.trim().isEmpty()) {
                    session.setAttribute("updateError", "Необходимо задать текст заметки");
                    session.setAttribute("updateErrorItemId", id);
                } else {
                    try {
                        todoItemsRepository.update(new TodoItem(id, text));
                    } catch (IllegalArgumentException e) {
                        session.setAttribute("findError", "Не удалось найти заметку с указанным id");
                    }
                }
            }

            case "delete" -> {
                int id = Integer.parseInt(req.getParameter("id"));
                todoItemsRepository.delete(id);
            }
        }

        resp.sendRedirect(getServletContext().getContextPath() + "/");
    }

    private static String getErrorMessage(HttpSession session, String errorAttribute) {
        String errorMessage = session.getAttribute(errorAttribute) != null
                ? session.getAttribute(errorAttribute).toString()
                : "";
        session.removeAttribute(errorAttribute);

        return errorMessage;
    }

    private static String getErrorMessage(HttpSession session, String errorAttribute, String errorItemIdAttribute, int itemId) {
        Integer errorItemId = (Integer) session.getAttribute(errorItemIdAttribute);

        if (errorItemId == null || errorItemId != itemId) {
            return "";
        }

        session.removeAttribute(errorItemIdAttribute);
        return getErrorMessage(session, errorAttribute);
    }
}