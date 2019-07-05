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
package org.eclipse.che.plugin.java.plain.client.service;

import com.google.inject.ImplementedBy;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;

/**
 * Interface for the service which updates classpath.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(ClasspathUpdaterServiceClientImpl.class)
public interface ClasspathUpdaterServiceClient {

  /**
   * Updates classpath.
   *
   * @param projectPath path to the current project
   * @param entries list of the classpath entries
   */
  Promise<Void> setRawClasspath(String projectPath, List<ClasspathEntry> entries);
}
