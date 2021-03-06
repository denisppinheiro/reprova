package br.ufmg.engsoft.reprova.routes;

import spark.Spark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.engsoft.reprova.database.AnswersDAO;
import br.ufmg.engsoft.reprova.database.QuestionsDAO;
import br.ufmg.engsoft.reprova.routes.api.Answers;
import br.ufmg.engsoft.reprova.routes.api.Questions;
import br.ufmg.engsoft.reprova.database.QuestionnairesDAO;
import br.ufmg.engsoft.reprova.routes.api.Questionnaires;
import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Environments;


/**
 * Service setup class.
 * This class is static.
 */
public class Setup {
  /**
   * Static class.
   */
  protected Setup() { }

  /**
   * Logger instance.
   */
  protected static Logger logger = LoggerFactory.getLogger(Setup.class);

  /**
   * The port for the webserver.
   */
  protected static final int port = Environments.getInstance().getPort();


  /**
   * Setup the service routes.
   * This sets up the routes under the routes directory,
   * and also static files on '/public'.
   * @param json          the json formatter
   * @param questionsDAO  the DAO for Question
   * @throws IllegalArgumentException  if any parameter is null
   */
  public static void routes(Json json, QuestionsDAO questionsDAO) {
    if (json == null) {
      throw new IllegalArgumentException("json mustn't be null");
    }

    if (questionsDAO == null) {
      throw new IllegalArgumentException("questionsDAO mustn't be null");
    }

    Spark.port(Setup.port);

    logger.info("Spark on port {}", Setup.port);

    logger.info("Setting up static resources.");
    Spark.staticFiles.location("/public");

    logger.info("Setting up questions route:");
    var questions = new Questions(json, questionsDAO);
    questions.setup();    
  }
  
  public static void answerRoutes(Json json, AnswersDAO answersDAO) {      
      logger.info("Setting up answers route:");
      if (answersDAO == null) {
        throw new IllegalArgumentException("answersDAO mustn't be null");
      }
      var answers = new Answers(json, answersDAO);
      answers.setup();
  }
  
  public static void questionnaireRoutes(Json json, QuestionnairesDAO questionnairesDAO, QuestionsDAO questionsDAO) {
      logger.info("Setting up questionnaires route:");
      if (questionnairesDAO == null) {
          throw new IllegalArgumentException("questionnairesDAO mustn't be null");
        }
      var questionnaires = new Questionnaires(json, questionnairesDAO, questionsDAO);
      questionnaires.setup();
  }
}
