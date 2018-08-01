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
package org.eclipse.che.plugin.yaml.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared class between yaml client and server
 *
 * @author Joshua Pinkney
 */
public class PreferenceHelper {

  /**
   * Converts List of Yaml Preferences to Map of Globs to List of urls
   *
   * @param pref The list of Yaml Preferences you want to convert
   * @return Map of Globs to List of urls of the Yaml Preferences
   */
  public static Map<String, List<String>> yamlPreferenceToMap(List<YamlPreference> pref) {
    Map<String, List<String>> preferenceMap = new HashMap<String, List<String>>();

    for (YamlPreference prefItr : pref) {

      if (preferenceMap.containsKey(prefItr.getGlob())) {
        ArrayList<String> prefList = new ArrayList<String>(preferenceMap.get(prefItr.getGlob()));
        prefList.add(prefItr.getUrl());
        preferenceMap.put(prefItr.getGlob(), prefList);
      } else {
        ArrayList<String> prefList = new ArrayList<String>();
        prefList.add(prefItr.getUrl());
        preferenceMap.put(prefItr.getGlob(), prefList);
      }
    }

    return preferenceMap;
  }
}
