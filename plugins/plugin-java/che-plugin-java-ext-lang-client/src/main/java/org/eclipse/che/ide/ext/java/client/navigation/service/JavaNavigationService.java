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
package org.eclipse.che.ide.ext.java.client.navigation.service;

import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;

/**
 * Service for the operations of navigation.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
public interface JavaNavigationService {
  Promise<List<JavaProject>> getProjectsAndPackages(boolean includePackage);
}
