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
  protected static final Logger LOGGER = LoggerFactory.getLogger(Questions.class);

  public static final String PTOKEN = "token";
  public static final String APPJSON = "application/json";
  public static final String DONERESP = "Done. Responding...";
  public static final String UNAUTHTOKEN = "Unauthorized token: ";

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

    LOGGER.info("Setup /api/questions.");
  }

  /**
   * Get endpoint: lists all questions, or a single question if a 'id' query parameter is
   * provided.
   */
  protected Object get(Request request, Response response) {
    LOGGER.info("Received questions get:");

    var id = request.queryParams("id");
    var auth = authorized(request.queryParams(PTOKEN));
      
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

    response.type(APPJSON);

    LOGGER.info("Fetching question " + id);

    var question = questionsDAO.get(id);

    if (question == null) {
      LOGGER.error("Invalid request!");
      response.status(400);
      return INVALID;
    }

    if (question.pvt && !auth) {
      LOGGER.info(UNAUTHTOKEN + TOKEN);
      response.status(403);
      return UNAUTHORIZED;
    }

    LOGGER.info(DONERESP);

    response.status(200);

    return json.render(question);
  }

  /**
   * Get all endpoint: fetch all questions from the database.
   * If not authorized, fetches only public questions.
   */
  protected Object get(Request request, Response response, boolean auth) {
    response.type(APPJSON);

    LOGGER.info("Fetching questions.");

    var questions = questionsDAO.list(
      null, // theme filtering is not implemented in this endpoint.
      auth ? null : false
    );

    LOGGER.info(DONERESP);

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

    LOGGER.info("Received questions post:" + body);

    response.type(APPJSON);

    var token = request.queryParams(PTOKEN);

    if (!authorized(token)) {
      LOGGER.info(UNAUTHTOKEN + token);
      response.status(403);
      return UNAUTHORIZED;
    }

    Question question;
    try {
      question = json
        .parse(body, Question.Builder.class)
        .build();
    }
    catch (Exception e) {
      LOGGER.error("Invalid request payload!", e);
      response.status(400);
      return INVALID;
    }

    LOGGER.info("Parsed " + question.toString());
    LOGGER.info("Adding question.");

    var success = questionsDAO.add(question);

    response.status(
       success ? 200
               : 400
    );

    LOGGER.info(DONERESP);

    return OKSTATUS;
  }


  /**
   * Delete endpoint: remove a question from the database.
   * The question's id must be supplied through the 'id' query parameter.
   * This endpoint is for authorized access only.
   */
  protected Object delete(Request request, Response response) {
    LOGGER.info("Received questions delete:");

    response.type(APPJSON);

    var id = request.queryParams("id");
    var token = request.queryParams(PTOKEN);

    if (!authorized(token)) {
      LOGGER.info(UNAUTHTOKEN + token);
      response.status(403);
      return UNAUTHORIZED;
    }

    if (id == null) {
      LOGGER.error("Invalid request!");
      response.status(400);
      return INVALID;
    }

    LOGGER.info("Deleting question " + id);

    var success = questionsDAO.remove(id);

    LOGGER.info(DONERESP);

    response.status(
      success ? 200
              : 400
    );

    return OKSTATUS;
  }

  /**
   * Delete All endpoint: remove all questions from the database.
   * This endpoint is for authorized access only.
   */
  protected Object deleteAll(Request request, Response response) {
    LOGGER.info("Received questions delete all:");

    response.type(APPJSON);

    var token = request.queryParams(PTOKEN);

    if (!authorized(token)) {
      LOGGER.info(UNAUTHTOKEN + token);
      response.status(403);
      return UNAUTHORIZED;
    }

    boolean success = false;
    LOGGER.info("Deleting all questions");
    ArrayList<Question> questions = new ArrayList<Question>(questionsDAO.list(null, null));
    for (Question question : questions){
      String id = question.id;
      LOGGER.info("Deleting question " + id);
      
      success = questionsDAO.remove(id);
      if (!success){
        break;
      }
    }
      
    LOGGER.info(DONERESP);

    response.status(
      success ? 200
              : 400
    );

    return OKSTATUS;
  }
}
