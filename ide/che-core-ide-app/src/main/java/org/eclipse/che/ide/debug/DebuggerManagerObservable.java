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
package org.eclipse.che.ide.debug;

/** @author Anatoliy Bazko */
public interface DebuggerManagerObservable {

  /** Adds observer. */
  void addObserver(DebuggerManagerObserver observer);

  /** Removes observer. */
  void removeObserver(DebuggerManagerObserver observer);
}
