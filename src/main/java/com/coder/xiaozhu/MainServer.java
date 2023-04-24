package com.coder.xiaozhu;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.http.HttpServer;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;
import io.vertx.rxjava3.ext.web.handler.LoggerHandler;
import io.vertx.rxjava3.jdbcclient.JDBCPool;
import io.vertx.rxjava3.mysqlclient.MySQLPool;
import io.vertx.rxjava3.sqlclient.*;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.PrepareOptions;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


/**
 * @author : Coder
 * @date : 2022-9-7
 * @desc :
 */
@Slf4j
public class MainServer extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(MainServer.class.getName());
    }

    @Override
    public Completable rxStart() {

        GlobalConst.jdbcClient = createJdbc();

        HttpServer httpServer = vertx.createHttpServer();
        Single<HttpServer> listen = httpServer
                .requestHandler(scanRouter())
                .listen(9999);
        Disposable ignore = listen
                .subscribe(onSuccess -> log.info("Http Server started on port " + httpServer.actualPort() + " successfully "));
        return listen.ignoreElement();
    }

    public Router scanRouter() {
        Router router = Router.router(vertx);
        router.route()
                .failureHandler(context -> {
                    Throwable failure = context.failure();
                    log.error("error", failure);
                    context.json(new JsonObject()
                            .put("code", 500)
                            .put("msg", failure.getMessage())
                            .put("data", null));
                })
                .handler(LoggerHandler.create())
                .handler(BodyHandler.create())
                .handler(this::logUri);

        UserController.INSTANCE.pushRouter(router, vertx);

        return router;
    }

    private void logUri(RoutingContext context) {
        context.request().headers().add("startTime", String.valueOf(System.currentTimeMillis()));

        HttpMethod method = context.request().method();
        JSON pojo = context.body().asPojo(JSON.class);
        log.info("request method: {}, uri: {}, body: {}", method, context.request().uri(), pojo);

        context.next();

        context.response().endHandler(event -> {
            log.info("response: {},time using:{}", context.response().getStatusCode(), System.currentTimeMillis() - Long.parseLong(context.request().getHeader("startTime")));
        });
    }

    private JDBCClient createJdbc() {
        return JDBCClient.createShared(io.vertx.core.Vertx.vertx(), new JsonObject()
                .put("url", "jdbc:mysql://127.0.0.1:3306/blog_xiaozhu?generateSimpleParameterMetadata=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowMultiQueries=true")
                .put("user", "root")
                .put("password", "root")
                .put("database", "blog_xiaozhu")
                .put("driver_class", "com.mysql.cj.jdbc.Driver"));
    }

    @Data
    public static class JSON {
        private String name;
        private String password;
    }

    @Data
    public static class User {
        private Integer uid;
        private String name;
        private String password;
        private String mail;
        private String url;
        private String screenName;
        private Integer created;
        private Integer activated;
        private String group;
        private Integer logged;
        private String authCode;

    }
}