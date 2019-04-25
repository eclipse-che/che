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
package org.eclipse.che.api.devfile.server.convert.component.plugin;

import static java.lang.String.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginMeta;

/**
 * Parses plugin reference in devfile to plugin meta object. Only fields which value are present in
 * reference are populated in the object.
 *
 * @author Alexander Garagatyi
 */
public class PluginReferenceParser {
  private static final Pattern PLUGIN_PATTERN =
      Pattern.compile("(.*/)?(?<publisher>[-a-z0-9]+)/(?<name>[-a-z0-9]+)/(?<version>[-.a-z0-9]+)");

  public static PluginMeta resolveMeta(String ref) {
    Matcher matcher = PLUGIN_PATTERN.matcher(ref);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(format("Plugin reference '%s' is invalid", ref));
    }

    PluginMeta meta =
        new PluginMeta()
            .publisher(matcher.group("publisher"))
            .name(matcher.group("name"))
            .version(matcher.group("version"));
    meta.id(meta.getPublisher() + "/" + meta.getName() + "/" + meta.getVersion());
    return meta;
  }
}
