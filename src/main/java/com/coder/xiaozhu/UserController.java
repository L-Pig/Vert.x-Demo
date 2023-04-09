package com.coder.xiaozhu;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserController {

    public static final UserController INSTANCE = new UserController();

    public void pushRouter(Router router, Vertx vertx) {
        Router subRouter = Router.router(vertx);
        subRouter.get("/").handler(this::handleRoot);
        subRouter.post("/login").handler(this::handleLogin);

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

        MainServer.JSON pojo = context.body().asPojo(MainServer.JSON.class);

        if (pojo.getName() == null || pojo.getPassword() == null) {
            context.response().setStatusCode(400).end();
            return;
        }
        if (pojo.getName().equals("admin") && pojo.getPassword().equals("admin")) {
            context.json(new JsonObject()
                    .put("code", 200)
                    .put("msg", "Hello World!")
                    .put("data", new JsonObject()
                            .put("username", pojo.getName())
                            .put("password", pojo.getPassword())));
        }
    }
}
