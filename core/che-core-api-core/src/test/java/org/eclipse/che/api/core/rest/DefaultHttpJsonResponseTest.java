/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.rest;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.util.LinksHelper.createLink;
import static org.testng.Assert.assertEquals;

import com.google.common.reflect.TypeToken;
import java.io.IOException;
import java.util.Set;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.dto.server.JsonArrayImpl;
import org.eclipse.che.dto.server.JsonStringMapImpl;
import org.testng.annotations.Test;

/**
 * Tests of {@link DefaultHttpJsonResponse}.
 *
 * @author Yevhenii Voevodin
 */
public class DefaultHttpJsonResponseTest {

  @Test
  public void shouldReturnStringAsItIsIfStringIsRequested() throws Exception {
    final DefaultHttpJsonResponse response = new DefaultHttpJsonResponse("string response", 200);

    assertEquals(response.asString(), "string response");
  }

  @Test
  public void shouldReturnJsonSerializableInstanceIfItWasRequested() throws Exception {
    final Link testLink = createLink("POST", "http://localhost:8080", "rel");
    final DefaultHttpJsonResponse response =
        new DefaultHttpJsonResponse(DtoFactory.getInstance().toJson(testLink), 200);

    assertEquals(response.asDto(Link.class), testLink);
  }

  @Test
  public void shouldDeserializeResponseToGivenType() throws Exception {
    final String responseBody =
        DtoFactory.getInstance().toJson(new JsonArrayImpl<>(singletonList("element")));
    final DefaultHttpJsonResponse response = new DefaultHttpJsonResponse(responseBody, 200);

    assertEquals(
        response.as(Set.class, new TypeToken<Set<String>>() {}.getType()), singleton("element"));
  }

  @Test
  public void shouldBeAbleToRequestProperties() throws Exception {
    final String responseBody =
        DtoFactory.getInstance().toJson(new JsonStringMapImpl<>(singletonMap("key", "value")));
    final DefaultHttpJsonResponse response = new DefaultHttpJsonResponse(responseBody, 200);

    assertEquals(response.asProperties(), singletonMap("key", "value"));
  }

  @Test
  public void shouldBeAbleToRequestListOfJsonSerializableElements() throws Exception {
    final Link testLink = createLink("POST", "http://localhost:8080", "rel");
    final String responseBody =
        DtoFactory.getInstance().toJson(new JsonArrayImpl<>(singletonList(testLink)));
    final DefaultHttpJsonResponse response = new DefaultHttpJsonResponse(responseBody, 200);

    assertEquals(response.asList(Link.class), singletonList(testLink));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionWhenClazzIsNull() throws Exception {
    new DefaultHttpJsonResponse("{}", 200).as(null, null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNullPointerExceptionWhenDtoInterfaceIsNull() throws Exception {
    new DefaultHttpJsonResponse("{}", 200).asDto(null);
  }

  @Test(expectedExceptions = IOException.class)
  public void shouldThrowServerExceptionWhenParsingNotValidJsonContent() throws Exception {
    new DefaultHttpJsonResponse("not valid json", 200).as(Set.class, null);
  }

  @Test
  public void shouldReturnCorrectResponseCode() {
    final DefaultHttpJsonResponse response = new DefaultHttpJsonResponse("not valid json", 201);

    assertEquals(response.getResponseCode(), 201);
  }
}
