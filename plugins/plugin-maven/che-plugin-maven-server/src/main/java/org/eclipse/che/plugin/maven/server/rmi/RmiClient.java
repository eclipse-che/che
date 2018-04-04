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
package org.eclipse.che.plugin.maven.server.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.rmi.PortableRemoteObject;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.lang.execution.ExecutionException;
import org.eclipse.che.commons.lang.execution.ProcessEvent;
import org.eclipse.che.commons.lang.execution.ProcessExecutor;
import org.eclipse.che.commons.lang.execution.ProcessHandler;
import org.eclipse.che.commons.lang.execution.ProcessListener;
import org.eclipse.che.commons.lang.execution.ProcessOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Evgen Vidolob */
public abstract class RmiClient<Remote> {
  private static final Logger LOG = LoggerFactory.getLogger(RmiClient.class);

  private final Class<Remote> remoteClass;
  private final Map<Pair<Object, Object>, ProcessInfo> infoMap = new HashMap<>();

  public RmiClient(Class<Remote> remoteClass) {
    this.remoteClass = remoteClass;
  }

  public Remote acquire(Object target, Object param) throws Exception {
    Ref<RunningInfo> ref = Ref.ofNull();
    Pair<Object, Object> key = Pair.of(target, param);
    if (!getInfo(ref, key)) {
      startProcess(key);
      if (ref.isNull()) {
        try {
          synchronized (ref) {
            while (ref.isNull()) {
              ref.wait(1000);
            }
          }
        } catch (InterruptedException ignored) {
        }
      }
    }

    if (ref.isNull()) {
      throw new RuntimeException("Can't get remote proxy for: " + target.toString());
    }
    RunningInfo info = ref.getValue();
    if (info.processHandler == null) {
      throw new Exception(info.name);
    }

    return acquire(info);
  }

  private Remote acquire(RunningInfo info) throws Exception {
    Remote result;
    Registry registry = LocateRegistry.getRegistry("localhost", info.port);
    java.rmi.Remote lookup = registry.lookup(info.name);
    if (java.rmi.Remote.class.isAssignableFrom(remoteClass)) {
      Remote entry =
          remoteClass.isInstance(lookup)
              ? (Remote) lookup
              : (Remote) PortableRemoteObject.narrow(lookup, remoteClass);
      if (entry == null) {
        result = null;
      } else {
        // TODO add proxy for remote object
        result = (Remote) lookup;
      }
    } else {
      result = (Remote) lookup;
    }

    info.remoteRef = result;
    return result;
  }

  private void startProcess(Pair<Object, Object> key) {
    ProcessHandler handler;
    try {
      ProcessExecutor executor = getExecutor();
      handler = executor.execute();
    } catch (ExecutionException e) {
      removeProcessInfo(key, null, e.getMessage());
      return;
    }
    handler.addProcessListener(getProcessListener(key));
    handler.startNotify();
  }

  private ProcessListener getProcessListener(Pair<Object, Object> key) {
    return new ProcessListener() {
      @Override
      public void onStart(ProcessEvent event) {
        ProcessHandler processHandler = event.getProcessHandler();
        synchronized (infoMap) {
          ProcessInfo info = infoMap.get(key);
          if (info instanceof PendingInfo) {
            infoMap.put(key, new PendingInfo(processHandler, ((PendingInfo) info).ref));
          }
        }
      }

      @Override
      public void onText(ProcessEvent event, ProcessOutputType outputType) {
        String text = event.getText();
        if (text == null) {
          text = "";
        }
        if (outputType == ProcessOutputType.STDERR) {
          LOG.error(text);
        } else {
          LOG.info(text);
        }

        RunningInfo runningInfo = null;
        PendingInfo pendingInfo = null;
        synchronized (infoMap) {
          ProcessInfo info = infoMap.get(key);
          if (info instanceof PendingInfo) {
            pendingInfo = (PendingInfo) info;
            if (outputType == ProcessOutputType.STDOUT) {
              String prefix = "Port/Name:";
              if (text.startsWith(prefix)) {
                String portName = text.substring(prefix.length()).trim();
                int i = portName.indexOf('/');
                runningInfo =
                    new RunningInfo(
                        pendingInfo.processHandler,
                        Integer.parseInt(portName.substring(0, i)),
                        portName.substring(i + 1));
                infoMap.put(key, runningInfo);
                infoMap.notifyAll();
              }
            } else if (outputType == ProcessOutputType.STDERR) {
              pendingInfo.stderr.append(text);
            }
          }
        }

        if (runningInfo != null) {
          synchronized (pendingInfo.ref) {
            pendingInfo.ref.setValue(runningInfo);
            pendingInfo.ref.notifyAll();
          }
          // todo add ping there
        }
      }

      @Override
      public void onProcessTerminated(ProcessEvent event) {
        removeProcessInfo(key, event.getProcessHandler(), null);
      }

      @Override
      public void onProcessWillTerminate(ProcessEvent event) {
        removeProcessInfo(key, event.getProcessHandler(), null);
      }
    };
  }

  private boolean removeProcessInfo(
      Pair<Object, Object> key, ProcessHandler handler, String message) {
    ProcessInfo info;
    synchronized (infoMap) {
      info = infoMap.get(key);
      if (info != null && (handler == null || info.processHandler == handler)) {
        infoMap.remove(key);
        infoMap.notifyAll();
      } else {
        info = null;
      }
    }

    if (info instanceof PendingInfo) {
      PendingInfo pendingInfo = (PendingInfo) info;
      if (pendingInfo.stderr.length() > 0 || pendingInfo.ref.isNull()) {
        if (message != null) {
          pendingInfo.stderr.append(message);
        }
        pendingInfo.ref.setValue(new RunningInfo(null, -1, pendingInfo.stderr.toString()));
      }

      synchronized (pendingInfo.ref) {
        pendingInfo.ref.notifyAll();
      }
    }

    return info != null;
  }

  protected abstract ProcessExecutor getExecutor();

  private boolean getInfo(Ref<RunningInfo> ref, Pair<Object, Object> key) {
    ProcessInfo info;
    synchronized (infoMap) {
      info = infoMap.get(key);
      try {
        while (info != null
            && (!(info instanceof RunningInfo)
                || info.processHandler.isProcessTerminating()
                || info.processHandler.isProcessTerminated())) {
          infoMap.wait(1000);
          info = infoMap.get(key);
        }
      } catch (InterruptedException ignore) {
      }

      if (info == null) {
        infoMap.put(key, new PendingInfo(null, ref));
      }
    }

    if (info instanceof RunningInfo) {
      synchronized (ref) {
        ref.setValue((RunningInfo) info);
        ref.notifyAll();
      }
    }
    return info != null;
  }

  public void stopAll(boolean wait) {
    List<ProcessInfo> processList;
    synchronized (infoMap) {
      processList =
          (infoMap
              .values()
              .stream()
              .filter(info -> info.processHandler != null)
              .collect(Collectors.toList()));
    }

    if (processList.isEmpty()) {
      return;
    }

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    Future<?> future =
        executorService.submit(
            () -> {
              for (ProcessInfo processInfo : processList) {
                processInfo.processHandler.destroyProcess();
              }

              if (wait) {
                for (ProcessInfo processInfo : processList) {
                  processInfo.processHandler.waitFor();
                }
              }
            });

    if (wait) {
      try {
        future.get();
      } catch (InterruptedException ignore) {
      } catch (java.util.concurrent.ExecutionException e) {
        LOG.error(e.getMessage(), e);
      }
    }
    executorService.shutdown();
  }

  private static class ProcessInfo {
    final ProcessHandler processHandler;

    public ProcessInfo(ProcessHandler processHandler) {
      this.processHandler = processHandler;
    }
  }

  private static class RunningInfo extends ProcessInfo {
    final int port;
    final String name;
    Object remoteRef;

    public RunningInfo(ProcessHandler processHandler, int port, String name) {
      super(processHandler);
      this.port = port;
      this.name = name;
    }

    @Override
    public String toString() {
      return "RunnigInfo{" + "port=" + port + ", name='" + name + '\'' + '}';
    }
  }

  private static class PendingInfo extends ProcessInfo {
    final Ref<RunningInfo> ref;
    final StringBuilder stderr = new StringBuilder();

    public PendingInfo(ProcessHandler processHandler, Ref<RunningInfo> ref) {
      super(processHandler);
      this.ref = ref;
    }

    @Override
    public String toString() {
      return "PendingInfo{" + "ref=" + ref + '}';
    }
  }

  static {
    // set up RMI
    System.setProperty("java.rmi.server.hostname", "localhost");
    System.setProperty("java.rmi.server.disableHttp", "true");
  }
}
