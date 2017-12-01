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
package org.eclipse.che.workspace.infrastructure.docker;

import java.util.List;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.infrastructure.docker.client.json.ContainerListEntry;

/** @author Alexander Garagatyi */
public interface DockerRuntimeFactory {
  DockerInternalRuntime create(DockerRuntimeContext context, List<Warning> warnings);

  DockerInternalRuntime create(
      DockerRuntimeContext context, List<ContainerListEntry> containers, List<Warning> warnings);
}
