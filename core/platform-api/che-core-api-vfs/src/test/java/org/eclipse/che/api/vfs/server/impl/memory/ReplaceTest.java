/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.server.impl.memory;


import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.shared.dto.ReplacementSet;
import org.eclipse.che.api.vfs.shared.dto.Variable;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.core.impl.ContainerResponse;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

public class ReplaceTest extends MemoryFileSystemTest {
    private VirtualFile replaceTestFolder;

    static final String find1            = "VAR_NUM_1";
    static final String find2            = "VAR_NUM_2";
    static final String replace1         = "value1";
    static final String replace2         = "value2";
    static final String template         = "some super content\n with ${%s} and another variable ${%s}";
    static final String templateReplaced = "some super content\n with %s and another variable %s";


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        replaceTestFolder = mountPoint.getRoot().createFolder(name);
    }

    public void testSimpleReplaceVar() throws Exception {
        final String fileName = "test_file.txt";
        VirtualFile file = replaceTestFolder
                .createFile(fileName,
                            new ByteArrayInputStream(String.format(template, find1, find2).getBytes()));
        List<Variable> variables = new ArrayList<>(2);
        variables.add(DtoFactory.getInstance().createDto(Variable.class).withFind(find1).withReplace(replace1));
        variables.add(DtoFactory.getInstance().createDto(Variable.class).withFind(find2).withReplace(replace2));

        List<String> expression = Arrays.asList("test_file.txt");

        ReplacementSet replacementSet =
                DtoFactory.getInstance().createDto(ReplacementSet.class).withEntries(variables).withFiles(expression);
        Map<String, List<String>> h = new HashMap<>(1);
        h.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON));

        String path = SERVICE_URI + "replace/" + replaceTestFolder.getName();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, h,
                                                      String.format("[%s]",
                                                                    DtoFactory.getInstance().toJson(replacementSet))
                                                            .getBytes(), null, null);
        assertEquals(204, response.getStatus());
        assertEquals(String.format(templateReplaced, replace1, replace2),
                     IoUtil.readAndCloseQuietly(mountPoint.getVirtualFileById(file.getId()).getContent().getStream()));
    }

    public void testSimpleReplaceByExtensionVar() throws Exception {
        final String fileName1 = "test_file.txt";
        final String fileName2 = "test_file.java";
        final String fileName3 = "test_file.class";

        VirtualFile file1 = replaceTestFolder
                .createFile(fileName1, new ByteArrayInputStream(String.format(template, find1, find2).getBytes()));
        VirtualFile file2 = replaceTestFolder
                .createFile(fileName2, new ByteArrayInputStream(String.format(template, find1, find2).getBytes()));
        VirtualFile file3 = replaceTestFolder
                .createFile(fileName3, new ByteArrayInputStream(String.format(template, find1, find2).getBytes()));

        List<Variable> variables = new ArrayList<>(2);
        variables.add(DtoFactory.getInstance().createDto(Variable.class).withFind(find1).withReplace(replace1));
        variables.add(DtoFactory.getInstance().createDto(Variable.class).withFind(find2).withReplace(replace2));

        List<String> expression = Arrays.asList("test_(.*).java");

        ReplacementSet replacementSet = DtoFactory.getInstance().createDto(ReplacementSet.class).withEntries(variables).withFiles(expression);
        Map<String, List<String>> h = new HashMap<>(1);
        h.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON));

        String path = SERVICE_URI + "replace/" + replaceTestFolder.getName();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, h,
                                                      String.format("[%s]", DtoFactory.getInstance().toJson(replacementSet)).getBytes() , null, null);
        assertEquals(204, response.getStatus());
        assertEquals(String.format(templateReplaced, replace1, replace2), IoUtil.readAndCloseQuietly(mountPoint.getVirtualFileById(file2.getId()).getContent().getStream()));
        assertEquals(String.format(template, find1, find2), IoUtil.readAndCloseQuietly(mountPoint.getVirtualFileById(file1.getId()).getContent().getStream()));
        assertEquals(String.format(template, find1, find2), IoUtil.readAndCloseQuietly(mountPoint.getVirtualFileById(file3.getId()).getContent().getStream()));
    }


    public void testSimpleReplaceMutipassVar() throws Exception {
        final String template_local = "some super content\n with ${%s} and another variable %s";
        final String fileName = "test_file.txt";
        VirtualFile file = replaceTestFolder
                .createFile(fileName,
                            new ByteArrayInputStream(String.format(template_local, find1, find2).getBytes()));
        List<Variable> variables = new ArrayList<>(2);
        variables.add(DtoFactory.getInstance().createDto(Variable.class).withFind(find1).withReplace(replace1));
        variables.add(DtoFactory.getInstance().createDto(Variable.class).withFind(find2).withReplace(replace2)
                                .withReplacemode("text_multipass"));

        List<String> expression = Arrays.asList("test_file.txt");

        ReplacementSet replacementSet =
                DtoFactory.getInstance().createDto(ReplacementSet.class).withEntries(variables).withFiles(expression);
        Map<String, List<String>> h = new HashMap<>(1);
        h.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON));

        String path = SERVICE_URI + "replace/" + replaceTestFolder.getName();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, h,
                                                      String.format("[%s]",
                                                                    DtoFactory.getInstance().toJson(replacementSet))
                                                            .getBytes(), null, null);
        assertEquals(204, response.getStatus());
        assertEquals(String.format(templateReplaced, replace1, replace2),
                     IoUtil.readAndCloseQuietly(mountPoint.getVirtualFileById(file.getId()).getContent().getStream()));
    }

    public void testSimpleReplaceAllMatched() throws Exception {
        final String fileName1 = "test_file.txt";
        final String fileName2 = "test_file.java";
        final String fileName3 = "test_file.class";

        VirtualFile file1 = replaceTestFolder
                .createFile(fileName1,
                            new ByteArrayInputStream(String.format(template, find1, find2).getBytes()));
        VirtualFile file2 = replaceTestFolder
                .createFile(fileName2,
                            new ByteArrayInputStream(String.format(template, find1, find2).getBytes()));
        VirtualFile file3 = replaceTestFolder
                .createFile(fileName3,
                            new ByteArrayInputStream(String.format(template, find1, find2).getBytes()));

        List<Variable> variables = new ArrayList<>(2);
        variables.add(DtoFactory.getInstance().createDto(Variable.class).withFind(find1).withReplace(replace1));
        variables.add(DtoFactory.getInstance().createDto(Variable.class).withFind(find2).withReplace(replace2));

        List<String> expression = Arrays.asList("test_(.*)");

        ReplacementSet replacementSet =
                DtoFactory.getInstance().createDto(ReplacementSet.class).withEntries(variables).withFiles(expression);
        Map<String, List<String>> h = new HashMap<>(1);
        h.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON));

        String path = SERVICE_URI + "replace/" + replaceTestFolder.getName();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, h,
                                                      String.format("[%s]",
                                                                    DtoFactory.getInstance().toJson(replacementSet))
                                                            .getBytes(), null, null);
        assertEquals(204, response.getStatus());
        assertEquals(String.format(templateReplaced, replace1, replace2),
                     IoUtil.readAndCloseQuietly(mountPoint.getVirtualFileById(file1.getId()).getContent().getStream()));
        assertEquals(String.format(templateReplaced, replace1, replace2),
                     IoUtil.readAndCloseQuietly(mountPoint.getVirtualFileById(file2.getId()).getContent().getStream()));
        assertEquals(String.format(templateReplaced, replace1, replace2),
                     IoUtil.readAndCloseQuietly(mountPoint.getVirtualFileById(file3.getId()).getContent().getStream()));
    }

    public void testSimpleReplaceMatchedByQ() throws Exception {
        final String fileName1 = "test_File.txt";
        final String fileName2 = "test_Mile.bat";

        VirtualFile file1 = replaceTestFolder
                .createFile(fileName1,
                            new ByteArrayInputStream(String.format(template, find1, find2).getBytes()));
        VirtualFile file2 = replaceTestFolder
                .createFile(fileName2,
                            new ByteArrayInputStream(String.format(template, find1, find2).getBytes()));

        List<Variable> variables = new ArrayList<>(2);
        variables.add(DtoFactory.getInstance().createDto(Variable.class).withFind(find1).withReplace(replace1));
        variables.add(DtoFactory.getInstance().createDto(Variable.class).withFind(find2).withReplace(replace2));

        List<String> expression = Arrays.asList("test_(.+)ile(.*)");

        ReplacementSet replacementSet =
                DtoFactory.getInstance().createDto(ReplacementSet.class).withEntries(variables).withFiles(expression);
        Map<String, List<String>> h = new HashMap<>(1);
        h.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON));

        String path = SERVICE_URI + "replace/" + replaceTestFolder.getName();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, h,
                                                      String.format("[%s]",
                                                                    DtoFactory.getInstance().toJson(replacementSet))
                                                            .getBytes(), null, null);
        assertEquals(204, response.getStatus());
        assertEquals(String.format(templateReplaced, replace1, replace2),
                     IoUtil.readAndCloseQuietly(mountPoint.getVirtualFileById(file1.getId()).getContent().getStream()));
        assertEquals(String.format(templateReplaced, replace1, replace2),
                     IoUtil.readAndCloseQuietly(mountPoint.getVirtualFileById(file2.getId()).getContent().getStream()));
    }


    public void testReplaceInSubFolderVar() throws Exception {
        final String fileName = "test_file.txt";
        VirtualFile src = replaceTestFolder.createFolder("src/main/java");
        VirtualFile file = src
                .createFile(fileName,
                            new ByteArrayInputStream(String.format(template, find1, find2).getBytes()));
        List<Variable> variables = new ArrayList<>(2);
        variables.add(DtoFactory.getInstance().createDto(Variable.class).withFind(find1).withReplace(replace1));
        variables.add(DtoFactory.getInstance().createDto(Variable.class).withFind(find2).withReplace(replace2));

        List<String> expression = Arrays.asList("src/main/java/(.*)");

        ReplacementSet replacementSet =
                DtoFactory.getInstance().createDto(ReplacementSet.class).withEntries(variables).withFiles(expression);
        Map<String, List<String>> h = new HashMap<>(1);
        h.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON));

        String path = SERVICE_URI + "replace/" + replaceTestFolder.getName();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, h,
                                                      String.format("[%s]",
                                                                    DtoFactory.getInstance().toJson(replacementSet))
                                                            .getBytes(), null, null);
        assertEquals(204, response.getStatus());
        assertEquals(String.format(templateReplaced, replace1, replace2),
                     IoUtil.readAndCloseQuietly(mountPoint.getVirtualFileById(file.getId()).getContent().getStream()));
    }
}
