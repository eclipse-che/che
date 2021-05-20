/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import java.util.Map;
import org.eclipse.che.api.core.ValidationException;

/**
 * Adds an ability to extends logic of workspace attributes validation.
 *
 * <p>It may needed since attributes may be used as configuration storage by different components.
 * And that components may need to validate attributes.
 *
 * @author Sergii Leshchenko
 */
public interface WorkspaceAttributeValidator {

  /**
   * Validates if the specified workspace attributes does not contain invalid attributes according
   * to implementors rules.
   *
   * @param attributes workspace attributes to validate
   * @throws ValidationException when the specified workspace attributes is not valid
   */
  void validate(Map<String, String> attributes) throws ValidationException;

  /**
   * Validates if the specified workspace attributes can be updated with new values.
   *
   * <p>Note that this method must not allow updates that would not validate using the {@link
   * #validate(Map)} method.
   *
   * <p>This check includes:
   *
   * <ul>
   *   <li>format checking;
   *   <li>checking if unmodifiable attributes are not changed,<br>
   *       if they are two scenarios depending on the implementation:
   *       <ul>
   *         <li>error is thrown;
   *         <li>the needed value is provisioned into update;
   *       </ul>
   * </ul>
   *
   * @param existing workspace attributes to validate
   * @param update new workspace attributes
   * @throws ValidationException when the specified workspace attributes is not valid
   */
  void validateUpdate(Map<String, String> existing, Map<String, String> update)
      throws ValidationException;
}
