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

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.text.JavaWordIterator;

/**
 * This class contains methods for suggesting new names for variables or methods whose name consists
 * at least partly of the name of their declaring type (or in case of methods, the return type or a
 * parameter type).
 *
 * <p>The methods return the newly suggested method or variable name in case of a match, or null in
 * case nothing matched.
 *
 * <p>In any case, prefixes and suffixes are removed from variable names. As method names have no
 * configurable suffixes or prefixes, they are left unchanged. The remaining name is called
 * "stripped element name".
 *
 * <p>After the match according to the strategy, prefixes and suffixes are reapplied to the names.
 *
 * <p>EXACT STRATEGY (always performed).
 * ----------------------------------------------------------------
 *
 * <p>The stripped element name is directly compared with the type name:
 *
 * <p>a) the first character must match case-insensitive
 *
 * <p>b) all other characters must match case-sensitive
 *
 * <p>In case of a match, the new type name is returned (first character adapted, respectively).
 * Suffixes/Prefixes are reapplied.
 *
 * <p>Note that this also matches fields with names like "SomeField", "fsomeField", and method names
 * like "JavaElement()".
 *
 * <p>EMBEDDED STRATEGY (performed second if chosen by user).
 * ----------------------------------------------------------------
 *
 * <p>A search is performed in the stripped element name for the old type name:
 *
 * <p>a) the first character must match case-insensitive
 *
 * <p>b) all other characters must match case-sensitive
 *
 * <p>c) the stripped element name must end after the type name, or the next character must be a
 * non-letter, or the next character must be upper cased.
 *
 * <p>In case of a match, the new type is inserted into the stripped element name, replacing the old
 * type name, first character adapted to the correct case. Suffixes/Prefixes are reapplied.
 *
 * <p>Note that this also matches methods with names like "createjavaElement()" or fields like
 * "fjavaElementCache".
 *
 * <p>SUFFIX STRATEGY (performed third if chosen by user)
 * ----------------------------------------------------------------
 *
 * <p>The new and old type names are analyzed for "camel case suffixes", that is, substrings which
 * begin with an uppercased letter. For example, "SimpleJavaElement" is split into the three hunks
 * "Simple", "Java", and "Element". If one type name has more suffixes than the other, both are
 * stripped to the smaller size.
 *
 * <p>Then, a search is performed in the stripped variable name hunks from back to front. At least
 * the last hunk must be found, others may then extend the match. Each hunk must match like in the
 * exact strategy, i.e.
 *
 * <p>a) the first character must match case-insensitive
 *
 * <p>b) all other characters must match case-sensitive
 *
 * <p>In case of a match, the matched hunks of the new type replace the hunks of the old type.
 * Suffixes/Prefixes are reapplied.
 *
 * <p>Note that numbers and other non-letter characters belong to the previous camel case substring.
 *
 * @since 3.2
 */
public class RenamingNameSuggestor {

  /*
   * ADDITIONAL OPTIONS
   * ----------------------------------------------------------------
   *
   * There are two additional flags which may be set in this class to allow
   * better matching of special cases:
   *
   * a) Special treatment of leading "I"s in type names, i.e. interface names
   * 	  like "IJavaElement". If the corresponding flag is set, leading "I"s are
   * 	  stripped from type names if the second char is also uppercase to allow
   * 	  exact matching of variable names like "javaElement" for type
   * 	  "IJavaElement". Note that embedded matching already matches cases like
   * 	  this.
   *
   * b) Special treatment of all-uppercase type names or all-uppercase type
   * 	  name camel-case hunks, i.e. names like "AST" or "PersonalURL". If the
   * 	  corresponding flag is set, the type name hunks will be transformed such
   * 	  that variables like "fAst", "ast", "personalUrl", or "url" are found as
   * 	  well. The target name will be transformed too if it is an
   * 	  all-uppercase type name camel-case hunk as well.
   *
   * 	  NOTE that in exact or embedded mode, the whole type name must be
   * 	  all-uppercase to allow matching custom-lowercased variable names, i.e.
   *    there are no attempts to "guess" which hunk of the new name should be lowercased
   *    to match a partly lowercased variable name. In suffix mode, hunks of the
   *    new type which are at the same position as in the old type will be
   *    lowercased if necessary.
   *
   * c) Support for (english) plural forms. If the corresponding flag is set, the
   *    suggestor will try to match variables which have plural forms of the
   *    type name, for example "handies" for "Handy" or "phones" for "MobilePhone".
   *    The target name will be transformed as well, i.e. conversion like
   *    "fHandies" -> "fPhones" are supported.
   *
   */

  public static final int STRATEGY_EXACT = 1;
  public static final int STRATEGY_EMBEDDED = 2;
  public static final int STRATEGY_SUFFIX = 3;

  private static final String PLURAL_S = "s"; // $NON-NLS-1$
  private static final String PLURAL_IES = "ies"; // $NON-NLS-1$
  private static final String SINGULAR_Y = "y"; // $NON-NLS-1$

  private int fStrategy;
  private String[] fFieldPrefixes;
  private String[] fFieldSuffixes;
  private String[] fStaticFieldPrefixes;
  private String[] fStaticFieldSuffixes;
  private String[] fLocalPrefixes;
  private String[] fLocalSuffixes;
  private String[] fArgumentPrefixes;
  private String[] fArgumentSuffixes;

  private boolean fExtendedInterfaceNameMatching;
  private boolean fExtendedAllUpperCaseHunkMatching;
  private boolean fExtendedPluralMatching;

  public RenamingNameSuggestor() {
    this(STRATEGY_SUFFIX);
  }

  public RenamingNameSuggestor(int strategy) {

    Assert.isTrue(strategy >= 1 && strategy <= 3);

    fStrategy = strategy;
    fExtendedInterfaceNameMatching = true;
    fExtendedAllUpperCaseHunkMatching = true;
    fExtendedPluralMatching = true;

    resetPrefixes();
  }

  public String suggestNewFieldName(
      IJavaProject project,
      String oldFieldName,
      boolean isStatic,
      String oldTypeName,
      String newTypeName) {

    initializePrefixesAndSuffixes(project);

    if (isStatic)
      return suggestNewVariableName(
          fStaticFieldPrefixes, fStaticFieldSuffixes, oldFieldName, oldTypeName, newTypeName);
    else
      return suggestNewVariableName(
          fFieldPrefixes, fFieldSuffixes, oldFieldName, oldTypeName, newTypeName);
  }

  public String suggestNewLocalName(
      IJavaProject project,
      String oldLocalName,
      boolean isArgument,
      String oldTypeName,
      String newTypeName) {

    initializePrefixesAndSuffixes(project);

    if (isArgument)
      return suggestNewVariableName(
          fArgumentPrefixes, fArgumentSuffixes, oldLocalName, oldTypeName, newTypeName);
    else
      return suggestNewVariableName(
          fLocalPrefixes, fLocalSuffixes, oldLocalName, oldTypeName, newTypeName);
  }

  public String suggestNewMethodName(String oldMethodName, String oldTypeName, String newTypeName) {

    Assert.isNotNull(oldMethodName);
    Assert.isNotNull(oldTypeName);
    Assert.isNotNull(newTypeName);
    Assert.isTrue(oldMethodName.length() > 0);
    Assert.isTrue(oldTypeName.length() > 0);
    Assert.isTrue(newTypeName.length() > 0);

    resetPrefixes();

    return match(oldTypeName, newTypeName, oldMethodName);
  }

  public String suggestNewVariableName(
      String[] prefixes,
      String[] suffixes,
      String oldVariableName,
      String oldTypeName,
      String newTypeName) {

    Assert.isNotNull(prefixes);
    Assert.isNotNull(suffixes);
    Assert.isNotNull(oldVariableName);
    Assert.isNotNull(oldTypeName);
    Assert.isNotNull(newTypeName);
    Assert.isTrue(oldVariableName.length() > 0);
    Assert.isTrue(oldTypeName.length() > 0);
    Assert.isTrue(newTypeName.length() > 0);

    final String usedPrefix = findLongestPrefix(oldVariableName, prefixes);
    final String usedSuffix = findLongestSuffix(oldVariableName, suffixes);
    final String strippedVariableName =
        oldVariableName.substring(
            usedPrefix.length(), oldVariableName.length() - usedSuffix.length());

    String newVariableName = match(oldTypeName, newTypeName, strippedVariableName);
    return (newVariableName != null) ? usedPrefix + newVariableName + usedSuffix : null;
  }

  // -------------------------------------- Match methods

  private String match(
      final String oldTypeName, final String newTypeName, final String strippedVariableName) {

    String oldType = oldTypeName;
    String newType = newTypeName;

    if (fExtendedInterfaceNameMatching && isInterfaceName(oldType) && isInterfaceName(newType)) {
      oldType = getInterfaceName(oldType);
      newType = getInterfaceName(newType);
    }

    String newVariableName = matchDirect(oldType, newType, strippedVariableName);

    if (fExtendedPluralMatching && newVariableName == null && canPluralize(oldType))
      newVariableName = matchDirect(pluralize(oldType), pluralize(newType), strippedVariableName);

    return newVariableName;
  }

  private String matchDirect(String oldType, String newType, final String strippedVariableName) {
    /*
     * Use all strategies applied by the user. Always start with exact
     * matching.
     *
     * Note that suffix matching may not match the whole type name if the
     * new type name has a smaller camel case chunk count.
     */

    String newVariableName = exactMatch(oldType, newType, strippedVariableName);
    if (newVariableName == null && fStrategy >= STRATEGY_EMBEDDED)
      newVariableName = embeddedMatch(oldType, newType, strippedVariableName);
    if (newVariableName == null && fStrategy >= STRATEGY_SUFFIX)
      newVariableName = suffixMatch(oldType, newType, strippedVariableName);

    return newVariableName;
  }

  private String exactMatch(
      final String oldTypeName, final String newTypeName, final String strippedVariableName) {

    String newName = exactDirectMatch(oldTypeName, newTypeName, strippedVariableName);
    if (newName != null) return newName;

    if (fExtendedAllUpperCaseHunkMatching && isUpperCaseCamelCaseHunk(oldTypeName)) {
      String oldTN = getFirstUpperRestLowerCased(oldTypeName);
      String newTN =
          isUpperCaseCamelCaseHunk(newTypeName)
              ? getFirstUpperRestLowerCased(newTypeName)
              : newTypeName;
      newName = exactDirectMatch(oldTN, newTN, strippedVariableName);
    }

    return newName;
  }

  private String exactDirectMatch(
      final String oldTypeName, final String newTypeName, final String strippedVariableName) {

    if (strippedVariableName.equals(oldTypeName)) return newTypeName;

    if (strippedVariableName.equals(getLowerCased(oldTypeName))) return getLowerCased(newTypeName);

    return null;
  }

  private String embeddedMatch(
      String oldTypeName, String newTypeName, String strippedVariableName) {

    // possibility of a match?
    final String lowerCaseVariable = strippedVariableName.toLowerCase();
    final String lowerCaseOldTypeName = oldTypeName.toLowerCase();
    int presumedIndex = lowerCaseVariable.indexOf(lowerCaseOldTypeName);

    while (presumedIndex != -1) {
      // it may be there
      final String presumedTypeName =
          strippedVariableName.substring(presumedIndex, presumedIndex + oldTypeName.length());
      final String prefix = strippedVariableName.substring(0, presumedIndex);
      final String suffix = strippedVariableName.substring(presumedIndex + oldTypeName.length());

      // can match at all? (depends on suffix)
      if (startsNewHunk(suffix)) {

        String name = exactMatch(oldTypeName, newTypeName, presumedTypeName);
        if (name != null) return prefix + name + suffix;
      }

      // did not match -> find next occurrence
      presumedIndex = lowerCaseVariable.indexOf(lowerCaseOldTypeName, presumedIndex + 1);
    }

    return null;
  }

  private String suffixMatch(
      final String oldType, final String newType, final String strippedVariableName) {

    // get an array of all camel-cased elements from both types + the
    // variable
    String[] suffixesOld = getSuffixes(oldType);
    String[] suffixesNew = getSuffixes(newType);
    String[] suffixesVar = getSuffixes(strippedVariableName);

    // get an equal-sized array of the last n camel-cased elements
    int min = Math.min(suffixesOld.length, suffixesNew.length);
    String[] suffixesOldEqual = new String[min];
    String[] suffixesNewEqual = new String[min];
    System.arraycopy(suffixesOld, suffixesOld.length - min, suffixesOldEqual, 0, min);
    System.arraycopy(suffixesNew, suffixesNew.length - min, suffixesNewEqual, 0, min);

    // find endIndex. endIndex is the index of the last hunk of the old type
    // name in the variable name.
    int endIndex = -1;
    for (int j = suffixesVar.length - 1; j >= 0; j--) {
      String newHunkName =
          exactMatch(
              suffixesOldEqual[suffixesOldEqual.length - 1],
              suffixesNewEqual[suffixesNewEqual.length - 1],
              suffixesVar[j]);
      if (newHunkName != null) {
        endIndex = j;
        break;
      }
    }

    if (endIndex == -1) return null; // last hunk not found -> no match

    int stepBack = 0;
    int lastSuffixMatched = -1;
    int hunkInVarName = -1;
    for (int i = suffixesOldEqual.length - 1; i >= 0; i--) {

      hunkInVarName = endIndex - stepBack;
      stepBack++;

      if (hunkInVarName < 0) {
        // we have reached the beginning of the variable name
        break;
      }

      // try to match this hunk:
      String newHunkName =
          exactMatch(suffixesOldEqual[i], suffixesNewEqual[i], suffixesVar[hunkInVarName]);

      if (newHunkName == null) break; // only match complete suffixes

      suffixesVar[hunkInVarName] = newHunkName;
      lastSuffixMatched = i;
    }

    if (lastSuffixMatched == 0) {
      // we have matched ALL type hunks in the variable name,
      // insert any new prefixes of the new type name
      int newPrefixes = suffixesNew.length - suffixesNewEqual.length;
      if (newPrefixes > 0) {

        // Propagate lowercased start to the front
        if (Character.isLowerCase(suffixesVar[hunkInVarName].charAt(0))
            && Character.isUpperCase(suffixesOldEqual[lastSuffixMatched].charAt(0))) {
          suffixesVar[hunkInVarName] = getUpperCased(suffixesVar[hunkInVarName]);
          suffixesNew[0] = getLowerCased(suffixesNew[0]);
        }

        String[] newVariableName = new String[suffixesVar.length + newPrefixes];
        System.arraycopy(
            suffixesVar,
            0,
            newVariableName,
            0,
            hunkInVarName); // hunks before type name in variable name
        System.arraycopy(
            suffixesNew,
            0,
            newVariableName,
            hunkInVarName,
            newPrefixes); // new hunks in new type name
        System.arraycopy(
            suffixesVar,
            hunkInVarName,
            newVariableName,
            hunkInVarName + newPrefixes,
            suffixesVar.length - hunkInVarName); // matched + rest hunks
        suffixesVar = newVariableName;
      }
    }

    String varName = concat(suffixesVar);
    if (varName.equals(strippedVariableName)) return null; // no "silly suggestions"
    else return varName;
  }

  // ---------------- Helper methods

  /** True if the string is the beginning of a new camel case hunk. False if it is not. */
  private boolean startsNewHunk(String string) {

    if (string.length() == 0) return true;

    return isLegalChar(string.charAt(0));
  }

  /**
   * True if hunk is longer than 1 character and all letters in the hunk are uppercase. False if
   * not.
   */
  private boolean isUpperCaseCamelCaseHunk(String hunk) {
    if (hunk.length() < 2) return false;

    for (int i = 0; i < hunk.length(); i++) {
      if (!isLegalChar(hunk.charAt(i))) return false;
    }
    return true;
  }

  /** False if the character is a letter and it is lowercase. True in all other cases. */
  private boolean isLegalChar(char c) {
    if (Character.isLetter(c)) return Character.isUpperCase(c);
    return true;
  }

  /**
   * Grab a list of camelCase-separated suffixes from the typeName, for example:
   *
   * <p>"JavaElementName" => { "Java", "Element", "Name }
   *
   * <p>"ASTNode" => { "AST", "Node" }
   */
  private String[] getSuffixes(String typeName) {
    List<String> suffixes = new ArrayList<String>();
    JavaWordIterator iterator = new JavaWordIterator();
    iterator.setText(typeName);
    int lastmatch = 0;
    int match;
    while ((match = iterator.next()) != BreakIterator.DONE) {
      suffixes.add(typeName.substring(lastmatch, match));
      lastmatch = match;
    }
    return suffixes.toArray(new String[0]);
  }

  private String concat(String[] suffixesNewEqual) {
    StringBuffer returner = new StringBuffer();
    for (int j = 0; j < suffixesNewEqual.length; j++) {
      returner.append(suffixesNewEqual[j]);
    }
    return returner.toString();
  }

  private String getLowerCased(String name) {
    if (name.length() > 1) return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    else return name.toLowerCase();
  }

  private String getUpperCased(String name) {
    if (name.length() > 1) return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    else return name.toLowerCase();
  }

  private String getFirstUpperRestLowerCased(String name) {
    if (name.length() > 1)
      return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
    else return name.toLowerCase();
  }

  private boolean isInterfaceName(String typeName) {
    return ((typeName.length() >= 2)
        && typeName.charAt(0) == 'I'
        && Character.isUpperCase(typeName.charAt(1)));
  }

  private String getInterfaceName(String typeName) {
    return typeName.substring(1);
  }

  private String findLongestPrefix(String name, String[] prefixes) {
    String usedPrefix = ""; // $NON-NLS-1$
    int bestLen = 0;
    for (int i = 0; i < prefixes.length; i++) {
      if (name.startsWith(prefixes[i])) {
        if (prefixes[i].length() > bestLen) {
          bestLen = prefixes[i].length();
          usedPrefix = prefixes[i];
        }
      }
    }
    return usedPrefix;
  }

  private String findLongestSuffix(String name, String[] suffixes) {
    String usedPrefix = ""; // $NON-NLS-1$
    int bestLen = 0;
    for (int i = 0; i < suffixes.length; i++) {
      if (name.endsWith(suffixes[i])) {
        if (suffixes[i].length() > bestLen) {
          bestLen = suffixes[i].length();
          usedPrefix = suffixes[i];
        }
      }
    }
    return usedPrefix;
  }

  /**
   * Returns true if the type name can be pluralized by a string operation. This is always the case
   * if it does not already end with an "s".
   */
  private boolean canPluralize(String typeName) {
    return !typeName.endsWith(PLURAL_S);
  }

  private String pluralize(String typeName) {
    if (typeName.endsWith(SINGULAR_Y))
      typeName = typeName.substring(0, typeName.length() - 1).concat(PLURAL_IES);
    else if (!typeName.endsWith(PLURAL_S)) typeName = typeName.concat(PLURAL_S);
    return typeName;
  }

  private void resetPrefixes() {
    String[] empty = new String[0];
    fFieldPrefixes = empty;
    fFieldSuffixes = empty;
    fStaticFieldPrefixes = empty;
    fStaticFieldSuffixes = empty;
    fLocalPrefixes = empty;
    fLocalSuffixes = empty;
    fArgumentPrefixes = empty;
    fArgumentSuffixes = empty;
  }

  private void initializePrefixesAndSuffixes(IJavaProject project) {
    fFieldPrefixes = readCommaSeparatedPreference(project, JavaCore.CODEASSIST_FIELD_PREFIXES);
    fFieldSuffixes = readCommaSeparatedPreference(project, JavaCore.CODEASSIST_FIELD_SUFFIXES);
    fStaticFieldPrefixes =
        readCommaSeparatedPreference(project, JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES);
    fStaticFieldSuffixes =
        readCommaSeparatedPreference(project, JavaCore.CODEASSIST_STATIC_FIELD_SUFFIXES);
    fLocalPrefixes = readCommaSeparatedPreference(project, JavaCore.CODEASSIST_LOCAL_PREFIXES);
    fLocalSuffixes = readCommaSeparatedPreference(project, JavaCore.CODEASSIST_LOCAL_SUFFIXES);
    fArgumentPrefixes =
        readCommaSeparatedPreference(project, JavaCore.CODEASSIST_ARGUMENT_PREFIXES);
    fArgumentSuffixes =
        readCommaSeparatedPreference(project, JavaCore.CODEASSIST_ARGUMENT_SUFFIXES);
  }

  private String[] readCommaSeparatedPreference(IJavaProject project, String option) {
    String list = project.getOption(option, true);
    return list == null ? new String[0] : list.split(","); // $NON-NLS-1$
  }
}
