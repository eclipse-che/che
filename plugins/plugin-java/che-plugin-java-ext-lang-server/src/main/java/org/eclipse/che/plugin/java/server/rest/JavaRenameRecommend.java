package org.eclipse.che.plugin.java.server.rest;

import org.eclipse.che.ide.ext.java.shared.dto.refactoring.LinkedRenameRefactoringApply;
import org.eclipse.che.plugin.java.server.refactoring.RefactoringException;
import org.eclipse.che.plugin.java.server.rest.recommend.model.DataModel;
import org.eclipse.che.plugin.java.server.rest.recommend.model.ResultModel;
import org.eclipse.che.plugin.java.server.rest.recommend.parser.Parser;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;

public class JavaRenameRecommend {

  public static ResultModel result;
  public static IJavaElement javaElement;

  public JavaRenameRecommend() {
    result = null;
  }

  private DataModel getInfo(LinkedRenameRefactoringApply refactoringApply)
      throws RefactoringException, CoreException {
    DataModel dataModel = new DataModel();
    int elementType = javaElement.getElementType();
    if (elementType == IJavaElement.TYPE)
      dataModel.setRefactorType("org.eclipse.jdt.ui.recommend.type");
    else if (elementType == IJavaElement.FIELD)
      dataModel.setRefactorType("org.eclipse.jdt.ui.recommend.field");
    else if (elementType == IJavaElement.METHOD)
      dataModel.setRefactorType("org.eclipse.jdt.ui.recommend.method");
    else if (elementType == IJavaElement.LOCAL_VARIABLE)
      dataModel.setRefactorType("org.eclipse.jdt.ui.recommend.local.variable");
    else return null;

    dataModel.setProjectName(javaElement.getJavaProject().getProject().getName());
    dataModel.setOriginalName(javaElement.getElementName());
    dataModel.setSubsequentName(refactoringApply.getNewName());
    dataModel.setJavaElement(javaElement);

    if (dataModel.getRefactorType().equals("org.eclipse.jdt.ui.recommend.local.variable")) {
      dataModel.setPackageName(
          javaElement.getParent().getParent().getParent().getParent().getElementName());

      dataModel.setTypeName(javaElement.getParent().getParent().getElementName());
      dataModel.setMethodName(javaElement.getParent().getElementName());
    } else if (dataModel.getRefactorType().equals("org.eclipse.jdt.ui.recommend.type")) {
      dataModel.setPackageName(javaElement.getParent().getParent().getElementName());
      dataModel.setTypeName(javaElement.getElementName());
    } else {
      dataModel.setPackageName(javaElement.getParent().getParent().getParent().getElementName());
      dataModel.setTypeName(javaElement.getParent().getElementName());
    }

    return dataModel;
  }

  public void recommend(LinkedRenameRefactoringApply refactoringApply)
      throws RefactoringException, CoreException {
    DataModel renameData = getInfo(refactoringApply);
    if (renameData == null) {
      result.setRecommendOriginalName("");
      return;
    }
    result = new ResultModel();
    Parser parser = new Parser();
    parser.parse(renameData);
    result = parser.getResult();
  }
}
