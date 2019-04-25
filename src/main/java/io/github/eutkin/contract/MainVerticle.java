package io.github.eutkin.contract;

import io.github.eutkin.contract.service.SimpleServiceVerticle;
import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Cookie;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.api.contract.RouterFactory;
import io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;

public class MainVerticle extends AbstractVerticle {

  @Override
  public Completable rxStart() {
    Completable deployService = vertx
      .rxDeployVerticle(SimpleServiceVerticle.class.getCanonicalName())
      .ignoreElement();


    Function<RoutingContext, JsonObject> extraFunc =  rc -> {
      Set<Cookie> cookies = rc.cookies();
      JsonObject json = cookies
        .stream()
        .collect(collectingAndThen(Collectors.<Cookie, String, Object>toMap(Cookie::getName, Cookie::getValue), JsonObject::new));
      json.put("ip", rc.request().remoteAddress().toString());
      return json;
    };

    Completable runServer = OpenAPI3RouterFactory.rxCreate(vertx, "spec.yml")
      .map(OpenAPI3RouterFactory::mountServicesFromExtensions)
      .map(factory -> factory.setBodyHandler(BodyHandler.create()))
      .map(factory -> factory.setExtraOperationContextPayloadMapper(extraFunc))
      .map(RouterFactory::getRouter)
      .map(vertx.createHttpServer()::requestHandler)
      .flatMap(server -> server.rxListen(8080))
      .ignoreElement();

    return deployService.concatWith(runServer);
  }
}
