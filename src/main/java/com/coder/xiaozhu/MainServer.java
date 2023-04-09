package com.coder.xiaozhu;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.http.HttpServer;
import io.vertx.rxjava3.ext.web.Router;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;
import io.vertx.rxjava3.ext.web.handler.LoggerHandler;
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
        router.route().failureHandler(context -> {
                    Throwable failure = context.failure();
                    log.error("error", failure);
                    context.json(new JsonObject()
                            .put("code", 500)
                            .put("msg", failure.getMessage())
                            .put("data", null));
                }).handler(LoggerHandler.create())
                .handler(BodyHandler.create());
        router.route().handler(this::logUri);
        UserController.INSTANCE.pushRouter(router, vertx);
        return router;
    }

    private void logUri(RoutingContext context) {
        HttpMethod method = context.request().method();

        JSON pojo = context.body().asPojo(JSON.class);
        log.info("request method: {}, uri: {}, body: {}", method, context.request().uri(), pojo);
        log.info("request method: {}, uri: {}", context.request().method(), context.request().uri());
        context.next();
    }

    @Data
    public static class JSON {
        private String name;
        private String password;
    }
}