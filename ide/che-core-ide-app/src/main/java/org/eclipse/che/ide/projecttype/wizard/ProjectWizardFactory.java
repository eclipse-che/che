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
package org.eclipse.che.ide.projecttype.wizard;

import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardMode;

/**
 * Helps to create new instances of {@link ProjectWizard}.
 *
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
public interface ProjectWizardFactory {
  ProjectWizard newWizard(MutableProjectConfig dataObject, ProjectWizardMode mode);
}
