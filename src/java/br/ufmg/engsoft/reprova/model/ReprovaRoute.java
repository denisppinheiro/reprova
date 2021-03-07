package br.ufmg.engsoft.reprova.model;

public class ReprovaRoute {
    
	private ReprovaRoute() {}
	
    /**
     * Access token.
     */
    public static final String TOKEN = Environments.getInstance().getToken();

    /**
     * Messages.
     */
    public static final String UNAUTHORIZED = "\"Unauthorized\"";
    public static final String INVALID = "\"Invalid request\"";
    public static final String OK = "\"Ok\"";
    
    /**
     * Check if the given token is authorized.
     */
    public static boolean authorized(String token) {
      return TOKEN.equals(token);
    }

}
