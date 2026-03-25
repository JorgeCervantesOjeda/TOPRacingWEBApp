package View;

import java.io.IOException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

final class NavigationUtil {

  private NavigationUtil() {
  }

  static void redirectTo( String page ) {
    FacesContext context = FacesContext.getCurrentInstance();
    if( context == null ) {
      return;
    }

    ExternalContext externalContext = context.getExternalContext();
    try {
      externalContext.redirect( externalContext.getRequestContextPath()
                                + "/" + page );
      context.responseComplete();
    } catch( IOException e ) {
      throw new IllegalStateException( "Unable to redirect to " + page,
                                       e );
    }
  }
}
