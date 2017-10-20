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
package org.eclipse.che.api.debug.shared.model.impl.action;

import org.eclipse.che.api.debug.shared.model.action.RunToLocationAction;

/** @author Igor Vinokur */
public class RunToLocationActionImpl extends ActionImpl implements RunToLocationAction {

  private final String target;
  private final int lineNumber;

  public RunToLocationActionImpl(String target, int lineNumber) {
    super(TYPE.RUN_TO_LOCATION);
    this.target = target;
    this.lineNumber = lineNumber;
  }

  @Override
  public String getTarget() {
    return target;
  }

  @Override
  public int getLineNumber() {
    return lineNumber;
  }
}
