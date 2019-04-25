package io.github.eutkin.contract.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.api.OperationRequest;
import io.vertx.ext.web.api.OperationResponse;

public class SimpleServiceImpl implements SimpleService {

  private static final Logger log = LoggerFactory.getLogger(SimpleServiceImpl.class);

  @Override
  public SimpleService doRedirect(String rpath, OperationRequest request, Handler<AsyncResult<OperationResponse>> handler) {
    log.info("Redirect by <{0}>", rpath);
    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("location", "https://google.com");
    OperationResponse response = new OperationResponse(302, "", Buffer.buffer(), headers);
    handler.handle(Future.succeededFuture(response));
    return this;
  }
}
