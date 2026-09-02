// src/main/java/Web/AuthenticationPageFilter.java
// Centralizes page-level authentication rules for JSF requests.
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
  private static final String HEALTH_CHECK_PAGE = "/healthz.txt";
  private static final String WELCOME_PAGE = "/welcome.xhtml";
  private static final String WELCOME_FACES_PAGE = "/faces/welcome.xhtml";
  private static final String EDIT_PARTICIPANT_PAGE = "/editparticipant.xhtml";
  private static final String EDIT_PARTICIPANT_FACES_PAGE = "/faces/editparticipant.xhtml";
  private static final String RESET_PASSWORD_PAGE = "/resetpassword.xhtml";
  private static final String RESET_PASSWORD_FACES_PAGE = "/faces/resetpassword.xhtml";
  private static final String CONFIRM_USER_MAIL_PAGE = "/confirmusermail.xhtml";
  private static final String CONFIRM_USER_MAIL_FACES_PAGE = "/faces/confirmusermail.xhtml";
  private static final String PAYPAL_RETURN_PAGE = "/paypalreturn.xhtml";
  private static final String PAYPAL_RETURN_FACES_PAGE = "/faces/paypalreturn.xhtml";
  private static final String PAYPAL_SANDBOX_MOCK_PAGE = "/paypalsandboxmock.xhtml";
  private static final String PAYPAL_SANDBOX_MOCK_FACES_PAGE = "/faces/paypalsandboxmock.xhtml";
  private static final String COMPLAINT_PAGE = "/complaint.xhtml";
  private static final String COMPLAINT_FACES_PAGE = "/faces/complaint.xhtml";
  private static final String COMPLAINT_BUYER_PAGE = "/complaintbuyer.xhtml";
  private static final String COMPLAINT_BUYER_FACES_PAGE = "/faces/complaintbuyer.xhtml";
  private static final String COMPLAINT_SELLER_PAGE = "/complaintseller.xhtml";
  private static final String COMPLAINT_SELLER_FACES_PAGE = "/faces/complaintseller.xhtml";
  private static final String POINTSCOUNTS_PAGE = "/listpointscounts.xhtml";
  private static final String POINTSCOUNTS_FACES_PAGE = "/faces/listpointscounts.xhtml";

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
           || HEALTH_CHECK_PAGE.equals( path )
           || path.startsWith( "/resources/" )
           || path.startsWith( "/jakarta.faces.resource/" )
           || path.startsWith( "/faces/jakarta.faces.resource/" );
  }

  private boolean isAnonymousAllowedPage( String path ) {
    return LOGIN_PAGE.equals( path )
           || LOGIN_FACES_PAGE.equals( path )
           || WELCOME_PAGE.equals( path )
           || WELCOME_FACES_PAGE.equals( path )
           || EDIT_PARTICIPANT_PAGE.equals( path )
           || EDIT_PARTICIPANT_FACES_PAGE.equals( path )
           || RESET_PASSWORD_PAGE.equals( path )
           || RESET_PASSWORD_FACES_PAGE.equals( path )
           || CONFIRM_USER_MAIL_PAGE.equals( path )
           || CONFIRM_USER_MAIL_FACES_PAGE.equals( path )
           || PAYPAL_RETURN_PAGE.equals( path )
           || PAYPAL_RETURN_FACES_PAGE.equals( path )
           || PAYPAL_SANDBOX_MOCK_PAGE.equals( path )
           || PAYPAL_SANDBOX_MOCK_FACES_PAGE.equals( path )
           || COMPLAINT_PAGE.equals( path )
           || COMPLAINT_FACES_PAGE.equals( path )
           || COMPLAINT_BUYER_PAGE.equals( path )
           || COMPLAINT_BUYER_FACES_PAGE.equals( path )
           || COMPLAINT_SELLER_PAGE.equals( path )
           || COMPLAINT_SELLER_FACES_PAGE.equals( path )
           || POINTSCOUNTS_PAGE.equals( path )
           || POINTSCOUNTS_FACES_PAGE.equals( path );
  }
}
