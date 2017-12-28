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
package org.eclipse.che.plugin.java.plain.server.projecttype;

import static org.eclipse.che.ide.ext.java.shared.Constants.JAVAC;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;

/**
 * Init handler for simple java project.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
public class PlainJavaInitHandler implements ProjectInitHandler {
  @Override
  public String getProjectType() {
    return JAVAC;
  }

  @Override
  public void onProjectInitialized(String projectFolder)
      throws ServerException, ForbiddenException, ConflictException, NotFoundException {}
}
