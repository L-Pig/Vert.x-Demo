package com.coder.xiaozhu;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
public class UserController {

    public static final UserController INSTANCE = new UserController();

    public void pushRouter(Router router, Vertx vertx) {
        Router subRouter = Router.router(vertx);
        subRouter.get("/").handler(this::handleRoot);
        subRouter.get("/login").handler(this::handleLogin);

        router.route("/user/*").subRouter(subRouter);

    }

    private void handleRoot(RoutingContext context) {
        System.out.println(context.request().uri());
        JsonObject put = new JsonObject()
                .put("code", 200)
                .put("msg", "Hello World!")
                .put("data", null);

        context.json(put);
    }

    private void handleLogin(RoutingContext context) {

//        MainServer.JSON pojo = context.body().asPojo(MainServer.JSON.class);

        GlobalConst.jdbcClient.getConnection(conn -> {
            if (conn.succeeded()) {
                conn.result().queryWithParams("select * from typecho_users where name = ?", new JsonArray().add(context.request().getParam("name")), res -> {
                    if (res.succeeded()) {
                        List<JsonObject> rows = res.result().getRows();
                        if (rows != null && !rows.isEmpty()) {
                            MainServer.User user = rows.get(0).mapTo(MainServer.User.class);

                            context.json(new JsonObject()
                                    .put("code", 200)
                                    .put("msg", "successfully")
                                    .put("data", user));
                        } else {
                            context.json(new JsonObject()
                                    .put("code", 404)
                                    .put("msg", "user not found")
                                    .put("data", null));
                        }
                    }
                    if (res.failed()) {
                        log.error("error:", res.cause());
                    }
                });
            }
        });
    }
}
