import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
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

                        ArrayList<Book> gameArrayList = selectGames(conn);
                        m.put("games", gameArrayList);
                        return new ModelAndView(m, "home.html");
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
                "/create-game",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());

                    String bookName = request.queryParams("gameName");
                    String bookPrice = request.queryParams("gameGenre");


                    Book book = new Book(bookName, bookPrice);
                    insertGame(conn, bookName, bookPrice);


                    if (user != null) {
                        user.books.add(game);
                    }

                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/delete-game",
                (request, response) -> {
                    Integer id = Integer.parseInt(request.queryParams("id"));
                    deleteGame(conn, id);

                    response.redirect("/");
                    return "";
                }
        );

        Spark.post(
                "/edit-game",
                (request, response) -> {
                    Integer id = Integer.parseInt(request.queryParams("ID"));
                    String name = request.queryParams("gameName");
                    String genre = request.queryParams("gameGenre");
                    String platform = request.queryParams("gamePlatform");
                    Integer gameYear = Integer.parseInt(request.queryParams("gameYear"));

                    updateGame(conn, id, name, genre, platform, gameYear);

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
}
