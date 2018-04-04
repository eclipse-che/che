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
