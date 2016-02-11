/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ant.tools.buildfile;

import org.eclipse.che.api.core.ServerException;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Creates build.xml file. That need for Ant automation build tool to be able to build simple project using Ant.
 * Idea based on eclipse Ant build file creator from eclipse sources (org.eclipse.ant.internal.ui.datatransfer.BuildFileCreator).
 *
 * @author Vladyslav Zhukovskii
 */
public class BuildFileGenerator {

    /** Name of the project, which used in build file generation. */
    private String projectName;

    /**
     * Create instance of {@link BuildFileGenerator}.
     *
     * @param projectName
     *         name of the generated project.
     */
    public BuildFileGenerator(String projectName) {
        this.projectName = projectName;
    }

    /** Root container of the document tree. */
    private Document doc;

    /** Element which represent document tree. */
    private Element root;

    /**
     * Build simple Ant build file (build.xml) content based on project name.
     *
     * @return string representation of auto generated build file
     */
    public String getBuildFileContent() throws ServerException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            this.doc = dbf.newDocumentBuilder().newDocument();
            createRoot();
            createProperty();
//            createClassPath();
            createBuild();
            createClean();
            return documentToString(doc);
        } catch (ParserConfigurationException | TransformerException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    /**
     * Create project tag.
     * <project name="project_name" default="build" basedir=".">
     */
    private void createRoot() {
        root = doc.createElement("project");
        root.setAttribute("name", projectName);
        root.setAttribute("default", "build");
        root.setAttribute("basedir", ".");

        doc.appendChild(root);
    }


    /**
     * Create property tag.
     * <property name="value" {location,value}="value"/>
     */
    private void createProperty() {
        Map<String, String> locationProperties = new TreeMap<>();
        locationProperties.put("build", "${basedir}/build");
        locationProperties.put("build.classes", "${build}/classes");
        locationProperties.put("src.dir", "${basedir}/src");

        Element nameProperty = doc.createElement("property");
        nameProperty.setAttribute("name", "name");
        nameProperty.setAttribute("value", projectName);

        Node node = root.getFirstChild();
        node = root.insertBefore(nameProperty, node);

        for (Map.Entry<String, String> locationProperty : locationProperties.entrySet()) {
            Element locationElement = doc.createElement("property");
            locationElement.setAttribute("name", locationProperty.getKey());
            locationElement.setAttribute("location", locationProperty.getValue());
            node = node.getNextSibling();
            node = root.insertBefore(locationElement, node);
        }
    }

//    /**
//     * Create classpath tag.
//     * <path id="libs.dir">
//     * <fileset dir="lib" includes="**\/*\.jar"/>
//     * </path>
//     */
//    private void createClassPath() {
//        Element path = doc.createElement("path");
//        path.setAttribute("id", "libs.dir");
//
//        Element fieldSet = doc.createElement("fileset");
//        fieldSet.setAttribute("dir", "lib");
//        fieldSet.setAttribute("includes", "**/*.jar");
//
//        path.appendChild(fieldSet);
//
//        root.appendChild(path);
//    }

    /**
     * Create build target tag.
     * <target depends="clean" description="Builds the application" name="build">
     * ...
     * </target>
     */
    private void createBuild() {
        //Insert comment
        Comment buildComment = doc.createComment("Application build");
        root.appendChild(buildComment);

        //Create main target tag
        Element target = doc.createElement("target");
        target.setAttribute("name", "build");
        target.setAttribute("depends", "clean");
        target.setAttribute("description", "Builds the application");

        //Insert comment
        Comment createDirectoryComment = doc.createComment("Create directory");
        target.appendChild(createDirectoryComment);

        //Create mkdir tag inside target
        Element mkdir = doc.createElement("mkdir");
        mkdir.setAttribute("dir", "${build.classes}");
        target.appendChild(mkdir);

        //Insert comment
        Comment compileSourcesComment = doc.createComment("Compile source code");
        target.appendChild(compileSourcesComment);

        //Create javac tag inside target
        Element javac = doc.createElement("javac");
        javac.setAttribute("srcdir", "${src.dir}");
        javac.setAttribute("destdir", "${build.classes}");
        javac.setAttribute("debug", "false");
        javac.setAttribute("deprecation", "true");
        javac.setAttribute("optimize", "true");
        javac.setAttribute("includeantruntime", "true");

//        //Create classpath tag inside javac
//        Element classpath = doc.createElement("classpath");
//        classpath.setAttribute("refid", "libs.dir");
//        javac.appendChild(classpath);

        target.appendChild(javac);

        //Insert comment
        Comment copyNecessaryFiles = doc.createComment("Copy necessary files");
        target.appendChild(copyNecessaryFiles);

        //Create copy tag inside target
        Element copy = doc.createElement("copy");
        copy.setAttribute("todir", "${build.classes}");

        //Create fileset tag inside copy
        Element copyFileset = doc.createElement("fileset");
        copyFileset.setAttribute("dir", "${src.dir}");
        copyFileset.setAttribute("includes", "**/*.*");
        copyFileset.setAttribute("excludes", "**/*.java");
        copy.appendChild(copyFileset);

        target.appendChild(copy);

        //Insert comment
        Comment createJarComment = doc.createComment("Create JAR-file");
        target.appendChild(createJarComment);

        //Create jar tag inside target
        Element jar = doc.createElement("jar");
        jar.setAttribute("jarfile", "${build}/${name}.jar");

        //Create fileset tag inside jar
        Element jarFileset = doc.createElement("fileset");
        jarFileset.setAttribute("dir", "${build.classes}");
        jar.appendChild(jarFileset);

        target.appendChild(jar);

        root.appendChild(target);
    }

    /**
     * Create clean target tag.
     * <target description="Remove all temporary files" name="clean">
     * ...
     * </target>
     */
    private void createClean() {
        //Insert comment
        Comment cleanUpComment = doc.createComment("Clean up");
        root.appendChild(cleanUpComment);

        //Create main target tag
        Element target = doc.createElement("target");
        target.setAttribute("name", "clean");
        target.setAttribute("description", "Remove all temporary files");

        //Insert comment
        Comment deleteFileComment = doc.createComment("Delete files");
        target.appendChild(deleteFileComment);

        //Create delete tag inside target tag
        Element delete = doc.createElement("delete");
        delete.setAttribute("dir", "${build.classes}");

        target.appendChild(delete);

        root.appendChild(target);
    }

    /** Convert document to formatted XML string. */
    private String documentToString(Document doc) throws TransformerException {
        StringWriter writer = new StringWriter();
        Source source = new DOMSource(doc);
        Result result = new StreamResult(writer);
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute("indent-number", "4");

        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);

        return writer.toString();
    }
}
