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
package org.eclipse.che.selenium.core.requestfactory;

import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.shared.dto.Link;

/** @author Dmytro Nochevnov */
public abstract class TestHttpJsonRequestFactory extends DefaultHttpJsonRequestFactory {
  @Override
  public HttpJsonRequest fromUrl(@NotNull String url) {
    return super.fromUrl(url).setAuthorizationHeader(getAuthToken());
  }

  @Override
  public HttpJsonRequest fromLink(@NotNull Link link) {
    return super.fromLink(link).setAuthorizationHeader(getAuthToken());
  }

  protected abstract String getAuthToken();
}
