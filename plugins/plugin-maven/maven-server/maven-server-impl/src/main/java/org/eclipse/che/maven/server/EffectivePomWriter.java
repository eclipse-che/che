package org.eclipse.che.maven.server;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingResult;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;
import org.codehaus.plexus.util.xml.XmlWriterUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

class EffectivePomWriter {

  private static final String POM_XSD_URL = "http://maven.apache.org/maven-v4_0_0.xsd";
  private static final String SETTINGS_XSD_URL = "http://maven.apache.org/xsd/settings-1.0.0.xsd";

  private EffectivePomWriter() {}

  public static String getEffectivePom(
      MavenServerImpl server,
      final File pom,
      List<String> activeProfiles,
      List<String> inactiveProfiles) {
    StringWriter stringWriter = new StringWriter();
    try {
      MavenExecutionRequest request =
          server.newMavenRequest(pom, activeProfiles, inactiveProfiles, Collections.emptyList());
      server.runMavenRequest(
          request,
          () -> {
            try {
              ProjectBuilder builder = server.getMavenComponent(ProjectBuilder.class);
              ProjectBuildingResult projectBuildingResult =
                  builder.build(new File(pom.getPath()), request.getProjectBuildingRequest());
              MavenProject project = projectBuildingResult.getProject();
              XMLWriter writer = new PrettyPrintXMLWriter(stringWriter, "    ");
              writeHeader(writer);
              writeEffectivePom(project, writer);
            } catch (ProjectBuildingException | MojoExecutionException e) {
              e.printStackTrace();
              throw new RuntimeException(e);
            }
          });

    } catch (Exception e) {
      return null;
    }
    return stringWriter.toString();
  }

  /**
   * method from org.apache.maven.plugins.help.EffectivePomMojo Method for writing the effective pom
   * informations of the current build.
   *
   * @param project the project of the current build, not null.
   * @param writer the XML writer , not null, not null.
   * @throws MojoExecutionException if any
   */
  private static void writeEffectivePom(MavenProject project, XMLWriter writer)
      throws MojoExecutionException {
    Model pom = project.getModel();
    cleanModel(pom);

    String effectivePom;

    StringWriter sWriter = new StringWriter();
    MavenXpp3Writer pomWriter = new MavenXpp3Writer();
    try {
      pomWriter.write(sWriter, pom);
    } catch (IOException e) {
      throw new MojoExecutionException("Cannot serialize POM to XML.", e);
    }

    effectivePom = addMavenNamespace(sWriter.toString(), true);

    writeComment(writer, "Effective POM for project \'" + project.getId() + "\'");

    writer.writeMarkup(effectivePom);
  }

  /**
   * method from org.apache.maven.plugins.help.EffectivePomMojo Apply some logic to clean the model
   * before writing it.
   *
   * @param pom not null
   */
  private static void cleanModel(Model pom) {
    Properties properties = new SortedProperties();
    properties.putAll(pom.getProperties());
    pom.setProperties(properties);
  }

  /**
   * method from org.apache.maven.plugins.help.AbstractEffectiveMojo Write comments in the Effective
   * POM/settings header.
   *
   * @param writer not null
   */
  protected static void writeHeader(XMLWriter writer) {
    XmlWriterUtil.writeCommentLineBreak(writer);
    XmlWriterUtil.writeComment(writer, "    ");
    // Use ISO8601-format for date and time
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
    XmlWriterUtil.writeComment(
        writer,
        "Generated by Maven Help Plugin on "
            + dateFormat.format(new Date(System.currentTimeMillis())));
    XmlWriterUtil.writeComment(writer, "See: http://maven.apache.org/plugins/maven-help-plugin/");
    XmlWriterUtil.writeComment(writer, "    ");
    XmlWriterUtil.writeCommentLineBreak(writer);

    XmlWriterUtil.writeLineBreak(writer);
  }

  /**
   * method from org.apache.maven.plugins.help.AbstractEffectiveMojo Write comments in a normalize
   * way.
   *
   * @param writer not null
   * @param comment not null
   */
  protected static void writeComment(XMLWriter writer, String comment) {
    XmlWriterUtil.writeCommentLineBreak(writer);
    XmlWriterUtil.writeComment(writer, " ");
    XmlWriterUtil.writeComment(writer, comment);
    XmlWriterUtil.writeComment(writer, " ");
    XmlWriterUtil.writeCommentLineBreak(writer);

    XmlWriterUtil.writeLineBreak(writer);
  }

  /**
   * method from org.apache.maven.plugins.help.AbstractEffectiveMojo Add a Pom/Settings namespaces
   * to the effective XML content.
   *
   * @param effectiveXml not null the effective POM or Settings
   * @param isPom if <code>true</code> add the Pom xsd url, otherwise add the settings xsd url.
   * @return the content of the root element, i.e. &lt;project/&gt; or &lt;settings/&gt; with the
   *     Maven namespace or the original <code>effective</code> if an error occurred.
   * @see #POM_XSD_URL
   * @see #SETTINGS_XSD_URL
   */
  protected static String addMavenNamespace(String effectiveXml, boolean isPom) {
    SAXBuilder builder = new SAXBuilder();

    try {
      Document document = builder.build(new StringReader(effectiveXml));
      Element rootElement = document.getRootElement();

      // added namespaces
      Namespace pomNamespace = Namespace.getNamespace("", "http://maven.apache.org/POM/4.0.0");
      rootElement.setNamespace(pomNamespace);

      Namespace xsiNamespace =
          Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
      rootElement.addNamespaceDeclaration(xsiNamespace);
      if (rootElement.getAttribute("schemaLocation", xsiNamespace) == null) {
        rootElement.setAttribute(
            "schemaLocation",
            "http://maven.apache.org/POM/4.0.0 " + (isPom ? POM_XSD_URL : SETTINGS_XSD_URL),
            xsiNamespace);
      }

      ElementFilter elementFilter = new ElementFilter(Namespace.getNamespace(""));
      for (Iterator<?> i = rootElement.getDescendants(elementFilter); i.hasNext(); ) {
        Element e = (Element) i.next();
        e.setNamespace(pomNamespace);
      }

      StringWriter w = new StringWriter();
      Format format = Format.getPrettyFormat();
      XMLOutputter out = new XMLOutputter(format);
      out.output(document.getRootElement(), w);

      return w.toString();
    } catch (JDOMException e) {
      return effectiveXml;
    } catch (IOException e) {
      return effectiveXml;
    }
  }

  /**
   * Class from org.apache.maven.plugins.help.AbstractEffectiveMojo Properties which provides a
   * sorted keySet().
   */
  protected static class SortedProperties extends Properties {
    /** serialVersionUID */
    static final long serialVersionUID = -8985316072702233744L;

    /** {@inheritDoc} */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Set<Object> keySet() {
      Set<Object> keynames = super.keySet();
      List list = new ArrayList(keynames);
      Collections.sort(list);

      return new LinkedHashSet<Object>(list);
    }
  }
}
