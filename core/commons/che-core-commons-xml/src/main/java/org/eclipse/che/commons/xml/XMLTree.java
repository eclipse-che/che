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
package org.eclipse.che.commons.xml;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static com.google.common.io.ByteStreams.toByteArray;
import static java.nio.file.Files.readAllBytes;
import static java.util.Objects.requireNonNull;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
import static javax.xml.XMLConstants.XML_NS_URI;
import static javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES;
import static javax.xml.stream.XMLInputFactory.SUPPORT_DTD;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION;
import static javax.xml.stream.XMLStreamConstants.SPACE;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static javax.xml.xpath.XPathConstants.NODESET;
import static javax.xml.xpath.XPathConstants.STRING;
import static org.eclipse.che.commons.xml.XMLTreeUtil.SPACES_IN_TAB;
import static org.eclipse.che.commons.xml.XMLTreeUtil.UTF_8;
import static org.eclipse.che.commons.xml.XMLTreeUtil.asElement;
import static org.eclipse.che.commons.xml.XMLTreeUtil.asElements;
import static org.eclipse.che.commons.xml.XMLTreeUtil.closeTagLength;
import static org.eclipse.che.commons.xml.XMLTreeUtil.indexOf;
import static org.eclipse.che.commons.xml.XMLTreeUtil.indexOfAttributeName;
import static org.eclipse.che.commons.xml.XMLTreeUtil.insertBetween;
import static org.eclipse.che.commons.xml.XMLTreeUtil.insertInto;
import static org.eclipse.che.commons.xml.XMLTreeUtil.lastIndexOf;
import static org.eclipse.che.commons.xml.XMLTreeUtil.level;
import static org.eclipse.che.commons.xml.XMLTreeUtil.openTagLength;
import static org.eclipse.che.commons.xml.XMLTreeUtil.replaceAll;
import static org.eclipse.che.commons.xml.XMLTreeUtil.rootStart;
import static org.eclipse.che.commons.xml.XMLTreeUtil.single;
import static org.eclipse.che.commons.xml.XMLTreeUtil.tabulate;
import static org.w3c.dom.Node.CDATA_SECTION_NODE;
import static org.w3c.dom.Node.TEXT_NODE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML tool which provides abilities to modify and search information in xml document without
 * affecting of existing formatting, comments.
 *
 * <p>XMLTree delegates out of the box implementation of org.w3c.dom and provides a lot of
 * functionality such as XPath selection. How does the XMLTree let content in required state? The
 * main idea is simple: know XML elements positions! If we know elements positions and source bytes
 * we can easily manipulate content as we want. So each time when client updates tree, tree rewrites
 * source bytes with new information, indexes new elements, updates delegated document, shifts
 * needed existed elements positions. As you may see there are a lot of data manipulations when
 * update is going, so <b>you should not use this tool for parsing huge xml documents or for often
 * complex updates.</b>
 *
 * <p>XPath is embedded to XMLTree so each query to tree is xpath query. You will be able to
 * select/update content provided with XMLTree elements or attributes without working with xpath
 * directly.
 *
 * <p>XMLTree provides methods which do the same as model methods but sometimes they are more
 * convenient, you can use tree methods as well as model methods.
 *
 * <p>XMLTree disallows using of {@code DOCTYPE} definition in security reasons(XML Entity Expansion
 * injection, XML External Entity Injection).
 *
 * <pre>
 *     For example:
 *
 *     XMLTree tree = XMLTree.from(...)
 *
 *     //tree call
 *     tree.updateText("/project/name", "new name");
 *
 *     //model call
 *     tree.getSingleElement("/project/name")
 *         .setText("new name");
 *
 * </pre>
 *
 * <b>NOTE: XMLTree is not thread-safe!</b>
 *
 * @author Eugene Voevodin
 */
public final class XMLTree {

  /** Creates XMLTree from input stream. Doesn't close the stream */
  public static XMLTree from(InputStream is) throws IOException {
    return new XMLTree(toByteArray(is));
  }

  /** Creates XMLTree from file */
  public static XMLTree from(java.io.File file) throws IOException {
    return from(file.toPath());
  }

  /** Creates XMLTree from path */
  public static XMLTree from(Path path) throws IOException {
    return new XMLTree(readAllBytes(path));
  }

  /** Creates XMLTree from string */
  public static XMLTree from(String xml) {
    return new XMLTree(xml.getBytes(UTF_8));
  }

  /** Creates XMLTree from byte array */
  public static XMLTree from(byte[] xml) {
    requireNonNull(xml, "Required not null bytes");
    return new XMLTree(Arrays.copyOf(xml, xml.length));
  }

  /** Creates XMLTree with given root element */
  public static XMLTree create(String rootName) {
    return from(String.format(ROOT_TEMPLATE, rootName, rootName));
  }

  private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newFactory();
  private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY =
      DocumentBuilderFactory.newInstance();
  private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();
  private static final String ROOT_TEMPLATE =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<%s>\n</%s>";
  private static final int EXPECTED_NAMESPACES_SIZE = 2;

  /** Factories configuration. */
  static {
    try {
      // Disable doctype declaration to avoid: XML Entity Expansion injection, XML External Entity
      // Injection
      DOCUMENT_BUILDER_FACTORY.setFeature(
          "http://apache.org/xml/features/disallow-doctype-decl", true);
      // Force parser to use secure settings
      DOCUMENT_BUILDER_FACTORY.setFeature(FEATURE_SECURE_PROCESSING, true);
      // Disable usage of entity references to avoid: XML External Entity Injection
      // It is not needed as long as doctype is disabled, but when doctype is enabled
      // this adjustment guarantees avoiding of entity references expansion
      DOCUMENT_BUILDER_FACTORY.setExpandEntityReferences(false);

      // Force xpath factory to use secure settings
      XPATH_FACTORY.setFeature(FEATURE_SECURE_PROCESSING, true);

      // Disable DTD support at all to avoid: XML Entity Expansion injection, XML External Entity
      // Injection
      XML_INPUT_FACTORY.setProperty(SUPPORT_DTD, false);
      // Disable usage of external entities to avoid: XML External Entity Injection
      XML_INPUT_FACTORY.setProperty(IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    } catch (ParserConfigurationException | XPathFactoryConfigurationException confEx) {
      throw XMLTreeException.wrap(confEx);
    }
  }

  private Document document;
  private Map<String, String> namespaces;
  private List<Element> elements;
  private byte[] xml;

  private XMLTree(byte[] xml) {
    if (xml.length == 0) {
      throw new XMLTreeException("Source content is empty");
    }
    elements = new LinkedList<>();
    namespaces = newHashMapWithExpectedSize(EXPECTED_NAMESPACES_SIZE);
    this.xml = normalizeLineEndings(xml);
    // reason: parser is going to replace all '\r\n' sequences with single '\n'
    // which will affect elements position in source xml and produce incorrect XMLTree behaviour
    // it comes from spec http://www.w3.org/TR/2004/REC-xml11-20040204/
    document = parseQuietly(this.xml);
    constructTreeQuietly();
  }

  /**
   * Searches for requested element text. If there are more then only element were found {@link
   * XMLTreeException} will be thrown
   *
   * @param expression xpath expression to search element
   * @return requested element text
   * @see Element#getText()
   */
  public String getSingleText(String expression) {
    return (String) evaluateXPath(expression, STRING);
  }

  /**
   * Searches for requested elements text. If there are no elements were found empty list will be
   * returned.
   *
   * <p>You can use this method to request not only elements text but for selecting attributes
   * values or whatever text information which is able to be selected with xpath
   *
   * @param expression xpath expression to search elements
   * @return list of elements text or empty list if nothing found
   */
  public List<String> getText(String expression) {
    return retrieveText(expression);
  }

  /**
   * Searches for requested elements.
   *
   * @param expression xpath expression to search elements
   * @return list of found elements or empty list if elements were not found
   */
  public List<Element> getElements(String expression) {
    final NodeList nodes = (NodeList) evaluateXPath(expression, NODESET);
    return asElements(nodes);
  }

  public <R> List<R> getElements(String expression, ElementMapper<? extends R> mapper) {
    final NodeList nodes = (NodeList) evaluateXPath(expression, NODESET);
    return asElements(nodes, mapper);
  }

  /** Returns root element for current tree */
  public Element getRoot() {
    return asElement(document.getDocumentElement());
  }

  /**
   * If there are more then only element or nothing were found {@link XMLTreeException} will be
   * thrown
   *
   * @param expression xpath expression to search element
   * @return found element
   */
  public Element getSingleElement(String expression) {
    return single(getElements(expression));
  }

  /**
   * Updates requested element text. XPath expression should be used only for element not for
   * attribute or something else. If there are more then only element were found {@link
   * XMLTreeException} will be thrown
   *
   * @param expression xpath expression to search element
   * @param newContent new element text content
   * @see Element#setText(String)
   */
  public void updateText(String expression, String newContent) {
    getSingleElement(expression).setText(newContent);
  }

  /**
   * Adds element to the end of the list of existed children or adds it as only child.
   *
   * <p>If there are more then only parent element were found {@link XMLTreeException} will be
   * thrown
   *
   * @param expression xpath expression to search parent
   * @param newElement new element which will be inserted. It should be created with same tree
   *     instance
   */
  public void appendChild(String expression, NewElement newElement) {
    single(getElements(expression)).appendChild(newElement);
  }

  /**
   * Inserts element before referenced one. All comments related before referenced element going to
   * have same positions like they had before.
   *
   * <p>If there are more then only referenced element were found {@link XMLTreeException} will be
   * thrown
   *
   * @param expression xpath expression to search referenced element
   * @param newElement new element which will be inserted. It should be created with same tree
   *     instance
   */
  public void insertBefore(String expression, NewElement newElement) {
    single(getElements(expression)).insertBefore(newElement);
  }

  /**
   * Inserts element after referenced one.
   *
   * <p>If there are more then only referenced elements were found {@link XMLTreeException} will be
   * thrown
   *
   * @param expression xpath expression to search referenced element
   * @param newElement new element which will be inserted. It should be created with same tree
   *     instance
   */
  public void insertAfter(String expression, NewElement newElement) {
    single(getElements(expression)).insertAfter(newElement);
  }

  /**
   * Removes requested element. If there are was any <b>text</b> before removal element it will be
   * removed as well. It is important when we need to keep formatting pretty - if it was pretty. It
   * is really strange when parent element contains not only whitespaces but another text content.
   *
   * <p>If there are more then only referenced element were found {@link XMLTreeException} will be
   * thrown
   *
   * @param expression xpath expression to remove element
   */
  public void removeElement(String expression) {
    single(getElements(expression)).remove();
  }

  /** Returns copy of source bytes. TODO: write replacement explanation */
  public byte[] getBytes() {
    final String separator = System.getProperty("line.separator");
    if (!"\n".equals(separator)) {
      return replaceAll(xml, "\n".getBytes(), separator.getBytes());
    }
    return Arrays.copyOf(xml, xml.length);
  }

  /** Writes copy of source bytes to output stream. Doesn't close the stream */
  public void writeTo(OutputStream outputStream) throws IOException {
    outputStream.write(getBytes());
  }

  /** Writes source bytes to path */
  public void writeTo(Path path) throws IOException {
    Files.write(path, getBytes());
  }

  /** Writes source bytes to file */
  public void writeTo(java.io.File file) throws IOException {
    Files.write(file.toPath(), getBytes());
  }

  /**
   * Evaluates xpath expression with given return type. Rethrows all exceptions as {@link
   * XMLTreeException}
   */
  @SuppressWarnings("unchecked")
  private Object evaluateXPath(String expression, QName returnType) {
    final XPath xpath = XPATH_FACTORY.newXPath();
    try {
      return xpath.evaluate(expression, document, returnType);
    } catch (XPathExpressionException xpathEx) {
      throw XMLTreeException.wrap(xpathEx);
    }
  }

  /**
   * Parses document using {@link DocumentBuilder} Rethrows all exceptions as {@link
   * XMLTreeException}
   */
  private Document parseQuietly(byte[] xml) {
    try {
      final DocumentBuilder db = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
      return db.parse(new ByteArrayInputStream(xml));
    } catch (Exception ex) {
      throw XMLTreeException.wrap(ex);
    }
  }

  /**
   * Evaluates xpath expression and maps result as list of strings using {@link
   * Node#getTextContent()} method
   */
  private List<String> retrieveText(String expression) {
    final NodeList nodeList = (NodeList) evaluateXPath(expression, NODESET);
    final List<String> elementsText = new ArrayList<>(nodeList.getLength());
    for (int i = 0; i < nodeList.getLength(); i++) {
      elementsText.add(nodeList.item(i).getTextContent());
    }
    return elementsText;
  }

  /**
   * Constructs tree based on segments which are supplied by {@link XMLStreamReader}. Before this
   * method is invoked {@link #document} should be initialized first. For START_ELEMENT,
   * END_ELEMENT, CHARACTERS reader provides offset from start of source array bytes, so we can
   * fetch position of elements and text. Each created element associated with related {@link Node}
   * and vise-versa.
   */
  private void constructTree() throws XMLStreamException {
    final XMLStreamReader reader = newXMLStreamReader();
    final LinkedList<Element> stack = new LinkedList<>();
    // before element open tag index
    int beforeStart = rootStart(xml) - 1;
    // used to associate each element with document node
    Node node = document.getDocumentElement();
    // used to hold previous reader event
    int prevEvent = START_DOCUMENT;
    while (reader.hasNext()) {
      switch (reader.next()) {
        case START_ELEMENT:
          final Element newElement = new Element(this);
          newElement.start = new Segment(beforeStart + 1, elementRight(beforeStart + 1, reader));
          // if new node is not xml root - set up relationships
          if (!stack.isEmpty()) {
            node = deepNext(node, true);
          }
          // connect node with element
          node.setUserData("element", newElement, null);

          newElement.delegate = safeCast(node);
          // let next event know about its start
          beforeStart = newElement.start.right;
          // if element has declared namespaces register it
          putNamespaces(reader);
          stack.push(newElement);
          break;
        case END_ELEMENT:
          final Element element = stack.pop();
          element.end = new Segment(beforeStart + 1, elementRight(beforeStart + 1, reader));
          elements.add(element);
          beforeStart = element.end.right;
          break;
        case CHARACTERS:
          // characters event may be invoked 2 or more times
          // on the element text, but related node is single text node
          // so the only segment should be created for it
          if (prevEvent == CHARACTERS) continue;

          final Element current = stack.peek();
          if (current.text == null) {
            // TODO replace with array list as we know current node 'text nodes' count
            current.text = new LinkedList<>();
          }

          final Node nextNode = deepNext(node, true);

          final int left = beforeStart + 1;
          final int right = left + textLength(nextNode) - 1;

          current.text.add(new Segment(left, right));
          beforeStart = right;
          node = skipTextNodes(nextNode);
          break;
        case COMMENT:
        case SPACE:
        case PROCESSING_INSTRUCTION:
          if (!stack.isEmpty()) {
            node = deepNext(node, true);
            beforeStart = lastIndexOf(xml, '>', reader.getLocation().getCharacterOffset());
          }
          break;
        default:
          // DO NOTHING
      }
      prevEvent = reader.getEventType();
    }
  }

  /** Returns length of cdata and text nodes chain. */
  private int textLength(Node node) {
    int length = node.getTextContent().length();
    node = node.getNextSibling();
    while (node != null
        && (node.getNodeType() == TEXT_NODE || node.getNodeType() == CDATA_SECTION_NODE)) {
      length += node.getTextContent().length();
      if (node.getNodeType() == CDATA_SECTION_NODE) {
        length += 12; // <![CDATA[]]> - 12
      }
      node = node.getNextSibling();
    }
    return length;
  }

  /**
   * Returns safe Element instance for node or throws exception if it is not possible to cast {@code
   * node} to {@link org.w3c.dom.Element}.
   */
  private org.w3c.dom.Element safeCast(Node node) {
    if (!(node instanceof org.w3c.dom.Element)) {
      throw new XMLTreeException("It is not possible to associate xml elements");
    }
    return (org.w3c.dom.Element) node;
  }

  /**
   * Returns last text or cdata node in the chain of cdata and text nodes.
   *
   * <p>i.e. node1 is text <i>node</i> and <i>node1</i> has next sibling <i>node2</i> as cdata node
   * and <i>node2</i> has next sibling <i>node3</i> as text node and node3 has next sibling
   * <i>node4</i> as element node, then <i>node3</i> will be returned as last text node in the
   * chain. Consider following examples:
   *
   * <pre>
   * 1.
   * node3 - last text node
   *
   * text     cdata    text     element
   * node1 -> node2 -> node3 -> node4
   *
   * 2.
   * node2 - last text result
   *
   * text     cdata
   * node1 -> node2 -> null
   *
   * </pre>
   */
  private Node skipTextNodes(Node node) {
    final Node next = node.getNextSibling();
    if (next != null
        && (next.getNodeType() == CDATA_SECTION_NODE || next.getNodeType() == TEXT_NODE)) {
      return skipTextNodes(next);
    }
    return node;
  }

  /**
   * Searches for the element start right bound index. TODO respect element attributes text content
   * while checking '<'
   */
  private int elementRight(int left, XMLStreamReader reader) {
    int rightIdx = lastIndexOf(xml, '>', reader.getLocation().getCharacterOffset());
    int leftIdx = lastIndexOf(xml, '<', rightIdx);
    while (leftIdx > left) {
      rightIdx = lastIndexOf(xml, '>', rightIdx - 1);
      leftIdx = lastIndexOf(xml, '<', rightIdx);
    }
    return rightIdx;
  }

  private Node deepNext(Node node, boolean deep) {
    if (deep && node.getChildNodes().getLength() != 0) {
      return node.getFirstChild();
    }
    final Node next = node.getNextSibling();
    if (next != null) {
      return next;
    } else if (node == document.getDocumentElement()) {
      return node;
    }
    return deepNext(node.getParentNode(), false);
  }

  /**
   * Same as {@link #constructTree()}, only difference that it wraps {@link XMLStreamException} to
   * {@link XMLTreeException}
   */
  private void constructTreeQuietly() {
    try {
      constructTree();
    } catch (XMLStreamException xmlEx) {
      throw XMLTreeException.wrap(xmlEx);
    }
  }

  /** Should be invoked on ELEMENT_START event */
  private void putNamespaces(XMLStreamReader reader) {
    for (int i = 0; i < reader.getNamespaceCount(); i++) {
      final String prefix = reader.getNamespacePrefix(i);
      if (prefix != null) {
        putNamespace(prefix, reader.getNamespaceURI(i));
      }
    }
  }

  /** Creates new stream reader instance */
  private XMLStreamReader newXMLStreamReader() {
    try {
      return XML_INPUT_FACTORY.createXMLStreamReader(new ByteArrayInputStream(xml), "UTF-8");
    } catch (Exception xmlEx) {
      throw XMLTreeException.wrap(xmlEx);
    }
  }

  /**
   * Updates element text content. Update based on element text segments. If element doesn't have
   * any text segment then new segment will be created which positions based on container bounds,
   * otherwise only first text segment will be used for update, other text segments will be removed.
   */
  void updateText(Element target) {
    // it may be null when target element doesn't contain
    // text <element></element> so CHARACTERS event was not processed
    if (target.text == null) {
      target.text = new LinkedList<>();
      // updateSegmentContent will set up right bound
      target.text.add(new Segment(target.start.right + 1, target.start.right));
    }
    final Iterator<Segment> segIt = target.text.iterator();
    final Segment first = segIt.next();
    // removing all segments instead of first
    while (segIt.hasNext()) {
      final Segment removal = segIt.next();
      segIt.remove();
      removeSegment(removal);
    }
    updateSegmentContent(first, target.getText());
  }

  void updateAttributeValue(Attribute attribute, String oldValue) {
    final Segment segment = valueSegment(attribute, oldValue);
    updateSegmentContent(segment, attribute.getValue());
  }

  /** Adds new element to the end of children list with given parent. */
  void appendChild(NewElement newElement, Element relatedToNew, Element parent) {
    final int level = level(parent) + 1;
    final int lengthBefore = xml.length;
    final int insertHere = lastIndexOf(xml, '>', parent.end.left) + 1;
    // inserting new element bytes to tree bytes
    xml = insertInto(xml, insertHere, '\n' + tabulate(newElement.asString(), level));
    // shift existing segments which are after parent start
    shiftSegments(insertHere, xml.length - lengthBefore);
    // create and set up start, end, text segments to created element
    applySegments(newElement, relatedToNew, insertHere - 1, level);
    // let tree know about added element
    registerElement(relatedToNew);
  }

  /** Inserts element after referenced one */
  void insertAfter(NewElement newElement, Element relatedToNew, Element refElement) {
    final int level = level(refElement);
    final int lengthBefore = xml.length;
    // inserting new element bytes to tree bytes
    xml = insertInto(xml, refElement.end.right + 1, '\n' + tabulate(newElement.asString(), level));
    // shift existing segments which are after parent start
    shiftSegments(refElement.end.right, xml.length - lengthBefore);
    // create and set up start, end, text segments to created element
    // +1 because of \n
    applySegments(newElement, relatedToNew, refElement.end.right, level);
    // let tree know about inserted element
    registerElement(relatedToNew);
  }

  /**
   * Inserts element before referenced one. It is important to let all related to {@code refElement}
   * comments on their places, so to avoid deformation we inserting new element after previous
   * sibling or after element parent if element doesn't have previous sibling
   */
  void insertAfterParent(NewElement newElement, Element relatedToNew, Element parent) {
    final int level = level(parent) + 1;
    final int lengthBefore = xml.length;
    // inserting after parent
    xml = insertInto(xml, parent.start.right + 1, '\n' + tabulate(newElement.asString(), level));
    // shift existing segments which are after parent start
    shiftSegments(parent.start.right, xml.length - lengthBefore);
    // create and set up start, end, text segments to created element
    applySegments(newElement, relatedToNew, parent.start.right, level);
    // let tree know about inserted element
    registerElement(relatedToNew);
  }

  /**
   * Removes element bytes from tree.
   *
   * <p>It is important to save xml tree pretty view, so element should be removed without of
   * destroying style of xml document.
   *
   * <pre>
   *      e.g.
   *
   *      {@literal <level1>}
   *          {@literal <level2>} {@literal <level2>+\n}
   *          {@literal <level2>} {@literal <level2+>\n}
   *      {@literal <level1>}
   *
   *      first + is before left border
   *      last + is before right border
   *
   *      segment [first, last] - will be removed
   * </pre>
   *
   * So after removing - formatting will be the same. We can't remove just element from start to end
   * because it will produce not pretty formatting for good and pretty formatted before document.
   */
  void removeElement(Element element) {
    final int leftBound = lastIndexOf(xml, '>', element.start.left) + 1;
    final int lengthBefore = xml.length;
    // if text segment before removal element
    // exists it should go to hell with removal
    if (leftBound != element.start.left - 1) {
      removeSegmentFromElement(element.getParent(), leftBound);
    }
    // replacing content with nothing
    xml = insertBetween(xml, leftBound, element.end.right, "");
    // shift all elements which are right from removed element
    shiftSegments(element.end.right, xml.length - lengthBefore);
    // let tree know that element is not a family member
    unregisterElement(element);
  }

  /** Inserts new attribute value content to tree bytes */
  void insertAttribute(NewAttribute attribute, Element owner) {
    final int len = xml.length;
    // inserting new attribute content
    xml = insertInto(xml, owner.start.right, ' ' + attribute.asString());
    // shift all elements which are right from removed element
    shiftSegments(owner.start.left - 1, xml.length - len);
  }

  /** Removes element bytes from tree */
  void removeAttribute(Attribute attribute) {
    final Element element = attribute.getElement();
    final int lengthBefore = xml.length;
    final Segment segment = attributeSegment(attribute);
    // replacing attribute segment with nothing
    xml = insertBetween(xml, segment.left - 1, segment.right, "");
    // shift all elements which are left from owner left
    shiftSegments(element.start.left, xml.length - lengthBefore);
  }

  // TODO should it be public?
  void putNamespace(String prefix, String uri) {
    namespaces.put(prefix, uri);
  }

  // TODO should it be public?
  String getNamespaceUri(String prefix) {
    final String uri = namespaces.get(prefix);
    return uri == null ? XML_NS_URI : uri;
  }

  /**
   * Shift given segment on offset if it is righter then idx
   *
   * @param segment segment to shift
   * @param leftBound left bound
   * @param offset offset to shift on, it can be negative
   */
  private void shiftSegment(Segment segment, int leftBound, int offset) {
    if (segment.left > leftBound) {
      segment.left += offset;
      segment.right += offset;
    }
  }

  /** Removes segment which left bound equal to {@param left} from element */
  private void removeSegmentFromElement(Element element, int left) {
    for (Iterator<Segment> segIt = element.text.iterator(); segIt.hasNext(); ) {
      if (segIt.next().left == left) {
        segIt.remove();
        break;
      }
    }
  }

  /** Iterates all existed elements and shifts their segments if needed */
  private void shiftSegments(int fromIdx, int offset) {
    for (Element element : elements) {
      if (element.end.left > fromIdx) {
        shiftSegment(element.start, fromIdx, offset);
        shiftSegment(element.end, fromIdx, offset);
        if (element.text != null) {
          for (Segment textSegment : element.text) {
            shiftSegment(textSegment, fromIdx, offset);
          }
        }
      }
    }
  }

  /**
   * Removes given segment from source bytes and shifts segments left on offset equal to removal
   * segment length
   */
  private void removeSegment(Segment segment) {
    final int lengthBefore = xml.length;
    xml = insertBetween(xml, segment.left, segment.right, "");
    shiftSegments(segment.left, xml.length - lengthBefore);
  }

  /**
   * Inserts content bytes between left and right segment bounds and shifts segments on offset equal
   * to difference between new and old source bytes length
   */
  private void updateSegmentContent(Segment segment, String content) {
    final int lengthBefore = xml.length;
    xml = insertBetween(xml, segment.left, segment.right, content);
    shiftSegments(segment.left, xml.length - lengthBefore);
    segment.right = segment.left + content.length() - 1;
  }

  /** Adds element and it children to tree */
  private void registerElement(Element element) {
    elements.add(element);
    for (Element child : element.getChildren()) {
      registerElement(child);
    }
  }

  /** Removes element and children from tree */
  private void unregisterElement(Element element) {
    elements.remove(element);
    for (Element child : element.getChildren()) {
      unregisterElement(child);
    }
  }

  /** Retrieves attribute segment */
  private Segment attributeSegment(Attribute attribute) {
    final Element owner = attribute.getElement();

    final byte[] name = attribute.getName().getBytes();
    final byte[] value = attribute.getValue().getBytes();

    final int attrLeft =
        indexOfAttributeName(xml, name, owner.start.left + owner.getName().length());
    final int valueLeft = indexOf(xml, value, attrLeft + name.length);

    return new Segment(attrLeft, valueLeft + value.length);
  }

  /** Retrieves attribute value segment */
  private Segment valueSegment(Attribute attribute, String oldValue) {
    final Element owner = attribute.getElement();

    final byte[] name = attribute.getName().getBytes();
    final byte[] value = oldValue.getBytes();

    final int attrLeft =
        indexOfAttributeName(xml, name, owner.start.left + owner.getName().length());
    final int valueLeft = indexOf(xml, value, attrLeft + name.length);

    return new Segment(valueLeft, valueLeft + value.length - 1);
  }

  /** Creates segments for newly created element and related children */
  private int applySegments(
      NewElement newElement, Element relatedToNew, int prevElementCloseRight, int level) {
    // text length before element
    // child - new element
    // '+'   - text before element
    //
    // <parent>\n
    // ++++<child>...
    final int levelTextLength = level * SPACES_IN_TAB;

    // '*' - before element open tag pos
    //
    //       | prevElementCloseRight
    //       v
    // <parent>\n   *<child>...
    // +1 because of '\n'
    final int beforeOpenLeft = 1 + prevElementCloseRight + levelTextLength;

    // we should add text segment which
    // is before new element to the parent text segments and start to track it
    final Element parent = relatedToNew.getParent();
    if (parent.text == null) {
      parent.text = new LinkedList<>();
    }
    parent.text.add(new Segment(prevElementCloseRight + 1, beforeOpenLeft));

    // pos of open tag right '>'
    final int openRight = beforeOpenLeft + openTagLength(newElement);

    relatedToNew.start = new Segment(beforeOpenLeft + 1, openRight);
    // if element is void it doesn't have children and text
    // and it has same start and end so we can initialize
    // only start and end segments
    if (relatedToNew.isVoid()) {
      relatedToNew.end = relatedToNew.start;
      return openRight;
    }
    // if element has children it doesn't have text instead of
    // whitespaces, so all what we need - detect element close tag segment
    // to do so we need to map segments for all children first
    int childRight = openRight;
    if (newElement.hasChildren()) {

      final Iterator<NewElement> newChIt = newElement.getChildren().iterator();
      final Iterator<Element> chIt = relatedToNew.getChildren().iterator();

      while (newChIt.hasNext()) {
        childRight = applySegments(newChIt.next(), chIt.next(), childRight, level + 1);
      }
    } else {
      relatedToNew.text = new LinkedList<>();
    }
    // before element close tag pos
    //                        +
    // <parent>\n    <child>text</child>
    int beforeCloseLeft;
    if (newElement.hasChildren()) {
      beforeCloseLeft = childRight + levelTextLength + 1;
    } else {
      beforeCloseLeft = childRight + newElement.getText().length();
    }
    relatedToNew.text.add(new Segment(childRight + 1, beforeCloseLeft));
    relatedToNew.end =
        new Segment(beforeCloseLeft + 1, beforeCloseLeft + closeTagLength(newElement));
    return relatedToNew.end.right;
  }

  private byte[] normalizeLineEndings(byte[] src) {
    final String separator = System.getProperty("line.separator");
    // replacing all \r\n with \n
    if (separator.equals("\r\n")) {
      src = replaceAll(src, "\r\n".getBytes(), "\n".getBytes());
    }
    // replacing all \r with \n to prevent combination of \r\n which was created after
    // \r\n replacement, i.e. content \r\r\n after first replacement will be \r\n which is not okay
    return replaceAll(src, "\r".getBytes(), "\n".getBytes());
  }

  /** Describes element, attribute or text position in the source array of bytes. */
  static class Segment {
    int left;
    int right;

    Segment(int left, int right) {
      this.left = left;
      this.right = right;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof Segment)) {
        return false;
      }
      final Segment other = (Segment) obj;
      return other.left == left && other.right == right;
    }

    @Override
    public int hashCode() {
      return 31 * left ^ 31 * right;
    }

    @Override
    public String toString() {
      return "left: " + left + ", right: " + right;
    }
  }

  @Override
  public String toString() {
    return new String(getBytes(), UTF_8);
  }
}
