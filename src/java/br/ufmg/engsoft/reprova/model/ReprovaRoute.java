package br.ufmg.engsoft.reprova.model;

public abstract class ReprovaRoute {
    
    /**
     * Access token.
     */
    protected static final String TOKEN = Environments.getInstance().getToken();

    /**
     * Messages.
     */
    protected static final String unauthorized = "\"Unauthorized\"";
    
    /* invalid */
    protected static final String invalid = "\"Invalid request\"";
    /*okstatus*/
    protected static final String okStatus = "\"Ok\"";
    
    /**
     * Check if the given TOKEN is authorized.
     */
    protected static boolean authorized(String token) {
      return ReprovaRoute.TOKEN.equals(token);
    }

}
