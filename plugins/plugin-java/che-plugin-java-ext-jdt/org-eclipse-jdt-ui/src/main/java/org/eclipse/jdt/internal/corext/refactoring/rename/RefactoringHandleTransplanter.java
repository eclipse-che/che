/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.rename;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;

/**
 * Helper class to transplant a IJavaElement handle from a certain state of the Java Model into
 * another.
 *
 * <p>The changes to the workspace include one type rename, a number of field renames, and a number
 * of method renames including signature changes.
 *
 * <p>The returned handle exists in the target model state.
 *
 * @since 3.2
 */
public class RefactoringHandleTransplanter {

  private final IType fOldType;
  private final IType fNewType;
  private final Map<IJavaElement, String> fRefactoredSimilarElements;

  /**
   * @param oldType old type
   * @param newType renamed type
   * @param refactoredSimilarElements map from similar element (IJavaElement) to new name (String),
   *     or <code>null</code>
   */
  public RefactoringHandleTransplanter(
      IType oldType, IType newType, Map<IJavaElement, String> refactoredSimilarElements) {
    fOldType = oldType;
    fNewType = newType;
    if (refactoredSimilarElements == null) fRefactoredSimilarElements = Collections.emptyMap();
    else fRefactoredSimilarElements = refactoredSimilarElements;
  }

  /**
   * Converts the handle. Handle need not exist, but must be a source reference.
   *
   * @param handle
   * @return the new handle
   */
  public IMember transplantHandle(IMember handle) {

    /*
     * Create a list of handles from top-level type to the handle
     */
    final LinkedList<IMember> oldElements = new LinkedList<IMember>();
    addElements(handle, oldElements);

    /*
     * Step through the elements and re-locate them in the new parents.
     */
    final IMember[] newElements = convertElements(oldElements.toArray(new IMember[0]));

    return newElements[newElements.length - 1];
  }

  private void addElements(IMember element, LinkedList<IMember> chain) {
    chain.addFirst(element);
    IJavaElement parent = element.getParent();
    if (parent instanceof IMember) addElements((IMember) parent, chain);
  }

  private IMember[] convertElements(IMember[] oldElements) {

    final IMember[] newElements = new IMember[oldElements.length];
    final IMember first = oldElements[0];

    Assert.isTrue(first instanceof IType);

    if (first.equals(fOldType))
      // We renamed a top level type.
      newElements[0] = fNewType;
    else newElements[0] = first;

    /*
     * Note that we only need to translate the information necessary to
     * create new handles. For example, the return type of a method is not
     * relevant; neither is information about generic specifics in types.
     */

    for (int i = 1; i < oldElements.length; i++) {
      final IJavaElement newParent = newElements[i - 1];
      final IJavaElement currentElement = oldElements[i];
      switch (newParent.getElementType()) {
        case IJavaElement.TYPE:
          {
            switch (currentElement.getElementType()) {
              case IJavaElement.TYPE:
                {
                  final String newName = resolveTypeName((IType) currentElement);
                  newElements[i] = ((IType) newParent).getType(newName);
                  break;
                }
              case IJavaElement.METHOD:
                {
                  final String newName = resolveElementName(currentElement);
                  final String[] newParameterTypes =
                      resolveParameterTypes((IMethod) currentElement);
                  newElements[i] = ((IType) newParent).getMethod(newName, newParameterTypes);
                  break;
                }
              case IJavaElement.INITIALIZER:
                {
                  final IInitializer initializer = (IInitializer) currentElement;
                  newElements[i] =
                      ((IType) newParent).getInitializer(initializer.getOccurrenceCount());
                  break;
                }
              case IJavaElement.FIELD:
                {
                  final String newName = resolveElementName(currentElement);
                  newElements[i] = ((IType) newParent).getField(newName);
                  break;
                }
            }
            break;
          }
        case IJavaElement.METHOD:
          {
            switch (currentElement.getElementType()) {
              case IJavaElement.TYPE:
                {
                  newElements[i] = resolveTypeInMember((IMethod) newParent, (IType) currentElement);
                  break;
                }
            }
            break;
          }
        case IJavaElement.INITIALIZER:
          {
            switch (currentElement.getElementType()) {
              case IJavaElement.TYPE:
                {
                  newElements[i] =
                      resolveTypeInMember((IInitializer) newParent, (IType) currentElement);
                  break;
                }
            }
            break;
          }
        case IJavaElement.FIELD:
          {
            switch (currentElement.getElementType()) {
              case IJavaElement.TYPE:
                {
                  // anonymous type in field declaration
                  newElements[i] = resolveTypeInMember((IField) newParent, (IType) currentElement);
                  break;
                }
            }
            break;
          }
      }
    }
    return newElements;
  }

  private String[] resolveParameterTypes(IMethod method) {
    final String[] oldParameterTypes = method.getParameterTypes();
    final String[] newparams = new String[oldParameterTypes.length];

    final String[] possibleOldSigs = new String[4];
    possibleOldSigs[0] = Signature.createTypeSignature(fOldType.getElementName(), false);
    possibleOldSigs[1] = Signature.createTypeSignature(fOldType.getElementName(), true);
    possibleOldSigs[2] = Signature.createTypeSignature(fOldType.getFullyQualifiedName(), false);
    possibleOldSigs[3] = Signature.createTypeSignature(fOldType.getFullyQualifiedName(), true);

    final String[] possibleNewSigs = new String[4];
    possibleNewSigs[0] = Signature.createTypeSignature(fNewType.getElementName(), false);
    possibleNewSigs[1] = Signature.createTypeSignature(fNewType.getElementName(), true);
    possibleNewSigs[2] = Signature.createTypeSignature(fNewType.getFullyQualifiedName(), false);
    possibleNewSigs[3] = Signature.createTypeSignature(fNewType.getFullyQualifiedName(), true);

    // Textually replace all occurrences
    // This handles stuff like Map<SomeClass, some.package.SomeClass>
    for (int i = 0; i < oldParameterTypes.length; i++) {
      newparams[i] = oldParameterTypes[i];
      for (int j = 0; j < possibleOldSigs.length; j++) {
        newparams[i] = replaceAll(newparams[i], possibleOldSigs[j], possibleNewSigs[j]);
      }
    }
    return newparams;
  }

  private String resolveElementName(IJavaElement element) {
    final String newName = fRefactoredSimilarElements.get(element);
    if (newName != null) return newName;
    else return element.getElementName();
  }

  private IMember resolveTypeInMember(final IMember newParent, IType oldChild) {
    // Local type or anonymous type. Only local types can be renamed.
    String newName = ""; // $NON-NLS-1$
    if (oldChild.getElementName().length() != 0) newName = resolveTypeName(oldChild);
    return newParent.getType(newName, oldChild.getOccurrenceCount());
  }

  private String resolveTypeName(IType type) {
    return type.equals(fOldType) ? fNewType.getElementName() : type.getElementName();
  }

  private static String replaceAll(
      final String source, final String replaceFrom, final String replaceTo) {
    final StringBuffer buf = new StringBuffer(source.length());
    int currentIndex = 0;
    int matchIndex;
    while ((matchIndex = source.indexOf(replaceFrom, currentIndex)) != -1) {
      buf.append(source.substring(currentIndex, matchIndex));
      buf.append(replaceTo);
      currentIndex = matchIndex + replaceFrom.length();
    }
    buf.append(source.substring(currentIndex));
    return buf.toString();
  }
}
