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
package org.eclipse.che.ide.ext.java.client.navigation.service;

import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;
import org.eclipse.che.ide.ext.java.shared.dto.model.MethodParameters;
import org.eclipse.che.ide.resource.Path;

/**
 * Service for the operations of navigation.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
public interface JavaNavigationService {
  Promise<List<JavaProject>> getProjectsAndPackages(boolean includePackage);

  Promise<List<MethodParameters>> getMethodParametersHints(
      Path project, String fqn, int offset, int lineStartOffset);
}
