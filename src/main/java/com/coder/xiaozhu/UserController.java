package com.coder.xiaozhu;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        JsonObject put = new JsonObject().put("code", 200).put("msg", "Hello World!").put("data", null);

        context.json(put);
    }

    private void handleLogin(RoutingContext context)  {

        GlobalConst.jdbcClient.getConnection(conn -> {
            if (conn.failed()) {
                throw new RuntimeException(conn.cause());
            }
            long l = System.currentTimeMillis();
            conn.result().queryWithParams("select * from typecho_users where name = ?", new JsonArray().add(context.request().getParam("name")), res -> {
                if (res.failed()) {
                    throw new RuntimeException(res.cause());
                }
                log.info("time test:{}",System.currentTimeMillis() - l);
                List<JsonObject> rows = res.result().getRows();
                if (rows == null || rows.isEmpty()) {
                    context.json(new JsonObject()
                            .put("code", 404)
                            .put("msg", "user not found")
                            .put("data", null));
                    return;
                }
                MainServer.User user = rows
                        .get(0)
                        .mapTo(MainServer.User.class);



                context.json(new JsonObject()
                        .put("code", 200)
                        .put("msg", "successfully")
                        .put("data", user));


            });
        });
    }
}
