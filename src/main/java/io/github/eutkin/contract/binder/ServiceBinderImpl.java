package io.github.eutkin.contract.binder;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHandler;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ServiceBinderImpl implements Binder {
  public static final long DEFAULT_CONNECTION_TIMEOUT = 5 * 60; // 5 minutes

  private final Vertx vertx;

  private String address;
  private boolean topLevel = true;
  private long timeoutSeconds = DEFAULT_CONNECTION_TIMEOUT;
  private List<Function<Message<JsonObject>, Future<Message<JsonObject>>>> interceptors;

  /**
   * Creates a factory.
   *
   * @param vertx a non null instance of vertx.
   */
  public ServiceBinderImpl(Vertx vertx) {
    Objects.requireNonNull(vertx);

    this.vertx = vertx;
  }

  /**
   * Set the address to use on the subsequent proxy creations or service registrations.
   *
   * @param address an eventbus address
   * @return self
   */
  public ServiceBinderImpl setAddress(String address) {
    this.address = address;
    return this;
  }

  /**
   * Set if the services to create are a top level services.
   *
   * @param topLevel true for top level (default: true)
   * @return self
   */
  public ServiceBinderImpl setTopLevel(boolean topLevel) {
    this.topLevel = topLevel;
    return this;
  }

  /**
   * Set the default timeout in seconds while waiting for a reply.
   *
   * @param timeoutSeconds the default timeout (default: 5 minutes)
   * @return self
   */
  public ServiceBinderImpl setTimeoutSeconds(long timeoutSeconds) {
    this.timeoutSeconds = timeoutSeconds;
    return this;
  }

  public ServiceBinderImpl addInterceptor(Function<Message<JsonObject>, Future<Message<JsonObject>>> interceptor) {
    if (interceptors == null) {
      interceptors = new ArrayList<>();
    }
    interceptors.add(interceptor);
    return this;
  }

  /**
   * Registers a service on the event bus.
   *
   * @param clazz   the service class (interface)
   * @param service the service object
   * @param <T>     the type of the service interface
   * @return the consumer used to unregister the service
   */
  public <T> MessageConsumer<JsonObject> register(Class<T> clazz, T service) {
    Objects.requireNonNull(address);
    // register
    return getProxyHandler(clazz, service).register(vertx.eventBus(), address, interceptors);
  }

  /**
   * Registers a local service on the event bus.
   * The registration will not be propagated to other nodes in the cluster.
   *
   * @param clazz   the service class (interface)
   * @param service the service object
   * @param <T>     the type of the service interface
   * @return the consumer used to unregister the service
   */
  public <T> MessageConsumer<JsonObject> registerLocal(Class<T> clazz, T service) {
    Objects.requireNonNull(address);
    // register
    return getProxyHandler(clazz, service).registerLocal(vertx.eventBus(), address, interceptors);
  }

  /**
   * Unregisters a published service.
   *
   * @param consumer the consumer returned by {@link #register(Class, Object)}.
   */
  public void unregister(MessageConsumer<JsonObject> consumer) {
    Objects.requireNonNull(consumer);

    if (consumer instanceof ProxyHandler) {
      ((ProxyHandler) consumer).close();
    } else {
      // Fall back to plain unregister.
      consumer.unregister();
    }
  }

  private <T> ProxyHandler getProxyHandler(Class<T> clazz, T service) {
    String handlerClassName = clazz.getName() + "VertxProxyHandler";
    Class<?> handlerClass = loadClass(handlerClassName, clazz);
    Constructor constructor = getConstructor(handlerClass, Vertx.class, clazz, boolean.class, long.class);
    Object instance = createInstance(constructor, vertx, service, topLevel, timeoutSeconds);
    return (ProxyHandler) instance;
  }

  private static Class<?> loadClass(String name, Class origin) {
    try {
      return origin.getClassLoader().loadClass(name);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Cannot find proxyClass: " + name, e);
    }
  }

  private static Constructor getConstructor(Class<?> clazz, Class<?>... types) {
    try {
      return clazz.getDeclaredConstructor(types);
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("Cannot find constructor on: " + clazz.getName(), e);
    }
  }

  private static Object createInstance(Constructor constructor, Object... args) {
    try {
      return constructor.newInstance(args);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to call constructor on", e);
    }
  }
}
