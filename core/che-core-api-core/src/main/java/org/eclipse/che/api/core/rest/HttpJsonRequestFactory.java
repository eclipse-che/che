/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.rest;

import com.google.common.annotations.Beta;
import com.google.inject.ImplementedBy;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.rest.shared.dto.Link;

/**
 * Factory for {@link HttpJsonRequest} instances.
 *
 * @author Yevhenii Voevodin
 */
@Beta
@ImplementedBy(DefaultHttpJsonRequestFactory.class)
public interface HttpJsonRequestFactory {

  /**
   * Creates {@link HttpJsonRequest} based on {@code url}.
   *
   * @param url request url
   * @return new instance of {@link HttpJsonRequest}
   * @throws NullPointerException when url is null
   */
  HttpJsonRequest fromUrl(@NotNull String url);

  /**
   * Crates {@link HttpJsonRequest} based on {@code link}.
   *
   * @param link request link
   * @return new instance of {@link HttpJsonRequest}
   * @throws NullPointerException when link is null
   */
  HttpJsonRequest fromLink(@NotNull Link link);
}
