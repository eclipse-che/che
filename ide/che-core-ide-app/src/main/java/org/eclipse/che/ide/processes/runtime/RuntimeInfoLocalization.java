/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
