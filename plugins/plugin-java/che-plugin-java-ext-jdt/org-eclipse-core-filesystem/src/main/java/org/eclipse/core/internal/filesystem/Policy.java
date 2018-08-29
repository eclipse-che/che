/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.core.internal.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import org.eclipse.core.internal.runtime.RuntimeLog;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

/** Grab bag of utility methods for the file system plugin */
public class Policy {
  public static final String PI_FILE_SYSTEM = "org.eclipse.core.filesystem"; // $NON-NLS-1$

  public static void checkCanceled(IProgressMonitor monitor) {
    if (monitor.isCanceled()) throw new OperationCanceledException();
  }

  /**
   * Print a debug message to the console. Pre-pend the message with the current date and the name
   * of the current thread.
   */
  public static void debug(String message) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(new Date(System.currentTimeMillis()));
    buffer.append(" - ["); // $NON-NLS-1$
    buffer.append(Thread.currentThread().getName());
    buffer.append("] "); // $NON-NLS-1$
    buffer.append(message);
    System.out.println(buffer.toString());
  }

  public static void error(int code, String message) throws CoreException {
    error(code, message, null);
  }

  public static void error(int code, String message, Throwable exception) throws CoreException {
    int severity = code == 0 ? 0 : 1 << (code % 100 / 33);
    throw new CoreException(new Status(severity, PI_FILE_SYSTEM, code, message, exception));
  }

  public static void log(int severity, String message, Throwable t) {
    if (message == null) message = ""; // $NON-NLS-1$
    RuntimeLog.log(new Status(severity, PI_FILE_SYSTEM, 1, message, t));
  }

  public static IProgressMonitor monitorFor(IProgressMonitor monitor) {
    return monitor == null ? new NullProgressMonitor() : monitor;
  }

  /** Closes a stream and ignores any resulting exception. */
  public static void safeClose(InputStream in) {
    try {
      if (in != null) in.close();
    } catch (IOException e) {
      // ignore
    }
  }

  /** Closes a stream and ignores any resulting exception. */
  public static void safeClose(OutputStream out) {
    try {
      if (out != null) out.close();
    } catch (IOException e) {
      // ignore
    }
  }

  public static IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks) {
    if (monitor == null) return new NullProgressMonitor();
    if (monitor instanceof NullProgressMonitor) return monitor;
    return new SubProgressMonitor(monitor, ticks);
  }
}
