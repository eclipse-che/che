/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.corext.util.JdtFlags;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.search.ElementQuerySpecification;
import org.eclipse.jdt.ui.search.PatternQuerySpecification;
import org.eclipse.jdt.ui.search.QuerySpecification;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchFilter;

abstract class JavaMatchFilter extends MatchFilter {

  public abstract boolean filters(JavaElementMatch match);

  /**
   * Returns whether this filter is applicable for this query.
   *
   * @param query the query
   * @return <code>true</code> if this match filter is applicable for the given query
   */
  public abstract boolean isApplicable(JavaSearchQuery query);

  /* (non-Javadoc)
   * @see org.eclipse.search.ui.text.MatchFilter#filters(org.eclipse.search.ui.text.Match)
   */
  @Override
  public boolean filters(Match match) {
    if (match instanceof JavaElementMatch) {
      return filters((JavaElementMatch) match);
    }
    return false;
  }

  private static final String SETTINGS_LAST_USED_FILTERS = "filters_last_used"; // $NON-NLS-1$

  public static MatchFilter[] getLastUsedFilters() {
    String string = JavaPlugin.getDefault().getDialogSettings().get(SETTINGS_LAST_USED_FILTERS);
    if (string != null) {
      return decodeFiltersString(string);
    }
    return getDefaultFilters();
  }

  public static void setLastUsedFilters(MatchFilter[] filters) {
    String encoded = encodeFilters(filters);
    JavaPlugin.getDefault().getDialogSettings().put(SETTINGS_LAST_USED_FILTERS, encoded);
  }

  public static MatchFilter[] getDefaultFilters() {
    return new MatchFilter[] {IMPORT_FILTER};
  }

  private static String encodeFilters(MatchFilter[] enabledFilters) {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < enabledFilters.length; i++) {
      MatchFilter matchFilter = enabledFilters[i];
      buf.append(matchFilter.getID());
      buf.append(';');
    }
    return buf.toString();
  }

  private static JavaMatchFilter[] decodeFiltersString(String encodedString) {
    StringTokenizer tokenizer = new StringTokenizer(encodedString, String.valueOf(';'));
    HashSet<JavaMatchFilter> result = new HashSet<JavaMatchFilter>();
    while (tokenizer.hasMoreTokens()) {
      JavaMatchFilter curr = findMatchFilter(tokenizer.nextToken());
      if (curr != null) {
        result.add(curr);
      }
    }
    return result.toArray(new JavaMatchFilter[result.size()]);
  }

  private static final JavaMatchFilter POTENTIAL_FILTER = new PotentialFilter();
  private static final JavaMatchFilter IMPORT_FILTER = new ImportFilter();
  private static final JavaMatchFilter JAVADOC_FILTER = new JavadocFilter();
  private static final JavaMatchFilter READ_FILTER = new ReadFilter();
  private static final JavaMatchFilter WRITE_FILTER = new WriteFilter();

  private static final JavaMatchFilter POLYMORPHIC_FILTER = new PolymorphicFilter();
  private static final JavaMatchFilter INEXACT_FILTER = new InexactMatchFilter();
  private static final JavaMatchFilter ERASURE_FILTER = new ErasureMatchFilter();

  private static final JavaMatchFilter NON_PUBLIC_FILTER = new NonPublicFilter();
  private static final JavaMatchFilter STATIC_FILTER = new StaticFilter();
  private static final JavaMatchFilter NON_STATIC_FILTER = new NonStaticFilter();
  private static final JavaMatchFilter DEPRECATED_FILTER = new DeprecatedFilter();
  private static final JavaMatchFilter NON_DEPRECATED_FILTER = new NonDeprecatedFilter();

  private static final JavaMatchFilter[] ALL_FILTERS =
      new JavaMatchFilter[] {
        POTENTIAL_FILTER,
        IMPORT_FILTER,
        JAVADOC_FILTER,
        READ_FILTER,
        WRITE_FILTER,
        POLYMORPHIC_FILTER,
        INEXACT_FILTER,
        ERASURE_FILTER,
        NON_PUBLIC_FILTER,
        STATIC_FILTER,
        NON_STATIC_FILTER,
        DEPRECATED_FILTER,
        NON_DEPRECATED_FILTER
      };

  public static JavaMatchFilter[] allFilters() {
    return ALL_FILTERS;
  }

  public static JavaMatchFilter[] allFilters(JavaSearchQuery query) {
    ArrayList<JavaMatchFilter> res = new ArrayList<JavaMatchFilter>();
    for (int i = 0; i < ALL_FILTERS.length; i++) {
      JavaMatchFilter curr = ALL_FILTERS[i];
      if (curr.isApplicable(query)) {
        res.add(curr);
      }
    }
    return res.toArray(new JavaMatchFilter[res.size()]);
  }

  private static JavaMatchFilter findMatchFilter(String id) {
    for (int i = 0; i < ALL_FILTERS.length; i++) {
      JavaMatchFilter matchFilter = ALL_FILTERS[i];
      if (matchFilter.getID().equals(id)) return matchFilter;
    }
    return null;
  }
}

class PotentialFilter extends JavaMatchFilter {
  @Override
  public boolean filters(JavaElementMatch match) {
    return match.getAccuracy() == SearchMatch.A_INACCURATE;
  }

  @Override
  public String getName() {
    return SearchMessages.MatchFilter_PotentialFilter_name;
  }

  @Override
  public String getActionLabel() {
    return SearchMessages.MatchFilter_PotentialFilter_actionLabel;
  }

  @Override
  public String getDescription() {
    return SearchMessages.MatchFilter_PotentialFilter_description;
  }

  @Override
  public boolean isApplicable(JavaSearchQuery query) {
    return true;
  }

  @Override
  public String getID() {
    return "filter_potential"; // $NON-NLS-1$
  }
}

class ImportFilter extends JavaMatchFilter {
  @Override
  public boolean filters(JavaElementMatch match) {
    return match.getElement() instanceof IImportDeclaration;
  }

  @Override
  public String getName() {
    return SearchMessages.MatchFilter_ImportFilter_name;
  }

  @Override
  public String getActionLabel() {
    return SearchMessages.MatchFilter_ImportFilter_actionLabel;
  }

  @Override
  public String getDescription() {
    return SearchMessages.MatchFilter_ImportFilter_description;
  }

  @Override
  public boolean isApplicable(JavaSearchQuery query) {
    QuerySpecification spec = query.getSpecification();
    if (spec instanceof ElementQuerySpecification) {
      ElementQuerySpecification elementSpec = (ElementQuerySpecification) spec;
      IJavaElement element = elementSpec.getElement();
      switch (element.getElementType()) {
        case IJavaElement.TYPE:
        case IJavaElement.METHOD:
        case IJavaElement.FIELD:
        case IJavaElement.PACKAGE_FRAGMENT:
          return true;
        default:
          return false;
      }
    } else if (spec instanceof PatternQuerySpecification) {
      return true;
    }
    return false;
  }

  @Override
  public String getID() {
    return "filter_imports"; // $NON-NLS-1$
  }
}

abstract class VariableFilter extends JavaMatchFilter {
  @Override
  public boolean isApplicable(JavaSearchQuery query) {
    QuerySpecification spec = query.getSpecification();
    if (spec instanceof ElementQuerySpecification) {
      ElementQuerySpecification elementSpec = (ElementQuerySpecification) spec;
      IJavaElement element = elementSpec.getElement();
      return element instanceof IField || element instanceof ILocalVariable;
    } else if (spec instanceof PatternQuerySpecification) {
      PatternQuerySpecification patternSpec = (PatternQuerySpecification) spec;
      return patternSpec.getSearchFor() == IJavaSearchConstants.FIELD;
    }
    return false;
  }
}

class WriteFilter extends VariableFilter {
  @Override
  public boolean filters(JavaElementMatch match) {
    return match.isWriteAccess() && !match.isReadAccess();
  }

  @Override
  public String getName() {
    return SearchMessages.MatchFilter_WriteFilter_name;
  }

  @Override
  public String getActionLabel() {
    return SearchMessages.MatchFilter_WriteFilter_actionLabel;
  }

  @Override
  public String getDescription() {
    return SearchMessages.MatchFilter_WriteFilter_description;
  }

  @Override
  public String getID() {
    return "filter_writes"; // $NON-NLS-1$
  }
}

class ReadFilter extends VariableFilter {
  @Override
  public boolean filters(JavaElementMatch match) {
    return match.isReadAccess() && !match.isWriteAccess();
  }

  @Override
  public String getName() {
    return SearchMessages.MatchFilter_ReadFilter_name;
  }

  @Override
  public String getActionLabel() {
    return SearchMessages.MatchFilter_ReadFilter_actionLabel;
  }

  @Override
  public String getDescription() {
    return SearchMessages.MatchFilter_ReadFilter_description;
  }

  @Override
  public String getID() {
    return "filter_reads"; // $NON-NLS-1$
  }
}

class JavadocFilter extends JavaMatchFilter {
  @Override
  public boolean filters(JavaElementMatch match) {
    return match.isJavadoc();
  }

  @Override
  public String getName() {
    return SearchMessages.MatchFilter_JavadocFilter_name;
  }

  @Override
  public String getActionLabel() {
    return SearchMessages.MatchFilter_JavadocFilter_actionLabel;
  }

  @Override
  public String getDescription() {
    return SearchMessages.MatchFilter_JavadocFilter_description;
  }

  @Override
  public boolean isApplicable(JavaSearchQuery query) {
    return true;
  }

  @Override
  public String getID() {
    return "filter_javadoc"; // $NON-NLS-1$
  }
}

class PolymorphicFilter extends JavaMatchFilter {
  @Override
  public boolean filters(JavaElementMatch match) {
    return match.isSuperInvocation();
  }

  @Override
  public String getName() {
    return SearchMessages.MatchFilter_PolymorphicFilter_name;
  }

  @Override
  public String getActionLabel() {
    return SearchMessages.MatchFilter_PolymorphicFilter_actionLabel;
  }

  @Override
  public String getDescription() {
    return SearchMessages.MatchFilter_PolymorphicFilter_description;
  }

  @Override
  public boolean isApplicable(JavaSearchQuery query) {
    QuerySpecification spec = query.getSpecification();
    switch (spec.getLimitTo()) {
      case IJavaSearchConstants.REFERENCES:
      case IJavaSearchConstants.ALL_OCCURRENCES:
        if (spec instanceof ElementQuerySpecification) {
          ElementQuerySpecification elementSpec = (ElementQuerySpecification) spec;
          return elementSpec.getElement() instanceof IMethod;
        } else if (spec instanceof PatternQuerySpecification) {
          PatternQuerySpecification patternSpec = (PatternQuerySpecification) spec;
          return patternSpec.getSearchFor() == IJavaSearchConstants.METHOD;
        }
    }
    return false;
  }

  @Override
  public String getID() {
    return "filter_polymorphic"; // $NON-NLS-1$
  }
}

abstract class GenericTypeFilter extends JavaMatchFilter {
  @Override
  public boolean isApplicable(JavaSearchQuery query) {
    QuerySpecification spec = query.getSpecification();
    if (spec instanceof ElementQuerySpecification) {
      ElementQuerySpecification elementSpec = (ElementQuerySpecification) spec;
      IJavaElement element = elementSpec.getElement();
      return isParameterizedElement(element);
    }
    return false;
  }

  private static boolean isParameterizedElement(IJavaElement element) {
    while (element != null) {
      ITypeParameter[] typeParameters = null;
      try {
        if (element instanceof IType) {
          typeParameters = ((IType) element).getTypeParameters();
        } else if (element instanceof IMethod) {
          typeParameters = ((IMethod) element).getTypeParameters();
        }
      } catch (JavaModelException e) {
        return false;
      }
      if (typeParameters == null) return false;

      if (typeParameters.length > 0) return true;

      element = element.getParent();
    }
    return false;
  }
}

class ErasureMatchFilter extends GenericTypeFilter {
  @Override
  public boolean filters(JavaElementMatch match) {
    return (match.getMatchRule() & (SearchPattern.R_FULL_MATCH | SearchPattern.R_EQUIVALENT_MATCH))
        == 0;
  }

  @Override
  public String getName() {
    return SearchMessages.MatchFilter_ErasureFilter_name;
  }

  @Override
  public String getActionLabel() {
    return SearchMessages.MatchFilter_ErasureFilter_actionLabel;
  }

  @Override
  public String getDescription() {
    return SearchMessages.MatchFilter_ErasureFilter_description;
  }

  @Override
  public String getID() {
    return "filter_erasure"; // $NON-NLS-1$
  }
}

class InexactMatchFilter extends GenericTypeFilter {
  @Override
  public boolean filters(JavaElementMatch match) {
    return (match.getMatchRule() & (SearchPattern.R_FULL_MATCH)) == 0;
  }

  @Override
  public String getName() {
    return SearchMessages.MatchFilter_InexactFilter_name;
  }

  @Override
  public String getActionLabel() {
    return SearchMessages.MatchFilter_InexactFilter_actionLabel;
  }

  @Override
  public String getDescription() {
    return SearchMessages.MatchFilter_InexactFilter_description;
  }

  @Override
  public String getID() {
    return "filter_inexact"; // $NON-NLS-1$
  }
}

abstract class ModifierFilter extends JavaMatchFilter {
  @Override
  public boolean isApplicable(JavaSearchQuery query) {
    return true;
  }
}

class NonPublicFilter extends ModifierFilter {
  @Override
  public boolean filters(JavaElementMatch match) {
    Object element = match.getElement();
    if (element instanceof IMember) {
      try {
        return !JdtFlags.isPublic((IMember) element);
      } catch (JavaModelException e) {
        JavaPlugin.log(e);
      }
    }
    return false;
  }

  @Override
  public String getName() {
    return SearchMessages.MatchFilter_NonPublicFilter_name;
  }

  @Override
  public String getActionLabel() {
    return SearchMessages.MatchFilter_NonPublicFilter_actionLabel;
  }

  @Override
  public String getDescription() {
    return SearchMessages.MatchFilter_NonPublicFilter_description;
  }

  @Override
  public String getID() {
    return "filter_non_public"; // $NON-NLS-1$
  }
}

class StaticFilter extends ModifierFilter {
  @Override
  public boolean filters(JavaElementMatch match) {
    Object element = match.getElement();
    if (element instanceof IMember) {
      try {
        return JdtFlags.isStatic((IMember) element);
      } catch (JavaModelException e) {
        JavaPlugin.log(e);
      }
    }
    return false;
  }

  @Override
  public String getName() {
    return SearchMessages.MatchFilter_StaticFilter_name;
  }

  @Override
  public String getActionLabel() {
    return SearchMessages.MatchFilter_StaticFilter_actionLabel;
  }

  @Override
  public String getDescription() {
    return SearchMessages.MatchFilter_StaticFilter_description;
  }

  @Override
  public String getID() {
    return "filter_static"; // $NON-NLS-1$
  }
}

class NonStaticFilter extends ModifierFilter {
  @Override
  public boolean filters(JavaElementMatch match) {
    Object element = match.getElement();
    if (element instanceof IMember) {
      try {
        return !JdtFlags.isStatic((IMember) element);
      } catch (JavaModelException e) {
        JavaPlugin.log(e);
      }
    }
    return false;
  }

  @Override
  public String getName() {
    return SearchMessages.MatchFilter_NonStaticFilter_name;
  }

  @Override
  public String getActionLabel() {
    return SearchMessages.MatchFilter_NonStaticFilter_actionLabel;
  }

  @Override
  public String getDescription() {
    return SearchMessages.MatchFilter_NonStaticFilter_description;
  }

  @Override
  public String getID() {
    return "filter_non_static"; // $NON-NLS-1$
  }
}

class DeprecatedFilter extends ModifierFilter {
  @Override
  public boolean filters(JavaElementMatch match) {
    Object element = match.getElement();
    if (element instanceof IMember) {
      try {
        return JdtFlags.isDeprecated((IMember) element);
      } catch (JavaModelException e) {
        JavaPlugin.log(e);
      }
    }
    return false;
  }

  @Override
  public String getName() {
    return SearchMessages.MatchFilter_DeprecatedFilter_name;
  }

  @Override
  public String getActionLabel() {
    return SearchMessages.MatchFilter_DeprecatedFilter_actionLabel;
  }

  @Override
  public String getDescription() {
    return SearchMessages.MatchFilter_DeprecatedFilter_description;
  }

  @Override
  public String getID() {
    return "filter_deprecated"; // $NON-NLS-1$
  }
}

class NonDeprecatedFilter extends ModifierFilter {
  @Override
  public boolean filters(JavaElementMatch match) {
    Object element = match.getElement();
    if (element instanceof IMember) {
      try {
        return !JdtFlags.isDeprecated((IMember) element);
      } catch (JavaModelException e) {
        JavaPlugin.log(e);
      }
    }
    return false;
  }

  @Override
  public String getName() {
    return SearchMessages.MatchFilter_NonDeprecatedFilter_name;
  }

  @Override
  public String getActionLabel() {
    return SearchMessages.MatchFilter_NonDeprecatedFilter_actionLabel;
  }

  @Override
  public String getDescription() {
    return SearchMessages.MatchFilter_NonDeprecatedFilter_description;
  }

  @Override
  public String getID() {
    return "filter_non_deprecated"; // $NON-NLS-1$
  }
}
