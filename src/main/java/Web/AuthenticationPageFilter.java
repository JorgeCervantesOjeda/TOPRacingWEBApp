package Web;

import Controller.Controller;
import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter( "/*" )
public class AuthenticationPageFilter
  implements Filter {

  private static final String LOGIN_PAGE = "/login.xhtml";
  private static final String LOGIN_FACES_PAGE = "/faces/login.xhtml";
  private static final String WELCOME_PAGE = "/welcome.xhtml";
  private static final String WELCOME_FACES_PAGE = "/faces/welcome.xhtml";

  @Override
  public void doFilter( ServletRequest request,
                        ServletResponse response,
                        FilterChain chain ) throws IOException,
                                                  ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    String path = httpRequest.getRequestURI()
      .substring( httpRequest.getContextPath()
        .length() );

    if( isAlwaysAllowed( path )
        || isAuthenticated( httpRequest )
        || isAnonymousAllowedPage( path ) ) {
      chain.doFilter( request,
                      response );
      return;
    }

    httpResponse.sendRedirect( httpRequest.getContextPath()
                               + LOGIN_FACES_PAGE );
  }

  private boolean isAuthenticated( HttpServletRequest request ) {
    HttpSession session = request.getSession( false );
    return session != null
           && session.getAttribute( Controller.AUTH_SESSION_KEY ) != null;
  }

  private boolean isAlwaysAllowed( String path ) {
    return path == null
           || path.isBlank()
           || "/".equals( path )
           || "/favicon.ico".equals( path )
           || path.startsWith( "/resources/" )
           || path.startsWith( "/jakarta.faces.resource/" )
           || path.startsWith( "/faces/jakarta.faces.resource/" );
  }

  private boolean isAnonymousAllowedPage( String path ) {
    return LOGIN_PAGE.equals( path )
           || LOGIN_FACES_PAGE.equals( path )
           || WELCOME_PAGE.equals( path )
           || WELCOME_FACES_PAGE.equals( path );
  }
}
