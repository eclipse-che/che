package org.eclipse.che.commons.tracing;

import io.opentracing.Span;

public final class TracingUtil {

  public static void setErrorStatus(Span span, Throwable e) {
    TracingTags.ERROR.set(span, true);
    TracingTags.ERROR_REASON.set(span, e.getMessage());
    TracingTags.SAMPLING_PRIORITY.set(span, 1);
  }

  public static void setWorkspaceIdAndMachineName(
      Span span, String workspaceId, String machineName) {
    TracingTags.WORKSPACE_ID.set(span, workspaceId);
    TracingTags.MACHINE_NAME.set(span, machineName);
  }

  public static void setWorkspaceId(Span span, String workspaceId) {
    TracingTags.WORKSPACE_ID.set(span, workspaceId);
  }
}
