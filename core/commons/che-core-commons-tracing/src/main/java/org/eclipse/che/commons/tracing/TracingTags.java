/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.tracing;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.annotations.Beta;
import io.opentracing.Span;
import io.opentracing.tag.Tags;

/** The standard tags used in Che server. */
@Beta
public final class TracingTags {

  /** The id of the workspace the span is related to. */
  public static final AnnotationAwareStringTag WORKSPACE_ID =
      new AnnotationAwareStringTag("workspace.id");

  /** The name of the machine (container) the span is related to. */
  public static final AnnotationAwareStringTag MACHINE_NAME =
      new AnnotationAwareStringTag("machine.name");

  /** The id of the user the span is related to. */
  public static final AnnotationAwareStringTag USER_ID = new AnnotationAwareStringTag("user.id");

  /** The id of the stack the span is related to. */
  public static final AnnotationAwareStringTag STACK_ID = new AnnotationAwareStringTag("stack.id");

  /**
   * The entity that stopped workspace, which can be either user ID, or name of component that
   * stopped it (e.g. activity checker) .
   */
  public static final AnnotationAwareStringTag STOPPED_BY =
      new AnnotationAwareStringTag("stopped_by");

  /**
   * This is the standard {@link Tags#ERROR} "reexported" as an annotation aware tag so that it can
   * be easily set in the {@link org.eclipse.che.commons.annotation.Traced @Traced} methods.
   */
  public static final AnnotationAwareBooleanTag ERROR =
      new AnnotationAwareBooleanTag(Tags.ERROR.getKey());

  /** We can record the reason for an error in this tag. */
  public static final AnnotationAwareStringTag ERROR_REASON =
      new AnnotationAwareStringTag("error.reason");

  /**
   * If some asynchronous job has been cancelled due to some reason (but itself didn't fail) one can
   * use this tag instead of the "error" tag.
   */
  public static final AnnotationAwareBooleanTag CANCELLED =
      new AnnotationAwareBooleanTag("cancelled");

  /** A place to report the reason for the cancellation as a tag on a span */
  public static final AnnotationAwareStringTag CANCELLED_REASON =
      new AnnotationAwareStringTag("cancelled.reason");

  /**
   * This is the standard {@link Tags#SAMPLING_PRIORITY} "reexported" as an annotation aware tag so
   * that it can be easily set in the {@link org.eclipse.che.commons.annotation.Traced @Traced}
   * methods.
   */
  public static final AnnotationAwareIntTag SAMPLING_PRIORITY =
      new AnnotationAwareIntTag(Tags.SAMPLING_PRIORITY.getKey());

  /** Set error status and associated tags on a span, given a throwable */
  public static void setErrorStatus(Span span, Throwable e) {
    TracingTags.ERROR.set(span, true);
    TracingTags.ERROR_REASON.set(span, firstNonNull(e.getMessage(), "Unknown reason"));
    TracingTags.SAMPLING_PRIORITY.set(span, 1);
  }

  private TracingTags() {}
}
