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
