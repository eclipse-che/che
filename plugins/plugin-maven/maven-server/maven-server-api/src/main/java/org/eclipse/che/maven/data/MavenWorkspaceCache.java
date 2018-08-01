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
package org.eclipse.che.maven.data;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Stores all maven key(artifacts) in our workspace.
 *
 * @author Evgen Vidolob
 */
public class MavenWorkspaceCache implements Serializable {
  private static final long serialVersionUID = 1L;

  private final Map<MavenKey, Entry> cache = new HashMap<MavenKey, Entry>();

  public void put(MavenKey key, File file) {
    put(key, file, null);
  }

  public void put(MavenKey key, File file, File output) {
    for (MavenKey mavenKey : getAllPossibleKeys(key)) {
      cache.put(mavenKey, new Entry(mavenKey, file, output));
    }
  }

  public void invalidate(MavenKey key) {
    for (MavenKey mavenKey : getAllPossibleKeys(key)) {
      cache.remove(mavenKey);
    }
  }

  public Set<MavenKey> getAllKeys() {
    return cache.keySet();
  }

  public Entry findEntry(MavenKey key) {
    return cache.get(key);
  }

  private MavenKey[] getAllPossibleKeys(MavenKey key) {
    MavenKey latestKey = new MavenKey(key.getGroupId(), key.getArtifactId(), MavenConstants.LATEST);

    String version = key.getVersion();
    if (version != null && version.contains(MavenConstants.SNAPSHOT)) {
      return new MavenKey[] {key, latestKey};
    } else {
      return new MavenKey[] {
        key, latestKey, new MavenKey(key.getGroupId(), key.getArtifactId(), MavenConstants.RELEASE)
      };
    }
  }

  public MavenWorkspaceCache copy() {
    MavenWorkspaceCache copy = new MavenWorkspaceCache();
    copy.cache.putAll(cache);
    return copy;
  }

  public static class Entry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final MavenKey key;
    private final File file;
    private final File output;

    Entry(MavenKey key, File file, File output) {
      this.key = key;
      this.file = file;
      this.output = output;
    }

    public File getFile(String type) {
      if (output == null || MavenConstants.POM_EXTENSION.equalsIgnoreCase(type)) {
        return file;
      } else {
        return output;
      }
    }

    public MavenKey getKey() {
      return key;
    }
  }
}
