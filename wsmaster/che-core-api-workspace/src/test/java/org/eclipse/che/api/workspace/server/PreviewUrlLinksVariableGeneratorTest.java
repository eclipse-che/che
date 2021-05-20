/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import static java.util.Collections.singletonList;
import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.PreviewUrlImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PreviewUrlLinksVariableGeneratorTest {

  private PreviewUrlLinksVariableGenerator generator;
  private UriBuilder uriBuilder;

  @BeforeMethod
  public void setUp() {
    generator = new PreviewUrlLinksVariableGenerator();
    uriBuilder = UriBuilder.fromUri("http://host/path");
  }

  @Test
  public void shouldDoNothingWhenSomethingIsNull() {
    assertTrue(generator.genLinksMapAndUpdateCommands(null, null).isEmpty());
  }

  @Test
  public void shouldDoNothingWhenRuntimeIsNull() {
    WorkspaceImpl w = new WorkspaceImpl();
    w.setRuntime(null);

    assertTrue(generator.genLinksMapAndUpdateCommands(w, uriBuilder).isEmpty());
  }

  @Test
  public void shouldDoNothingWhenCommandsIsNull() {
    WorkspaceImpl w = createWorkspaceWithCommands(null);

    assertTrue(generator.genLinksMapAndUpdateCommands(w, uriBuilder).isEmpty());
  }

  @Test
  public void shouldDoNothingWhenNoCommandWithPreviewUrl() {
    WorkspaceImpl w = createWorkspaceWithCommands(singletonList(new CommandImpl("a", "a", "a")));

    assertTrue(generator.genLinksMapAndUpdateCommands(w, uriBuilder).isEmpty());
  }

  @Test
  public void shouldDoNothingWhenNoCommandWithPreviewurlAttribute() {
    CommandImpl command =
        new CommandImpl("a", "a", "a", new PreviewUrlImpl(123, null), Collections.emptyMap());
    WorkspaceImpl w = createWorkspaceWithCommands(singletonList(command));

    assertTrue(generator.genLinksMapAndUpdateCommands(w, uriBuilder).isEmpty());
  }

  @Test
  public void shouldUpdateCommandAndReturnLinkMapWhenPreviewUrlFound() {
    Map<String, String> commandAttrs = new HashMap<>();
    commandAttrs.put(Command.PREVIEW_URL_ATTRIBUTE, "preview_url_host");
    CommandImpl command =
        new CommandImpl("a", "a", "a", new PreviewUrlImpl(123, null), commandAttrs);
    WorkspaceImpl w =
        createWorkspaceWithCommands(Arrays.asList(command, new CommandImpl("b", "b", "b")));

    Map<String, String> linkMap = generator.genLinksMapAndUpdateCommands(w, uriBuilder);
    assertEquals(linkMap.size(), 1);
    assertEquals(linkMap.values().iterator().next(), "http://preview_url_host");
    String varKey = linkMap.keySet().iterator().next();
    assertTrue(varKey.startsWith("previewurl/"));

    Command aCommand =
        w.getRuntime()
            .getCommands()
            .stream()
            .filter(c -> c.getName().equals("a"))
            .findFirst()
            .get();

    assertTrue(aCommand.getAttributes().get(Command.PREVIEW_URL_ATTRIBUTE).contains(varKey));
    assertEquals(aCommand.getAttributes().get(Command.PREVIEW_URL_ATTRIBUTE), "${" + varKey + "}");
  }

  @Test
  public void variableNamesForTwoCommandsWithSimilarNameMustBeDifferent() {
    Map<String, String> commandAttrs = new HashMap<>();
    commandAttrs.put(Command.PREVIEW_URL_ATTRIBUTE, "preview_url_host");

    CommandImpl command =
        new CommandImpl("run command", "a", "a", new PreviewUrlImpl(123, null), commandAttrs);

    CommandImpl command2 = new CommandImpl(command);
    command2.setName("runcommand");

    WorkspaceImpl w = createWorkspaceWithCommands(Arrays.asList(command, command2));

    Map<String, String> linkMap = generator.genLinksMapAndUpdateCommands(w, uriBuilder);
    assertEquals(linkMap.size(), 2);

    List<? extends Command> commandsAfter = w.getRuntime().getCommands();
    assertNotEquals(
        commandsAfter.get(1).getAttributes().get(Command.PREVIEW_URL_ATTRIBUTE),
        commandsAfter.get(0).getAttributes().get(Command.PREVIEW_URL_ATTRIBUTE));
  }

  @Test
  public void shouldGetHttpsWhenUriBuilderHasHttps() {
    UriBuilder httpsUriBuilder = UriBuilder.fromUri("https://host/path");

    Map<String, String> commandAttrs = new HashMap<>();
    commandAttrs.put(Command.PREVIEW_URL_ATTRIBUTE, "preview_url_host");

    CommandImpl command =
        new CommandImpl("run command", "a", "a", new PreviewUrlImpl(123, null), commandAttrs);

    Map<String, String> linkMap =
        generator.genLinksMapAndUpdateCommands(
            createWorkspaceWithCommands(singletonList(command)), httpsUriBuilder);

    assertTrue(linkMap.values().iterator().next().startsWith("https://"));
  }

  @Test
  public void shouldAppendPathWhenDefinedInPreviewUrl() {
    Map<String, String> commandAttrs = new HashMap<>();
    commandAttrs.put(Command.PREVIEW_URL_ATTRIBUTE, "preview_url_host");

    CommandImpl command =
        new CommandImpl("run command", "a", "a", new PreviewUrlImpl(123, "testpath"), commandAttrs);

    WorkspaceImpl workspace = createWorkspaceWithCommands(singletonList(command));

    Map<String, String> linkMap = generator.genLinksMapAndUpdateCommands(workspace, uriBuilder);

    assertTrue(linkMap.values().iterator().next().endsWith("preview_url_host"));
    String linkKey = linkMap.keySet().iterator().next();
    assertEquals(
        workspace
            .getRuntime()
            .getCommands()
            .get(0)
            .getAttributes()
            .get(Command.PREVIEW_URL_ATTRIBUTE),
        "${" + linkKey + "}testpath");
  }

  @Test
  public void shouldAppendQueryParamsWhenDefinedInPreviewUrl() {
    Map<String, String> commandAttrs = new HashMap<>();
    commandAttrs.put(Command.PREVIEW_URL_ATTRIBUTE, "preview_url_host");

    CommandImpl command =
        new CommandImpl("run command", "a", "a", new PreviewUrlImpl(123, "?a=b"), commandAttrs);

    WorkspaceImpl workspace = createWorkspaceWithCommands(singletonList(command));

    Map<String, String> linkMap = generator.genLinksMapAndUpdateCommands(workspace, uriBuilder);

    assertTrue(linkMap.values().iterator().next().endsWith("preview_url_host"));
    String linkKey = linkMap.keySet().iterator().next();
    assertEquals(
        workspace
            .getRuntime()
            .getCommands()
            .get(0)
            .getAttributes()
            .get(Command.PREVIEW_URL_ATTRIBUTE),
        "${" + linkKey + "}?a=b");
  }

  @Test
  public void shouldAppendMultipleQueryParamsWhenDefinedInPreviewUrl() {
    Map<String, String> commandAttrs = new HashMap<>();
    commandAttrs.put(Command.PREVIEW_URL_ATTRIBUTE, "preview_url_host");

    CommandImpl command =
        new CommandImpl("run command", "a", "a", new PreviewUrlImpl(123, "?a=b&c=d"), commandAttrs);

    WorkspaceImpl workspace = createWorkspaceWithCommands(singletonList(command));
    Map<String, String> linkMap = generator.genLinksMapAndUpdateCommands(workspace, uriBuilder);

    assertTrue(linkMap.values().iterator().next().endsWith("preview_url_host"));
    String linkKey = linkMap.keySet().iterator().next();
    assertEquals(
        workspace
            .getRuntime()
            .getCommands()
            .get(0)
            .getAttributes()
            .get(Command.PREVIEW_URL_ATTRIBUTE),
        "${" + linkKey + "}?a=b&c=d");
  }

  @Test
  public void shouldAppendPathWithQueryParamsWhenDefinedInPreviewUrl() {
    Map<String, String> commandAttrs = new HashMap<>();
    commandAttrs.put(Command.PREVIEW_URL_ATTRIBUTE, "preview_url_host");

    CommandImpl command =
        new CommandImpl(
            "run command", "a", "a", new PreviewUrlImpl(123, "/hello?a=b"), commandAttrs);

    WorkspaceImpl workspace = createWorkspaceWithCommands(singletonList(command));

    Map<String, String> linkMap = generator.genLinksMapAndUpdateCommands(workspace, uriBuilder);

    assertTrue(linkMap.values().iterator().next().endsWith("preview_url_host"));
    String linkKey = linkMap.keySet().iterator().next();
    assertEquals(
        workspace
            .getRuntime()
            .getCommands()
            .get(0)
            .getAttributes()
            .get(Command.PREVIEW_URL_ATTRIBUTE),
        "${" + linkKey + "}/hello?a=b");
  }

  private WorkspaceImpl createWorkspaceWithCommands(List<CommandImpl> commands) {
    RuntimeImpl runtime =
        new RuntimeImpl("", Collections.emptyMap(), "", commands, new ArrayList<>());
    WorkspaceImpl w = new WorkspaceImpl();
    w.setRuntime(runtime);

    return w;
  }
}
