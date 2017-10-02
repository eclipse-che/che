/**
 * ***************************************************************************** Copyright (c) 2016
 * Rogue Wave Software, Inc. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Rogue Wave Software, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.composer.server.projecttype;

import static org.eclipse.che.plugin.composer.shared.Constants.PACKAGE;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.project.server.type.ReadonlyValueProvider;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.project.server.type.ValueStorageException;

public class ComposerValueProviderFactory implements ValueProviderFactory {

  @Override
  public ValueProvider newInstance(ProjectConfig projectConfig) {
    return new ComposerValueProvider(projectConfig);
  }

  protected class ComposerValueProvider extends ReadonlyValueProvider {

    protected Path projectFsPath;

    protected ComposerValueProvider(ProjectConfig projectConfig) {
      this.projectFsPath = Paths.get("/projects", projectConfig.getPath());
    }

    @Override
    public List<String> getValues(String attributeName) throws ValueStorageException {
      try {
        Path composerDotJsonFsPath =
            Paths.get(projectFsPath.toAbsolutePath().toString(), "composer.json");
        if (!composerDotJsonFsPath.toFile().exists()) {
          return Collections.emptyList();
        }
        JsonObject model = readModel(projectFsPath);
        String value = "";

        if (attributeName.equals(PACKAGE) && model.has("name")) {
          value = model.get("name").getAsString();
        }

        return Collections.singletonList(value);
      } catch (ServerException | IOException e) {
        throw new ValueStorageException("Can't read composer.json : " + e.getMessage());
      }
    }

    private JsonObject readModel(Path projectFsPath) throws ServerException, IOException {
      return new Gson().fromJson(Files.newBufferedReader(projectFsPath), JsonObject.class);
    }
  }
}
