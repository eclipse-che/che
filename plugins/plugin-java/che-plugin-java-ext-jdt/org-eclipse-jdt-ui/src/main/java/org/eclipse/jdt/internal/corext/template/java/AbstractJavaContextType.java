/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2014 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation Sebastian Davids:
 * sdavids@gmx.de - see bug 25376 Lukas Hanke <hanke@yatta.de> - [templates][content assist] Content
 * assist for 'for' loop should suggest member variables - https://bugs.eclipse.org/117215
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.template.java;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.template.java.CompilationUnitCompletion.Variable;
import org.eclipse.jdt.internal.ui.text.template.contentassist.MultiVariable;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

/**
 * An abstract context type for templates inside Java code.
 *
 * @since 3.4
 */
public abstract class AbstractJavaContextType extends CompilationUnitContextType {

  protected abstract static class AbstractIterable extends TemplateVariableResolver {
    public AbstractIterable(String type, String description) {
      super(type, description);
    }

    @Override
    protected String[] resolveAll(TemplateContext context) {
      JavaContext jc = (JavaContext) context;
      Variable[] iterables = getVariables(jc);
      String[] names = new String[iterables.length];
      for (int i = 0; i < iterables.length; i++) names[i] = iterables[i].getName();
      if (names.length > 0) jc.markAsUsed(names[0]);
      return names;
    }

    protected abstract Variable[] getVariables(JavaContext jc);

    /*
     * @see org.eclipse.jface.text.templates.TemplateVariableResolver#resolve(org.eclipse.jface.text.templates.TemplateVariable, org.eclipse.jface.text.templates.TemplateContext)
     */
    @Override
    public void resolve(TemplateVariable variable, TemplateContext context) {
      if (variable instanceof MultiVariable) {
        JavaContext jc = (JavaContext) context;
        JavaVariable jv = (JavaVariable) variable;
        Variable[] iterables = getVariables(jc);
        if (iterables.length > 0) {
          jv.setChoices(iterables);
          jc.markAsUsed(iterables[0].getName());

          if (iterables.length > 1) variable.setUnambiguous(false);
          else variable.setUnambiguous(isUnambiguous(context));

          return;
        }
      }

      super.resolve(variable, context);
    }
  }

  protected static class Array extends AbstractIterable {
    public Array() {
      super(
          "array", JavaTemplateMessages.JavaContextType_variable_description_array); // $NON-NLS-1$
    }

    @Override
    protected Variable[] getVariables(JavaContext jc) {
      return jc.getArrays();
    }
  }

  protected static class Iterable extends AbstractIterable {
    public Iterable() {
      super(
          "iterable",
          JavaTemplateMessages.JavaContextType_variable_description_iterable); // $NON-NLS-1$
    }

    @Override
    protected Variable[] getVariables(JavaContext jc) {
      return jc.getIterables();
    }
  }

  protected abstract static class AbstractIterableType extends TemplateVariableResolver {
    private String fMasterName;

    public AbstractIterableType(String type, String desc, String master) {
      super(type, desc);
      fMasterName = master;
    }

    @Override
    protected String[] resolveAll(TemplateContext context) {
      JavaContext jc = (JavaContext) context;
      Variable[] iterables = getVariablesInContextScope(jc);
      String[] types = new String[iterables.length];
      for (int i = 0; i < iterables.length; i++) types[i] = iterables[i].getMemberTypeNames()[0];
      return types;
    }

    protected abstract Variable[] getVariablesInContextScope(JavaContext jc);

    /*
     * @see org.eclipse.jface.text.templates.TemplateVariableResolver#resolve(org.eclipse.jface.text.templates.TemplateVariable, org.eclipse.jface.text.templates.TemplateContext)
     */
    @Override
    public void resolve(TemplateVariable variable, TemplateContext context) {
      if (variable instanceof MultiVariable) {
        JavaContext jc = (JavaContext) context;
        MultiVariable mv = (MultiVariable) variable;

        Variable[] iterables = getVariablesInContextScope(jc);
        if (iterables.length > 0) {

          for (int i = 0; i < iterables.length; i++)
            mv.setChoices(iterables[i], iterables[i].getMemberTypeNames());

          TemplateVariable master = jc.getTemplateVariable(fMasterName);
          if (master instanceof MultiVariable) {
            final MultiVariable masterMv = (MultiVariable) master;
            jc.addDependency(masterMv, mv);
            mv.setKey(masterMv.getCurrentChoice());
          }

          if (iterables.length > 1 || iterables.length == 1 && mv.getChoices().length > 1)
            variable.setUnambiguous(false);
          else variable.setUnambiguous(isUnambiguous(context));

          return;
        }
      }

      super.resolve(variable, context);
    }
  }

  protected static class ArrayType extends AbstractIterableType {
    public ArrayType() {
      super(
          "array_type",
          JavaTemplateMessages.JavaContextType_variable_description_array_type,
          "array"); // $NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected Variable[] getVariablesInContextScope(JavaContext jc) {
      return jc.getArrays();
    }
  }

  protected static class IterableType extends AbstractIterableType {
    public IterableType() {
      super(
          "iterable_type",
          JavaTemplateMessages.JavaContextType_variable_description_iterable_type,
          "iterable"); // $NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected Variable[] getVariablesInContextScope(JavaContext jc) {
      return jc.getIterables();
    }
  }

  protected abstract static class AbstractIterableElement extends TemplateVariableResolver {
    private String fMasterName;

    public AbstractIterableElement(String type, String desc, String master) {
      super(type, desc);
      fMasterName = master;
    }

    @Override
    protected String[] resolveAll(TemplateContext context) {
      JavaContext jc = (JavaContext) context;
      Variable[] iterables = getLocalVariables(jc);
      String[] elements = new String[iterables.length];
      for (int i = 0; i < iterables.length; i++) {
        elements[i] = jc.suggestVariableNames(iterables[i].getMemberTypeNames()[0])[0];
        if (i == 0) jc.markAsUsed(elements[0]);
      }

      return elements;
    }

    protected abstract Variable[] getLocalVariables(JavaContext jc);

    /*
     * @see org.eclipse.jface.text.templates.TemplateVariableResolver#resolve(org.eclipse.jface.text.templates.TemplateVariable, org.eclipse.jface.text.templates.TemplateContext)
     */
    @Override
    public void resolve(TemplateVariable variable, TemplateContext context) {
      if (variable instanceof MultiVariable) {
        JavaContext jc = (JavaContext) context;
        MultiVariable mv = (MultiVariable) variable;

        Variable[] iterables = getLocalVariables(jc);
        if (iterables.length > 0) {
          for (int i = 0; i < iterables.length; i++) {
            String[] elements = jc.suggestVariableNames(iterables[i].getMemberTypeNames()[0]);
            mv.setChoices(iterables[i], elements);
          }

          TemplateVariable master = jc.getTemplateVariable(fMasterName);
          if (master instanceof MultiVariable) {
            final MultiVariable masterMv = (MultiVariable) master;
            jc.addDependency(masterMv, mv);
            mv.setKey(masterMv.getCurrentChoice());
          }
          jc.markAsUsed(mv.getDefaultValue());

          if (iterables.length > 1 || iterables.length == 1 && mv.getChoices().length > 1)
            variable.setUnambiguous(false);
          else variable.setUnambiguous(isUnambiguous(context));

          return;
        }
      }
      super.resolve(variable, context);
    }
  }

  protected static class ArrayElement extends AbstractIterableElement {
    public ArrayElement() {
      super(
          "array_element",
          JavaTemplateMessages.JavaContextType_variable_description_array_element,
          "array"); // $NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected Variable[] getLocalVariables(JavaContext jc) {
      return jc.getArrays();
    }
  }

  protected static class IterableElement extends AbstractIterableElement {
    public IterableElement() {
      super(
          "iterable_element",
          JavaTemplateMessages.JavaContextType_variable_description_iterable_element,
          "iterable"); // $NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected Variable[] getLocalVariables(JavaContext jc) {
      return jc.getIterables();
    }
  }

  protected static class Index extends NameResolver {
    public Index() {
      super("int"); // $NON-NLS-1$
      setType("index"); // $NON-NLS-1$
      setDescription(JavaTemplateMessages.JavaContextType_variable_description_index);
    }
  }

  protected static class Collection extends VarResolver {
    public Collection() {
      super("java.util.Collection"); // $NON-NLS-1$
      setType("collection"); // $NON-NLS-1$
      setDescription(JavaTemplateMessages.JavaContextType_variable_description_collection);
    }
  }

  protected static class Iterator extends NameResolver {
    public Iterator() {
      super("java.util.Iterator"); // $NON-NLS-1$
      setType("iterator"); // $NON-NLS-1$
      setDescription(JavaTemplateMessages.JavaContextType_variable_description_iterator);
    }
  }

  protected static class Todo extends TemplateVariableResolver {

    public Todo() {
      super("todo", JavaTemplateMessages.JavaContextType_variable_description_todo); // $NON-NLS-1$
    }

    @Override
    protected String resolve(TemplateContext context) {
      JavaContext javaContext = (JavaContext) context;
      ICompilationUnit compilationUnit = javaContext.getCompilationUnit();
      if (compilationUnit == null) return "XXX"; // $NON-NLS-1$

      IJavaProject javaProject = compilationUnit.getJavaProject();
      String todoTaskTag = StubUtility.getTodoTaskTag(javaProject);
      if (todoTaskTag == null) return "XXX"; // $NON-NLS-1$

      return todoTaskTag;
    }
  }
  /*
  	protected static class Arguments extends SimpleVariableResolver {
  	    public Arguments() {
  	     	super("arguments", TemplateMessages.getString("Javavariable.description.arguments"), "");
  	    }
  	}
  */

  /**
   * Initializes the context type resolvers.
   *
   * <p><strong>Note:</strong> Only call this method if this context type doesn't inherit from
   * another context type which already has these resolvers.
   *
   * @since 3.4
   */
  public void initializeContextTypeResolvers() {

    // global
    addResolver(new GlobalTemplateVariables.Cursor());
    addResolver(new GlobalTemplateVariables.WordSelection());
    addResolver(new SurroundWithLineSelection());
    addResolver(new GlobalTemplateVariables.Dollar());
    addResolver(new GlobalTemplateVariables.Date());
    addResolver(new GlobalTemplateVariables.Year());
    addResolver(new GlobalTemplateVariables.Time());
    addResolver(new GlobalTemplateVariables.User());

    // compilation unit
    addResolver(new File());
    addResolver(new PrimaryTypeName());
    addResolver(new ReturnType());
    addResolver(new Method());
    addResolver(new Type());
    addResolver(new Package());
    addResolver(new Project());
    addResolver(new Arguments());

    // java
    addResolver(new Array());
    addResolver(new ArrayType());
    addResolver(new ArrayElement());
    addResolver(new Index());
    addResolver(new Iterator());
    addResolver(new Collection());
    addResolver(new Iterable());
    addResolver(new IterableType());
    addResolver(new IterableElement());
    addResolver(new Todo());
  }

  /*
   * @see org.eclipse.jdt.internal.corext.template.java.CompilationUnitContextType#createContext(org.eclipse.jface.text.IDocument, int, int, org.eclipse.jdt.core.ICompilationUnit)
   */
  @Override
  public CompilationUnitContext createContext(
      IDocument document, int offset, int length, ICompilationUnit compilationUnit) {
    JavaContext javaContext = new JavaContext(this, document, offset, length, compilationUnit);
    initializeContext(javaContext);
    return javaContext;
  }

  /*
   * @see org.eclipse.jdt.internal.corext.template.java.CompilationUnitContextType#createContext(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.Position, org.eclipse.jdt.core.ICompilationUnit)
   */
  @Override
  public CompilationUnitContext createContext(
      IDocument document, Position completionPosition, ICompilationUnit compilationUnit) {
    JavaContext javaContext = new JavaContext(this, document, completionPosition, compilationUnit);
    initializeContext(javaContext);
    return javaContext;
  }

  /**
   * Hook to initialize the context
   *
   * @param context the context
   */
  protected void initializeContext(JavaContext context) {}
}
