package org.eclipse.che.multiuser.api.authentication.commons;


import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import org.eclipse.che.commons.subject.Subject;

@FunctionalInterface
public interface SubjectSupplier {

   Subject getSubject(String token, ServletResponse response) throws IOException, ServletException;
}
