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
package io.tonlabs.projecttype;

import com.google.inject.Inject;
import io.tonlabs.shared.Constants;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;

/** The TON C project type. */
public class TonProjectProjectType extends ProjectTypeDef {

  /** Constructor for the JSON example project type. */
  @Inject
  public TonProjectProjectType() {
    super(Constants.TON_C_PROJECT_TYPE_ID, Constants.TON_C_PROJECT_CAPTION, true, false);
    this.addConstantDefinition(Constants.LANGUAGE, Constants.LANGUAGE, Constants.TON_C_LANG);
  }
}
