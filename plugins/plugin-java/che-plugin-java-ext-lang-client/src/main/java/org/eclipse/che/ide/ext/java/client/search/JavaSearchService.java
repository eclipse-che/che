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
package org.eclipse.che.ide.ext.java.client.search;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesRequest;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesResponse;

/**
 * Client service for java related search
 *
 * @author Evgen Vidolob
 */
public interface JavaSearchService {

  /**
   * Invoke find usage request.
   *
   * @param request the request parameters, contains project and file paths and cursor position
   * @return find usages response
   */
  Promise<FindUsagesResponse> findUsages(FindUsagesRequest request);
}
