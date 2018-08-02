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
package org.eclipse.che.ide.api.editor.signature;

import com.google.common.base.Optional;
import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * Represents the signature of something callable. A signature can have label, like method name, a
 * documentation and list of parameters
 *
 * @author Evgen Vidolob
 */
public interface SignatureInfo {
  /**
   * The label of this signature.
   *
   * @return
   */
  @NotNull
  String getLabel();

  /**
   * The documentation of this signature
   *
   * @return
   */
  Optional<String> getDocumentation();

  /**
   * The parameters of this signature.
   *
   * @return
   */
  Optional<List<ParameterInfo>> getParameters();
}
