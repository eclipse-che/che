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
package org.eclipse.che.api.debug.shared.model;

import java.util.List;
import org.eclipse.che.commons.annotation.Nullable;

/** @author Anatoliy Bazko */
public interface ThreadState {

  /** Returns thread name. */
  String getName();

  /** Returns thread unique id. */
  long getId();

  /** Returns thread group name. */
  @Nullable
  String getGroupName();

  /** Returns list of frames of the thread. */
  List<? extends StackFrameDump> getFrames();

  /** Returns thread status. */
  ThreadStatus getStatus();

  /** Indicates if thread is suspended. */
  boolean isSuspended();
}
