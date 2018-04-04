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
