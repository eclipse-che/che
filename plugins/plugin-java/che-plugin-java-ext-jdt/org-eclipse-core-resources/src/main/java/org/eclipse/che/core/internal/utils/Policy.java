/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.core.internal.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

public class Policy {
  //	public static final DebugOptionsListener RESOURCES_DEBUG_OPTIONS_LISTENER = new
  // DebugOptionsListener() {
  //		public void optionsChanged(DebugOptions options) {
  //			DEBUG = options.getBooleanOption(ResourcesPlugin.PI_RESOURCES + "/debug", false);
  // //$NON-NLS-1$
  //
  //			DEBUG_AUTO_REFRESH = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES +
  // "/refresh", false); //$NON-NLS-1$
  //
  //			DEBUG_BUILD_DELTA = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES +
  // "/build/delta", false); //$NON-NLS-1$
  //			DEBUG_BUILD_FAILURE = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES +
  // "/build/failure", false); //$NON-NLS-1$
  //			DEBUG_BUILD_INTERRUPT =
  //					DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES + "/build/interrupt",
  // false); //$NON-NLS-1$
  //			DEBUG_BUILD_INVOKING = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES +
  // "/build/invoking", false);
  //			//$NON-NLS-1$
  //			DEBUG_BUILD_NEEDED = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES +
  // "/build/needbuild", false); //$NON-NLS-1$
  //			DEBUG_BUILD_NEEDED_STACK =
  //					DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES + "/build/needbuildstack",
  // false); //$NON-NLS-1$
  //			DEBUG_BUILD_STACK = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES +
  // "/build/stacktrace", false); //$NON-NLS-1$
  //
  //			DEBUG_CONTENT_TYPE = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES +
  // "/contenttype", false); //$NON-NLS-1$
  //			DEBUG_CONTENT_TYPE_CACHE =
  //					DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES + "/contenttype/cache",
  // false); //$NON-NLS-1$
  //			DEBUG_HISTORY = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES + "/history",
  // false); //$NON-NLS-1$
  //			DEBUG_NATURES = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES + "/natures",
  // false); //$NON-NLS-1$
  //			DEBUG_NOTIFICATIONS = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES +
  // "/notifications", false); //$NON-NLS-1$
  //			DEBUG_PREFERENCES = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES +
  // "/preferences", false); //$NON-NLS-1$
  //
  //			DEBUG_RESTORE = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES + "/restore",
  // false); //$NON-NLS-1$
  //			DEBUG_RESTORE_MARKERS =
  //					DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES + "/restore/markers",
  // false); //$NON-NLS-1$
  //			DEBUG_RESTORE_MASTERTABLE =
  //					DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES + "/restore/mastertable",
  // false); //$NON-NLS-1$
  //			DEBUG_RESTORE_METAINFO =
  //					DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES + "/restore/metainfo",
  // false); //$NON-NLS-1$
  //			DEBUG_RESTORE_SNAPSHOTS =
  //					DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES + "/restore/snapshots",
  // false); //$NON-NLS-1$
  //			DEBUG_RESTORE_SYNCINFO =
  //					DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES + "/restore/syncinfo",
  // false); //$NON-NLS-1$
  //			DEBUG_RESTORE_TREE = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES +
  // "/restore/tree", false); //$NON-NLS-1$
  //
  //			DEBUG_SAVE = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES + "/save",
  // false); //$NON-NLS-1$
  //			DEBUG_SAVE_MARKERS = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES +
  // "/save/markers", false); //$NON-NLS-1$
  //			DEBUG_SAVE_MASTERTABLE =
  //					DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES + "/save/mastertable",
  // false); //$NON-NLS-1$
  //			DEBUG_SAVE_METAINFO = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES +
  // "/save/metainfo", false); //$NON-NLS-1$
  //			DEBUG_SAVE_SYNCINFO = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES +
  // "/save/syncinfo", false); //$NON-NLS-1$
  //			DEBUG_SAVE_TREE = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES +
  // "/save/tree", false); //$NON-NLS-1$
  //
  //			DEBUG_STRINGS = DEBUG && options.getBooleanOption(ResourcesPlugin.PI_RESOURCES + "/strings",
  // false); //$NON-NLS-1$
  //		}
  //	};

  public static final boolean buildOnCancel = false;
  // general debug flag for the plugin
  public static boolean DEBUG = false;

  public static boolean DEBUG_AUTO_REFRESH = false;

  // debug constants
  public static boolean DEBUG_BUILD_DELTA = false;
  public static boolean DEBUG_BUILD_FAILURE = false;
  public static boolean DEBUG_BUILD_INTERRUPT = false;
  public static boolean DEBUG_BUILD_INVOKING = false;
  public static boolean DEBUG_BUILD_NEEDED = false;
  public static boolean DEBUG_BUILD_NEEDED_STACK = false;
  public static boolean DEBUG_BUILD_STACK = false;

  public static boolean DEBUG_CONTENT_TYPE = false;
  public static boolean DEBUG_CONTENT_TYPE_CACHE = false;
  public static boolean DEBUG_HISTORY = false;
  public static boolean DEBUG_NATURES = false;
  public static boolean DEBUG_NOTIFICATIONS = false;
  public static boolean DEBUG_PREFERENCES = false;
  // Get timing information for restoring data
  public static boolean DEBUG_RESTORE = false;
  public static boolean DEBUG_RESTORE_MARKERS = false;
  public static boolean DEBUG_RESTORE_MASTERTABLE = false;

  public static boolean DEBUG_RESTORE_METAINFO = false;
  public static boolean DEBUG_RESTORE_SNAPSHOTS = false;
  public static boolean DEBUG_RESTORE_SYNCINFO = false;
  public static boolean DEBUG_RESTORE_TREE = false;
  // Get timing information for save and snapshot data
  public static boolean DEBUG_SAVE = false;
  public static boolean DEBUG_SAVE_MARKERS = false;
  public static boolean DEBUG_SAVE_MASTERTABLE = false;

  public static boolean DEBUG_SAVE_METAINFO = false;
  public static boolean DEBUG_SAVE_SYNCINFO = false;
  public static boolean DEBUG_SAVE_TREE = false;
  public static boolean DEBUG_STRINGS = false;
  public static int endOpWork = 1;
  public static final long MAX_BUILD_DELAY = 1000;

  public static final long MIN_BUILD_DELAY = 100;
  public static int opWork = 99;
  public static final int totalWork = 100;

  public static void checkCanceled(IProgressMonitor monitor) {
    if (monitor.isCanceled()) throw new OperationCanceledException();
  }

  /**
   * Print a debug message to the console. Prepend the message with the current date, the name of
   * the current thread and the current job if present.
   */
  public static void debug(String message) {
    StringBuilder output = new StringBuilder();
    output.append(new Date(System.currentTimeMillis()));
    output.append(" - ["); // $NON-NLS-1$
    output.append(Thread.currentThread().getName());
    output.append("] "); // $NON-NLS-1$
    Job currentJob = Job.getJobManager().currentJob();
    if (currentJob != null) {
      output.append(currentJob.getClass().getName());
      output.append("("); // $NON-NLS-1$
      output.append(currentJob.getName());
      output.append("): "); // $NON-NLS-1$
    }
    output.append(message);
    System.out.println(output.toString());
  }

  /** Print a debug throwable to the console. */
  public static void debug(Throwable t) {
    StringWriter writer = new StringWriter();
    t.printStackTrace(new PrintWriter(writer));
    String str = writer.toString();
    if (str.endsWith("\n")) // $NON-NLS-1$
    str = str.substring(0, str.length() - 2);
    debug(str);
  }

  public static void log(int severity, String message, Throwable t) {
    if (message == null) message = ""; // $NON-NLS-1$
    log(new Status(severity, ResourcesPlugin.getPluginId(), 1, message, t));
  }

  public static void log(IStatus status) {
    //		final Bundle bundle = Platform.getBundle(ResourcesPlugin.PI_RESOURCES);
    //		if (bundle == null)
    //			return;
    //		Platform.getLog(bundle).log(status);
    ResourcesPlugin.log(status);
  }

  /**
   * Logs a throwable, assuming severity of error
   *
   * @param t
   */
  public static void log(Throwable t) {
    log(IStatus.ERROR, "Internal Error", t); // $NON-NLS-1$
  }

  public static IProgressMonitor monitorFor(IProgressMonitor monitor) {
    return monitor == null ? new NullProgressMonitor() : monitor;
  }

  public static IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks) {
    if (monitor == null) return new NullProgressMonitor();
    if (monitor instanceof NullProgressMonitor) return monitor;
    return new SubProgressMonitor(monitor, ticks);
  }

  public static IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks, int style) {
    if (monitor == null) return new NullProgressMonitor();
    if (monitor instanceof NullProgressMonitor) return monitor;
    return new SubProgressMonitor(monitor, ticks, style);
  }
}
