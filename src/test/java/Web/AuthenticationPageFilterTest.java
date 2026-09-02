// src/test/java/Web/AuthenticationPageFilterTest.java
// Verifies the central anonymous and authenticated page access rules.
package Web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Controller.Controller;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class AuthenticationPageFilterTest {

  private final AuthenticationPageFilter filter = new AuthenticationPageFilter();

  @Test
  void redirectsAnonymousProtectedRequestsToFacesLogin() throws IOException,
                                                                ServletException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    HttpServletResponse response = mock( HttpServletResponse.class );
    FilterChain chain = mock( FilterChain.class );

    when( request.getRequestURI() ).thenReturn( "/topracingwebapp/faces/editregatta.xhtml" );
    when( request.getContextPath() ).thenReturn( "/topracingwebapp" );
    when( request.getSession( false ) ).thenReturn( null );

    filter.doFilter( request,
                     response,
                     chain );

    verify( response ).sendRedirect( "/topracingwebapp/faces/login.xhtml" );
    verify( chain,
            never() ).doFilter( request,
                                response );
  }

  @Test
  void allowsAnonymousAccessToPublicPages() throws IOException,
                                                   ServletException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    HttpServletResponse response = mock( HttpServletResponse.class );
    FilterChain chain = mock( FilterChain.class );

    when( request.getRequestURI() ).thenReturn( "/topracingwebapp/faces/login.xhtml" );
    when( request.getContextPath() ).thenReturn( "/topracingwebapp" );
    when( request.getSession( false ) ).thenReturn( null );

    filter.doFilter( request,
                     response,
                     chain );

    verify( chain ).doFilter( request,
                              response );
  }

  @Test
  void allowsAnonymousAccessToConfirmationLinks() throws IOException,
                                                         ServletException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    HttpServletResponse response = mock( HttpServletResponse.class );
    FilterChain chain = mock( FilterChain.class );

    when( request.getRequestURI() ).thenReturn( "/topracingwebapp/faces/confirmusermail.xhtml" );
    when( request.getContextPath() ).thenReturn( "/topracingwebapp" );
    when( request.getSession( false ) ).thenReturn( null );

    filter.doFilter( request,
                     response,
                     chain );

    verify( chain ).doFilter( request,
                              response );
  }

  @Test
  void allowsAnonymousAccessToPaypalReturn() throws IOException,
                                                    ServletException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    HttpServletResponse response = mock( HttpServletResponse.class );
    FilterChain chain = mock( FilterChain.class );

    when( request.getRequestURI() ).thenReturn( "/topracingwebapp/faces/paypalreturn.xhtml" );
    when( request.getContextPath() ).thenReturn( "/topracingwebapp" );
    when( request.getSession( false ) ).thenReturn( null );

    filter.doFilter( request,
                     response,
                     chain );

    verify( chain ).doFilter( request,
                              response );
  }

  @Test
  void allowsAnonymousAccessToPaypalSandboxMock() throws IOException,
                                                         ServletException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    HttpServletResponse response = mock( HttpServletResponse.class );
    FilterChain chain = mock( FilterChain.class );

    when( request.getRequestURI() ).thenReturn( "/topracingwebapp/faces/paypalsandboxmock.xhtml" );
    when( request.getContextPath() ).thenReturn( "/topracingwebapp" );
    when( request.getSession( false ) ).thenReturn( null );

    filter.doFilter( request,
                     response,
                     chain );

    verify( chain ).doFilter( request,
                              response );
  }

  @Test
  void allowsAnonymousAccessToStandings() throws IOException,
                                                 ServletException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    HttpServletResponse response = mock( HttpServletResponse.class );
    FilterChain chain = mock( FilterChain.class );

    when( request.getRequestURI() ).thenReturn( "/topracingwebapp/faces/listpointscounts.xhtml" );
    when( request.getContextPath() ).thenReturn( "/topracingwebapp" );
    when( request.getSession( false ) ).thenReturn( null );

    filter.doFilter( request,
                     response,
                     chain );

    verify( chain ).doFilter( request,
                              response );
    verify( response,
            never() ).sendRedirect( "/topracingwebapp/faces/login.xhtml" );
  }

  @Test
  void allowsAnonymousAccessToHealthCheck() throws IOException,
                                                   ServletException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    HttpServletResponse response = mock( HttpServletResponse.class );
    FilterChain chain = mock( FilterChain.class );

    when( request.getRequestURI() ).thenReturn( "/topracingwebapp/healthz.txt" );
    when( request.getContextPath() ).thenReturn( "/topracingwebapp" );
    when( request.getSession( false ) ).thenReturn( null );

    filter.doFilter( request,
                     response,
                     chain );

    verify( chain ).doFilter( request,
                              response );
    verify( response,
            never() ).sendRedirect( "/topracingwebapp/faces/login.xhtml" );
  }

  @Test
  void allowsAuthenticatedUsersIntoProtectedPages() throws IOException,
                                                           ServletException {
    HttpServletRequest request = mock( HttpServletRequest.class );
    HttpServletResponse response = mock( HttpServletResponse.class );
    FilterChain chain = mock( FilterChain.class );
    HttpSession session = mock( HttpSession.class );

    when( request.getRequestURI() ).thenReturn( "/topracingwebapp/faces/editregatta.xhtml" );
    when( request.getContextPath() ).thenReturn( "/topracingwebapp" );
    when( request.getSession( false ) ).thenReturn( session );
    when( session.getAttribute( Controller.AUTH_SESSION_KEY ) ).thenReturn( 7L );

    filter.doFilter( request,
                     response,
                     chain );

    verify( chain ).doFilter( request,
                              response );
  }
}
