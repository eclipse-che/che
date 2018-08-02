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
package org.eclipse.che.plugin.java.server.rest;

import com.google.inject.ImplementedBy;
import java.util.List;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Interface for the service which gets information about classpath.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(ClasspathService.class)
public interface ClasspathServiceInterface {
  List<ClasspathEntryDto> getClasspath(String projectPath) throws JavaModelException;
}
