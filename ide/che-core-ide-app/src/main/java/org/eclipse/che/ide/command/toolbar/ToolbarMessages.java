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
package org.eclipse.che.ide.command.toolbar;

import com.google.gwt.i18n.client.Messages;

/**
 * I18n messages for the Command Toolbar.
 *
 * @author Artem Zatsarynnyi
 */
public interface ToolbarMessages extends Messages {

  @Key("guide.label")
  String guideItemLabel(String goalName);

  @Key("goal_button.tooltip.no_command")
  String goalButtonTooltipNoCommand(String goalId);

  @Key("goal_button.tooltip.choose_command")
  String goalButtonTooltipChooseCommand(String goalId);

  @Key("goal_button.tooltip.execute")
  String goalButtonTooltipExecute(String commandName);

  @Key("goal_button.tooltip.execute_on_machine")
  String goalButtonTooltipExecuteOnMachine(String commandName, String machineName);

  @Key("previews.tooltip")
  String previewsTooltip();

  @Key("previews.no_previews")
  String previewsNoPreviews();

  @Key("previews.error.not_available")
  String previewsNotAvailableError();
}
