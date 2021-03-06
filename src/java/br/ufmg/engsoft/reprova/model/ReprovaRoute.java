package br.ufmg.engsoft.reprova.model;

public abstract class ReprovaRoute {
    
	protected ReprovaRoute() {}
	
    /**
     * Access token.
     */
    protected static final String token = Environments.getInstance().getToken();

    /**
     * Messages.
     */
    protected static final String unauthorized = "\"Unauthorized\"";
    protected static final String invalid = "\"Invalid request\"";
    protected static final String ok = "\"Ok\"";
    
    /**
     * Check if the given token is authorized.
     */
    protected static boolean authorized(String token) {
      return ReprovaRoute.token.equals(token);
    }

}
