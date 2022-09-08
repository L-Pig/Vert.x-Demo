package com.codezf;

import io.vertx.core.AbstractVerticle;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpServer;
import lombok.extern.slf4j.Slf4j;



/**
 * @author : Coder
 * @date : 2022-9-7
 * @desc :
 */
@Slf4j
public class MainServer extends AbstractVerticle {


    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {

        HttpServer httpServer = Vertx.vertx().createHttpServer();
        httpServer
                .requestStream().toObservable()
                .subscribe(request -> request.response().end("Hello World!"));
        httpServer.rxListen(9999).subscribe(success ->
                        log.info("Http Server started on port " + httpServer.actualPort() + " successfully ")
                );

//                .requestHandler(request -> request.response().end("Hello World!"))
//                .listen(9999)
//                .onSuccess(server -> System.out.println("Http Server started on port " + server.actualPort() + " successfully "));
    }

}
