package br.ufmg.engsoft.reprova.routes.api;

import spark.Spark;
import spark.Request;
import spark.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import br.ufmg.engsoft.reprova.database.QuestionsDAO;
import br.ufmg.engsoft.reprova.model.Question;
import br.ufmg.engsoft.reprova.model.ReprovaRoute;
import br.ufmg.engsoft.reprova.mime.json.Json;


/**
 * Questions route.
 */
public class Questions extends ReprovaRoute {
  /**
   * Logger instance.
   */
  protected static final Logger logger = LoggerFactory.getLogger(Questions.class);


  /**
   * Json formatter.
   */
  protected final Json json;
  /**
   * DAO for Question.
   */
  protected final QuestionsDAO questionsDAO;

  /**
   * Instantiate the questions endpoint.
   * The setup method must be called to install the endpoint.
   * @param json          the json formatter
   * @param questionsDAO  the DAO for Question
   * @throws IllegalArgumentException  if any parameter is null
   */
  public Questions(Json json, QuestionsDAO questionsDAO) {
    if (json == null) {
      throw new IllegalArgumentException("json mustn't be null");
    }

    if (questionsDAO == null) {
      throw new IllegalArgumentException("questionsDAO mustn't be null");
    }

    this.json = json;
    this.questionsDAO = questionsDAO;
  }


  /**
   * Install the endpoint in Spark.
   * Methods:
   * - get
   * - post
   * - delete
   */
  public void setup() {
    Spark.get("/api/questions", this::get);
    Spark.post("/api/questions", this::post);
    Spark.delete("/api/questions", this::delete);
    Spark.delete("/api/questions/deleteAll", this::deleteAll);

    logger.info("Setup /api/questions.");
  }

  /**
   * Get endpoint: lists all questions, or a single question if a 'id' query parameter is
   * provided.
   */
  protected Object get(Request request, Response response) {
    logger.info("Received questions get:");

    var id = request.queryParams("id");
    var auth = authorized(request.queryParams("token"));
      
    if (id == null) {
    	return this.get(request, response, auth);
    }
     
    return this.get(request, response, id, auth);
  }

  /**
   * Get id endpoint: fetch the specified question from the database.
   * If not authorised, and the given question is private, returns an error message.
   */
  protected Object get(Request request, Response response, String id, boolean auth) {
    if (id == null) {
      throw new IllegalArgumentException("id mustn't be null");
    }

    response.type("application/json");

    logger.info("Fetching question " + id);

    var question = questionsDAO.get(id);

    if (question == null) {
      logger.error("Invalid request!");
      response.status(400);
      return invalid;
    }

    if (question.pvt && !auth) {
      logger.info("Unauthorized token: " + token);
      response.status(403);
      return unauthorized;
    }

    logger.info("Done. Responding...");

    response.status(200);

    return json.render(question);
  }

  /**
   * Get all endpoint: fetch all questions from the database.
   * If not authorized, fetches only public questions.
   */
  protected Object get(Request request, Response response, boolean auth) {
    response.type("application/json");

    logger.info("Fetching questions.");

    var questions = questionsDAO.list(
      null, // theme filtering is not implemented in this endpoint.
      auth ? null : false
    );

    logger.info("Done. Responding...");

    response.status(200);

    return json.render(questions);
  }


  /**
   * Post endpoint: add or update a question in the database.
   * The question must be supplied in the request's body.
   * If the question has an 'id' field, the operation is an update.
   * Otherwise, the given question is added as a new question in the database.
   * This endpoint is for authorized access only.
   */
  protected Object post(Request request, Response response) {
    String body = request.body();

    logger.info("Received questions post:" + body);

    response.type("application/json");

    var token = request.queryParams("token");

    if (!authorized(token)) {
      logger.info("Unauthorized token: " + token);
      response.status(403);
      return unauthorized;
    }

    Question question;
    try {
      question = json
        .parse(body, Question.Builder.class)
        .build();
    }
    catch (Exception e) {
      logger.error("Invalid request payload!", e);
      response.status(400);
      return invalid;
    }

    logger.info("Parsed " + question.toString());
    logger.info("Adding question.");

    var success = questionsDAO.add(question);

    response.status(
       success ? 200
               : 400
    );

    logger.info("Done. Responding...");

    return ok;
  }


  /**
   * Delete endpoint: remove a question from the database.
   * The question's id must be supplied through the 'id' query parameter.
   * This endpoint is for authorized access only.
   */
  protected Object delete(Request request, Response response) {
    logger.info("Received questions delete:");

    response.type("application/json");

    var id = request.queryParams("id");
    var token = request.queryParams("token");

    if (!authorized(token)) {
      logger.info("Unauthorized token: " + token);
      response.status(403);
      return unauthorized;
    }

    if (id == null) {
      logger.error("Invalid request!");
      response.status(400);
      return invalid;
    }

    logger.info("Deleting question " + id);

    var success = questionsDAO.remove(id);

    logger.info("Done. Responding...");

    response.status(
      success ? 200
              : 400
    );

    return ok;
  }

  /**
   * Delete All endpoint: remove all questions from the database.
   * This endpoint is for authorized access only.
   */
  protected Object deleteAll(Request request, Response response) {
    logger.info("Received questions delete all:");

    response.type("application/json");

    var token = request.queryParams("token");

    if (!authorized(token)) {
      logger.info("Unauthorized token: " + token);
      response.status(403);
      return unauthorized;
    }

    boolean success = false;
    logger.info("Deleting all questions");
    ArrayList<Question> questions = new ArrayList<Question>(questionsDAO.list(null, null));
    for (Question question : questions){
      String id = question.getId();
      logger.info("Deleting question " + id);
      
      success = questionsDAO.remove(id);
      if (!success){
        break;
      }
    }
      
    logger.info("Done. Responding...");

    response.status(
      success ? 200
              : 400
    );

    return ok;
  }
}
