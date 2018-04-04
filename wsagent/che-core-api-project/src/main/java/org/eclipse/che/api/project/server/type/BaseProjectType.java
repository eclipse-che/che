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
package org.eclipse.che.api.project.server.type;

import static org.eclipse.che.api.project.shared.Constants.COMMANDS_ATTRIBUTE_DESCRIPTION;
import static org.eclipse.che.api.project.shared.Constants.COMMANDS_ATTRIBUTE_NAME;

import javax.inject.Singleton;

/** @author gazarenkov */
@Singleton
public class BaseProjectType extends ProjectTypeDef {

  public static final String ID = "blank";

  public BaseProjectType() {
    super(ID, "Blank", true, false);

    addVariableDefinition(COMMANDS_ATTRIBUTE_NAME, COMMANDS_ATTRIBUTE_DESCRIPTION, false);
  }
}
