package org.eclipse.che.multiuser.api.authentication.commons;


import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.eclipse.che.commons.subject.Subject;

@FunctionalInterface
public interface SubjectSupplier {

   Subject getSubject(ServletRequest request, ServletResponse response, FilterChain chain,
       String token) throws IOException, ServletException;
}
