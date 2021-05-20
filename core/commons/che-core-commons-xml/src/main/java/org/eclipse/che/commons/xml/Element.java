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
package org.eclipse.che.commons.xml;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
import static org.eclipse.che.commons.xml.XMLTreeUtil.asElement;
import static org.eclipse.che.commons.xml.XMLTreeUtil.asElements;
import static org.w3c.dom.Node.DOCUMENT_NODE;
import static org.w3c.dom.Node.ELEMENT_NODE;
import static org.w3c.dom.Node.TEXT_NODE;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.commons.xml.XMLTree.Segment;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XMLTree element which provides abilities to fetch and update xml document data.
 *
 * <p>Delegates for related {@link org.w3c.dom.Element}
 *
 * @author Eugene Voevodin
 */
public final class Element {

  private final XMLTree xmlTree;

  Segment start;
  Segment end;
  List<Segment> text;

  org.w3c.dom.Element delegate;

  Element(XMLTree xmlTree) {
    this.xmlTree = xmlTree;
  }

  /**
   * Returns name of element as <i>prefix:name</i>. If element doesn't have prefix only local name
   * will be returned
   *
   * @return name of element tag as <i>prefix:name</i>
   * @throws XMLTreeException when {@link #remove()} has been invoked on this element instance
   * @see org.w3c.dom.Element#getTagName()
   */
  public String getName() {
    checkNotRemoved();
    return delegate.getTagName();
  }

  /**
   * Returns local name of element
   *
   * @return element local name
   * @throws XMLTreeException when this element has been removed from xml tree
   * @see org.w3c.dom.Element#getLocalName()
   */
  public String getLocalName() {
    checkNotRemoved();
    return delegate.getLocalName();
  }

  /**
   * Returns element name prefix or {@code null} if element name is not prefixed
   *
   * @return element name prefix
   * @throws XMLTreeException when this element has been removed from xml tree
   * @see org.w3c.dom.Element#getPrefix()
   */
  public String getPrefix() {
    checkNotRemoved();
    return delegate.getPrefix();
  }

  /**
   * Returns element parent or {@code null} if element doesn't have parent.
   *
   * @return element parent or {@code null} if element is xml root
   * @throws XMLTreeException when this element has been removed from xml tree
   */
  public Element getParent() {
    checkNotRemoved();
    return asElement(delegate.getParentNode());
  }

  /**
   * Searches for element sibling with given name. If more than one sibling was found throws {@link
   * XMLTreeException}. If sibling with given name doesn't exist returns {@code null}.
   *
   * <p>Note that {@link #getName} method used to compare element names.
   *
   * @param name sibling name to search
   * @return element sibling with given name or {@code null} if sibling with given <i>name</i> was
   *     not found
   * @throws XMLTreeException when element has more than one sibling with given <i>name</i> or this
   *     element has been removed from xml tree
   * @throws NullPointerException when name parameter is {@code null}
   */
  public Element getSingleSibling(String name) {
    checkNotRemoved();
    requireNonNull(name, "Non-null sibling name required.");
    Element target = null;
    for (Element sibling : asElements(delegate.getParentNode().getChildNodes())) {
      if (this != sibling && sibling.getName().equals(name)) {
        if (target != null) {
          throw new XMLTreeException(
              "Element " + name + " has more than one sibling with name " + name);
        }
        target = sibling;
      }
    }
    return target;
  }

  /**
   * Searches for element child with given name. If element has more then only child with given name
   * then {@link XMLTreeException} will be thrown. If child with given name doesn't exist returns
   * {@code null}
   *
   * <p>Note that {@link #getName} method used to compare element names.
   *
   * @param name name to search child
   * @return child element with given name or {@code null} if element with given name was not found
   * @throws XMLTreeException when element has more than one child with given <i>name</i> or this
   *     element has been removed from xml tree
   * @throws NullPointerException when name parameter is {@code null}
   */
  public Element getSingleChild(String name) {
    checkNotRemoved();
    requireNonNull(name, "Non-null child name required.");
    for (Element child : asElements(delegate.getChildNodes())) {
      if (name.equals(child.getName())) {
        if (child.hasSibling(name)) {
          throw new XMLTreeException(
              "Element " + name + " has more than one child with the name " + name);
        }
        return child;
      }
    }
    return null;
  }

  /**
   * Returns last element child or {@code null} if element doesn't have children
   *
   * @return last child element or {@code null} if this element doesn't have children
   * @throws XMLTreeException when this element has been removed from xml tree
   */
  public Element getLastChild() {
    checkNotRemoved();
    final Node lastChild = delegate.getLastChild();
    if (lastChild != null && lastChild.getNodeType() != ELEMENT_NODE) {
      return asElement(previousElementNode(lastChild));
    }
    return asElement(lastChild);
  }

  /**
   * Returns first element child or {@code null} if element doesn't have children
   *
   * @return first child element or {@code null} if this element doesn't have children
   * @throws XMLTreeException when this element has been removed from xml tree
   */
  public Element getFirstChild() {
    checkNotRemoved();
    final Node firstChild = delegate.getFirstChild();
    if (firstChild.getNodeType() != ELEMENT_NODE) {
      return asElement(nextElementNode(firstChild));
    }
    return asElement(firstChild);
  }

  /**
   * Returns element children or empty list when element doesn't have children
   *
   * @return list of element children
   * @throws XMLTreeException when this element has been removed from xml tree
   */
  public List<Element> getChildren() {
    checkNotRemoved();
    return asElements(delegate.getChildNodes());
  }

  /**
   * Returns children mapped with given mapper or empty list when element doesn't have children
   *
   * @param mapper function which will be applied on each child element
   * @param <R> mapper result type
   * @return list of element children which are mapped with given mapper
   * @throws XMLTreeException when this element has been removed from xml tree
   */
  public <R> List<R> getChildren(ElementMapper<? extends R> mapper) {
    checkNotRemoved();
    return asElements(delegate.getChildNodes(), mapper);
  }

  /**
   * Returns element text content.
   *
   * <p>Note that only element text going to be fetched, no CDATA or children text content.
   *
   * @return element text content
   * @throws XMLTreeException when this element has been removed from xml tree
   */
  public String getText() {
    checkNotRemoved();
    return fetchText();
  }

  /**
   * Returns {@code true} if element has at least one sibling with given name, otherwise returns
   * {@code false}.
   *
   * @return {@code true} if element has at least one singling with given name, otherwise {@code
   *     false}.
   * @throws XMLTreeException when this element has been removed from xml tree
   * @throws NullPointerException when name parameter is {@code null}
   */
  public boolean hasSibling(String name) {
    checkNotRemoved();
    requireNonNull(name, "Non-null sibling name required.");
    final NodeList nodes = delegate.getParentNode().getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      if (nodes.item(i) != delegate && name.equals(nodes.item(i).getNodeName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns {@code true} if this element instance is xml root element, otherwise returns {@code
   * false}
   *
   * @return {@code true} if element has parent, otherwise {@code false}
   * @throws XMLTreeException when this element has been removed from xml tree
   */
  public boolean hasParent() {
    checkNotRemoved();
    return delegate.getParentNode() != null
        && delegate.getParentNode().getNodeType() != DOCUMENT_NODE;
  }

  /**
   * Returns previous element sibling or {@code null} when element doesn't have previous sibling
   *
   * @return previous element sibling or {@code null} when element doesn't have previous sibling
   * @throws XMLTreeException when this element has been removed from xml tree
   */
  public Element getPreviousSibling() {
    checkNotRemoved();
    return asElement(previousElementNode(delegate));
  }

  /**
   * Returns next element sibling or {@code null} if element doesn't have next sibling
   *
   * @return next element sibling or {@code null} if element doesn't have next sibling
   * @throws XMLTreeException when this element has been removed from xml tree
   */
  public Element getNextSibling() {
    checkNotRemoved();
    return asElement(nextElementNode(delegate));
  }

  /**
   * Returns element attributes or empty list if element doesn't have attributes.
   *
   * <p>When element doesn't have attributes returns {@link java.util.Collections#emptyList()} which
   * is unmodifiable, so clients should not use list 'update' methods.
   *
   * @return list of element attributes or empty list if element doesn't have attributes
   * @throws XMLTreeException when this element has been removed from xml tree
   */
  public List<Attribute> getAttributes() {
    checkNotRemoved();
    if (delegate != null && delegate.hasAttributes()) {
      final NamedNodeMap attributes = delegate.getAttributes();
      final List<Attribute> copy = new ArrayList<>(attributes.getLength());
      for (int i = 0; i < attributes.getLength(); i++) {
        final Node item = attributes.item(i);
        copy.add(asAttribute(item));
      }
      return copy;
    }
    return emptyList();
  }

  /**
   * Returns list of element sibling or empty list if element doesn't have siblings.
   *
   * @return list of element sibling
   * @throws XMLTreeException when this element has been removed from xml tree
   */
  public List<Element> getSiblings() {
    checkNotRemoved();
    final List<Element> siblings = asElements(delegate.getParentNode().getChildNodes());
    siblings.remove(asElement(delegate));
    return siblings;
  }

  /**
   * Returns {@code true} if element has at least one child with given name, otherwise returns
   * {@code false}.
   *
   * @param name child name to check
   * @return {@code true} if element has at least one child with given name, otherwise {@code false}
   * @throws XMLTreeException when this element has been removed from xml tree
   * @throws NullPointerException when name parameter is {@code null}
   */
  public boolean hasChild(String name) {
    checkNotRemoved();
    requireNonNull(name, "Non-null child name required.");
    final NodeList nodes = delegate.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      if (name.equals(nodes.item(i).getNodeName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns {@code true} if element has at least one child or {@code false} if doesn't
   *
   * @return {@code true} if element has at least one child or {@code false} if doesn't
   * @throws XMLTreeException when this element has been removed from xml tree
   */
  public boolean hasChildren() {
    checkNotRemoved();
    final NodeList childNodes = delegate.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      if (childNodes.item(i).getNodeType() == ELEMENT_NODE) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sets new text content to element
   *
   * @param newText new text content
   * @throws XMLTreeException when this element has been removed from xml tree
   * @throws NullPointerException when newText parameter is {@code null}
   */
  public Element setText(String newText) {
    checkNotRemoved();
    requireNonNull(newText, "Non-null new text required.");
    if (!newText.equals(getText())) {
      removeTextNodes();
      delegate.appendChild(document().createTextNode(newText));
      // let tree do dirty job
      xmlTree.updateText(this);
    }
    return this;
  }

  /**
   * Returns text content of child with given name.
   *
   * @param childName child name to fetch text content
   * @return child text or {@code null} if child doesn't exist or element has more then only child
   *     with given name
   */
  public String getChildText(String childName) {
    return getChildTextOrDefault(childName, null);
  }

  /**
   * Returns text content of child with given name or default value if child doesn't exist or it has
   * sibling with same name
   *
   * @param childName name of child
   * @param defaultValue value which will be returned if child doesn't exist or it has sibling with
   *     same name
   * @return child text
   * @throws XMLTreeException when this element has been removed from xml tree
   * @throws NullPointerException when childName parameter is {@code null}
   */
  public String getChildTextOrDefault(String childName, String defaultValue) {
    checkNotRemoved();
    requireNonNull(childName, "Non-null child name required.");
    return hasSingleChild(childName) ? getSingleChild(childName).getText() : defaultValue;
  }

  /**
   * Returns {@code true} if element has only sibling with given name or {@code false} if element
   * has more then 1 or 0 siblings with given name
   *
   * @param childName name of sibling
   * @return {@code true} if element has only sibling with given name otherwise {@code false}
   * @throws XMLTreeException when this element has been removed from xml tree
   * @throws NullPointerException when childName parameter is {@code null}
   */
  public boolean hasSingleChild(String childName) {
    checkNotRemoved();
    requireNonNull(childName, "Non-null child name required.");
    for (Element child : asElements(delegate.getChildNodes())) {
      if (childName.equals(child.getName())) {
        return !child.hasSibling(childName);
      }
    }
    return false;
  }

  /**
   * Removes single element child. If child does not exist nothing will be done
   *
   * @param name child name to removeElement
   * @return this element instance
   * @throws XMLTreeException when this element has been removed from xml tree
   */
  public Element removeChild(String name) {
    checkNotRemoved();
    final Element child = getSingleChild(name);
    if (child != null) {
      child.remove();
    }
    return this;
  }

  /**
   * Removes current element and related children from xml
   *
   * @throws XMLTreeException when this element has been removed from xml tree or this element is
   *     root element
   */
  public void remove() {
    checkNotRemoved();
    notPermittedOnRootElement();
    if (hasChildren()) {
      for (Element element : getChildren()) {
        element.remove();
      }
    }
    // let tree do dirty job
    xmlTree.removeElement(this);
    // remove self from document
    delegate.getParentNode().removeChild(delegate);
    // if references to 'this' element exist
    // we should disallow ability to use delegate
    delegate = null;
  }

  /**
   * Removes children which names equal to given name
   *
   * @param name name to remove children
   * @return this element instance
   * @throws XMLTreeException when this element has been removed from xml tree
   */
  public Element removeChildren(String name) {
    checkNotRemoved();
    final List<Node> matched = new LinkedList<>();
    final NodeList nodes = delegate.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      if (name.equals(nodes.item(i).getNodeName())) {
        matched.add(nodes.item(i));
      }
    }
    for (Node node : matched) {
      asElement(node).remove();
    }
    return this;
  }

  /**
   * Sets new attribute to element. If element has attribute with given name attribute value will be
   * replaced with new value
   *
   * @param name attribute name
   * @param value attribute value
   * @return this element instance
   * @throws XMLTreeException when this element has been removed from xml tree
   */
  public Element setAttribute(String name, String value) {
    return setAttribute(new NewAttribute(name, value));
  }

  /**
   * Sets new attribute to element. If element has attribute with {@code newAttribute#name} then
   * existing attribute value will be replaced with {@code newAttribute#value}.
   *
   * @param newAttribute attribute that should be added to element
   * @return this element instance
   * @throws XMLTreeException when this element has been removed from xml tree
   */
  public Element setAttribute(NewAttribute newAttribute) {
    checkNotRemoved();
    requireNonNull(newAttribute, "Required not null new attribute");
    // if tree already contains element replace value
    if (hasAttribute(newAttribute.getName())) {
      final Attribute attr = getAttribute(newAttribute.getName());
      attr.setValue(newAttribute.getValue());
      return this;
    }
    //
    if (newAttribute.hasPrefix()) {
      delegate.setAttributeNodeNS(createAttrNSNode(newAttribute));
    } else {
      delegate.setAttributeNode(createAttrNode(newAttribute));
    }
    // let tree do dirty job
    xmlTree.insertAttribute(newAttribute, this);
    return this;
  }

  /**
   * Removes attribute with given name. If element doesn't have attribute with given name nothing
   * will be done.
   *
   * @param name name of attribute which should be removed from element
   * @return this element instance
   * @throws XMLTreeException when this element has been removed from xml tree
   * @throws NullPointerException when name parameter is {@code null}
   */
  public Element removeAttribute(String name) {
    checkNotRemoved();
    final Attribute attribute = getAttribute(name);
    if (attribute != null) {
      xmlTree.removeAttribute(attribute);
      delegate.getAttributes().removeNamedItem(name);
    }
    return this;
  }

  /**
   * Returns {@code true} if element has attribute with given name
   *
   * @param name name of attribute to check
   * @return {@code true} if element has attribute with {@code name} otherwise {@code false}
   * @throws XMLTreeException when this element has been removed from xml tree
   */
  public boolean hasAttribute(String name) {
    checkNotRemoved();
    return delegate.hasAttribute(name);
  }

  /**
   * Returns {@code true} if element doesn't have closing tag i.e {@literal <element
   * attr="value"/>}, otherwise {@code false}
   */
  public boolean isVoid() {
    return start.equals(end);
  }

  /**
   * Returns attribute with given name or {@code null} if element doesn't have such attribute
   *
   * @param name name to search attribute
   * @return attribute with {@code name} or {@code null} if nothing found
   * @throws XMLTreeException when this element has been removed from xml tree
   * @throws NullPointerException when name parameter is {@code null}
   */
  public Attribute getAttribute(String name) {
    checkNotRemoved();
    requireNonNull(name, "Non-null new attribute name required.");
    if (delegate.hasAttributes()) {
      return asAttribute(getAttributeNode(name));
    }
    return null;
  }

  /**
   * Replaces this element with new one.
   *
   * @param newElement new element which is replacement for current element
   * @return newly created element
   * @throws XMLTreeException when this element has been removed from xml tree or this element is
   *     root element
   * @throws NullPointerException when newElement parameter is {@code null}
   */
  public Element replaceWith(NewElement newElement) {
    checkNotRemoved();
    notPermittedOnRootElement();
    requireNonNull(newElement, "Required not null new element");
    insertAfter(newElement);
    final Element inserted = getNextSibling();
    remove();
    return inserted;
  }

  /**
   * Appends new element to the end of children list
   *
   * @param newElement element which will be inserted to the end of children list
   * @return this element instance
   * @throws XMLTreeException when this element has been removed from xml tree
   * @throws NullPointerException when newElement parameter is {@code null}
   */
  public Element appendChild(NewElement newElement) {
    checkNotRemoved();
    requireNonNull(newElement, "Required not null new element");
    if (isVoid()) {
      throw new XMLTreeException("Append child is not permitted on void elements");
    }
    final Node newNode = createNode(newElement);
    final Element element = createElement(newNode);
    // append new node into document
    delegate.appendChild(newNode);
    // let tree do dirty job
    xmlTree.appendChild(newElement, element, this);
    return this;
  }

  /**
   * Inserts new element after current
   *
   * @param newElement element which will be inserted after current
   * @return this element instance
   * @throws XMLTreeException when this element has been removed from xml tree or this element is
   *     root element
   * @throws NullPointerException when newElement parameter is {@code null}
   */
  public Element insertAfter(NewElement newElement) {
    checkNotRemoved();
    notPermittedOnRootElement();
    requireNonNull(newElement, "Required not null new element");
    final Node newNode = createNode(newElement);
    final Element element = createElement(newNode);
    // if element has next sibling append child to parent
    // else insert before next sibling
    final Node nextNode = nextElementNode(delegate);
    if (nextNode != null) {
      delegate.getParentNode().insertBefore(newNode, nextNode);
    } else {
      delegate.getParentNode().appendChild(newNode);
    }
    // let tree do dirty job
    xmlTree.insertAfter(newElement, element, this);
    return this;
  }

  /**
   * Inserts new element before current element
   *
   * @param newElement element which will be inserted before current
   * @return this element instance
   * @throws XMLTreeException when this element has been removed from xml tree or this element is
   *     root element
   * @throws NullPointerException when newElement parameter is {@code null}
   */
  public Element insertBefore(NewElement newElement) {
    checkNotRemoved();
    notPermittedOnRootElement();
    requireNonNull(newElement, "Required not null new element");
    // if element has previous sibling insert new element after it
    // inserting before this element to let existing comments
    // or whatever over referenced element
    if (previousElementNode(delegate) != null) {
      getPreviousSibling().insertAfter(newElement);
      return this;
    }
    final Node newNode = createNode(newElement);
    final Element element = createElement(newNode);
    delegate.getParentNode().insertBefore(newNode, delegate);
    // let tree do dirty job
    xmlTree.insertAfterParent(newElement, element, getParent());
    return this;
  }

  /**
   * Adds new element as child to the specified by {@link XMLTreeLocation} location.
   *
   * <p>If it is not possible to insert element in specified location then {@link XMLTreeException}
   * will be thrown
   *
   * @param child new child
   */
  public Element insertChild(NewElement child, XMLTreeLocation place) {
    place.evalInsert(this, child);
    return this;
  }

  void setAttributeValue(Attribute attribute) {
    checkNotRemoved();
    final Node attributeNode = getAttributeNode(attribute.getName());
    xmlTree.updateAttributeValue(attribute, attributeNode.getNodeValue());
    getAttributeNode(attribute.getName()).setNodeValue(attribute.getValue());
  }

  private void removeTextNodes() {
    final NodeList childNodes = delegate.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      if (childNodes.item(i).getNodeType() == TEXT_NODE) {
        delegate.removeChild(childNodes.item(i));
      }
    }
  }

  private Attribute asAttribute(Node node) {
    if (node == null) {
      return null;
    }
    return new Attribute(this, node.getNodeName(), node.getNodeValue());
  }

  private String fetchText() {
    final StringBuilder sb = new StringBuilder();
    final NodeList childNodes = delegate.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      if (childNodes.item(i).getNodeType() == TEXT_NODE) {
        sb.append(childNodes.item(i).getTextContent());
      }
    }
    return sb.toString();
  }

  private Attr createAttrNode(NewAttribute newAttribute) {
    final Attr attr = document().createAttribute(newAttribute.getName());
    attr.setValue(newAttribute.getValue());
    return attr;
  }

  private Attr createAttrNSNode(NewAttribute attribute) {
    if (attribute.getPrefix().equals(XMLNS_ATTRIBUTE)) {
      final Attr attr = document().createAttributeNS(XMLNS_ATTRIBUTE_NS_URI, attribute.getName());
      attr.setValue(attribute.getValue());
      // save uri
      xmlTree.putNamespace(attribute.getLocalName(), attribute.getValue());
      return attr;
    } else {
      // retrieve namespace
      final String uri = xmlTree.getNamespaceUri(attribute.getPrefix());
      final Attr attr = document().createAttributeNS(uri, attribute.getName());
      attr.setValue(attribute.getValue());
      return attr;
    }
  }

  private Node nextElementNode(Node node) {
    node = node.getNextSibling();
    while (node != null && node.getNodeType() != ELEMENT_NODE) {
      node = node.getNextSibling();
    }
    return node;
  }

  private Node previousElementNode(Node node) {
    node = node.getPreviousSibling();
    while (node != null && node.getNodeType() != ELEMENT_NODE) {
      node = node.getPreviousSibling();
    }
    return node;
  }

  private void notPermittedOnRootElement() {
    if (!hasParent()) {
      throw new XMLTreeException("Operation not permitted for root element");
    }
  }

  private void checkNotRemoved() {
    if (delegate == null) {
      throw new XMLTreeException(
          "Operation not permitted for element which has been removed from XMLTree");
    }
  }

  private Element createElement(Node node) {
    final Element element = new Element(xmlTree);
    element.delegate = (org.w3c.dom.Element) node;
    node.setUserData("element", element, null);
    if (node.hasChildNodes()) {
      final NodeList children = node.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        if (children.item(i).getNodeType() == ELEMENT_NODE) {
          createElement(children.item(i));
        }
      }
    }
    return element;
  }

  private Node createNode(NewElement newElement) {
    final org.w3c.dom.Element newNode;
    if (newElement.hasPrefix()) {
      final String uri = xmlTree.getNamespaceUri(newElement.getPrefix());
      newNode = document().createElementNS(uri, newElement.getName());
    } else {
      newNode = document().createElement(newElement.getLocalName());
    }
    newNode.setTextContent(newElement.getText());
    // creating all related children
    for (NewElement child : newElement.getChildren()) {
      newNode.appendChild(createNode(child));
    }
    // creating all related attributes
    for (NewAttribute attribute : newElement.getAttributes()) {
      if (attribute.hasPrefix()) {
        newNode.setAttributeNodeNS(createAttrNSNode(attribute));
      } else {
        newNode.setAttributeNode(createAttrNode(attribute));
      }
    }
    return newNode;
  }

  private Document document() {
    return delegate.getOwnerDocument();
  }

  private Node getAttributeNode(String name) {
    final NamedNodeMap attributes = delegate.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
      if (attributes.item(i).getNodeName().equals(name)) {
        return attributes.item(i);
      }
    }
    return null;
  }
}
