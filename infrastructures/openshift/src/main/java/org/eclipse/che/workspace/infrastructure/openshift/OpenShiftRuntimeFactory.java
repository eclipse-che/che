/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift;

import java.util.List;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;

/** @author Sergii Leshchenko */
public interface OpenShiftRuntimeFactory {
  OpenShiftInternalRuntime create(
      OpenShiftRuntimeContext context, OpenShiftProject namespace, List<Warning> warnings);
}
