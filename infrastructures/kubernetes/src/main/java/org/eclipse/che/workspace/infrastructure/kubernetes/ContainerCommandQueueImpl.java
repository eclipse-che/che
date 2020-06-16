package org.eclipse.che.workspace.infrastructure.kubernetes;

import java.util.LinkedList;
import java.util.Queue;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContainerCommandQueueImpl implements ContainerCommandQueue {

  private static final Logger LOG = LoggerFactory.getLogger(ContainerCommandQueueImpl.class);

  private final Queue<ContainerCommand> commands = new LinkedList<>();
  private final KubernetesNamespace namespace;

  public ContainerCommandQueueImpl(
      KubernetesNamespace namespace) {
    this.namespace = namespace;
  }

  @Override
  public void add(String pod, String container, String command) {
    LOG.info("should execute '{}' on '{} : {}'", command, pod, container);
    commands.add(new ContainerCommand(pod, container, command));
  }

  @Override
  public void execute() {
    ContainerCommand command;
    while ((command = commands.poll()) != null) {
      try {
        LOG.info("execute '{}' on '{} : {}'", command.command, command.pod, command.container);
        namespace.deployments().exec(command.pod, command.container, 10,
            command.command.split(" "), (s, s2) -> LOG.info("[{}] {}", s, s2));
      } catch (InfrastructureException e) {
        e.printStackTrace();
      }
    }
  }

  private static final class ContainerCommand {

    private final String pod;
    private final String container;
    private final String command;

    private ContainerCommand(String pod, String container, String command) {
      this.pod = pod;
      this.container = container;
      this.command = command;
    }
  }
}
