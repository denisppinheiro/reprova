package br.ufmg.engsoft.reprova.tests.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.ufmg.engsoft.reprova.database.QuestionnairesDAO;
import br.ufmg.engsoft.reprova.database.QuestionsDAO;
import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Environments;
import br.ufmg.engsoft.reprova.model.Question;
import br.ufmg.engsoft.reprova.model.Questionnaire;
import br.ufmg.engsoft.reprova.routes.api.Questionnaires;
import br.ufmg.engsoft.reprova.tests.utils.EnvironmentUtils;
import spark.Request;
import spark.Response;

public class QuestionnairesTest {
	private ArrayList<Questionnaire> _questionnaires;
	private Json _json;
	private Questionnaire _questionnaire;
	private QuestionsDAO _questionsDAO;
	private QuestionnairesDAO _questionnairesDAO;
	private Request _request;
	private Response _response;
	private String _renderedQuestionnaire;
	private String _renderedQuestionnaires;
	private ArrayList<Question> _questions;
	private Question _question;

	private void setup() {
		var builder = new Questionnaire.Builder().id("1");
		_questionnaire = builder.build();
		_renderedQuestionnaire = "questionnaire";
		_renderedQuestionnaires = "questionnaires";

		_json = mock(Json.class);
		when(_json.render(_questionnaire)).thenReturn(_renderedQuestionnaire);
		when(_json.parse("body", Questionnaire.Builder.class)).thenReturn(builder);

		_questionsDAO = mock(QuestionsDAO.class);

		_questionnairesDAO = mock(QuestionnairesDAO.class);
		when(_questionnairesDAO.get("1")).thenReturn(_questionnaire);
		when(_questionnairesDAO.add(any(Questionnaire.class))).thenReturn(true);
		when(_questionnairesDAO.remove("1")).thenReturn(true);

		_request = mock(Request.class);
		when(_request.queryParams("id")).thenReturn("1");
		when(_request.queryParams("token")).thenReturn("token");
		when(_request.body()).thenReturn("body");

		_response = mock(Response.class);

		_questionnaires = new ArrayList<Questionnaire>();
		_questionnaires.add(_questionnaire);
		when(_questionnairesDAO.list()).thenReturn(_questionnaires);
		when(_json.render(_questionnaires)).thenReturn(_renderedQuestionnaires);
	}

	@BeforeEach
	public void init() throws Exception {
		Environments.reset();
		EnvironmentUtils.setEnvVariables(false, 0);

		setup();
	}

	@Test
	void getWithId() {
		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return get(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals(_renderedQuestionnaire, resposta);

		verify(_request, times(1)).queryParams("id");
		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(200);
	}

	@Test
	void getWithId_nullQuestionnaire() {
		when(_questionnairesDAO.get("1")).thenReturn(null);
		_renderedQuestionnaire = "\"Invalid request\"";

		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return get(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals(_renderedQuestionnaire, resposta);

		verify(_request, times(1)).queryParams("id");
		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(400);
	}

	@Test
	void getWithoutId_authorized() {
		when(_request.queryParams("id")).thenReturn(null);

		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return get(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals(_renderedQuestionnaires, resposta);

		verify(_request, times(1)).queryParams("id");
		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(200);
	}

	@Test
	void post() {
		_question = new Question.Builder().theme("theme").description("description").build();
		_questions = new ArrayList<Question>();
		_questions.add(_question);

		var builder = new Questionnaire.Builder().questions(_questions);
		when(_json.parse("body", Questionnaire.Builder.class)).thenReturn(builder);

		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return post(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals("\"Ok\"", resposta);

		verify(_request, times(1)).body();
		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(200);
	}

	@Test
	void post_unauthorized() {
		when(_request.queryParams("token")).thenReturn("");

		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return post(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals("\"Unauthorized\"", resposta);

		verify(_request, times(1)).body();
		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(403);
	}

	@Test
	void post_invalidBody() {
		when(_request.body()).thenReturn("invalidBody");

		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return post(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals("\"Invalid request\"", resposta);

		verify(_request, times(1)).body();
		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(400);
	}

	@Test
	void post_DAOError() {
		when(_questionnairesDAO.add(any(Questionnaire.class))).thenReturn(false);

		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return post(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals("\"Ok\"", resposta);

		verify(_request, times(1)).body();
		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(400);
	}

	@Test
	void generate() {
		var generator = new Questionnaire.Generator().id("1");
		when(_json.parse("body", Questionnaire.Generator.class)).thenReturn(generator);

		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return generate(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals("\"Ok\"", resposta);

		verify(_request, times(1)).body();
		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(200);
	}

	@Test
	void generate_unauthorized() {
		when(_request.queryParams("token")).thenReturn("");

		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return generate(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals("\"Unauthorized\"", resposta);

		verify(_request, times(1)).body();
		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(403);
	}

	@Test
	void generate_invalidBody() {
		when(_request.body()).thenReturn("invalidBody");

		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return generate(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals("\"Invalid request\"", resposta);

		verify(_request, times(1)).body();
		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(400);
	}

	@Test
	void generate_DAOError() {
		var generator = new Questionnaire.Generator().id("1");
		when(_json.parse("body", Questionnaire.Generator.class)).thenReturn(generator);
		when(_questionnairesDAO.add(any(Questionnaire.class))).thenReturn(false);

		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return generate(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals("\"Ok\"", resposta);

		verify(_request, times(1)).body();
		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(400);
	}

	@Test
	void delete() {
		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return delete(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals("\"Ok\"", resposta);

		verify(_request, times(1)).queryParams("id");
		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(200);
	}

	@Test
	void delete_unauthorized() {
		when(_request.queryParams("token")).thenReturn("");

		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return delete(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals("\"Unauthorized\"", resposta);

		verify(_request, times(1)).queryParams("id");
		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(403);
	}

	@Test
	void delete_nullId() {
		when(_request.queryParams("id")).thenReturn(null);

		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return delete(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals("\"Invalid request\"", resposta);

		verify(_request, times(1)).queryParams("id");
		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(400);
	}

	@Test
	void delete_DAOError() {
		when(_questionnairesDAO.remove("1")).thenReturn(false);

		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return delete(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals("\"Ok\"", resposta);

		verify(_request, times(1)).queryParams("id");
		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(400);
	}

	@Test
	void deleteAll() {
		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return deleteAll(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals("\"Ok\"", resposta);

		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(200);
	}

	@Test
	void deleteAll_unauthorized() {
		when(_request.queryParams("token")).thenReturn("");

		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return deleteAll(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals("\"Unauthorized\"", resposta);

		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(403);
	}

	@Test
	void deleteAll_DAOError() {
		when(_questionnairesDAO.remove("1")).thenReturn(false);

		var resposta = new Questionnaires(_json, _questionnairesDAO, _questionsDAO) {
			public Object callProtectedMethod(Request request, Response response) {
				return deleteAll(request, response);
			}
		}.callProtectedMethod(_request, _response);

		assertEquals("\"Ok\"", resposta);

		verify(_request, times(1)).queryParams("token");

		verify(_response, times(1)).type("application/json");
		verify(_response, times(1)).status(400);
	}

	/**
	 * Questionnaires mustn't have a null Json.
	 */
	@Test
	void constructor_nullJson() {
		assertThrows(IllegalArgumentException.class, () -> {
			new Questionnaires(null, _questionnairesDAO, _questionsDAO);
		});
	}

	/**
	 * Questionnaires mustn't have a null QuestionnairesDAO.
	 */
	@Test
	void constructor_nullQuestionnairesDAO() {
		assertThrows(IllegalArgumentException.class, () -> {
			new Questionnaires(_json, null, _questionsDAO);
		});
	}

	/**
	 * Questionnaires mustn't have a null QuestionsDAO.
	 */
	@Test
	void constructor_nullQuestionsDAO() {
		assertThrows(IllegalArgumentException.class, () -> {
			new Questionnaires(_json, _questionnairesDAO, null);
		});
	}
}