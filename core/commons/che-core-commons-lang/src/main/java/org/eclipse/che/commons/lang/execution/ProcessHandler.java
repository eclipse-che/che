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
package org.eclipse.che.commons.lang.execution;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for native OS process
 *
 * @author Evgen Vidolob
 */
public class ProcessHandler implements Executor {
  private static final Logger LOG = LoggerFactory.getLogger(ProcessHandler.class);

  private final Process process;
  private final WaitForProcessEnd waitForProcess;
  private final ExecutorService executorService;
  private final ProcessListener listenerNotifier;
  private final TerminatingTaskRunner terminatingListener;
  private final CountDownLatch latch;

  private final List<ProcessListener> listeners = new CopyOnWriteArrayList<>();

  private volatile ProcessState state = ProcessState.INITIAL;

  public ProcessHandler(Process process) {
    executorService = createExecutorService();
    this.process = process;
    waitForProcess = new WaitForProcessEnd(process, this);
    listenerNotifier = createNotifier();
    terminatingListener = new TerminatingTaskRunner();
    addProcessListener(terminatingListener);
    latch = new CountDownLatch(1);
  }

  private ProcessListener createNotifier() {
    InvocationHandler invocationHandler =
        (proxy, method, args) -> {
          // call method over all listeners
          for (ProcessListener listener : listeners) {
            method.invoke(listener, args);
          }
          return null;
        };

    return (ProcessListener)
        Proxy.newProxyInstance(
            ProcessListener.class.getClassLoader(),
            new Class[] {ProcessListener.class},
            invocationHandler);
  }

  private ExecutorService createExecutorService() {
    return new ThreadPoolExecutor(
        10,
        Integer.MAX_VALUE,
        1,
        TimeUnit.MINUTES,
        new SynchronousQueue<>(),
        (r -> new Thread(r, "Native process polled Thread")));
  }

  public boolean isProcessTerminating() {
    return false;
  }

  public boolean isProcessTerminated() {
    return false;
  }

  public void addProcessListener(ProcessListener processListener) {
    listeners.add(processListener);
  }

  public void startNotify() {
    addProcessListener(
        new ProcessListener() {
          @Override
          public void onStart(ProcessEvent event) {
            try {
              OutputReader stdOutReader = createStdOutReader();
              stdOutReader.start();

              OutputReader stdErrReader = createStdErrReader();
              stdErrReader.start();

              waitForProcess.setEndCallback(
                  exitCode -> {
                    try {
                      stdErrReader.stop();
                      stdOutReader.stop();
                      try {
                        stdErrReader.waitFor();
                        stdOutReader.waitFor();
                      } catch (InterruptedException ignore) {
                      }
                    } finally {
                      ProcessHandler.this.onProcessTerminated(exitCode);
                      latch.countDown();
                    }
                  });
            } finally {
              removeProcessListener(this);
            }
          }

          @Override
          public void onText(ProcessEvent event, ProcessOutputType outputType) {}

          @Override
          public void onProcessTerminated(ProcessEvent event) {}

          @Override
          public void onProcessWillTerminate(ProcessEvent event) {}
        });
    state = ProcessState.RUNNING;
    listenerNotifier.onStart(new ProcessEvent(this));
  }

  private void onProcessTerminated(int exitCode) {
    terminatingListener.runTask(
        () -> {
          if (state == ProcessState.RUNNING) {
            state = ProcessState.TERMINATING;
            notifyOnTerminating();
          }

          if (state == ProcessState.TERMINATING) {
            state = ProcessState.TERMINATED;
            listenerNotifier.onProcessTerminated(new ProcessEvent(ProcessHandler.this, exitCode));
          }
        });
  }

  private void removeProcessListener(ProcessListener listener) {
    listeners.remove(listener);
  }

  private OutputReader createStdErrReader() {
    return new OutputReader(
        new InputStreamReader(process.getErrorStream()),
        this,
        (s -> notifyOnText(s, ProcessOutputType.STDERR)));
  }

  private void notifyOnTerminating() {
    listenerNotifier.onProcessWillTerminate(new ProcessEvent(this));
  }

  private void notifyOnText(String text, ProcessOutputType type) {
    listenerNotifier.onText(new ProcessEvent(this, text), type);
  }

  private OutputReader createStdOutReader() {
    return new OutputReader(
        new InputStreamReader(process.getInputStream()),
        this,
        (s -> {
          notifyOnText(s, ProcessOutputType.STDOUT);
        }));
  }

  @Override
  public Future<?> execute(Runnable runnable) {
    return executorService.submit(runnable);
  }

  public boolean isStarted() {
    return state != ProcessState.INITIAL;
  }

  public void destroyProcess() {
    terminatingListener.runTask(
        () -> {
          if (state == ProcessState.RUNNING) {
            state = ProcessState.TERMINATING;
            notifyOnTerminating();
            try {
              closeStream();
            } finally {
              process.destroy();
            }
          }
        });
  }

  private void closeStream() {
    try {
      process.getOutputStream().close();
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  public boolean waitFor() {
    try {
      latch.await();
      return true;
    } catch (InterruptedException ignored) {
      return false;
    }
  }

  private enum ProcessState {
    INITIAL,
    RUNNING,
    TERMINATING,
    TERMINATED
  }

  private class TerminatingTaskRunner implements ProcessListener {

    private List<Runnable> tasks = new ArrayList<>();

    @Override
    public void onStart(ProcessEvent event) {
      removeProcessListener(this);
      runAllTasks();
    }

    public void runTask(Runnable task) {
      if (isStarted()) {
        task.run();
      } else {
        synchronized (tasks) {
          tasks.add(task);
        }
        if (isStarted()) {
          runAllTasks();
        }
      }
    }

    private void runAllTasks() {
      List<Runnable> taskList;
      synchronized (tasks) {
        taskList = new ArrayList<>(tasks);
        tasks.clear();
      }
      taskList.forEach(Runnable::run);
    }

    @Override
    public void onText(ProcessEvent event, ProcessOutputType outputType) {}

    @Override
    public void onProcessTerminated(ProcessEvent event) {}

    @Override
    public void onProcessWillTerminate(ProcessEvent event) {}
  }
}
