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
package org.eclipse.che.ide.maven.tools;

import static org.eclipse.che.commons.xml.NewElement.createElement;
import static org.eclipse.che.commons.xml.XMLTreeLocation.after;
import static org.eclipse.che.commons.xml.XMLTreeLocation.afterAnyOf;
import static org.eclipse.che.commons.xml.XMLTreeLocation.before;
import static org.eclipse.che.commons.xml.XMLTreeLocation.beforeAnyOf;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheBegin;
import static org.eclipse.che.commons.xml.XMLTreeLocation.inTheEnd;

import org.eclipse.che.commons.xml.Element;
import org.eclipse.che.commons.xml.NewElement;

/**
 * Describes repository for <i>/project/repositories</i> or <i>/project/pluginRepositories</i>
 *
 * <p>Supports next data:
 *
 * <ul>
 *   <li>id
 *   <li>name
 *   <li>url
 *   <li>layout
 *   <li>snapshots
 *   <li>releases
 * </ul>
 *
 * @author Eugene Voevodin
 */
public class Repository {

  Element element;

  private String id;
  private String name;
  private String url;
  private String layout;
  private RepositoryPolicy snapshots;
  private RepositoryPolicy releases;

  public Repository() {}

  Repository(Element element) {
    this.element = element;
    id = element.getChildText("id");
    name = element.getChildText("name");
    url = element.getChildText("url");
    layout = element.getChildText("layout");
    if (element.hasSingleChild("snapshots")) {
      snapshots = new RepositoryPolicy(element.getSingleChild("snapshots"));
    }
    if (element.hasSingleChild("releases")) {
      releases = new RepositoryPolicy(element.getSingleChild("releases"));
    }
  }

  /** Returns repository identifier (i.e. maven2). */
  public String getId() {
    return id;
  }

  /**
   * Sets repository identifier.
   *
   * @param id new repository identifier, if it is {@code null} then <i>id</i> element will be
   *     removed from xml if exists
   */
  public Repository setId(String id) {
    this.id = id;
    if (element != null) {
      if (id == null) {
        element.removeChild("id");
      } else if (element.hasSingleChild("id")) {
        element.getSingleChild("id").setText(id);
      } else {
        element.insertChild(createElement("id", id), inTheBegin());
      }
    }
    return this;
  }

  /** Returns repository name. */
  public String getName() {
    return name;
  }

  /**
   * Sets repository name.
   *
   * @param name new repository name, if it is {@code null} then <i>name</i> element will be removed
   *     from xml if exists
   */
  public Repository setName(String name) {
    this.name = name;
    if (element != null) {
      if (name == null) {
        element.removeChild("name");
      } else if (element.hasSingleChild("name")) {
        element.getSingleChild("name").setText(name);
      } else {
        element.insertChild(createElement("name", name), after("id").or(inTheBegin()));
      }
    }
    return this;
  }

  /** Returns repository url. */
  public String getUrl() {
    return url;
  }

  /**
   * Sets repository url.
   *
   * @param url new repository url, if it is {@code null} then <i>url</i> element will be removed
   *     from xml if exists
   */
  public Repository setUrl(String url) {
    this.url = url;
    if (element != null) {
      if (url == null) {
        element.removeChild("url");
      } else if (element.hasSingleChild("url")) {
        element.getSingleChild("url").setText(url);
      } else {
        element.insertChild(createElement("url", url), afterAnyOf("name", "id").or(inTheBegin()));
      }
    }
    return this;
  }

  /** Returns repository layout(i.e. default, legacy) */
  public String getLayout() {
    return layout;
  }

  /**
   * Sets repository layout(i.e. default, legacy)
   *
   * @param layout new repository layout, if {@code null} then <i>layout</i> element will be removed
   *     from xml if exists
   */
  public Repository setLayout(String layout) {
    this.layout = layout;
    if (element != null) {
      if (layout == null) {
        element.removeChild("layout");
      } else if (element.hasSingleChild("layout")) {
        element.getSingleChild("layout").setText(layout);
      } else {
        element.insertChild(
            createElement("layout", layout), beforeAnyOf("snapshots", "releases").or(inTheEnd()));
      }
    }
    return this;
  }

  /** Returns repository snapshots details */
  public RepositoryPolicy getSnapshots() {
    return snapshots;
  }

  /**
   * Sets repository snapshots details
   *
   * @param snapshots new repository snapshots details, if {@code null} then element will be removed
   *     from xml if exists
   */
  public Repository setSnapshots(RepositoryPolicy snapshots) {
    this.snapshots = snapshots;
    if (element != null) {
      if (snapshots == null) {
        element.removeChild("snapshots");
      } else if (element.hasSingleChild("snapshots")) {
        snapshots.element =
            element.getSingleChild("snapshots").replaceWith(snapshots.asXMLElement("snapshots"));
      } else {
        element.insertChild(snapshots.asXMLElement("snapshots"), before("releases").or(inTheEnd()));
        snapshots.element = element.getSingleChild("snapshots");
      }
    }
    return this;
  }

  /** Returns repository releases details */
  public RepositoryPolicy getReleases() {
    return releases;
  }

  /**
   * Sets repository releases details
   *
   * @param releases new repository releases details, if {@code null} then element will be removed
   *     from xml if exists
   */
  public Repository setReleases(RepositoryPolicy releases) {
    this.releases = releases;
    if (element != null) {
      if (releases == null) {
        element.removeChild("releases");
      } else if (element.hasSingleChild("releases")) {
        releases.element =
            element.getSingleChild("releases").replaceWith(releases.asXMLElement("releases"));
      } else {
        element.appendChild(releases.asXMLElement("releases"));
        releases.element = element.getSingleChild("releases");
      }
    }
    return this;
  }

  NewElement asXMLElement() {
    final NewElement repository = createElement("repository");
    if (id != null) {
      repository.appendChild(createElement("id", id));
    }
    if (name != null) {
      repository.appendChild(createElement("name", name));
    }
    if (url != null) {
      repository.appendChild(createElement("url", url));
    }
    if (layout != null) {
      repository.appendChild(createElement("layout", layout));
    }
    if (snapshots != null) {
      repository.appendChild(snapshots.asXMLElement("snapshots"));
    }
    if (releases != null) {
      repository.appendChild(releases.asXMLElement("releases"));
    }
    return repository;
  }

  void remove() {
    if (element != null) {
      element.remove();
      element = null;
      if (getSnapshots() != null) {
        getSnapshots().remove();
      }
      if (getReleases() != null) {
        getReleases().remove();
      }
    }
  }
}
