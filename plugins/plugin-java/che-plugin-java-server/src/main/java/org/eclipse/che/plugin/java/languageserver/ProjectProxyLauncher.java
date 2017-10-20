/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.languageserver;

import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.che.api.languageserver.registry.ServerInitializerImpl;
import org.eclipse.che.api.languageserver.registry.ServerInitializerObserver;
import org.eclipse.che.api.languageserver.service.FileContentAccess;
import org.eclipse.che.api.languageserver.util.DynamicWrapper;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectProxyLauncher implements LanguageServerLauncher, ServerInitializerObserver {
  private static class State {
    public static final int INIT = 0;
    public static final int LAUNCHING = 1;
    public static final int LAUNCHED = 2;
    public static final int INITIALIZING = 3;
    public static final int INITIALIZED = 4;
    public static final int POST_INITIALIZING = 5;
    public static final int POST_INITIALIZED = 6;
  }

  private static final Logger LOG = LoggerFactory.getLogger(ServerInitializerImpl.class);

  private JavaLanguageServerLauncher wrappedLauncher;
  private LanguageServer languageServer;
  private InitializeResult initResult;
  private Object launchLock = new Object();
  private int state = State.INIT;
  private int launchedCount = 0;
  private int shutDownCount = 0;
  private int exitCount = 0;

  public ProjectProxyLauncher(JavaLanguageServerLauncher wrapped) {
    this.wrappedLauncher = wrapped;
  }

  @Override
  public LanguageServer launch(String projectPath, LanguageClient client)
      throws LanguageServerException {
    LOG.debug("launching on thread " + Thread.currentThread().getId());
    assureServerLaunched(projectPath, client);

    LOG.debug("launched on thread " + Thread.currentThread().getId());

    return (LanguageServer)
        Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] {LanguageServer.class, FileContentAccess.class},
            new DynamicWrapper(this, languageServer));
  }

  private void assureServerLaunched(String projectPath, LanguageClient client)
      throws LanguageServerException {
    boolean mustLaunch = false;
    synchronized (launchLock) {
      try {
        while (state == State.LAUNCHING) {
          launchLock.wait();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new LanguageServerException("Interrupted");
      }
      if (state == State.INIT) {
        mustLaunch = true;
        state = State.LAUNCHING;
        launchLock.notifyAll();
      }
      launchedCount++;
    }
    if (mustLaunch) {
      try {
        LanguageServer ls = wrappedLauncher.launch(projectPath, client);
        synchronized (launchLock) {
          languageServer = ls;
          state = State.LAUNCHED;
          launchLock.notifyAll();
        }
      } catch (LanguageServerException e) {
        synchronized (launchLock) {
          state = State.INIT;
          launchLock.notifyAll();
        }
        throw e;
      }
    }
  }

  @Override
  public LanguageServerDescription getDescription() {
    return wrappedLauncher.getDescription();
  }

  @Override
  public boolean isAbleToLaunch() {
    return languageServer != null || wrappedLauncher.isAbleToLaunch();
  }

  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    boolean mustInit = false;
    synchronized (launchLock) {
      if (state == State.LAUNCHED) {
        state = State.INITIALIZING;
        mustInit = true;
        launchLock.notifyAll();
      } else if (state < State.INITIALIZING) {
        throw new IllegalStateException("Called init when not launched");
      }
    }

    long threadId = Thread.currentThread().getId();
    if (mustInit) {
      LOG.info("initializing language server  on thread {} for {}", threadId, params.getRootUri());
      params.setRootUri("file:///projects");
      params.setRootPath("/projects");
      CompletableFuture<InitializeResult> res = new CompletableFuture<>();
      languageServer
          .initialize(params)
          .thenApply(
              (result) -> {
                LOG.info(
                    "initialized language server on thread {} for {}",
                    threadId,
                    params.getRootUri());
                synchronized (launchLock) {
                  initResult = result;
                  state = State.INITIALIZED;
                  launchLock.notifyAll();
                }
                return res.complete(result);
              })
          .exceptionally(
              (e) -> {
                LOG.debug(
                    "failed to initialize language server on thread {} for {}",
                    threadId,
                    params.getRootUri());
                synchronized (launchLock) {
                  state = State.LAUNCHED;
                  launchLock.notifyAll();
                }
                res.completeExceptionally(e);
                return null;
              });
      return res;
    } else {
      LOG.info(
          "already initialized language server on thread {} for {}", threadId, params.getRootUri());

      CompletableFuture<InitializeResult> res = new CompletableFuture<>();
      CompletableFuture.runAsync(
          () -> {
            try {
              synchronized (launchLock) {
                while (state == State.INITIALIZING) {
                  LOG.debug(
                      "waiting for language server init on thread {} for {}",
                      threadId,
                      params.getRootUri());
                  launchLock.wait();
                }
                if (state > State.INITIALIZING) {
                  LOG.info("completing init on thread {} for {}", threadId, params.getRootUri());
                  res.complete(initResult);
                } else {
                  LOG.debug(
                      "recursively calling init on thread {} for {}",
                      threadId,
                      params.getRootUri());
                  CompletableFuture<Void> f =
                      initialize(params)
                          .thenAccept(
                              (result) -> {
                                res.complete(result);
                              });
                  f.exceptionally(
                      (e) -> {
                        LOG.debug(
                            "failed recursively calling init on thread {} for {}",
                            threadId,
                            params.getRootUri());
                        res.completeExceptionally(e);
                        return null;
                      });
                }
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              res.completeExceptionally(e);
            }
          });
      return res;
    }
  }

  @Override
  public void onServerInitialized(
      LanguageServerLauncher launcher,
      LanguageServer server,
      ServerCapabilities capabilities,
      String projectPath) {
    boolean mustRun = false;
    synchronized (launchLock) {
      if (state < State.INITIALIZED) {
        throw new IllegalStateException();
      } else if (state == State.INITIALIZED) {
        state = State.POST_INITIALIZING;
        launchLock.notifyAll();
        mustRun = true;
      } else if (state == State.POST_INITIALIZED) {
        return;
      } else {
        while (state == State.POST_INITIALIZING) {
          try {
            launchLock.wait();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
          }
        }
      }
    }
    if (mustRun) {
      wrappedLauncher.onServerInitialized(wrappedLauncher, server, capabilities, "/");
      synchronized (launchLock) {
        state = State.POST_INITIALIZED;
        launchLock.notifyAll();
      }
    }
  }

  public CompletableFuture<Object> shutdown() {
    synchronized (launchLock) {
      if (shutDownCount == launchedCount - 1) {
        shutDownCount++;
        return languageServer.shutdown();
      } else {
        return CompletableFuture.completedFuture(null);
      }
    }
  }

  public void exit() {
    synchronized (launchLock) {
      if (exitCount == launchedCount - 1) {
        exitCount++;
        languageServer.exit();
      }
    }
  }
}
