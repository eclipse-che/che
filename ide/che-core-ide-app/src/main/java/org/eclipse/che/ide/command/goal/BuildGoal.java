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
package org.eclipse.che.ide.command.goal;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.command.BaseCommandGoal;

/**
 * Represents predefined 'Build' goal.
 *
 * @author Artem Zatsarynnyi
 */
public class BuildGoal extends BaseCommandGoal {

  @Inject
  public BuildGoal() {
    super("Build");
  }
}
