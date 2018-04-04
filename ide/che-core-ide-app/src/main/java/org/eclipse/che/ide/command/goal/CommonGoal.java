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
package org.eclipse.che.ide.command.goal;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.command.BaseCommandGoal;

/**
 * Represents predefined 'Common' goal. By default it's used for grouping commands which doesn't
 * belong to any goal.
 *
 * @author Artem Zatsarynnyi
 */
public class CommonGoal extends BaseCommandGoal {

  @Inject
  public CommonGoal() {
    super("Common");
  }
}
