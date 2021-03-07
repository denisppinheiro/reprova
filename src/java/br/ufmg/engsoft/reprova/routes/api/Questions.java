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
public class Questions extends ApiRoute {

	/**
	 * DAO for Question.
	 */
	protected final QuestionsDAO questionsDAO;

	/**
	 * Instantiate the questions endpoint. The setup method must be called to
	 * install the endpoint.
	 * 
	 * @param json         the json formatter
	 * @param questionsDAO the DAO for Question
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public Questions(Json json, QuestionsDAO questionsDAO) {
		super(json);

		if (questionsDAO == null) {
			throw new IllegalArgumentException("questionsDAO mustn't be null");
		}

		this.questionsDAO = questionsDAO;
	}

	@Override
	public String getCollectionName() {
		return "questions";
	}

	/**
	 * Install the endpoint in Spark. Methods: - get - post - delete
	 */
	@Override
	public void setup() {
		Spark.get("/api/questions", this::get);
		Spark.post("/api/questions", this::post);
		Spark.delete("/api/questions", this::delete);
		Spark.delete("/api/questions/deleteAll", this::deleteAll);

		logger.info("Setup /api/questions.");
	}

	/**
	 * Get id endpoint: fetch the specified question from the database. If not
	 * authorised, and the given question is private, returns an error message.
	 */
	@Override
	protected Object getById(Request request, Response response, String id, boolean auth) {
		if (id == null) {
			throw new IllegalArgumentException("id mustn't be null");
		}

		logger.info("Fetching question {}", id);

		var question = questionsDAO.get(id);

		if (question == null) {
			logger.error("Invalid request!");
			response.status(400);
			return ReprovaRoute.INVALID;
		}

		if (question.pvt && !auth) {
			logger.info("Unauthorized token: {}", ReprovaRoute.TOKEN);
			response.status(403);
			return ReprovaRoute.UNAUTHORIZED;
		}

		logger.info("Done. Responding...");

		response.status(200);

		return json.render(question);
	}

	/**
	 * Get all endpoint: fetch all questions from the database. If not authorized,
	 * fetches only public questions.
	 */
	@Override
	protected Object getAll(Request request, Response response, boolean auth) {

		logger.info("Fetching questions.");

		var questions = questionsDAO.list(null, // theme filtering is not implemented in this endpoint.
				auth ? null : false);

		logger.info("Done. Responding...");

		response.status(200);

		return json.render(questions);
	}

	/**
	 * Post endpoint: add or update a question in the database. The question must be
	 * supplied in the request's body. If the question has an 'id' field, the
	 * operation is an update. Otherwise, the given question is added as a new
	 * question in the database. This endpoint is for authorized access only.
	 */
	protected Object post(Request request, Response response) {
		String body = request.body();

		logger.info("Received questions post: " + body);

		response.type(APPLICATION_JSON);

		var token = request.queryParams("token");

		if (!ReprovaRoute.authorized(token)) {
			logger.info("Unauthorized token: " + token);
			response.status(403);
			return ReprovaRoute.UNAUTHORIZED;
		}

		Question question;
		try {
			question = json.parse(body, Question.Builder.class).build();
		} catch (Exception e) {
			logger.error("Invalid request payload!", e);
			response.status(400);
			return ReprovaRoute.INVALID;
		}

		logger.info("Parsed " + question.toString());
		logger.info("Adding question.");

		var success = questionsDAO.add(question);

		response.status(success ? 200 : 400);

		logger.info("Done. Responding...");

		return ReprovaRoute.OK;
	}

	/**
	 * Delete endpoint: remove a question from the database. The question's id must
	 * be supplied through the 'id' query parameter. This endpoint is for authorized
	 * access only.
	 */
	protected Object delete(Request request, Response response) {
		logger.info("Received questions delete:");

		response.type(APPLICATION_JSON);

		var id = request.queryParams("id");
		var token = request.queryParams("token");

		if (!ReprovaRoute.authorized(token)) {
			logger.info("Unauthorized token: " + token);
			response.status(403);
			return ReprovaRoute.UNAUTHORIZED;
		}

		if (id == null) {
			logger.error("Invalid request!");
			response.status(400);
			return ReprovaRoute.INVALID;
		}

		logger.info("Deleting question " + id);

		var success = questionsDAO.remove(id);

		logger.info("Done. Responding...");

		response.status(success ? 200 : 400);

		return ReprovaRoute.OK;
	}

	/**
	 * Delete All endpoint: remove all questions from the database. This endpoint is
	 * for authorized access only.
	 */
	protected Object deleteAll(Request request, Response response) {
		logger.info("Received questions delete all:");

		response.type(APPLICATION_JSON);

		var token = request.queryParams("token");

		if (!ReprovaRoute.authorized(token)) {
			logger.info("Unauthorized token: " + token);
			response.status(403);
			return ReprovaRoute.UNAUTHORIZED;
		}

		boolean success = false;
		logger.info("Deleting all questions");
		ArrayList<Question> questions = new ArrayList<Question>(questionsDAO.list(null, null));
		for (Question question : questions) {
			String id = question.getId();
			logger.info("Deleting question " + id);

			success = questionsDAO.remove(id);
			if (!success) {
				break;
			}
		}

		logger.info("Done. Responding...");

		response.status(success ? 200 : 400);

		return ReprovaRoute.OK;
	}

}
