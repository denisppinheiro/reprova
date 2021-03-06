package br.ufmg.engsoft.reprova.tests.model;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import br.ufmg.engsoft.reprova.model.Questionnaire;
import br.ufmg.engsoft.reprova.database.QuestionsDAO;
import br.ufmg.engsoft.reprova.model.Environments;
import br.ufmg.engsoft.reprova.model.Question;
import br.ufmg.engsoft.reprova.tests.utils.EnvironmentUtils;


public class QuestionnaireTest {
	private QuestionsDAO _questionsDAO;
	private ArrayList<Question> _questions;
	
	private void setUpQuestionsDAOMock(int questionsCount, int estimatedTime) {
		_questions = new ArrayList<Question>();

    for (int i = 0; i < questionsCount; i++) {
	    _questions.add(
	  		new Question.Builder()
	    		.setTheme("theme " + (i+1))
	    		.setDescription("description " + (i+1))
          .setDifficulty("Average")
          .setEstimatedTime(estimatedTime)
	    		.build()
			);
    }
  	
  	_questionsDAO = mock(QuestionsDAO.class);
  	when(_questionsDAO.list(null, null)).thenReturn(_questions);
	}
	
  @BeforeEach
  public void init() throws Exception {
		Environments.reset();
  	EnvironmentUtils.setEnvVariables(false, 0);
  }
  
  /**
   * Questionnaire building without any optional values nor features.
   */
  @Test
  void buildSuccess_defaultValues() {
    var questionnaire = new Questionnaire.Builder()
      .build();
    
    assertFalse(questionnaire == null);
    assertEquals(null, questionnaire.id);
    assertEquals(0, questionnaire.questions.size());
    assertEquals(0, questionnaire.totalEstimatedTime);
    assertEquals(null, questionnaire.averageDifficulty);
  }
  
  /**
   * Questionnaire building with optional values and features.
   * @throws Exception 
   */
  @Test
  void buildSuccess_allValues() throws Exception {
  	EnvironmentUtils.setEnvVariables(true, 3);
  	var questions = new ArrayList<Question>();
  	var question = new Question.Builder()
        .setTheme("theme")
        .setDescription("description")
  			.build();
  	
  	questions.add(question);
  	
    var questionnaire = new Questionnaire.Builder()
			.id("1")
    	.questions(questions)
    	.totalEstimatedTime(8)
    	.averageDifficulty("Average")
      .build();
    
    assertFalse(questionnaire == null);
    assertEquals("1", questionnaire.id);
    assertEquals(1, questionnaire.questions.size());
    assertEquals(8, questionnaire.totalEstimatedTime);
    assertEquals("Average", questionnaire.averageDifficulty);
  }
  
  /**
   * A question within the questionnaire mustn't be null.
   */
  @Test
  void nullQuestion() {
    var questions = new ArrayList<Question>();
    questions.add(null);
    
    assertThrows(
      IllegalArgumentException.class,
      () -> {
        new Questionnaire.Builder()
          .questions(questions)
          .build();
      }
    );
  }
  
  /**
   * Questionnaire generation without any optional values nor features.
   * @throws Exception 
   */
  @Test
  void generateSuccess_defaultValues() throws Exception {
  	setUpQuestionsDAOMock(8, 8);
  	
    var questionnaire = new Questionnaire.Generator()
      .generate(_questionsDAO);
    
    assertFalse(questionnaire == null);
    assertEquals(null, questionnaire.id);
    assertEquals(5, questionnaire.questions.size());
    assertEquals(60, questionnaire.totalEstimatedTime);
    assertEquals(null, questionnaire.averageDifficulty);
  }
  
  /**
   * Questionnaire generation with optional values and features.
   * @throws Exception 
   */
  @Test
  void generateSuccess_allValues() throws Exception {
  	EnvironmentUtils.setEnvVariables(true, 3);
  	setUpQuestionsDAOMock(8, 8);
  	
    var questionnaire = new Questionnaire.Generator()
			.setId("1")
    	.setTotalEstimatedTime(8)
    	.setAverageDifficulty("Average")
    	.setQuestionsCount(1)
      .generate(_questionsDAO);
    
    assertFalse(questionnaire == null);
    assertEquals(null, questionnaire.id);
    assertEquals(1, questionnaire.questions.size());
    assertEquals(8, questionnaire.totalEstimatedTime);
    assertEquals("Average", questionnaire.averageDifficulty);
  }
  
  /**
   * Questionnaire generation with optional values and features.
   * @throws Exception 
   */
  @Test
  void generateSuccess_allValues_fiveDifficulties() throws Exception {
  	EnvironmentUtils.setEnvVariables(true, 5);
  	setUpQuestionsDAOMock(8, 8);
  	
    var questionnaire = new Questionnaire.Generator()
			.setId("1")
    	.setTotalEstimatedTime(8)
    	.setAverageDifficulty("Average")
    	.setQuestionsCount(1)
      .generate(_questionsDAO);
    
    assertFalse(questionnaire == null);
    assertEquals(null, questionnaire.id);
    assertEquals(1, questionnaire.questions.size());
    assertEquals(8, questionnaire.totalEstimatedTime);
    assertEquals("Average", questionnaire.averageDifficulty);
  }
}
