package br.ufmg.engsoft.reprova.model;

public abstract class ReprovaRoute {
    
    /**
     * Access token.
     */
    protected static final String TOKEN = Environments.getInstance().getToken();

    /**
     * Messages.
     */
    protected static final String UNAUTHORIZED = "\"Unauthorized\"";
    
    /* invalid */
    protected static final String INVALID = "\"Invalid request\"";
    /*okstatus*/
    protected static final String OKSTATUS = "\"Ok\"";
    
    /**
     * Check if the given TOKEN is authorized.
     */
    protected static boolean authorized(String token) {
      return ReprovaRoute.TOKEN.equals(token);
    }

}
