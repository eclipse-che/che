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
package org.eclipse.che.plugin.web.vue;

import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.plugin.web.shared.Constants;

/**
 * Vue project type definition
 *
 * @author Sébastien Demanou
 */
public class VueProjectType extends ProjectTypeDef {

  public VueProjectType() {
    super(Constants.VUE_PROJECT_TYPE_ID, "Vue project", true, false, true);
    addConstantDefinition(Constants.LANGUAGE, "language", Constants.VUE_LANG);
  }
}
