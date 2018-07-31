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
package org.eclipse.che.ide.ext.java.client.settings.service;

import com.google.inject.ImplementedBy;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;

/**
 * Provides methods which allow send requests to special CompilerService to get ability setup
 * compiler.
 *
 * @author Dmitry Shnurenko
 */
@ImplementedBy(SettingsServiceClientImpl.class)
public interface SettingsServiceClient {

  /**
   * Sends changed parameters to special compiler service which applies them for current project.
   *
   * @param parameters parameters which will be applied
   * @return an instance of {@link Promise} which contains response.
   */
  Promise<Void> applyCompileParameters(@NotNull Map<String, String> parameters);

  /**
   * Sends special request to CompilerService to get all compiler parameters.
   *
   * @return an instance of {@link Promise} which contains response with parameters.
   */
  Promise<Map<String, String>> getCompileParameters();
}
