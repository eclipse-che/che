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
package org.eclipse.che.ide.part.explorer.project.macro;

import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import java.util.Collections;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Folder;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.SelectionModel;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.junit.Before;
import org.mockito.Mock;

/** @author Vlad Zhukovskyi */
public class AbstractExplorerMacroTest {

  public static final String FILE_1_NAME = "file_1.ext";
  public static final String FILE_1_NAME_WITHOUT_EXT = "file_1";
  public static final String FILE_2_NAME = "file_2.ext";
  public static final String FILE_2_NAME_WITHOUT_EXT = "file_2";
  public static final String FOLDER_PATH = "/project";
  public static final String FILE_1_PATH = FOLDER_PATH + "/file_1.ext";
  public static final String FILE_2_PATH = FOLDER_PATH + "/file_2.ext";
  public static final String PROJECTS_ROOT = "/projects";
  public static final String PROJECT_NAME = "project-name";
  public static final String PROJECT_TYPE = "type";

  @Mock PromiseProvider promiseProvider;

  @Mock AppContext appContext;

  @Mock ProjectExplorerPresenter projectExplorer;

  @Mock CoreLocalizationConstant localizationConstants;

  @Mock Tree tree;

  @Mock SelectionModel selectionModel;

  @Mock ResourceNode node1;

  @Mock ResourceNode node2;

  @Mock File file1;

  @Mock File file2;

  @Mock Folder folder;

  @Mock Project project;

  @Before
  public void init() throws Exception {
    when(projectExplorer.getTree()).thenReturn(tree);
    when(tree.getSelectionModel()).thenReturn(selectionModel);
    when(appContext.getProjectsRoot()).thenReturn(Path.valueOf(PROJECTS_ROOT));
  }

  protected void initWithNoFiles() throws Exception {
    when(selectionModel.getSelectedNodes()).thenReturn(Collections.<Node>emptyList());
  }

  protected void initWithOneFile() throws Exception {
    when(selectionModel.getSelectedNodes()).thenReturn(Lists.<Node>newArrayList(node1));
    when(node1.getData()).thenReturn(file1);
    when(file1.getName()).thenReturn(FILE_1_NAME);
    when(file1.isFile()).thenReturn(true);
    when(file1.asFile()).thenReturn(file1);
    when(file1.getNameWithoutExtension()).thenReturn(FILE_1_NAME_WITHOUT_EXT);
    when(file1.getParent()).thenReturn(folder);
    when(file1.getLocation()).thenReturn(Path.valueOf(FILE_1_PATH));
    when(file1.getRelatedProject()).thenReturn(Optional.of(project));
    when(project.getName()).thenReturn(PROJECT_NAME);
    when(project.getType()).thenReturn(PROJECT_TYPE);
    when(folder.getLocation()).thenReturn(Path.valueOf(FOLDER_PATH));
  }

  protected void initWithTwoFiles() throws Exception {
    when(selectionModel.getSelectedNodes()).thenReturn(Lists.<Node>newArrayList(node1, node2));
    when(node1.getData()).thenReturn(file1);
    when(node2.getData()).thenReturn(file2);
    when(file1.getName()).thenReturn(FILE_1_NAME);
    when(file1.isFile()).thenReturn(true);
    when(file1.asFile()).thenReturn(file1);
    when(file1.getNameWithoutExtension()).thenReturn(FILE_1_NAME_WITHOUT_EXT);
    when(file1.getParent()).thenReturn(folder);
    when(file1.getLocation()).thenReturn(Path.valueOf(FILE_1_PATH));
    when(file1.getRelatedProject()).thenReturn(Optional.of(project));
    when(project.getName()).thenReturn(PROJECT_NAME);
    when(project.getType()).thenReturn(PROJECT_TYPE);
    when(file2.getName()).thenReturn(FILE_2_NAME);
    when(file2.isFile()).thenReturn(true);
    when(file2.asFile()).thenReturn(file2);
    when(file2.getNameWithoutExtension()).thenReturn(FILE_2_NAME_WITHOUT_EXT);
    when(file2.getParent()).thenReturn(folder);
    when(file2.getLocation()).thenReturn(Path.valueOf(FILE_2_PATH));
    when(file2.getRelatedProject()).thenReturn(Optional.of(project));
    when(project.getName()).thenReturn(PROJECT_NAME);
    when(folder.getLocation()).thenReturn(Path.valueOf(FOLDER_PATH));
  }
}
