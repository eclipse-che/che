package org.eclipse.che.workspace.infrastructure.kubernetes;

public interface ContainerCommandQueue {
  void add(String pod, String container, String command);
  void execute();
}
