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
