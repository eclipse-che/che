/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.command.toolbar;

import com.google.gwt.i18n.client.Messages;

/**
 * I18n messages for the Command Toolbar.
 *
 * @author Artem Zatsarynnyi
 */
public interface ToolbarMessages extends Messages {

    @Key("guide.label")
    String guideItemLabel();

    @Key("goal_button.tooltip.no_command")
    String goalButtonTooltipNoCommand(String goalId);

    @Key("goal_button.tooltip.choose_command")
    String goalButtonTooltipChooseCommand(String goalId);

    @Key("goal_button.tooltip.execute_prompt")
    String goalButtonTooltipExecutePrompt(String commandName);

    @Key("previews.tooltip")
    String previewsTooltip();

    @Key("previews.error.not_available")
    String previewsNotAvailableError();
}
