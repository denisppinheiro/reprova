package br.ufmg.engsoft.reprova.tests.model;

import static org.mockito.Mockito.*;

import java.util.ArrayList;

import spark.Request;
import spark.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import br.ufmg.engsoft.reprova.database.QuestionsDAO;
import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Environments;
import br.ufmg.engsoft.reprova.model.Question;
import br.ufmg.engsoft.reprova.routes.api.Questions;
import br.ufmg.engsoft.reprova.tests.utils.EnvironmentUtils;


public class QuestionsTest {	
	private ArrayList<Question> _questions;
	private Json _json;
	private Question _question;
	private QuestionsDAO _questionsDAO;
	private Request _request;
	private Response _response;
	private String _renderedQuestion;
	private String _renderedQuestions;
	
	private void setup() {
		var builder = new Question.Builder()
				.setId("1")
				.setTheme("theme")
				.setStatement("statement")
				.setDifficulty("Average")
				.setDescription("description");
		_question = builder.build();
		_renderedQuestion = "question";
		_renderedQuestions = "questions";
		
		_json = mock(Json.class);
		when(_json.render(_question)).thenReturn(_renderedQuestion);
		when(_json.parse("body", Question.Builder.class))
			.thenReturn(builder);
		
  	_questionsDAO = mock(QuestionsDAO.class);
  	when(_questionsDAO.get("1")).thenReturn(_question);
  	when(_questionsDAO.add(_question)).thenReturn(true);
  	when(_questionsDAO.remove("1")).thenReturn(true);
  	
  	_request = mock(Request.class);
  	when(_request.queryParams("id")).thenReturn("1");
  	when(_request.queryParams("token")).thenReturn("token");
  	when(_request.body()).thenReturn("body");
  	
  	_response = mock(Response.class);
  	
  	_questions = new ArrayList<Question>();
  	_questions.add(_question);
  	when(_questionsDAO.list(null, null)).thenReturn(_questions);
		when(_json.render(_questions)).thenReturn(_renderedQuestions);
	}
	
	@BeforeEach
	public void init() throws Exception {
		Environments.reset();
		EnvironmentUtils.setEnvVariables(false, 0);
		
		setup();
	}
	
  @Test
  void getWithId() {
  	var resposta = new Questions(_json, _questionsDAO) {
  		public Object callProtectedMethod(Request request, Response response) {
  			return get(request, response);
  		}
  	}.callProtectedMethod(_request, _response);
  	
  	assertEquals(_renderedQuestion, resposta);
  	
  	verify(_request, times(1)).queryParams("id");
  	verify(_request, times(1)).queryParams("token");
  	
  	verify(_response, times(1)).type("application/json");
  	verify(_response, times(1)).status(200);
  }
	
  @Test
  void getWithId_nullQuestion() {
  	when(_questionsDAO.get("1")).thenReturn(null);
  	_renderedQuestion = "\"Invalid request\"";
  	
  	var resposta = new Questions(_json, _questionsDAO) {
  		public Object callProtectedMethod(Request request, Response response) {
  			return get(request, response);
  		}
  	}.callProtectedMethod(_request, _response);
  	
  	assertEquals(_renderedQuestion, resposta);
  	
  	verify(_request, times(1)).queryParams("id");
  	verify(_request, times(1)).queryParams("token");
  	
  	verify(_response, times(1)).type("application/json");
  	verify(_response, times(1)).status(400);
  }
	
  @Test
  void getWithId_unauthorized() {
  	when(_request.queryParams("token")).thenReturn("");
  	_renderedQuestion = "\"Unauthorized\"";
  	
  	var resposta = new Questions(_json, _questionsDAO) {
  		public Object callProtectedMethod(Request request, Response response) {
  			return get(request, response);
  		}
  	}.callProtectedMethod(_request, _response);
  	
  	assertEquals(_renderedQuestion, resposta);
  	
  	verify(_request, times(1)).queryParams("id");
  	verify(_request, times(1)).queryParams("token");
  	
  	verify(_response, times(1)).type("application/json");
  	verify(_response, times(1)).status(403);
  }
	
  @Test
  void getWithoutId_authorized() {
  	when(_request.queryParams("id")).thenReturn(null);
  	
  	var resposta = new Questions(_json, _questionsDAO) {
  		public Object callProtectedMethod(Request request, Response response) {
  			return get(request, response);
  		}
  	}.callProtectedMethod(_request, _response);
  	
  	assertEquals(_renderedQuestions, resposta);
  	
  	verify(_request, times(1)).queryParams("id");
  	verify(_request, times(1)).queryParams("token");
  	
  	verify(_response, times(1)).type("application/json");
  	verify(_response, times(1)).status(200);
  }
	
  @Test
  void getWithoutId_unauthorized() {
  	when(_request.queryParams("token")).thenReturn("");
  	when(_request.queryParams("id")).thenReturn(null);
  	when(_questionsDAO.list(null, false)).thenReturn(null);
		when(_json.render(null)).thenReturn("");
  	
  	var resposta = new Questions(_json, _questionsDAO) {
  		public Object callProtectedMethod(Request request, Response response) {
  			return get(request, response);
  		}
  	}.callProtectedMethod(_request, _response);
  	
  	assertEquals("", resposta);
  	
  	verify(_request, times(1)).queryParams("id");
  	verify(_request, times(1)).queryParams("token");
  	
  	verify(_response, times(1)).type("application/json");
  	verify(_response, times(1)).status(200);
  }
	
  @Test
  void post() {
  	var resposta = new Questions(_json, _questionsDAO) {
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
  	
  	var resposta = new Questions(_json, _questionsDAO) {
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
  	
  	var resposta = new Questions(_json, _questionsDAO) {
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
  	when(_questionsDAO.add(_question)).thenReturn(false);
  	
  	var resposta = new Questions(_json, _questionsDAO) {
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
  void delete() {
  	var resposta = new Questions(_json, _questionsDAO) {
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
  	
  	var resposta = new Questions(_json, _questionsDAO) {
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
  	
  	var resposta = new Questions(_json, _questionsDAO) {
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
  	when(_questionsDAO.remove("1")).thenReturn(false);
  	
  	var resposta = new Questions(_json, _questionsDAO) {
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
  	var resposta = new Questions(_json, _questionsDAO) {
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
  	
  	var resposta = new Questions(_json, _questionsDAO) {
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
  	when(_questionsDAO.remove("1")).thenReturn(false);
  	
  	var resposta = new Questions(_json, _questionsDAO) {
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
   * Questions mustn't have a null Json.
   */
  @Test
  void constructor_nullJson() {
    assertThrows(
      IllegalArgumentException.class,
      () -> {
      	new Questions(null, _questionsDAO);
      }
    );
  }
	
  /**
   * Questions mustn't have a null QuestionsDAO.
   */
  @Test
  void constructor_nullQuestionsDAO() {
    assertThrows(
      IllegalArgumentException.class,
      () -> {
      	new Questions(_json, null);
      }
    );
  }
}