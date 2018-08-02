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
package org.eclipse.che.ide.processes.runtime;

import com.google.gwt.i18n.client.Messages;

/**
 * Localization constants.
 *
 * @author Vlad Zhukovskyi
 * @since 5.18.0
 */
public interface RuntimeInfoLocalization extends Messages {

  @DefaultMessage("Servers")
  String showInfoActionTitle();

  @DefaultMessage("Show servers for the selected machine")
  String showInfoActionDescription();

  @DefaultMessage("Servers of {0}:")
  String cellTableCaption(String machineName);

  @DefaultMessage("Reference")
  String cellTableReferenceColumn();

  @DefaultMessage("Port")
  String cellTablePortColumn();

  @DefaultMessage("Protocol")
  String cellTableProtocolColumn();

  @DefaultMessage("URL")
  String cellTableUrlColumn();

  @DefaultMessage("Servers")
  String infoTabTitle();
}
