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
package org.eclipse.che.infrastructure.docker.client;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.jna.ptr.LongByReference;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.commons.lang.Size;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Docker container OOM detector based on cgroup usage
 *
 * @author Alexander Garagatyi
 */
public class CgroupOOMDetector implements DockerOOMDetector {
  private static final Logger LOG = LoggerFactory.getLogger(CgroupOOMDetector.class);

  private final Map<String, OOMDetector> oomDetectors;
  private final URI dockerDaemonUri;
  private final DockerConnector dockerConnector;
  private final ExecutorService executor;

  @Inject
  public CgroupOOMDetector(
      DockerConnectorConfiguration connectorConfiguration, DockerConnector docker) {
    this(connectorConfiguration.getDockerDaemonUri(), docker);
  }

  public CgroupOOMDetector(URI dockerDaemonUri, DockerConnector dockerConnector) {
    this.dockerDaemonUri = dockerDaemonUri;
    this.dockerConnector = dockerConnector;
    this.oomDetectors = new ConcurrentHashMap<>();
    this.executor =
        Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                .setNameFormat("CgroupOOMDetector-%d")
                .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                .setDaemon(true)
                .build());
  }

  @Override
  public void stopDetection(String container) {
    final OOMDetector oomDetector = oomDetectors.remove(container);
    if (oomDetector != null) {
      oomDetector.stop();
    }
  }

  @Override
  public void startDetection(String container, MessageProcessor<LogMessage> containerLogProcessor) {
    if (needStartOOMDetector(container)) {
      if (cgroupMount == null) {
        LOG.warn("System doesn't support OOM events");
        return;
      }
      try {
        final long memory =
            dockerConnector.inspectContainer(container).getConfig().getHostConfig().getMemory();
        OOMDetector oomDetector = new OOMDetector(container, containerLogProcessor, memory);
        oomDetectors.putIfAbsent(container, oomDetector);
        oomDetector = oomDetectors.get(container);
        oomDetector.start();
      } catch (IOException e) {
        LOG.error(e.getLocalizedMessage(), e);
      }
    }
  }

  private boolean needStartOOMDetector(String container) {
    if (!oomDetectors.containsKey(container)) {
      if (DockerConnectorConfiguration.isUnixSocketUri(dockerDaemonUri)) {
        return true;
      }
      if (SystemInfo.isLinux()) {
        final String dockerDaemonHost = dockerDaemonUri.getHost();
        if ("localhost".equals(dockerDaemonHost) || "127.0.0.1".equals(dockerDaemonHost)) {
          return true;
        }
      }
    }
    return false;
  }

  /*
   * Need detect OOM errors and notify users about them. Without such notification if application is killed by oom-killer client often can
   * see message "Killed" and there is no any why to see why. Unfortunately for now docker doesn't provide clear mechanism how to control
   * OOM errors, with docker event mechanism can get something like that:
   * {"status":"die","id":"dfdf82bd3881","from":"base:latest","time":1374067970}
   * That is not enough.
   * Found two ways how to control OOM errors.
   *
   *     1. With parsing output of 'dmesg' command
   * ----
   * andrew@andrey:~> dmesg | grep oom-killer
   * [41313.629018] java invoked oom-killer: gfp_mask=0xd0, order=0, oom_score_adj=0
   * [41631.391818] java invoked oom-killer: gfp_mask=0xd0, order=0, oom_score_adj=0
   * ...
   * -----
   * Problem here is in timestamp format. Unfortunately dmesg doesn't provide real time correctly with -T option. Here is a piece of man
   * page:
   * -----
   * -T, --ctime
   *         Print human readable timestamps.  The timestamp could be inaccurate!
   *
   *         The time source used for the logs is not updated after system SUSPEND/RESUME.
   * -----
   * So it's complicated to detect time when oom-killer was activated and link its activity with failed docker container.
   *
   *     2. Usage of cgroup notification mechanism.
   * Good article about this: https://access.redhat.com/documentation/en-US/Red_Hat_Enterprise_Linux/6/html/Resource_Management_Guide/sec-Using_the_Notification_API.html
   */
  private static String cgroupMount;
  private static boolean systemd;

  static {
    if (SystemInfo.isLinux()) {
      final String mounts = "/proc/mounts";
      try (BufferedReader reader =
          Files.newBufferedReader(Paths.get(mounts), Charset.forName("UTF-8"))) {
        String line;
        while ((line = reader.readLine()) != null) {
          String[] a = line.split("\\s+");
          // line has format: "DEVICE PATH FILESYSTEM FLAGS_DELIMITED_BY_COMMAS ??? ???"
          String filesystem = a[2];
          if ("cgroup".equals(filesystem)) {
            String path = a[1];
            if (path.endsWith("cpu")
                || path.endsWith("cpuacct")
                || path.endsWith("cpuset")
                || path.endsWith("memory")
                || path.endsWith("devices")
                || path.endsWith("freezer")) {
              cgroupMount = Paths.get(path).getParent().toString();
            } else if (path.endsWith("systemd")) {
              systemd = true;
            }
          }
        }
      } catch (IOException e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  /**
   * Detects OOM with cgroup notification mechanism.
   *
   * <p>https://access.redhat.com/documentation/en-US/Red_Hat_Enterprise_Linux/6/html/Resource_Management_Guide/sec-Using_the_Notification_API.html
   */
  private class OOMDetector implements Runnable {
    private final String container;
    private final MessageProcessor<LogMessage> containerLogProcessor;
    private final long memory;
    private final CLibrary cLib;
    private final String containerCgroup;

    private volatile boolean stopped = false;
    private boolean started = false;

    OOMDetector(String container, MessageProcessor<LogMessage> containerLogProcessor, long memory) {
      this.container = container;
      this.containerLogProcessor = containerLogProcessor;
      this.memory = memory;
      cLib = CLibraryFactory.getCLibrary();

      if (systemd) {
        containerCgroup = cgroupMount + "/memory/system.slice/docker-" + container + ".scope/";
      } else {
        containerCgroup = cgroupMount + "/memory/docker/" + container + "/";
      }
    }

    @Override
    public void run() {
      final String cf = containerCgroup + "cgroup.event_control";
      final String oomf = containerCgroup + "memory.oom_control";
      int efd = -1;
      int oomfd = -1;
      try {
        if ((efd = cLib.eventfd(0, 1)) == -1) {
          LOG.error("Unable create a file descriptor for event notification");
          return;
        }
        int cfd;
        if ((cfd = cLib.open(cf, CLibrary.O_WRONLY)) == -1) {
          LOG.error("Unable open event control file '{}' for write", cf);
          return;
        }
        if ((oomfd = cLib.open(oomf, CLibrary.O_RDONLY)) == -1) {
          LOG.error("Unable open OOM event file '{}' for read", oomf);
          return;
        }
        final byte[] data = String.format("%d %d", efd, oomfd).getBytes();
        if (cLib.write(cfd, data, data.length) != data.length) {
          LOG.error("Unable write event control data to file '{}'", cf);
          return;
        }
        if (cLib.close(cfd) == -1) {
          LOG.error("Error closing of event control file '{}'", cf);
          return;
        }
        final LongByReference eventHolder = new LongByReference();
        if (cLib.eventfd_read(efd, eventHolder) == 0) {
          if (stopped) {
            return;
          }
          LOG.warn("OOM event received for container '{}'", container);
          if (readCgroupValue("memory.failcnt") > 0) {
            try {
              containerLogProcessor.process(
                  new LogMessage(
                      LogMessage.Type.DOCKER,
                      "[ERROR] The processes in this machine need more RAM. This machine started with "
                          + Size.toHumanSize(memory)));
              containerLogProcessor.process(
                  new LogMessage(
                      LogMessage.Type.DOCKER,
                      "[ERROR] Create a new machine configuration that allocates additional RAM or increase"
                          + " the workspace RAM limit in the user dashboard."));
            } catch (/*IOException*/ Exception e) {
              LOG.warn(e.getMessage(), e);
            }
          }
        }
      } finally {
        if (!stopped) {
          stopDetection(container);
        }
        close(oomfd);
        close(efd);
      }
    }

    private void close(int fd) {
      if (fd != -1) {
        cLib.close(fd);
      }
    }

    long readCgroupValue(String cgroupFile) {
      final String failCntf = containerCgroup + cgroupFile;
      try (BufferedReader reader =
          Files.newBufferedReader(Paths.get(failCntf), Charset.forName("UTF-8"))) {
        return Long.parseLong(reader.readLine().trim());
      } catch (IOException e) {
        LOG.warn("Unable read content of file '{}'", failCntf);
      } catch (NumberFormatException e) {
        LOG.error("Unable parse content of file '{}'", failCntf);
      }
      return 0;
    }

    synchronized void start() {
      if (!started) {
        started = true;
        executor.execute(this);
      }
    }

    void stop() {
      stopped = true;
    }
  }
}
