/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.server.projecttype.handler;

import java.util.Map;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.type.AttributeValue;

/** @author Vitaly Parfonov */
public interface GeneratorStrategy {

  String getId();

  void generateProject(
      String projectPath, Map<String, AttributeValue> attributes, Map<String, String> options)
      throws ForbiddenException, ConflictException, ServerException, NotFoundException;
}
