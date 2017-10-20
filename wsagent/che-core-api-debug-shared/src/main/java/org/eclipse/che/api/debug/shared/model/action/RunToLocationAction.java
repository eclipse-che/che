/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.debug.shared.model.action;

/** @author Igor Vinokur */
public interface RunToLocationAction extends Action {

  /** Returns target file path */
  String getTarget();

  /** Returns specified line number */
  int getLineNumber();
}
