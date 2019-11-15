/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.ZipUtils;

/**
 * REST API for JVM manipulations.
 *
 * @author Sergii Kabashniuk
 */
@Api("/jvm")
@Path("/jvm")
public class JvmService {

  public static final DateFormat MILLIS_FORMAT = new SimpleDateFormat("mm:ss:SSS");

  @GET
  @Path("/dump/thread")
  @Produces(MediaType.TEXT_PLAIN)
  @ApiOperation("Get thread dump of jvm")
  @ApiResponses(@ApiResponse(code = 200, message = "The response contains thread dump"))
  public StreamingOutput threadDump() {
    return output -> {
      OutputStreamWriter writer = new OutputStreamWriter(output);
      ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
      ThreadInfo[] threadInfos = mxBean.getThreadInfo(mxBean.getAllThreadIds(), Integer.MAX_VALUE);
      Map<Long, ThreadInfo> threadInfoMap = new HashMap<>();
      for (ThreadInfo threadInfo : threadInfos) {
        threadInfoMap.put(threadInfo.getThreadId(), threadInfo);
      }

      try {
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
                        new Date(mxBean.getThreadCpuTime(threadInfo.getThreadId()) / 1000000L)),
                    MILLIS_FORMAT.format(
                        new Date(mxBean.getThreadUserTime(threadInfo.getThreadId()) / 1000000L))));
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
      } finally {
        writer.close();
      }
    };
  }

  @GET
  @Path("/dump/heap")
  @Produces("application/zip")
  @ApiOperation("Get heap dump of jvm")
  @ApiResponses(@ApiResponse(code = 200, message = "The response contains jvm heap dump"))
  public Response heapDump() throws IOException {
    HotSpotDiagnosticMXBean mxBean =
        ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
    java.nio.file.Path tmpDir = Files.createTempDirectory("heapdump");
    File heapFile = new File(tmpDir.toFile(), "heapdump.hprof");
    mxBean.dumpHeap(heapFile.getAbsolutePath(), false);
    File zip = new File(tmpDir.toFile(), "heapdump.hprof.zip");
    try (ZipOutputStream out = ZipUtils.stream(zip.toPath())) {
      out.setLevel(-1);
      ZipUtils.add(out, heapFile.toPath());
    }

    return Response.ok(
            new FileInputStream(zip) {
              @Override
              public void close() throws IOException {
                super.close();
                IoUtil.deleteRecursive(tmpDir.toFile());
              }
            },
            "application/zip")
        .header("Content-Length", String.valueOf(Files.size(zip.toPath())))
        .header(
            "Content-Disposition", "attachment; filename=" + zip.toPath().getFileName().toString())
        .build();
  }
}
