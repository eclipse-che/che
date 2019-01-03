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
package org.eclipse.che.api.factory.shared;

/**
 * Constants for Factory API.
 *
 * @author Anton Korneta
 */
public final class Constants {

  public static final String CURRENT_VERSION = "4.0";

  // factory links rel attributes
  public static final String IMAGE_REL_ATT = "image";
  public static final String RETRIEVE_FACTORY_REL_ATT = "self";
  public static final String FACTORY_ACCEPTANCE_REL_ATT = "accept";
  public static final String NAMED_FACTORY_ACCEPTANCE_REL_ATT = "accept-named";

  // url factory parameter names
  public static final String URL_PARAMETER_NAME = "url";

  private Constants() {}
}
