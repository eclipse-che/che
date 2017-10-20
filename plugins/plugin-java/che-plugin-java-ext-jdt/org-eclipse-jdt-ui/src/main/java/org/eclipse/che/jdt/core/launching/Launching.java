/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.core.launching;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** @author Evgen Vidolob */
public class Launching {
  private static final Logger LOG = LoggerFactory.getLogger(Launching.class);
  /** The id of the JDT launching plug-in (value <code>"org.eclipse.jdt.launching"</code>). */
  public static final String ID_PLUGIN = "org.eclipse.jdt.launching"; // $NON-NLS-1$

  public static boolean DEBUG_JRE_CONTAINER = false;
  /** Mapping of top-level VM installation directories to library info for that VM. */
  private static Map<String, LibraryInfo> fgLibraryInfoMap = null;

  private static Launching fgLaunching;

  /** Mutex for checking the time stamp of an install location */
  private static Object installLock = new Object();

  /** List of install locations that have been detected to have changed */
  private static HashSet<String> fgHasChanged = new HashSet<>();

  /**
   * Mapping of the last time the directory of a given SDK was modified. <br>
   * <br>
   * Mapping: <code>Map&lt;String,Long&gt;</code>
   */
  private static Map<String, Long> fgInstallTimeMap = null;

  /**
   * Status code indicating an unexpected error.
   *
   * @since 3.4
   */
  public static final int ERROR = 125;

  /**
   * Returns the library info that corresponds to the specified JRE install path, or <code>null
   * </code> if none.
   *
   * @param javaInstallPath the absolute path to the java executable
   * @return the library info that corresponds to the specified JRE install path, or <code>null
   *     </code> if none
   */
  public static LibraryInfo getLibraryInfo(String javaInstallPath) {
    if (fgLibraryInfoMap == null) {
      restoreLibraryInfo();
    }
    return fgLibraryInfoMap.get(javaInstallPath);
  }

  /** Restores library information for VMs */
  private static void restoreLibraryInfo() {
    fgLibraryInfoMap = new HashMap<String, LibraryInfo>(10);
    IPath libPath = getDefault().getStateLocation();
    libPath = libPath.append("libraryInfos.xml"); // $NON-NLS-1$
    File file = libPath.toFile();
    if (file.exists()) {
      try {
        InputStream stream = new BufferedInputStream(new FileInputStream(file));
        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        parser.setErrorHandler(new DefaultHandler());
        Element root = parser.parse(new InputSource(stream)).getDocumentElement();
        if (!root.getNodeName().equals("libraryInfos")) { // $NON-NLS-1$
          return;
        }

        NodeList list = root.getChildNodes();
        int length = list.getLength();
        for (int i = 0; i < length; ++i) {
          Node node = list.item(i);
          short type = node.getNodeType();
          if (type == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            String nodeName = element.getNodeName();
            if (nodeName.equalsIgnoreCase("libraryInfo")) { // $NON-NLS-1$
              String version = element.getAttribute("version"); // $NON-NLS-1$
              String location = element.getAttribute("home"); // $NON-NLS-1$
              String[] bootpath = getPathsFromXML(element, "bootpath"); // $NON-NLS-1$
              String[] extDirs = getPathsFromXML(element, "extensionDirs"); // $NON-NLS-1$
              String[] endDirs = getPathsFromXML(element, "endorsedDirs"); // $NON-NLS-1$
              if (location != null) {
                LibraryInfo info = new LibraryInfo(version, bootpath, extDirs, endDirs);
                fgLibraryInfoMap.put(location, info);
              }
            }
          }
        }
      } catch (IOException | SAXException | ParserConfigurationException e) {
        log(e);
      }
    }
  }

  /**
   * Returns the location in the local file system of the plug-in state area for this plug-in. If
   * the plug-in state area did not exist prior to this call, it is created.
   *
   * @throws IllegalStateException, when the system is running with no data area (-data @none), or
   *     when a data area has not been set yet.
   * @return a local file system path
   */
  public final IPath getStateLocation() throws IllegalStateException {

    Path path = new Path("/tmp/codenvy/");
    File file = path.toFile();
    if (!file.exists()) {
      file.mkdirs();
    }
    return path;
  }

  /**
   * Returns the singleton instance of <code>LaunchingPlugin</code>
   *
   * @return the singleton instance of <code>LaunchingPlugin</code>
   */
  public static Launching getDefault() {
    if (fgLaunching == null) {
      fgLaunching = new Launching();
    }
    return fgLaunching;
  }

  /**
   * Logs the specified status
   *
   * @param status the status to log
   */
  public static void log(IStatus status) {
    //        getDefault().getLog().log(status);
    LOG.error(status.getMessage(), status.getException());
  }

  /**
   * Logs the specified message, by creating a new <code>Status</code>
   *
   * @param message the message to log as an error status
   */
  public static void log(String message) {
    log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
  }

  /**
   * Logs the specified exception by creating a new <code>Status</code>
   *
   * @param e the {@link Throwable} to log as an error
   */
  public static void log(Throwable e) {
    log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
  }

  /**
   * Convenience method which returns the unique identifier of this plug-in.
   *
   * @return the id of the {@link Launching}
   */
  public static String getUniqueIdentifier() {
    return ID_PLUGIN;
  }

  /**
   * Returns paths stored in XML
   *
   * @param lib the library path in {@link Element} form
   * @param pathType the type of the path
   * @return paths stored in XML
   */
  private static String[] getPathsFromXML(Element lib, String pathType) {
    List<String> paths = new ArrayList<String>();
    NodeList list = lib.getChildNodes();
    int length = list.getLength();
    for (int i = 0; i < length; ++i) {
      Node node = list.item(i);
      short type = node.getNodeType();
      if (type == Node.ELEMENT_NODE) {
        Element element = (Element) node;
        String nodeName = element.getNodeName();
        if (nodeName.equalsIgnoreCase(pathType)) {
          NodeList entries = element.getChildNodes();
          int numEntries = entries.getLength();
          for (int j = 0; j < numEntries; j++) {
            Node n = entries.item(j);
            short t = n.getNodeType();
            if (t == Node.ELEMENT_NODE) {
              Element entryElement = (Element) n;
              String name = entryElement.getNodeName();
              if (name.equals("entry")) { // $NON-NLS-1$
                String path = entryElement.getAttribute("path"); // $NON-NLS-1$
                if (path != null && path.length() > 0) {
                  paths.add(path);
                }
              }
            }
          }
        }
      }
    }
    return paths.toArray(new String[paths.size()]);
  }

  /**
   * Checks to see if the time stamp of the file describe by the given location string has been
   * modified since the last recorded time stamp. If there is no last recorded time stamp we assume
   * it has changed. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=266651 for more information
   *
   * @param location the location of the SDK we want to check the time stamp for
   * @return <code>true</code> if the time stamp has changed compared to the cached one or if there
   *     is no recorded time stamp, <code>false</code> otherwise.
   */
  public static boolean timeStampChanged(String location) {
    synchronized (installLock) {
      if (fgHasChanged.contains(location)) {
        return true;
      }
      File file = new File(location);
      if (file.exists()) {
        if (fgInstallTimeMap == null) {
          readInstallInfo();
        }
        Long stamp = fgInstallTimeMap.get(location);
        long fstamp = file.lastModified();
        if (stamp != null) {
          if (stamp.longValue() == fstamp) {
            return false;
          }
        }
        // if there is no recorded stamp we have to assume it is new
        stamp = new Long(fstamp);
        fgInstallTimeMap.put(location, stamp);
        writeInstallInfo();
        fgHasChanged.add(location);
        return true;
      }
    }
    return false;
  }

  /**
   * Reads the file of saved time stamps and populates the {@link #fgInstallTimeMap}. See
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=266651 for more information
   *
   * @since 3.7
   */
  private static void readInstallInfo() {
    fgInstallTimeMap = new HashMap<String, Long>();
    IPath libPath = getDefault().getStateLocation();
    libPath = libPath.append(".install.xml"); // $NON-NLS-1$
    File file = libPath.toFile();
    if (file.exists()) {
      try {
        InputStream stream = new BufferedInputStream(new FileInputStream(file));
        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        parser.setErrorHandler(new DefaultHandler());
        Element root = parser.parse(new InputSource(stream)).getDocumentElement();
        if (root.getNodeName().equalsIgnoreCase("dirs")) { // $NON-NLS-1$
          NodeList nodes = root.getChildNodes();
          Node node = null;
          Element element = null;
          for (int i = 0; i < nodes.getLength(); i++) {
            node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
              element = (Element) node;
              if (element.getNodeName().equalsIgnoreCase("entry")) { // $NON-NLS-1$
                String loc = element.getAttribute("loc"); // $NON-NLS-1$
                String stamp = element.getAttribute("stamp"); // $NON-NLS-1$
                try {
                  Long l = new Long(stamp);
                  fgInstallTimeMap.put(loc, l);
                } catch (NumberFormatException nfe) {
                  // do nothing
                }
              }
            }
          }
        }
      } catch (IOException e) {
        log(e);
      } catch (ParserConfigurationException e) {
        log(e);
      } catch (SAXException e) {
        log(e);
      }
    }
  }

  /**
   * Sets the library info that corresponds to the specified JRE install path.
   *
   * @param javaInstallPath home location for a JRE
   * @param info the library information, or <code>null</code> to remove
   */
  public static void setLibraryInfo(String javaInstallPath, LibraryInfo info) {
    if (fgLibraryInfoMap == null) {
      restoreLibraryInfo();
    }
    if (info == null) {
      fgLibraryInfoMap.remove(javaInstallPath);
      if (fgInstallTimeMap != null) {
        fgInstallTimeMap.remove(javaInstallPath);
        writeInstallInfo();
      }

    } else {
      fgLibraryInfoMap.put(javaInstallPath, info);
    }
    // once the library info has been set we can forget it has changed
    fgHasChanged.remove(javaInstallPath);
    saveLibraryInfo();
  }

  /** Saves the library info in a local workspace state location */
  private static void saveLibraryInfo() {
    OutputStream stream = null;
    try {
      String xml = getLibraryInfoAsXML();
      IPath libPath = getDefault().getStateLocation();
      libPath = libPath.append("libraryInfos.xml"); // $NON-NLS-1$
      File file = libPath.toFile();
      if (!file.exists()) {
        file.createNewFile();
      }
      stream = new BufferedOutputStream(new FileOutputStream(file));
      stream.write(xml.getBytes("UTF8")); // $NON-NLS-1$
    } catch (IOException e) {
      log(e);
    } catch (CoreException e) {
      log(e);
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException e1) {
        }
      }
    }
  }
  /**
   * Return the VM definitions contained in this object as a String of XML. The String is suitable
   * for storing in the workbench preferences.
   *
   * <p>The resulting XML is compatible with the static method <code>parseXMLIntoContainer</code>.
   *
   * @return String the results of flattening this object into XML
   * @throws CoreException if this method fails. Reasons include:
   *     <ul>
   *       <li>serialization of the XML document failed
   *     </ul>
   */
  private static String getLibraryInfoAsXML() throws CoreException {

    Document doc = newDocument();
    Element config = doc.createElement("libraryInfos"); // $NON-NLS-1$
    doc.appendChild(config);

    // Create a node for each info in the table
    Iterator<String> locations = fgLibraryInfoMap.keySet().iterator();
    while (locations.hasNext()) {
      String home = locations.next();
      LibraryInfo info = fgLibraryInfoMap.get(home);
      Element locationElemnet = infoAsElement(doc, info);
      locationElemnet.setAttribute("home", home); // $NON-NLS-1$
      config.appendChild(locationElemnet);
    }

    // Serialize the Document and return the resulting String
    return serializeDocument(doc);
  }

  /**
   * Creates an XML element for the given info.
   *
   * @param doc the backing {@link Document}
   * @param info the {@link LibraryInfo} to add to the {@link Document}
   * @return Element
   */
  private static Element infoAsElement(Document doc, LibraryInfo info) {
    Element libraryElement = doc.createElement("libraryInfo"); // $NON-NLS-1$
    libraryElement.setAttribute("version", info.getVersion()); // $NON-NLS-1$
    appendPathElements(doc, "bootpath", libraryElement, info.getBootpath()); // $NON-NLS-1$
    appendPathElements(
        doc, "extensionDirs", libraryElement, info.getExtensionDirs()); // $NON-NLS-1$
    appendPathElements(doc, "endorsedDirs", libraryElement, info.getEndorsedDirs()); // $NON-NLS-1$
    return libraryElement;
  }

  /**
   * Appends path elements to the given library element, rooted by an element of the given type.
   *
   * @param doc the backing {@link Document}
   * @param elementType the kind of {@link Element} to create
   * @param libraryElement the {@link Element} describing a given {@link LibraryInfo} object
   * @param paths the paths to add
   */
  private static void appendPathElements(
      Document doc, String elementType, Element libraryElement, String[] paths) {
    if (paths.length > 0) {
      Element child = doc.createElement(elementType);
      libraryElement.appendChild(child);
      for (int i = 0; i < paths.length; i++) {
        String path = paths[i];
        Element entry = doc.createElement("entry"); // $NON-NLS-1$
        child.appendChild(entry);
        entry.setAttribute("path", path); // $NON-NLS-1$
      }
    }
  }

  /**
   * Returns a Document that can be used to build a DOM tree
   *
   * @return the Document
   * @throws ParserConfigurationException if an exception occurs creating the document builder
   */
  public static Document getDocument() throws ParserConfigurationException {
    DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
    Document doc = docBuilder.newDocument();
    return doc;
  }

  /**
   * Creates and returns a new XML document.
   *
   * @return a new XML document
   * @throws CoreException if unable to create a new document
   */
  public static Document newDocument() throws CoreException {
    try {
      return getDocument();
    } catch (ParserConfigurationException e) {
      abort("Unable to create new XML document.", e); // $NON-NLS-1$
    }
    return null;
  }

  /**
   * Serializes a XML document into a string - encoded in UTF8 format, with platform line
   * separators.
   *
   * @param doc document to serialize
   * @return the document as a string
   * @throws TransformerException if an unrecoverable error occurs during the serialization
   * @throws IOException if the encoding attempted to be used is not supported
   */
  private static String serializeDocumentInt(Document doc)
      throws TransformerException, IOException {
    ByteArrayOutputStream s = new ByteArrayOutputStream();
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer transformer = factory.newTransformer();
    transformer.setOutputProperty(OutputKeys.METHOD, "xml"); // $NON-NLS-1$
    transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // $NON-NLS-1$
    DOMSource source = new DOMSource(doc);
    StreamResult outputTarget = new StreamResult(s);
    transformer.transform(source, outputTarget);
    return s.toString("UTF8"); // $NON-NLS-1$
  }

  /**
   * Serializes the given XML document into a string.
   *
   * @param document XML document to serialize
   * @return a string representing the given document
   * @throws CoreException if unable to serialize the document
   */
  public static String serializeDocument(Document document) throws CoreException {
    try {
      return serializeDocumentInt(document);
    } catch (TransformerException e) {
      abort("Unable to serialize XML document.", e); // $NON-NLS-1$
    } catch (IOException e) {
      abort("Unable to serialize XML document.", e); // $NON-NLS-1$
    }
    return null;
  }

  /**
   * Throws an exception with the given message and underlying exception.
   *
   * @param message error message
   * @param exception underlying exception, or <code>null</code>
   * @throws CoreException if a problem is encountered
   */
  private static void abort(String message, Throwable exception) throws CoreException {
    IStatus status = new Status(IStatus.ERROR, getUniqueIdentifier(), ERROR, message, exception);
    throw new CoreException(status);
  }

  /**
   * Writes out the mappings of SDK install time stamps to disk. See
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=266651 for more information.
   */
  private static void writeInstallInfo() {
    if (fgInstallTimeMap != null) {
      OutputStream stream = null;
      try {
        Document doc = newDocument();
        Element root = doc.createElement("dirs"); // $NON-NLS-1$
        doc.appendChild(root);
        Map.Entry<String, Long> entry = null;
        Element e = null;
        String key = null;
        for (Iterator<Map.Entry<String, Long>> i = fgInstallTimeMap.entrySet().iterator();
            i.hasNext(); ) {
          entry = i.next();
          key = entry.getKey();
          if (fgLibraryInfoMap == null || fgLibraryInfoMap.containsKey(key)) {
            // only persist the info if the library map also has info OR is null - prevent
            // persisting deleted JRE information
            e = doc.createElement("entry"); // $NON-NLS-1$
            root.appendChild(e);
            e.setAttribute("loc", key); // $NON-NLS-1$
            e.setAttribute("stamp", entry.getValue().toString()); // $NON-NLS-1$
          }
        }
        String xml = serializeDocument(doc);
        IPath libPath = getDefault().getStateLocation();
        libPath = libPath.append(".install.xml"); // $NON-NLS-1$
        File file = libPath.toFile();
        if (!file.exists()) {
          file.createNewFile();
        }
        stream = new BufferedOutputStream(new FileOutputStream(file));
        stream.write(xml.getBytes("UTF8")); // $NON-NLS-1$
      } catch (IOException e) {
        log(e);
      } catch (CoreException e) {
        log(e);
      } finally {
        if (stream != null) {
          try {
            stream.close();
          } catch (IOException e1) {
          }
        }
      }
    }
  }

  public static File getFileInPlugin(IPath path) {
    try {
      return new File(
          Launching.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()
              + path.toString());
    } catch (URISyntaxException e) {
      return null;
    }
  }

  /**
   * Compares two URL for equality, but do not connect to do DNS resolution
   *
   * @param url1 a given URL
   * @param url2 another given URL to compare to url1
   * @return <code>true</code> if the URLs are equal, <code>false</code> otherwise
   * @since 3.5
   */
  public static boolean sameURL(URL url1, URL url2) {
    if (url1 == url2) {
      return true;
    }
    if (url1 == null ^ url2 == null) {
      return false;
    }
    // check if URL are file: URL as we may have two URL pointing to the same doc location
    // but with different representation - (i.e. file:/C;/ and file:C:/)
    final boolean isFile1 = "file".equalsIgnoreCase(url1.getProtocol()); // $NON-NLS-1$
    final boolean isFile2 = "file".equalsIgnoreCase(url2.getProtocol()); // $NON-NLS-1$
    if (isFile1 && isFile2) {
      File file1 = new File(url1.getFile());
      File file2 = new File(url2.getFile());
      return file1.equals(file2);
    }
    // URL1 XOR URL2 is a file, return false. (They either both need to be files, or neither)
    if (isFile1 ^ isFile2) {
      return false;
    }
    return getExternalForm(url1).equals(getExternalForm(url2));
  }

  /**
   * Gets the external form of this URL. In particular, it trims any white space, removes a trailing
   * slash and creates a lower case string.
   *
   * @param url the URL to get the {@link String} value of
   * @return the lower-case {@link String} form of the given URL
   */
  private static String getExternalForm(URL url) {
    String externalForm = url.toExternalForm();
    if (externalForm == null) {
      return ""; // $NON-NLS-1$
    }
    externalForm = externalForm.trim();
    if (externalForm.endsWith("/")) { // $NON-NLS-1$
      // Remove the trailing slash
      externalForm = externalForm.substring(0, externalForm.length() - 1);
    }
    return externalForm.toLowerCase();
  }
}
