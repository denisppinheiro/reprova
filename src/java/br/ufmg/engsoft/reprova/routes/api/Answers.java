package br.ufmg.engsoft.reprova.routes.api;

import br.ufmg.engsoft.reprova.database.AnswersDAO;
import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Answer;
import br.ufmg.engsoft.reprova.model.ReprovaRoute;
import spark.Request;
import spark.Response;
import spark.Spark;

public class Answers extends ApiRoute {

	  private final AnswersDAO answersDAO;
	  
	  /**
	   * Instantiate the answers endpoint.
	   * The setup method must be called to install the endpoint.
	   * @param json          the json formatter
	   * @param answersDAO  the DAO for Answer
	   * @throws IllegalArgumentException  if any parameter is null
	   */
	  public Answers(Json json, AnswersDAO answersDAO) {
	    super(json);

	    if (answersDAO == null) {
	      throw new IllegalArgumentException("answersDAO mustn't be null");
	    }

	    this.answersDAO = answersDAO;
	  }
	
	/**
	   * Install the endpoint in Spark.
	   * Methods:
	   * - get
	   * - post
	   * - delete
	   */
	  public void setup() {
	    Spark.get("/api/questions/:questionId/answers", this::getAllAnswers);
	    Spark.get("/api/questions/:questionId/answers/:answerId", (req, res) -> "Specific answer");
	    Spark.post("/api/questions/:questionId/answers", this::addAnswer);

	    logger.info("Setup /api/answers.");
	  }
	  
	  /**
       * Get endpoint: lists all answers for a given question, or a single answer 
       * if an 'id' query parameter is provided.
       */
      protected Object getAllAnswers(Request request, Response response) {
        logger.info("Received answers get:");

        String id = request.params(":questionId");
        boolean auth = ReprovaRoute.authorized(request.queryParams("token"));
        
        // TODO check how to use auth here.
        var answers = answersDAO.list(id, auth ? null : false);
        
        logger.info("Done. Responding...");
        response.status(200);
        return json.render(answers);
      }
      
      /**
       * Post endpoint: add an answer in the database.
       * The answer must be supplied in the request's body.
       * The given answer is added as a new question in the database.
       * This endpoint is for authorized access only.
       */
      protected Object addAnswer(Request request, Response response) {
        String body = request.body();

        logger.info("Received answer post: {}", body);

        response.type(APPLICATION_JSON);

        String token = request.queryParams("token");
        String questionId = request.params(":questionId");

        if (!ReprovaRoute.authorized(token)) {
          logger.info("Unauthorized token: {}", token);
          response.status(403);
          return ReprovaRoute.UNAUTHORIZED;
        }

        Answer answer;
        try {
          answer = json
            .parse(body, Answer.Builder.class)
            .build();
        }
        catch (Exception e) {
          logger.error("Invalid request payload!", e);
          response.status(400);
          return ReprovaRoute.INVALID;
        }

        logger.info("Parsed " + answer.toString());
        logger.info("Adding question.");

        var success = answersDAO.add(answer, questionId);

        response.status(
           success ? 200
                   : 400
        );

        logger.info("Done. Responding...");

        return ReprovaRoute.OK;
      }

	@Override
	public String getCollectionName() {
		return "answers";
	}

	@Override
	protected Object getById(Request request, Response response, String id, boolean auth) {
		return null;
	}

	@Override
	protected Object getAll(Request request, Response response, boolean auth) {
		return null;
	}



}
