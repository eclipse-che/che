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
package org.eclipse.che.plugin.testing.ide.model.info;

import org.eclipse.che.plugin.testing.ide.model.Printable;
import org.eclipse.che.plugin.testing.ide.model.Printer;

/**
 * Base class for all test states (in progress, ignored, passed etc), extenders should add messages,
 * stacktrace etc.
 */
public abstract class AbstractTestStateInfo implements Printable, TestStateInfo {

  @Override
  public void print(Printer printer) {}
}
