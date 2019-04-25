package io.github.eutkin.contract.service;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;
import io.vertx.ext.web.api.generator.WebApiServiceGen;
import io.vertx.serviceproxy.ServiceProxyBuilder;

@WebApiServiceGen
@VertxGen
public interface SimpleService {

  String ADDRESS = "simple.service";

  static SimpleService create() {
    return new SimpleServiceImpl();
  }

  static SimpleService createProxy(Vertx vertx) {
    return new ServiceProxyBuilder(vertx).setAddress(ADDRESS).build(SimpleService.class);
  }

  @Fluent
  SimpleService doRedirect(String rpath, OperationRequest request, Handler<AsyncResult<OperationResponse>> handler);
}
