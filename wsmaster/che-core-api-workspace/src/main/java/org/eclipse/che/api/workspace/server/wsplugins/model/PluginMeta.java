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
package org.eclipse.che.api.workspace.server.wsplugins.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author Oleksandr Garagatyi */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginMeta {
  private String name = null;
  private String id = null;
  private String type = null;
  private String version = null;
  private String title = null;
  private String description = null;
  private String category = null;
  private String publisher = null;
  private String repository = null;
  private List<String> tags = null;
  private String mediaImage = null;
  private String mediaVideo = null;
  private String firstPublicationDate = null;
  private String latestUpdateDate = null;
  private String preview = null;
  private String icon = null;
  private String url = null;
  private Map<String, String> attributes = null;

  public PluginMeta name(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public PluginMeta id(String id) {
    this.id = id;
    return this;
  }

  public String getId() {
    return id;
  }

  public PluginMeta type(String type) {
    this.type = type;
    return this;
  }

  public String getType() {
    return type;
  }

  public PluginMeta version(String version) {
    this.version = version;
    return this;
  }

  public String getVersion() {
    return version;
  }

  public PluginMeta url(String url) {
    this.url = url;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public PluginMeta title(String title) {
    this.title = title;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public PluginMeta icon(String icon) {
    this.icon = icon;
    return this;
  }

  public String getIcon() {
    return icon;
  }

  public PluginMeta description(String description) {
    this.description = description;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public Map<String, String> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  public PluginMeta attributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }

  public PluginMeta category(String category) {
    this.category = category;
    return this;
  }

  public String getCategory() {
    return category;
  }

  public PluginMeta publisher(String publisher) {
    this.publisher = publisher;
    return this;
  }

  public String getPublisher() {
    return publisher;
  }

  public PluginMeta repository(String repository) {
    this.repository = repository;
    return this;
  }

  public String getRepository() {
    return repository;
  }

  public PluginMeta tags(List<String> tags) {
    this.tags = tags;
    return this;
  }

  public List<String> getTags() {
    if (tags == null) {
      tags = new ArrayList<>();
    }
    return tags;
  }

  public PluginMeta mediaImage(String mediaImage) {
    this.mediaImage = mediaImage;
    return this;
  }

  public String getMediaImage() {
    return mediaImage;
  }

  public PluginMeta mediaVideo(String mediaVideo) {
    this.mediaVideo = mediaVideo;
    return this;
  }

  public String getMediaVideo() {
    return mediaVideo;
  }

  public PluginMeta preview(String preview) {
    this.preview = preview;
    return this;
  }

  public String getPreview() {
    return preview;
  }

  public PluginMeta firstPublicationDate(String firstPublicationDate) {
    this.firstPublicationDate = firstPublicationDate;
    return this;
  }

  public String getFirstPublicationDate() {
    return firstPublicationDate;
  }

  public PluginMeta latestUpdateDate(String latestUpdateDate) {
    this.latestUpdateDate = latestUpdateDate;
    return this;
  }

  public String getLatestUpdateDate() {
    return latestUpdateDate;
  }
}
