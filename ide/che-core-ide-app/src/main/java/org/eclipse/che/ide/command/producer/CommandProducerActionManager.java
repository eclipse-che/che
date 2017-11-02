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
package org.eclipse.che.ide.command.producer;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_EDITOR_TAB_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_TOOLBAR;
import static org.eclipse.che.ide.api.constraints.Anchor.AFTER;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.command.CommandProducer;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * Manages actions for the commands.
 *
 * <p>Manager gets all registered {@link CommandProducer}s and creates related actions in context
 * menus.
 *
 * @see CommandProducer
 */
@Singleton
public class CommandProducerActionManager {

  private final ActionManager actionManager;
  private final CommandProducerActionFactory commandProducerActionFactory;
  private final Resources resources;
  private final ProducerMessages messages;

  private final Set<CommandProducer> commandProducers;

  private DefaultActionGroup commandActionsPopUpGroup;

  @Inject
  public CommandProducerActionManager(
      Set<CommandProducer> commandProducers,
      EventBus eventBus,
      ActionManager actionManager,
      CommandProducerActionFactory commandProducerActionFactory,
      Resources resources,
      ProducerMessages messages) {
    this.actionManager = actionManager;
    this.commandProducerActionFactory = commandProducerActionFactory;
    this.resources = resources;
    this.messages = messages;

    this.commandProducers = new HashSet<>();

    if (commandProducers != null) {
      this.commandProducers.addAll(commandProducers);
    }

    eventBus.addHandler(BasicIDEInitializedEvent.TYPE, e -> init());
  }

  private void init() {
    commandActionsPopUpGroup =
        new DefaultActionGroup(messages.actionCommandsTitle(), true, actionManager);
    actionManager.registerAction("commandActionsPopUpGroup", commandActionsPopUpGroup);
    commandActionsPopUpGroup
        .getTemplatePresentation()
        .setImageElement(new SVGImage(resources.compile()).getElement());
    commandActionsPopUpGroup
        .getTemplatePresentation()
        .setDescription(messages.actionCommandsDescription());

    DefaultActionGroup mainContextMenu =
        (DefaultActionGroup) actionManager.getAction(GROUP_MAIN_CONTEXT_MENU);
    mainContextMenu.add(commandActionsPopUpGroup);

    DefaultActionGroup editorTabContextMenu =
        (DefaultActionGroup) actionManager.getAction(GROUP_EDITOR_TAB_CONTEXT_MENU);
    editorTabContextMenu.add(commandActionsPopUpGroup);

    // add 'Commands' pop-up group to the main toolbar
    DefaultActionGroup commandActionsToolbarGroup = new CommandActionsToolbarGroup(actionManager);
    commandActionsToolbarGroup.add(commandActionsPopUpGroup);
    DefaultActionGroup mainToolbarGroup =
        (DefaultActionGroup) actionManager.getAction(GROUP_MAIN_TOOLBAR);
    mainToolbarGroup.add(commandActionsToolbarGroup, new Constraints(AFTER, "changeResourceGroup"));

    commandProducers.forEach(this::createActionsForProducer);
  }

  /** Creates actions for the given {@link CommandProducer}. */
  private void createActionsForProducer(CommandProducer producer) {
    BaseAction action = commandProducerActionFactory.create(producer.getName(), producer);

    actionManager.registerAction(producer.getName(), action);

    commandActionsPopUpGroup.add(action);
  }

  /**
   * Action group for placing {@link CommandProducerAction}s on the toolbar. It's visible when at
   * least one {@link CommandProducerAction} exists.
   */
  private class CommandActionsToolbarGroup extends DefaultActionGroup {

    CommandActionsToolbarGroup(ActionManager actionManager) {
      super(actionManager);
    }

    @Override
    public void update(ActionEvent e) {
      e.getPresentation().setEnabledAndVisible(commandActionsPopUpGroup.getChildrenCount() != 0);
    }
  }
}
