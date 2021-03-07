package br.ufmg.engsoft.reprova.tests.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Field;


public class EnvironmentUtils {
	protected static void setEnv(Map<String, String> newenv) throws Exception {
	  try {
	    Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
	    Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
	    theEnvironmentField.setAccessible(true);
	    Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
	    env.putAll(newenv);
	    Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
	    theCaseInsensitiveEnvironmentField.setAccessible(true);
	    Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
	    cienv.putAll(newenv);
	  } catch (NoSuchFieldException e) {
	    Class[] classes = Collections.class.getDeclaredClasses();
	    Map<String, String> env = System.getenv();
	    for(Class cl : classes) {
	      if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
	        Field field = cl.getDeclaredField("m");
	        field.setAccessible(true);
	        Object obj = field.get(env);
	        Map<String, String> map = (Map<String, String>) obj;
	        map.clear();
	        map.putAll(newenv);
	      }
	    }
	  }
	}
	
	protected static void clearEnv() throws Exception {
		try {
	    Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
	    Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
	    theEnvironmentField.setAccessible(true);
	    Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
	    env.clear();
	    Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
	    theCaseInsensitiveEnvironmentField.setAccessible(true);
	    Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
	    cienv.clear();
	  } catch (NoSuchFieldException e) {
	    Class[] classes = Collections.class.getDeclaredClasses();
	    Map<String, String> env = System.getenv();
	    for(Class cl : classes) {
	      if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
	        Field field = cl.getDeclaredField("m");
	        field.setAccessible(true);
	        Object obj = field.get(env);
	        Map<String, String> map = (Map<String, String>) obj;
	        map.clear();
	      }
	    }
	  }
	}
	
	public static void setEnvVariables(boolean setOptionalVariables, int difficultyGroup) throws Exception {
		clearEnv();
		
		var envVar = new HashMap<String, String>();

  	envVar.put("REPROVA_TOKEN", "token");
  	envVar.put("PORT", "8888");
  	
  	if (setOptionalVariables) {
  		envVar.put("ENABLE_STATISTICS", "true");
  		envVar.put("ENABLE_ESTIMATED_TIME", "true");
  		envVar.put("DIFFICULTY_GROUP", "" + difficultyGroup);
  		envVar.put("ENABLE_MULTIPLE_CHOICE", "true");
  	}
  	
  	setEnv(envVar);
	}
}
