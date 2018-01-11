/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.everrest;

import static javax.websocket.server.ServerEndpointConfig.Builder.create;
import static javax.websocket.server.ServerEndpointConfig.Configurator;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;
import javax.ws.rs.core.SecurityContext;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.subject.Subject;
import org.everrest.core.DependencySupplier;
import org.everrest.core.ResourceBinder;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.RequestDispatcher;
import org.everrest.core.impl.RequestHandlerImpl;
import org.everrest.core.impl.provider.json.JsonException;
import org.everrest.core.tools.SimplePrincipal;
import org.everrest.core.tools.WebApplicationDeclaredRoles;
import org.everrest.websockets.message.BaseTextDecoder;
import org.everrest.websockets.message.BaseTextEncoder;
import org.everrest.websockets.message.JsonMessageConverter;
import org.everrest.websockets.message.OutputMessage;
import org.everrest.websockets.message.RestInputMessage;

/** @author andrew00x */
public class ServerContainerInitializeListener implements ServletContextListener {
  public static final String ENVIRONMENT_CONTEXT =
      "ide.websocket." + EnvironmentContext.class.getName();
  public static final String EVERREST_PROCESSOR_ATTRIBUTE = EverrestProcessor.class.getName();
  public static final String HTTP_SESSION_ATTRIBUTE = HttpSession.class.getName();
  public static final String EVERREST_CONFIG_ATTRIBUTE = EverrestConfiguration.class.getName();
  public static final String EXECUTOR_ATTRIBUTE = "everrest.Executor";
  public static final String SECURITY_CONTEXT = SecurityContext.class.getName();

  private WebApplicationDeclaredRoles webApplicationDeclaredRoles;
  private EverrestConfiguration everrestConfiguration;
  private ServerEndpointConfig wsServerEndpointConfig;
  private ServerEndpointConfig eventbusServerEndpointConfig;
  private String websocketContext;
  private String websocketEndPoint;
  private String eventBusEndPoint;

  @Override
  public final void contextInitialized(ServletContextEvent sce) {
    final ServletContext servletContext = sce.getServletContext();
    websocketContext =
        MoreObjects.firstNonNull(
            servletContext.getInitParameter("org.everrest.websocket.context"), "");
    websocketEndPoint =
        MoreObjects.firstNonNull(
            servletContext.getInitParameter("org.eclipse.che.websocket.endpoint"), "");
    eventBusEndPoint =
        MoreObjects.firstNonNull(
            servletContext.getInitParameter("org.eclipse.che.eventbus.endpoint"), "");
    webApplicationDeclaredRoles = new WebApplicationDeclaredRoles(servletContext);
    everrestConfiguration =
        (EverrestConfiguration) servletContext.getAttribute(EVERREST_CONFIG_ATTRIBUTE);
    if (everrestConfiguration == null) {
      everrestConfiguration = new EverrestConfiguration();
    }
    final ServerContainer serverContainer =
        (ServerContainer) servletContext.getAttribute("javax.websocket.server.ServerContainer");
    try {
      wsServerEndpointConfig = createWsServerEndpointConfig(servletContext);
      eventbusServerEndpointConfig = createEventbusServerEndpointConfig(servletContext);
      serverContainer.addEndpoint(wsServerEndpointConfig);
      serverContainer.addEndpoint(eventbusServerEndpointConfig);
    } catch (DeploymentException e) {
      throw new IllegalStateException(e.getMessage(), e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    if (wsServerEndpointConfig != null) {
      ExecutorService executor =
          (ExecutorService) wsServerEndpointConfig.getUserProperties().get(EXECUTOR_ATTRIBUTE);
      if (executor != null) {
        executor.shutdownNow();
      }
    }
    if (eventbusServerEndpointConfig != null) {
      ExecutorService executor =
          (ExecutorService)
              eventbusServerEndpointConfig.getUserProperties().get(EXECUTOR_ATTRIBUTE);
      if (executor != null) {
        executor.shutdownNow();
      }
    }
  }

  protected ServerEndpointConfig createWsServerEndpointConfig(ServletContext servletContext) {
    final List<Class<? extends Encoder>> encoders = new LinkedList<>();
    final List<Class<? extends Decoder>> decoders = new LinkedList<>();
    encoders.add(OutputMessageEncoder.class);
    decoders.add(InputMessageDecoder.class);
    final ServerEndpointConfig endpointConfig =
        create(CheWSConnection.class, websocketContext + websocketEndPoint)
            .configurator(createConfigurator())
            .encoders(encoders)
            .decoders(decoders)
            .build();
    endpointConfig
        .getUserProperties()
        .put(EVERREST_PROCESSOR_ATTRIBUTE, getEverrestProcessor(servletContext));
    endpointConfig
        .getUserProperties()
        .put(EVERREST_CONFIG_ATTRIBUTE, getEverrestConfiguration(servletContext));
    endpointConfig.getUserProperties().put(EXECUTOR_ATTRIBUTE, createExecutor(servletContext));
    return endpointConfig;
  }

  protected ServerEndpointConfig createEventbusServerEndpointConfig(ServletContext servletContext) {
    final List<Class<? extends Encoder>> encoders = new LinkedList<>();
    final List<Class<? extends Decoder>> decoders = new LinkedList<>();
    encoders.add(OutputMessageEncoder.class);
    decoders.add(InputMessageDecoder.class);
    final ServerEndpointConfig endpointConfig =
        create(CheWSConnection.class, websocketContext + eventBusEndPoint)
            .configurator(createConfigurator())
            .encoders(encoders)
            .decoders(decoders)
            .build();
    endpointConfig
        .getUserProperties()
        .put(EVERREST_PROCESSOR_ATTRIBUTE, getEverrestProcessor(servletContext));
    endpointConfig
        .getUserProperties()
        .put(EVERREST_CONFIG_ATTRIBUTE, getEverrestConfiguration(servletContext));
    endpointConfig.getUserProperties().put(EXECUTOR_ATTRIBUTE, createExecutor(servletContext));
    return endpointConfig;
  }

  private Configurator createConfigurator() {
    return new Configurator() {
      @Override
      public void modifyHandshake(
          ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        super.modifyHandshake(sec, request, response);
        final HttpSession httpSession = (HttpSession) request.getHttpSession();
        if (httpSession != null) {
          sec.getUserProperties().put(HTTP_SESSION_ATTRIBUTE, httpSession);
        }
        sec.getUserProperties().put(SECURITY_CONTEXT, createSecurityContext(request));
        sec.getUserProperties().put(ENVIRONMENT_CONTEXT, EnvironmentContext.getCurrent());
      }
    };
  }

  protected EverrestProcessor getEverrestProcessor(ServletContext servletContext) {

    final DependencySupplier dependencies =
        (DependencySupplier) servletContext.getAttribute(DependencySupplier.class.getName());
    final ResourceBinder resources =
        (ResourceBinder) servletContext.getAttribute(ResourceBinder.class.getName());
    final ProviderBinder providers =
        (ProviderBinder) servletContext.getAttribute(ApplicationProviderBinder.class.getName());
    final EverrestConfiguration copyOfEverrestConfiguration =
        new EverrestConfiguration(getEverrestConfiguration(servletContext));
    copyOfEverrestConfiguration.setProperty(
        EverrestConfiguration.METHOD_INVOKER_DECORATOR_FACTORY,
        WebSocketMethodInvokerDecoratorFactory.class.getName());
    final RequestHandlerImpl requestHandler =
        new RequestHandlerImpl(new RequestDispatcher(resources), providers);
    return new EverrestProcessor(copyOfEverrestConfiguration, dependencies, requestHandler, null);
  }

  protected EverrestConfiguration getEverrestConfiguration(ServletContext servletContext) {
    return everrestConfiguration;
  }

  protected ExecutorService createExecutor(final ServletContext servletContext) {
    final EverrestConfiguration everrestConfiguration = getEverrestConfiguration(servletContext);
    final String threadNameFormat =
        "everrest.WSConnection." + servletContext.getServletContextName() + "-%d";
    return Executors.newFixedThreadPool(
        everrestConfiguration.getAsynchronousPoolSize(),
        new ThreadFactoryBuilder()
            .setNameFormat(threadNameFormat)
            .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
            .setDaemon(true)
            .build());
  }

  protected SecurityContext createSecurityContext(final HandshakeRequest req) {
    final boolean isSecure = false; // todo: get somehow from request
    final String authType = "BASIC";
    final Subject subject = EnvironmentContext.getCurrent().getSubject();

    final Principal principal = new SimplePrincipal(subject.getUserName());
    return new SecurityContext() {

      @Override
      public Principal getUserPrincipal() {
        return principal;
      }

      @Override
      public boolean isUserInRole(String role) {
        return false;
      }

      @Override
      public boolean isSecure() {
        return isSecure;
      }

      @Override
      public String getAuthenticationScheme() {
        return authType;
      }
    };
  }

  public static class InputMessageDecoder extends BaseTextDecoder<RestInputMessage> {
    private final JsonMessageConverter jsonMessageConverter = new JsonMessageConverter();

    @Override
    public RestInputMessage decode(String s) throws DecodeException {
      try {
        return jsonMessageConverter.fromString(s, RestInputMessage.class);
      } catch (JsonException e) {
        throw new DecodeException(s, e.getMessage(), e);
      }
    }

    @Override
    public boolean willDecode(String s) {
      return true;
    }
  }

  public static class OutputMessageEncoder extends BaseTextEncoder<OutputMessage> {
    private final JsonMessageConverter jsonMessageConverter = new JsonMessageConverter();

    @Override
    public String encode(OutputMessage output) throws EncodeException {
      try {
        return jsonMessageConverter.toString(output);
      } catch (JsonException e) {
        throw new EncodeException(output, e.getMessage(), e);
      }
    }
  }
}
