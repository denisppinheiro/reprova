package br.ufmg.engsoft.reprova.routes.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.ReprovaRoute;
import spark.Request;
import spark.Response;

public abstract class ApiRoute {

	protected static final String APPLICATION_JSON = "application/json";

	/**
	 * Logger instance.
	 */
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Json formatter.
	 */
	protected final Json json;

	/**
	 * Instantiate the route endpoint. The setup method must be called to install
	 * the endpoint.
	 * 
	 * @param json the json formatter
	 * @throws IllegalArgumentException if any parameter is null
	 */
	protected ApiRoute(Json json) {
		if (json == null) {
			throw new IllegalArgumentException("json mustn't be null");
		}

		this.json = json;
	}

	public abstract void setup();

	public abstract String getCollectionName();

	/**
	 * Get endpoint: lists all collections, or a single model if a 'id' query
	 * parameter is provided.
	 */
	protected Object get(Request request, Response response) {
		logger.info("Received {} get:", getCollectionName());

		var id = request.queryParams("id");

		var token = request.queryParams("token");

		response.type(APPLICATION_JSON);

		if (id == null) {
			return this.getAll(request, response, ReprovaRoute.authorized(token));
		}

		return this.getById(request, response, id, ReprovaRoute.authorized(token));
	}

	protected abstract Object getById(Request request, Response response, String id, boolean auth);

	protected abstract Object getAll(Request request, Response response, boolean auth);

}
