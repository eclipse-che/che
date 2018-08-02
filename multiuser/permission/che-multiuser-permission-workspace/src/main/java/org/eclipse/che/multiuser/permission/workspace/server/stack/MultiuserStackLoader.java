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
package org.eclipse.che.multiuser.permission.workspace.server.stack;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

import java.nio.file.Path;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.stack.StackLoader;
import org.eclipse.che.api.workspace.server.stack.image.StackIcon;
import org.eclipse.che.api.workspace.shared.stack.Stack;
import org.eclipse.che.core.db.DBInitializer;
import org.eclipse.che.multiuser.permission.workspace.server.spi.jpa.JpaStackPermissionsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for loading list predefined {@link Stack} to the {@link StackDao} and set {@link StackIcon}
 * to the predefined stack.
 *
 * @author Alexander Andrienko
 * @author Sergii Leshchenko
 * @author Anton Korneta
 */
@Singleton
public class MultiuserStackLoader extends StackLoader {

  public static final String CHE_PREDEFINED_STACKS = "che.predefined.stacks";

  private static final Logger LOG = LoggerFactory.getLogger(MultiuserStackLoader.class);

  private final JpaStackPermissionsDao permissionsDao;

  @Inject
  @SuppressWarnings("unused")
  public MultiuserStackLoader(
      @Named("che.predefined.stacks.reload_on_start") boolean reloadStacksOnStart,
      @Named(CHE_PREDEFINED_STACKS) Map<String, String> stacks2images,
      StackDao stackDao,
      JpaStackPermissionsDao permissionsDao,
      DBInitializer dbInitializer) {
    super(reloadStacksOnStart, stacks2images, stackDao, dbInitializer);
    this.permissionsDao = permissionsDao;
  }

  protected void loadStack(StackImpl stack, Path imagePath) {
    setIconData(stack, imagePath);
    try {
      try {
        stackDao.update(stack);
      } catch (NotFoundException ignored) {
        stackDao.create(stack);
      }
      permissionsDao.store(
          new StackPermissionsImpl("*", stack.getId(), singletonList(StackDomain.SEARCH)));
    } catch (ServerException | ConflictException ex) {
      LOG.warn(format("Failed to load stack with id '%s' ", stack.getId()), ex.getMessage());
    }
  }
}
