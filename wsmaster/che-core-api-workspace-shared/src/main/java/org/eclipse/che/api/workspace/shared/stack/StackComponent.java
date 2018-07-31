/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.shared.stack;

/**
 * Defines the interface that describes the stack component. It is a part of the {@link Stack}.
 *
 * @author Alexander Andrienko
 */
public interface StackComponent {

  /** Returns the name of the component. The name is unique per stack. (e.g. "jdk"). */
  String getName();

  /** Returns the version of the component. (e.g. "1.8") */
  String getVersion();
}
