/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.system.server;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import javax.inject.Singleton;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.ZipUtils;

/** The class that allow getting different diagnostic information from the current JVM. */
@Singleton
public class JvmManager {

  private static final DateFormat MILLIS_FORMAT = new SimpleDateFormat("mm:ss:SSS");
  private final ThreadMXBean threadMxBean;
  private final HotSpotDiagnosticMXBean hotSpotMxBean;

  public JvmManager() {
    threadMxBean = ManagementFactory.getThreadMXBean();
    hotSpotMxBean = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
  }

  /**
   * Write thread dump that contains stack traces of existed threads, plus it contains some other
   * diagnostic information about threads such as: daemon, priority, etc.
   */
  public void writeThreadDump(OutputStream outputStream) throws IOException {
    ThreadInfo[] threadInfos =
        threadMxBean.getThreadInfo(threadMxBean.getAllThreadIds(), Integer.MAX_VALUE);
    Map<Long, ThreadInfo> threadInfoMap = new HashMap<>();
    for (ThreadInfo threadInfo : threadInfos) {
      threadInfoMap.put(threadInfo.getThreadId(), threadInfo);
    }

    try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
      Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();

      writer.write(String.format("Dump of %d threads at %Tc\n", stacks.size(), new Date()));
      for (Map.Entry<Thread, StackTraceElement[]> entry : stacks.entrySet()) {
        Thread thread = entry.getKey();
        writer.write(
            String.format(
                "\"%s\" prio=%d tid=%d state=%s daemon=%s\n",
                thread.getName(),
                thread.getPriority(),
                thread.getId(),
                thread.getState(),
                thread.isDaemon()));
        ThreadInfo threadInfo = threadInfoMap.get(thread.getId());
        if (threadInfo != null) {
          writer.write(
              String.format(
                  "    native=%s, suspended=%s, block=%d, wait=%s\n",
                  threadInfo.isInNative(),
                  threadInfo.isSuspended(),
                  threadInfo.getBlockedCount(),
                  threadInfo.getWaitedCount()));
          writer.write(
              String.format(
                  "    lock=%s owned by %s (%s), cpu=%s, user=%s\n",
                  threadInfo.getLockName(),
                  threadInfo.getLockOwnerName(),
                  threadInfo.getLockOwnerId(),
                  MILLIS_FORMAT.format(
                      new Date(threadMxBean.getThreadCpuTime(threadInfo.getThreadId()) / 1000000L)),
                  MILLIS_FORMAT.format(
                      new Date(
                          threadMxBean.getThreadUserTime(threadInfo.getThreadId()) / 1000000L))));
        }
        for (StackTraceElement element : entry.getValue()) {
          writer.append("        ").append(element.toString()).append(System.lineSeparator());
        }
        writer.write(System.lineSeparator());
      }
      writer.write("------------------------------------------------------");
      writer.write(System.lineSeparator());
      writer.write("Non-daemon threads: ");
      writer.write(System.lineSeparator());

      for (Thread thread : stacks.keySet()) {
        if (!thread.isDaemon()) {
          writer
              .append("\"")
              .append(thread.getName())
              .append("\", ")
              .append(System.lineSeparator());
        }
      }
      writer.write("------------------------------------------------------");
      writer.write(System.lineSeparator());
      writer.write("Blocked threads: ");
      writer.write(System.lineSeparator());
      for (Thread thread : stacks.keySet()) {
        if (thread.getState() == Thread.State.BLOCKED) {
          writer
              .append("\"")
              .append(thread.getName())
              .append("\", ")
              .append(System.lineSeparator());
        }
      }
      writer.write("------------------------------------------------------");
    }
  }

  /** Create a file with a zipped hprof heap dump. */
  public File createZippedHeapDump() throws IOException {
    File tmpFolder = Files.createTempDirectory("heapdump").toFile();
    File heapFile = new File(tmpFolder, "heapdump.hprof");
    hotSpotMxBean.dumpHeap(heapFile.getAbsolutePath(), false);
    File zip = File.createTempFile("heapdump", ".zip");
    try (ZipOutputStream result =
        new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zip)))) {
      ZipUtils.add(result, heapFile.toPath());
    }
    IoUtil.deleteRecursive(tmpFolder);
    return zip;
  }
}
