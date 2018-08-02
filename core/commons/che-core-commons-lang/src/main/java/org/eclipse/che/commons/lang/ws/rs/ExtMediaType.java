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
package org.eclipse.che.commons.lang.ws.rs;

import javax.ws.rs.core.MediaType;

/**
 * Extended media type.
 *
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
public interface ExtMediaType {
  /** A {@code String} constant representing "{@value #APPLICATION_ZIP}" media type. */
  public static final String APPLICATION_ZIP = "application/zip";
  /** A {@link MediaType} constant representing "{@value #APPLICATION_ZIP}" media type. */
  public static final MediaType APPLICATION_ZIP_TYPE = new MediaType("application", "zip");
  /** A {@code String} constant representing "{@value #APPLICATION_X_TAR}" media type. */
  String APPLICATION_X_TAR = "application/x-tar";
  /** A {@link MediaType} constant representing "{@value #APPLICATION_X_TAR}" media type. */
  MediaType APPLICATION_X_TAR_TYPE = new MediaType("application", "x-tar");
}
