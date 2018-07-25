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
package org.eclipse.che.api.factory.shared;

/**
 * Constants for Factory API.
 *
 * @author Anton Korneta
 */
public final class Constants {

  // factory links rel attributes
  public static final String IMAGE_REL_ATT = "image";
  public static final String RETRIEVE_FACTORY_REL_ATT = "self";
  public static final String SNIPPET_REL_ATT = "snippet";
  public static final String FACTORY_ACCEPTANCE_REL_ATT = "accept";
  public static final String NAMED_FACTORY_ACCEPTANCE_REL_ATT = "accept-named";

  // factory snippet types
  public static final String MARKDOWN_SNIPPET_TYPE = "markdown";
  public static final String IFRAME_SNIPPET_TYPE = "iframe";
  public static final String HTML_SNIPPET_TYPE = "html";
  public static final String URL_SNIPPET_TYPE = "url";

  private Constants() {}
}
