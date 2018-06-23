package org.eclipse.che.plugin.java.server.rest.recommend.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class OverrideVisitor extends ASTVisitor {
	
	public IMethodBinding iMethodbinding = null;
	public String methodName = null;
	
	public OverrideVisitor() {
		super();
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration node) {		
		IMethodBinding iMethod = node.resolveBinding();
		if(iMethod == null || iMethod.getJavaElement() == null) return false;		
		String methodFullName = iMethod.getJavaElement().toString().substring(0, iMethod.getJavaElement().toString().indexOf("{")-1);
		if(this.methodName.equals(methodFullName)) this.iMethodbinding = iMethod;	
		return true;
	}

}