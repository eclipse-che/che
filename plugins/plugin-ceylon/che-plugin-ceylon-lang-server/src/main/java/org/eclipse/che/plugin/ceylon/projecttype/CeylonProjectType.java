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
package org.eclipse.che.plugin.ceylon.projecttype;

import com.google.inject.Inject;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.plugin.ceylon.shared.Constants;

/**
 * Ceylon project type
 *
 * @author David Festal
 */
public class CeylonProjectType extends ProjectTypeDef {
  @Inject
  public CeylonProjectType() {
    super(Constants.CEYLON_PROJECT_TYPE_ID, "Ceylon", true, false, true);
    addConstantDefinition(Constants.LANGUAGE, "language", Constants.CEYLON_LANG);
  }
}
