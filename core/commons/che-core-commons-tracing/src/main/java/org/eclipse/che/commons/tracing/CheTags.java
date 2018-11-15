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
package org.eclipse.che.commons.tracing;

import io.opentracing.tag.Tags;

/** The standard tags used in Che server. */
public final class CheTags {

  /** The id of the workspace the span is related to. */
  public static final AnnotationAwareStringTag WORKSPACE_ID =
      new AnnotationAwareStringTag("workspace.id");

  /** The name of the machine (container) the span is related to. */
  public static final AnnotationAwareStringTag MACHINE_NAME =
      new AnnotationAwareStringTag("machine.name");

  /**
   * This is the standard {@link Tags#ERROR} "reexported" as an annotation aware tag so that it can
   * be easily set in the {@link org.eclipse.che.commons.annotation.Traced @Traced} methods.
   */
  public static final AnnotationAwareBooleanTag ERROR =
      new AnnotationAwareBooleanTag(Tags.ERROR.getKey());

  /**
   * This is the standard {@link Tags#SAMPLING_PRIORITY} "reexported" as an annotation aware tag so
   * that it can be easily set in the {@link org.eclipse.che.commons.annotation.Traced @Traced}
   * methods.
   */
  public static final AnnotationAwareIntTag SAMPLING_PRIORITY =
      new AnnotationAwareIntTag(Tags.SAMPLING_PRIORITY.getKey());

  private CheTags() {}
}
