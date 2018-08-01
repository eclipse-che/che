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
package org.eclipse.che.commons.xml;

import static java.lang.Character.isWhitespace;
import static java.lang.Math.min;
import static java.lang.System.arraycopy;
import static java.util.Arrays.fill;
import static org.w3c.dom.Node.ELEMENT_NODE;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utils for xml tree
 *
 * @author Eugene Voevodin
 */
public final class XMLTreeUtil {

  public static final Charset UTF_8 = Charset.forName("utf-8");
  public static final int SPACES_IN_TAB = 4;

  /**
   *
   *
   * <pre>
   * New content schema:
   *
   * [0 - left) + content + (right, src.elementLength)
   * </pre>
   *
   * @param src source array
   * @param left left anchor - not included to result
   * @param right right anchor - not included to result
   * @param content content which will be inserted between left and right
   * @return new content
   */
  public static byte[] insertBetween(byte[] src, int left, int right, String content) {
    final byte[] contentSrc = content.getBytes(UTF_8);
    final byte[] newSrc = new byte[left + src.length - right + contentSrc.length - 1];
    arraycopy(src, 0, newSrc, 0, left);
    arraycopy(contentSrc, 0, newSrc, left, contentSrc.length);
    arraycopy(src, right + 1, newSrc, left + contentSrc.length, src.length - right - 1);
    return newSrc;
  }

  /**
   *
   *
   * <pre>
   * New content schema:
   *
   * [0 - pos) + content + [pos, src.elementLength)
   * </pre>
   *
   * @param src source array
   * @param pos start position for content insertion
   * @param content content which will be inserted from {@param anchor}
   * @return new content
   */
  public static byte[] insertInto(byte[] src, int pos, String content) {
    final byte[] contentSrc = content.getBytes(UTF_8);
    final byte[] newSrc = new byte[src.length + contentSrc.length];
    arraycopy(src, 0, newSrc, 0, pos);
    arraycopy(contentSrc, 0, newSrc, pos, contentSrc.length);
    arraycopy(src, pos, newSrc, pos + contentSrc.length, src.length - pos);
    return newSrc;
  }

  /**
   * Check given list contains only element and return it. If list size is not 1 {@link
   * XMLTreeException} will be thrown.
   *
   * @param target list to check
   * @return list only element
   */
  public static <T> T single(List<T> target) {
    if (target.size() != 1) {
      throw new XMLTreeException("Required list with one element");
    }
    return target.get(0);
  }

  public static int lastIndexOf(byte[] src, char c, int fromIdx) {
    for (int i = min(fromIdx, src.length - 1); i >= 0; i--) {
      if (src[i] == c) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Calculates how deep is the element in tree. First level is tree root and it is equal to 0.
   *
   * @param element target element
   * @return how deep is the element
   */
  public static int level(Element element) {
    int level = 0;
    while (element.hasParent()) {
      element = element.getParent();
      level++;
    }
    return level;
  }

  public static int openTagLength(NewElement element) {
    int len = 2; // '<' + '>'
    len += element.getName().length(); // 'name' or 'prefix:name'
    for (NewAttribute attribute : element.getAttributes()) {
      len += 1 + attributeLength(attribute); // ' ' + 'attr="value"' or 'pref:attr="value"'
    }
    // if is void add +1 '/'
    return element.isVoid() ? len + 1 : len;
  }

  public static int closeTagLength(NewElement element) {
    return 3 + element.getLocalName().length(); // '<' + '/' + 'name' + '>'
  }

  public static int attributeLength(NewAttribute attribute) {
    int len = 0;
    if (attribute.hasPrefix()) {
      len += attribute.getPrefix().length() + 1; // prefix  + ':'
    }
    len += attribute.getName().length() + attribute.getValue().length() + 3;
    return len;
  }

  /**
   * Inserts given number of tabs to each line of given source.
   *
   * @param src source which going to be tabulated
   * @param tabsCount how many tabs should be added before each line
   * @return tabulated source
   */
  public static String tabulate(String src, int tabsCount) {
    char[] tabs = new char[SPACES_IN_TAB * tabsCount];
    fill(tabs, ' ');
    final StringBuilder builder = new StringBuilder();
    final String[] lines = src.split("\n");
    for (int i = 0; i < lines.length - 1; i++) {
      builder.append(tabs).append(lines[i]).append('\n');
    }
    builder.append(tabs).append(lines[lines.length - 1]);
    return builder.toString();
  }

  /**
   * Fetches {@link Element} from {@link Node} using {@link Node#getUserData(String)}
   *
   * @param node node to fetch from
   * @return {@code null} if {@param node} is null or {@link Element} associated with given node
   */
  public static Element asElement(Node node) {
    if (node == null) {
      return null;
    }
    return (Element) node.getUserData("element");
  }

  /**
   * Converts {@link NodeList} to list of elements. Only nodes with type {@link Node#ELEMENT_NODE}
   * will be fetched other will be skipped
   *
   * @param list list of nodes to fetch elements from
   * @return list of fetched elements or empty list if node list doesn't contain any element node
   */
  public static List<Element> asElements(NodeList list) {
    final List<Element> elements = new ArrayList<>(list.getLength());
    for (int i = 0; i < list.getLength(); i++) {
      if (list.item(i).getNodeType() == ELEMENT_NODE) {
        elements.add(asElement(list.item(i)));
      }
    }
    return elements;
  }

  public static <R> List<R> asElements(NodeList list, ElementMapper<? extends R> mapper) {
    final List<R> elements = new ArrayList<>(list.getLength());
    for (int i = 0; i < list.getLength(); i++) {
      if (list.item(i).getNodeType() == ELEMENT_NODE) {
        elements.add(mapper.map(asElement(list.item(i))));
      }
    }
    return elements;
  }

  /**
   * Searches for target bytes in the source bytes.
   *
   * @param src where to search
   * @param target what to search
   * @param fromIdx source index to search from
   * @return index of the first occurrence or -1 if nothing was found
   */
  public static int indexOf(byte[] src, byte[] target, int fromIdx) {
    final int to = src.length - target.length + 1;
    for (int i = fromIdx; i < to; i++) {
      if (src[i] == target[0]) {
        boolean equals = true;
        for (int j = 1, k = i + 1; j < target.length && equals; j++, k++) {
          if (src[k] != target[j]) {
            equals = false;
          }
        }
        if (equals) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * Search for attribute name bytes in the source bytes. The main difference with {@link #indexOf}
   * is that if occurrence was found then we need to check next byte as character, if it is
   * whitespace character or it equals to '=' attribute name was found otherwise continue searching
   *
   * @param src where to search
   * @param target attribute name bytes to search
   * @param fromIdx source index to search from
   * @return index of the first attribute name occurrence or -1 if nothing was found
   */
  public static int indexOfAttributeName(byte[] src, byte[] target, int fromIdx) {
    final int idx = indexOf(src, target, fromIdx);
    if (idx == -1) {
      return -1;
    }
    final int next = idx + target.length;
    if (next == src.length || isWhitespace(src[next]) || src[next] == '=') {
      return idx;
    }
    return indexOfAttributeName(src, target, idx + 1);
  }

  public static byte[] replaceAll(byte[] src, byte[] target, byte[] replacement) {
    final ByteArrayOutputStream result = new ByteArrayOutputStream(src.length);
    int i = 0;
    int wrote = 0;
    while ((i = indexOf(src, target, i)) != -1) {
      int len = i - wrote;
      result.write(src, wrote, len);
      result.write(replacement, 0, replacement.length);
      wrote += len + target.length;
      i += target.length;
    }
    result.write(src, wrote, src.length - wrote);
    return result.toByteArray();
  }

  public static int rootStart(byte[] xml) {
    final byte[] open = {'<'};
    int pos = indexOf(xml, open, 0);
    while (xml[pos + 1] == '?' || xml[pos + 1] == '!') {
      if (xml[pos + 1] == '!' && xml[pos + 2] == '-' && xml[pos + 3] == '-') {
        pos = indexOf(xml, new byte[] {'-', '-', '>'}, pos + 1);
      }
      pos = indexOf(xml, open, pos + 1);
    }
    return pos;
  }

  private XMLTreeUtil() {}
}
