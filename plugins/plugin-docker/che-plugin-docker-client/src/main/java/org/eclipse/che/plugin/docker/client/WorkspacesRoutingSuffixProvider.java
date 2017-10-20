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
package org.eclipse.che.plugin.docker.client;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Retrieves a routing suffix that can be used when building workspace agents external address with
 * a custom server evaluation strategy. This is similar to the 'che.docker.ip.external' property,
 * but specific to the workspaces and dynamic (which for example depend on the current user). So
 * this is mainly useful when workspaces are not hosted on the same machine as the main Che server
 * and workspace master.
 *
 * <p>The default implementation returns null. But it is expected that extensions, such alternate
 * docker connectors, would provide a convenient non-null value.
 *
 * @author David Festal
 */
@Singleton
public class WorkspacesRoutingSuffixProvider implements Provider<String> {
  @Inject
  public WorkspacesRoutingSuffixProvider() {}

  @Override
  @Nullable
  public String get() {
    return null;
  }
}
