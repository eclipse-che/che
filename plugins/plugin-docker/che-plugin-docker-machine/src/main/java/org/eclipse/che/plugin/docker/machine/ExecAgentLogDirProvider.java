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
package org.eclipse.che.plugin.docker.machine;

import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Provider for the log directory of the exec agent.
 *
 * <p>Returns a default, but is expected to be overridden in bound subclasses
 *
 * @author David Festal
 */
@Singleton
public class ExecAgentLogDirProvider implements Provider<String> {
  @Override
  public String get() {
    return "$HOME/che/exec-agent/logs";
  }
}
