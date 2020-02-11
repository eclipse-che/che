package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEvent;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.event.PodEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogWatcher implements PodEventHandler, Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(LogWatcher.class);

  private static final ExecutorService pool = Executors.newFixedThreadPool(10);
  private final String namespace;
  private final KubernetesClient client;
  private PodLogHandler logHandler;
  private final List<LogWatch> lw = new ArrayList<>();

  public LogWatcher(KubernetesClientFactory clientFactory, String workspaceId, String namespace,
      PodLogHandler handler)
      throws InfrastructureException {
    this.logHandler = handler;
    this.client = clientFactory.create(workspaceId);
    this.namespace = namespace;
  }

  @Override
  public void handle(PodEvent event) {
    String podName = event.getPodName();
    String containerName = event.getContainerName();
    String reason = event.getReason();

    LOG.info("[{}][{}] reason [{}]", podName, containerName, reason);
    // TODO: must handle failure
    if (containerName != null && "Started".equals(reason)) {
      pool.submit(() -> {
        try {
          // TODO: must handle failure
          if (!client.pods().inNamespace(namespace).withName(podName).isReady()) {
            LOG.info("pod [{}] not ready, waiting ...", podName);
            client.pods().inNamespace(namespace).withName(podName).waitUntilReady(30, TimeUnit.SECONDS);
            LOG.info("pod [{}] is ready now, go go go !!!", podName);
          }
          client.pods().inNamespace(namespace).withName(podName).inContainer(containerName)
              .redirectingOutput();
          LogWatch log = client.pods().inNamespace(namespace).withName(podName)
              .inContainer(containerName).watchLog();
          lw.add(log);
          logHandler.handle(
              log,
              podName,
              containerName);
        } catch (IOException | InterruptedException e) {
          throw new RuntimeException(e);
        }
      });
    }
  }

  @Override
  public void close() throws IOException {
    LOG.info("cleaning logwatches");
    lw.forEach(LogWatch::close);
    lw.clear();
  }
}
