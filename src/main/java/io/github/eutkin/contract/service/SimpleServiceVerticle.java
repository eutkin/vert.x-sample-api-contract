package io.github.eutkin.contract.service;

import io.github.eutkin.contract.reactivex.binder.Binder;
import io.github.eutkin.contract.reactivex.service.SimpleService;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

public class SimpleServiceVerticle extends AbstractVerticle {

  @Override
  public void start() {
    Binder.create(vertx)
      .setAddress(SimpleService.ADDRESS)
      .register(SimpleService.class, SimpleService.create());
  }
}
