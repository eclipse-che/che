package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.log;

/** Various timeouts used in watching workspace logs logic. All values are in milliseconds. */
public class LogWatchTimeouts {
  private final long watchTimeoutMs;
  private final long waitBetweenTriesMs;
  private final long waitBeforeCleanupMs;

  public LogWatchTimeouts(long watchTimeoutMs, long waitBetweenTriesMs, long waitBeforeCleanupMs) {
    this.watchTimeoutMs = watchTimeoutMs;
    this.waitBetweenTriesMs = waitBetweenTriesMs;
    this.waitBeforeCleanupMs = waitBeforeCleanupMs;
  }

  /**
   * How long we should try watch the logs.
   *
   * @return timeout in ms
   */
  public long getWatchTimeoutMs() {
    return watchTimeoutMs;
  }

  /**
   * How long to block cleanup to get all container logs.
   *
   * @return timeout in ms
   */
  public long getWaitBeforeCleanupMs() {
    return waitBeforeCleanupMs;
  }

  /**
   * How long to wait between individual tries to get container logs.
   *
   * @return timeout in ms
   */
  public long getWaitBeforeNextTry() {
    return waitBetweenTriesMs;
  }
}
