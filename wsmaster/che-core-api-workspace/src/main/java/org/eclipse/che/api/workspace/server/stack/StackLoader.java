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
package org.eclipse.che.api.workspace.server.stack;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.io.IOUtils;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.server.stack.image.StackIcon;
import org.eclipse.che.api.workspace.shared.stack.Stack;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.core.db.DBInitializer;
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
public class StackLoader {

  public static final String CHE_PREDEFINED_STACKS = "che.predefined.stacks";

  private static final Logger LOG = LoggerFactory.getLogger(StackLoader.class);

  protected final StackDao stackDao;

  private final Gson GSON;
  private final Map<String, String> stacks2images;
  private final DBInitializer dbInitializer;
  private final Boolean reloadStacksOnStart;

  @Inject
  @SuppressWarnings("unused")
  public StackLoader(
      @Named("che.predefined.stacks.reload_on_start") boolean reloadStacksOnStart,
      @Named(CHE_PREDEFINED_STACKS) Map<String, String> stacks2images,
      StackDao stackDao,
      DBInitializer dbInitializer) {
    this.reloadStacksOnStart = reloadStacksOnStart;
    this.stacks2images = stacks2images;
    this.stackDao = stackDao;
    this.dbInitializer = dbInitializer;
    GSON = new GsonBuilder().create();
  }

  /** Load predefined stacks with their icons to the {@link StackDao}. */
  @PostConstruct
  public void start() {
    final boolean override;
    if (reloadStacksOnStart) {
      LOG.warn("Reload stacks on start is deprecated policy, and it will be removed soon");
      override = true;
    } else {
      override = dbInitializer.isBareInit();
    }
    if (override) {
      for (Map.Entry<String, String> stack2image : stacks2images.entrySet()) {
        final String stackFile = stack2image.getKey();
        final String imagesDir = stack2image.getValue();
        try (BufferedReader reader =
            new BufferedReader(new InputStreamReader(IoUtil.getResource(stackFile)))) {
          List<StackImpl> stacks =
              GSON.fromJson(reader, new TypeToken<List<StackImpl>>() {}.getType());
          final Path imagesDirPath = !isNullOrEmpty(imagesDir) ? Paths.get(imagesDir) : null;
          stacks.forEach(stack -> loadStack(stack, imagesDirPath));
        } catch (Exception ex) {
          LOG.error("Failed to store stacks from '{}'", stackFile);
        }
      }
      LOG.info("Stacks initialization finished");
    }
  }

  protected void loadStack(StackImpl stack, Path imagePath) {
    setIconData(stack, imagePath);
    try {
      try {
        stackDao.update(stack);
      } catch (NotFoundException ex) {
        stackDao.create(stack);
      }
    } catch (ServerException | ConflictException ex) {
      LOG.warn(format("Failed to load stack with id '%s' ", stack.getId()), ex.getMessage());
    }
  }

  /**
   * Searches for stack icon and set image data into given stack.
   *
   * @param stack stack for icon setup
   * @param stackIconFolderPath path to icon folder
   */
  protected void setIconData(StackImpl stack, Path stackIconFolderPath) {
    StackIcon stackIcon = stack.getStackIcon();
    if (stackIcon == null) {
      return;
    }
    if (stackIconFolderPath == null) {
      stack.setStackIcon(null);
      LOG.warn("No configured image found for stack {}", stack.getId());
      return;
    }
    try {
      final Path stackIconPath = stackIconFolderPath.resolve(stackIcon.getName());
      final byte[] imageData = IOUtils.toByteArray(IoUtil.getResource(stackIconPath.toString()));
      stackIcon = new StackIcon(stackIcon.getName(), stackIcon.getMediaType(), imageData);
      stack.setStackIcon(stackIcon);
    } catch (IOException ex) {
      stack.setStackIcon(null);
      LOG.error(
          format("Failed to load stack icon data for the stack with id '%s'", stack.getId()), ex);
    }
  }
}
