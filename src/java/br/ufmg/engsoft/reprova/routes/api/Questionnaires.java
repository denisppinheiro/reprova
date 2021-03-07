package br.ufmg.engsoft.reprova.routes.api;

import java.util.ArrayList;

import br.ufmg.engsoft.reprova.database.QuestionnairesDAO;
import br.ufmg.engsoft.reprova.database.QuestionsDAO;
import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Environments;
import br.ufmg.engsoft.reprova.model.Question;
import br.ufmg.engsoft.reprova.model.Questionnaire;
import br.ufmg.engsoft.reprova.model.ReprovaRoute;
import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Questionnaires route.
 */
public class Questionnaires extends ApiRoute {

	/**
	 * DAO for Questionnaire.
	 */
	protected final QuestionnairesDAO questionnairesDAO;
	/**
	 * DAO for Question.
	 */
	protected final QuestionsDAO questionsDAO;

	/**
	 * Instantiate the questionnaires endpoint. The setup method must be called to
	 * install the endpoint.
	 * 
	 * @param json              the json formatter
	 * @param questionnairesDAO the DAO for Questionnaire
	 * @param questionsDAO      the DAO for Question
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public Questionnaires(Json json, QuestionnairesDAO questionnairesDAO, QuestionsDAO questionsDAO) {
		super(json);

		if (questionnairesDAO == null) {
			throw new IllegalArgumentException("questionnairesDAO mustn't be null");
		}

		if (questionsDAO == null) {
			throw new IllegalArgumentException("questionsDAO mustn't be null");
		}

		this.questionnairesDAO = questionnairesDAO;
		this.questionsDAO = questionsDAO;
	}

	@Override
	public String getCollectionName() {
		return "questionnaires";
	}

	/**
	 * Install the endpoint in Spark. Methods: - get - post - generate - delete
	 */
	@Override
	public void setup() {
		Spark.get("/api/questionnaires", this::get);
		Spark.post("/api/questionnaires", this::post);
		Spark.post("/api/questionnaires/generate", this::generate);
		Spark.delete("/api/questionnaires", this::delete);
		Spark.delete("/api/questionnaires/deleteAll", this::deleteAll);

		logger.info("Setup /api/questionnaires.");
	}

	/**
	 * Get id endpoint: fetch the specified questionnaire from the database. If not
	 * authorised, and the given questionnaire is private, returns an error message.
	 */
	@Override
	protected Object getById(Request request, Response response, String id, boolean auth) {
		if (id == null) {
			throw new IllegalArgumentException("id mustn't be null");
		}

		logger.info("Fetching questionnaire {}", id);

		var questionnaire = questionnairesDAO.get(id);

		if (questionnaire == null) {
			logger.error("Invalid request!");
			response.status(400);
			return ReprovaRoute.INVALID;
		}

		logger.info("Done. Responding...");

		response.status(200);

		return json.render(questionnaire);
	}

	/**
	 * Get all endpoint: fetch all questionnaires from the database. If not
	 * authorized, fetches only public questionnaires.
	 */
	@Override
	protected Object getAll(Request request, Response response, boolean auth) {

		logger.info("Fetching questionnaires.");

		var questionnaires = questionnairesDAO.list();

		logger.info("Done. Responding...");

		response.status(200);

		return json.render(questionnaires);
	}

	/**
	 * Helper function to build Question
	 *
	 */
	private Question buildQuestion(Question question) {
		if (Environments.getInstance().getEnableEstimatedTime()) {
			return new Question.Builder().theme(question.theme).description(question.description)
					.statement(question.statement).estimatedTime(question.estimatedTime).record(question.record)
					.pvt(question.pvt).build();
		}

		return new Question.Builder().theme(question.theme).description(question.description)
				.statement(question.statement).record(question.record).pvt(question.pvt).build();
	}

	/**
	 * Post endpoint: add or update a questionnaire in the database. The
	 * questionnaire must be supplied in the request's body. If the questionnaire
	 * has an 'id' field, the operation is an update. Otherwise, the given
	 * questionnaire is added as a new questionnaire in the database. This endpoint
	 * is for authorized access only.
	 */
	protected Object post(Request request, Response response) {
		String authResult = extractUnauthorized(request, response);

		if (authResult != null) {
			return authResult;
		}

		Questionnaire questionnaire;
		try {
			questionnaire = json.parse(request.body(), Questionnaire.Builder.class).build();
		} catch (Exception e) {
			logger.error("Invalid request payload!", e);
			response.status(400);
			return ReprovaRoute.INVALID;
		}

		logger.info("Parsed {}", questionnaire);
		logger.info("Adding questionnaire.");

		var success = questionnairesDAO.add(questionnaire);

		logger.info("Added questionnaire.");
		logger.info("Adding questions.");

		for (var question : questionnaire.questions) {
			question = buildQuestion(question);

			questionsDAO.add(question);
		}

		response.status(success ? 200 : 400);

		logger.info("Done. Responding...");

		return ReprovaRoute.OK;
	}

	private String extractUnauthorized(Request request, Response response) {
		String body = request.body();

		logger.info("Received questionnaires post: {}", body);

		response.type(APPLICATION_JSON);

		var token = request.queryParams("token");

		if (!ReprovaRoute.authorized(token)) {
			logger.info("Unauthorized token: {}", token);
			response.status(403);
			return ReprovaRoute.UNAUTHORIZED;
		}
		return body;
	}

	/**
	 * Generate endpoint: create a questionnaire in the database. The parameters for
	 * the questionnaire's generation must be supplied in the request's body. Such
	 * parameters must include the averageDifficulty and may include
	 * totalEstimatedTime. This endpoint is for authorized access only.
	 */
	protected Object generate(Request request, Response response) {
		String authResult = extractUnauthorized(request, response);

		if (authResult != null) {
			return authResult;
		}

		Questionnaire questionnaire;
		try {
			questionnaire = json.parse(request.body(), Questionnaire.Generator.class).generate(questionsDAO);
		} catch (Exception e) {
			logger.error("Invalid request payload!", e);
			response.status(400);
			return ReprovaRoute.INVALID;
		}

		logger.info("Generated {}", questionnaire);
		logger.info("Adding questionnaire.");

		var success = questionnairesDAO.add(questionnaire);

		logger.info("Added questionnaire.");

		response.status(success ? 200 : 400);

		logger.info("Done. Responding...");

		return ReprovaRoute.OK;
	}

	/**
	 * Delete endpoint: remove a questionnaire from the database. The
	 * questionnaire's id must be supplied through the 'id' query parameter. This
	 * endpoint is for authorized access only.
	 */
	protected Object delete(Request request, Response response) {
		logger.info("Received questionnaires delete:");

		response.type(APPLICATION_JSON);

		var id = request.queryParams("id");
		var token = request.queryParams("token");

		if (!ReprovaRoute.authorized(token)) {
			logger.info("Unauthorized token: {}", token);
			response.status(403);
			return ReprovaRoute.UNAUTHORIZED;
		}

		if (id == null) {
			logger.error("Invalid request!");
			response.status(400);
			return ReprovaRoute.INVALID;
		}

		logger.info("Deleting questionnaire {}", id);

		var success = questionnairesDAO.remove(id);

		logger.info("Done. Responding...");

		response.status(success ? 200 : 400);

		return ReprovaRoute.OK;
	}

	/**
	 * Delete All endpoint: remove all questionnaires from the database. This
	 * endpoint is for authorized access only.
	 */
	protected Object deleteAll(Request request, Response response) {
		logger.info("Received questionnaires delete all:");

		response.type(APPLICATION_JSON);

		var token = request.queryParams("token");

		if (!ReprovaRoute.authorized(token)) {
			logger.info("Unauthorized token: {}", token);
			response.status(403);
			return ReprovaRoute.UNAUTHORIZED;
		}

		logger.info("Deleting all questionnaires");

		boolean success = false;
		ArrayList<Questionnaire> questionnaires = new ArrayList<>(questionnairesDAO.list());
		for (Questionnaire questionnaire : questionnaires) {
			String id = questionnaire.getId();
			logger.info("Deleting questionnaire {}", id);

			success = questionnairesDAO.remove(id);
			if (!success) {
				break;
			}
		}

		logger.info("Done. Responding...");

		response.status(success ? 200 : 400);

		return ReprovaRoute.OK;
	}

}