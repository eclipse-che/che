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
package org.eclipse.che.plugin.web.typescript;

import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.plugin.web.shared.Constants;

/** TypeScript project type definition */
public class TypeScriptProjectType extends ProjectTypeDef {

  public TypeScriptProjectType() {
    super(Constants.TS_PROJECT_TYPE_ID, "TypeScript project", true, false, true);
    addConstantDefinition(Constants.LANGUAGE, "language", Constants.TS_LANG);
  }
}
