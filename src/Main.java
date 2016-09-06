import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;



public class Main {
    // static ArrayList<Book> games = new ArrayList<>();
    static HashMap<String, User> users = new HashMap<>();

    public static void main(String[] args) throws SQLException {

        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS books");
        stmt.execute("CREATE TABLE IF NOT EXISTS books (id IDENTITY, bookName VARCHAR, genre VARCHAR, platform VARCHAR, release_year INT)");

        Spark.init();
        Spark.get(
                "/",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());

                    HashMap<Object, Object> m = new HashMap<>();
                    if (user == null) {
                        return new ModelAndView(m, "login.html");
                    }
                    else {
                        ArrayList<Book> bookArrayList = selectBooks(conn);
                        m.put("books", bookArrayList);
                        return new ModelAndView(m, "index.html");
                    }
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/create-user",
                ((request, response) -> {
                    String name = request.queryParams("loginName");
                    User user = users.get(name);
                    if (user == null) {
                        user = new User(name);
                        users.put(name, user);
                    }

                    Session session = request.session();
                    session.attribute("userName", name);

                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/create-book",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());
                    if (user == null) {
                        //throw new Exception("User is not logged in");
                        Spark.halt(403);
                    }

                    String bookName = request.queryParams("bookName");
                    String bookGenre = request.queryParams("bookGenre");
                    String bookPlatform = request.queryParams("bookPlatform");
                    Integer bookYear = Integer.valueOf(request.queryParams("bookYear"));
                    insertBook(conn, bookName, bookGenre, bookPlatform, bookYear);

                    if (user != null) {
                        selectBooks(conn);
//                        user.games.add(game);
                    }

                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/delete-book",
                (request, response) -> {
                    Integer id = Integer.parseInt(request.queryParams("id"));
                    deleteBook(conn, id);

                    response.redirect("/");
                    return "";
                }
        );

        Spark.post(
                "/edit-book",
                (request, response) -> {
                    Integer id = Integer.parseInt(request.queryParams("ID"));
                    String name = request.queryParams("bookName");
                    String genre = request.queryParams("bookGenre");
                    String platform = request.queryParams("bookPlatform");
                    Integer bookYear = Integer.parseInt(request.queryParams("bookYear"));

                    updateBook(conn, id, name, genre, platform, bookYear);

                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );
    }

    static void updateBook(Connection conn, Integer id, String name, String genre,
                           String platform, Integer releaseYear) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("UPDATE BOOKS SET bookName = " + name + " WHERE ID = " + id);
        stmt.execute("UPDATE BOOKS SET genre = " + genre + " WHERE ID = " + id);
        stmt.execute("UPDATE BOOKS SET platform = " + platform + " WHERE ID = " + id);
        stmt.execute("UPDATE BOOKS SET release_year = " + releaseYear + " WHERE ID = " + id);
    }

    static User getUserFromSession(Session session) {
        String name = session.attribute("userName");
        return users.get(name);
    }

    static ArrayList<Book> selectBooks(Connection conn) throws SQLException {
        ArrayList<Book> books = new ArrayList<>();
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT * FROM books");
        while(results.next()){
            Integer id = results.getInt("ID");
            String name = results.getString("BOOKNAME");
            String genre = results.getString("GENRE");
            String platform = results.getString("PLATFORM");
            Integer releaseYear = results.getInt("RELEASE_YEAR");

            books.add(new Book(id, name, genre, platform, releaseYear));
        }

        return books;
    }

    static void insertBook(Connection conn, String name, String genre, String platform, Integer releaseYear) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO books VALUES (NULL, ?, ?, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, genre);
        stmt.setString(3, platform);
        stmt.setInt(4, releaseYear);
        stmt.execute();
    }

    static void deleteBook(Connection conn, Integer id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM books WHERE ID = " + id);
        stmt.execute();
    }
}
