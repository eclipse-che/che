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
