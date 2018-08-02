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
package org.eclipse.che.ide.command.execute;

import com.google.gwt.i18n.client.Messages;

/**
 * I18n messages relate to the command execution.
 *
 * @author Artem Zatsarynnyi
 */
public interface ExecMessages extends Messages {

  @Key("action.commands.title")
  String actionCommandsTitle();
}
