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
package org.eclipse.che.ide.api.editor.signature;

import com.google.common.base.Optional;
import javax.validation.constraints.NotNull;

/**
 * Represent a parameter of callable signature. Parameter can have label and optional documentation.
 *
 * @author Evgen Vidolob
 */
public interface ParameterInfo {

  /**
   * The label of this parameter. Used for UI.
   *
   * @return the parameter label.
   */
  @NotNull
  String getLabel();

  /**
   * The documentation of this parameter.
   *
   * @return the human-readable documentation string.
   */
  Optional<String> getDocumentation();
}
