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
package org.eclipse.che.infrastructure.docker.client.params;

import java.util.Objects;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.MessageProcessor;
import org.eclipse.che.infrastructure.docker.client.json.Filters;

/**
 * Arguments holder for {@link DockerConnector#getEvents(GetEventsParams, MessageProcessor)}.
 *
 * @author Mykola Morhun
 */
public class GetEventsParams {

  private Long sinceSecond;
  private Long untilSecond;
  private Filters filters;

  /** Creates arguments holder. */
  public static GetEventsParams create() {
    return new GetEventsParams();
  }

  private GetEventsParams() {}

  /**
   * Adds since time filter to this parameters.
   *
   * @param sinceSecond UNIX date in seconds. Allow omit events created before specified date
   * @return this params instance
   */
  public GetEventsParams withSinceSecond(long sinceSecond) {
    this.sinceSecond = sinceSecond;
    return this;
  }

  /**
   * Adds until time filter to this parameters.
   *
   * @param untilSecond UNIX date in seconds. Allow omit events created after specified date
   * @return this params instance
   */
  public GetEventsParams withUntilSecond(long untilSecond) {
    this.untilSecond = untilSecond;
    return this;
  }

  /**
   * Adds filters to this parameters.
   *
   * @param filters filter of needed events. Available filters: {@code event=<string>} {@code
   *     image=<string>} {@code container=<string>}
   * @return this params instance
   */
  public GetEventsParams withFilters(Filters filters) {
    this.filters = filters;
    return this;
  }

  public Long getSinceSecond() {
    return sinceSecond;
  }

  public Long getUntilSecond() {
    return untilSecond;
  }

  public Filters getFilters() {
    return filters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GetEventsParams that = (GetEventsParams) o;
    return Objects.equals(sinceSecond, that.sinceSecond)
        && Objects.equals(untilSecond, that.untilSecond)
        && Objects.equals(filters, that.filters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sinceSecond, untilSecond, filters);
  }
}
