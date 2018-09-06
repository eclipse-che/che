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
package org.eclipse.che.plugin.languageserver.ide.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.lsp4j.ExecuteCommandParams;

/**
 * A processor for incoming custom command requests sent by a language server.
 *
 * @author V. Rubezhny
 */
@Singleton
public class ExecuteClientCommandProcessor {

  @Inject
  public ExecuteClientCommandProcessor() {}

  public void execute(ExecuteCommandParams params) {}
}
