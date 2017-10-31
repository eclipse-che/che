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

package org.eclipse.che.api.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.inject.Singleton;

/** @author Yevhen Vydolob */
@Singleton
public class PluginRegistry {

  public static final String PLUGIN_PATH = "/assembly/plugins";

  public PluginRegistry() {}

  public List<String> getPlugins() {
    //    String pluginPath = classLoader.getResource("/_app/plugins").getPath();
    File pluginsPath = new File(PLUGIN_PATH);
    List<String> result = new ArrayList<>();
    try {
      Stream<Path> stream = Files.walk(pluginsPath.toPath(), 1);
      stream.forEach(
          path -> {
            File file = path.toFile();
            if (file.isDirectory()) {
              File packageJS = new File(file, "package.json");
              try {
                byte[] bytes = Files.readAllBytes(packageJS.toPath());
                result.add(new String(bytes));
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          });
    } catch (IOException e) {
      e.printStackTrace();
    }

    return result;
  }
}
