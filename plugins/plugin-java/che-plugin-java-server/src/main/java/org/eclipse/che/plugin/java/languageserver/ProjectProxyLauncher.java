package org.eclipse.che.plugin.java.languageserver;

import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.che.api.languageserver.service.FileContentAccess;
import org.eclipse.che.api.languageserver.util.DynamicWrapper;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

public class ProjectProxyLauncher implements LanguageServerLauncher {
  private static class State {
    public static final int INIT = 0;
    public static final int LAUNCHING = 1;
    public static final int LAUNCHED = 2;
    public static final int INITIALIZING = 3;
    public static final int INITIALIZED = 4;
  }

  private JavaLanguageServerLauncher wrappedLauncher;
  private LanguageServer languageServer;
  private InitializeResult initResult;
  private Object launchLock = new Object();
  private int state = State.INIT;

  public ProjectProxyLauncher(JavaLanguageServerLauncher wrapped) {
    this.wrappedLauncher = wrapped;
  }

  @Override
  public LanguageServer launch(String projectPath, LanguageClient client)
      throws LanguageServerException {
    assureServerLaunched(projectPath, client);
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
    if (mustInit) {
      params.setRootUri("file:///projects");
      params.setRootPath("/projects");
      CompletableFuture<InitializeResult> res = new CompletableFuture<>();
      languageServer
          .initialize(params)
          .thenApply(
              (result) -> {
                synchronized (launchLock) {
                  initResult = result;
                  state = State.INITIALIZED;
                  launchLock.notifyAll();
                }
                return res.complete(result);
              })
          .exceptionally(
              (e) -> {
                synchronized (launchLock) {
                  state = State.LAUNCHED;
                  launchLock.notifyAll();
                }
                res.completeExceptionally(e);
                return null;
              });
      return res;
    } else {
      CompletableFuture<InitializeResult> res = new CompletableFuture<>();
      CompletableFuture.runAsync(
          () -> {
            try {
              while (state == State.INITIALIZING) {
                launchLock.wait();
              }
              if (state > State.INITIALIZING) {
                res.complete(initResult);
              } else {
                CompletableFuture<Void> f =
                    initialize(params)
                        .thenAccept(
                            (result) -> {
                              res.complete(result);
                            });
                f.exceptionally(
                    (e) -> {
                      res.completeExceptionally(e);
                      return null;
                    });
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              res.completeExceptionally(e);
            }
          });
      return res;
    }
  }
}
