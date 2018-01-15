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
package org.eclipse.che.workspace.infrastructure.openshift;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;

/**
 * Helps to convert servers related entities (like {@link ServerConfig} and machine name) to
 * OpenShift annotations and vise-versa.
 *
 * @author Sergii Leshchenko
 */
public class Annotations {
  public static final String ANNOTATION_PREFIX = "org.eclipse.che.";

  public static final String SERVER_PORT_ANNOTATION_FMT = ANNOTATION_PREFIX + "server.%s.port";
  public static final String SERVER_PROTOCOL_ANNOTATION_FMT =
      ANNOTATION_PREFIX + "server.%s.protocol";
  public static final String SERVER_PATH_ANNOTATION_FMT = ANNOTATION_PREFIX + "server.%s.path";
  public static final String SERVER_ATTR_ANNOTATION_FMT =
      ANNOTATION_PREFIX + "server.%s.attributes";

  public static final String MACHINE_NAME_ANNOTATION = ANNOTATION_PREFIX + "machine.name";

  /** Pattern that matches server annotations e.g. "org.eclipse.che.server.exec-agent.port". */
  private static final Pattern SERVER_ANNOTATION_PATTERN =
      Pattern.compile("org\\.eclipse\\.che\\.server\\.(?<ref>[\\w-/]+)\\..+");

  private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
  // used to avoid frequent creations of the object at runtime
  private static final java.lang.reflect.Type mapTypeToken =
      new TypeToken<Map<String, String>>() {}.getType();

  /** Creates new annotations serializer. */
  public static Serializer newSerializer() {
    return new Serializer();
  }

  /** Creates new label deserializer from given annotations. */
  public static Deserializer newDeserializer(Map<String, String> annotations) {
    return new Deserializer(annotations);
  }

  /** Helps to serialize ServerConfig entities to OpenShift annotations. */
  public static class Serializer {
    private final Map<String, String> annotations = new HashMap<>();

    /**
     * Serializes server configuration as OpenShift annotations. Appends serialization result to
     * this aggregate.
     *
     * @param ref server reference e.g. "exec-agent"
     * @param server server configuration
     * @return this serializer
     */
    public Serializer server(String ref, ServerConfig server) {
      annotations.put(String.format(SERVER_PORT_ANNOTATION_FMT, ref), server.getPort());
      annotations.put(String.format(SERVER_PROTOCOL_ANNOTATION_FMT, ref), server.getProtocol());
      if (server.getPath() != null) {
        annotations.put(String.format(SERVER_PATH_ANNOTATION_FMT, ref), server.getPath());
      }
      if (server.getAttributes() != null) {
        annotations.put(
            String.format(SERVER_ATTR_ANNOTATION_FMT, ref), GSON.toJson(server.getAttributes()));
      }
      return this;
    }

    public Serializer servers(Map<String, ? extends ServerConfig> servers) {
      servers.forEach(this::server);
      return this;
    }

    public Serializer machineName(String machineName) {
      annotations.put(MACHINE_NAME_ANNOTATION, machineName);
      return this;
    }

    public Map<String, String> annotations() {
      return annotations;
    }
  }

  /** Helps to deserialize OpenShift annotations to known {@link ServerConfig} related entities. */
  public static class Deserializer {
    private final Map<String, String> annotations;

    public Deserializer(Map<String, String> annotations) {
      this.annotations = annotations != null ? annotations : Collections.emptyMap();
    }

    /** Retrieves server configuration from annotations and returns (ref -> server config) map. */
    public Map<String, ServerConfigImpl> servers() {
      Map<String, ServerConfigImpl> servers = new HashMap<>();
      for (Map.Entry<String, String> entry : annotations.entrySet()) {
        Matcher refMatcher = SERVER_ANNOTATION_PATTERN.matcher(entry.getKey());
        if (refMatcher.matches()) {
          String ref = refMatcher.group("ref");
          if (!servers.containsKey(ref)) {
            servers.put(
                ref,
                new ServerConfigImpl(
                    annotations.get(String.format(SERVER_PORT_ANNOTATION_FMT, ref)),
                    annotations.get(String.format(SERVER_PROTOCOL_ANNOTATION_FMT, ref)),
                    annotations.get(String.format(SERVER_PATH_ANNOTATION_FMT, ref)),
                    GSON.fromJson(
                        annotations.get(String.format(SERVER_ATTR_ANNOTATION_FMT, ref)),
                        mapTypeToken)));
          }
        }
      }
      return servers;
    }

    public String machineName() {
      return annotations.get(MACHINE_NAME_ANNOTATION);
    }
  }

  private Annotations() {}
}
