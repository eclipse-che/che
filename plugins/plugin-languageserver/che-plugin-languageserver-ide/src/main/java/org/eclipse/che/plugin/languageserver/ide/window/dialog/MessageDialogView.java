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
package org.eclipse.che.plugin.languageserver.ide.window.dialog;

import com.google.inject.ImplementedBy;
import java.util.List;
import org.eclipse.lsp4j.MessageActionItem;

/**
 * The view interface for the confirmation dialog component.
 *
 * @author Mickaël Leduque
 * @author Artem Zatsarynnyi
 */
@ImplementedBy(MessageDialogViewImpl.class)
public interface MessageDialogView {

  /** Sets the action delegate. */
  void setDelegate(ActionDelegate delegate);

  /** Displays the dialog window. Sets "accept" button in the focus. */
  void showDialog();

  /** Closes the dialog window. */
  void closeDialog();

  /** Fill the window with its content. */
  void setContent(String content);

  /** Sets the window title. */
  void setTitle(String title);

  void setActions(List<MessageActionItem> actions);

  /** The interface for the action delegate. */
  interface ActionDelegate {

    void onAction(MessageActionItem actionItem);

    /** Performs any actions appropriate in response to the user having clicked the Enter key. */
    void onEnterClicked();
  }
}
