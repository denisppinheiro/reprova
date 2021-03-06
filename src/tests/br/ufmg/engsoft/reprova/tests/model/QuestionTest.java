package br.ufmg.engsoft.reprova.tests.model;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import br.ufmg.engsoft.reprova.model.Environments;
import br.ufmg.engsoft.reprova.model.Question;
import br.ufmg.engsoft.reprova.model.Semester;
import br.ufmg.engsoft.reprova.tests.utils.EnvironmentUtils;


public class QuestionTest {	
	private Semester _semester;
  private Map<Semester, Map<String, Map<String, Float>>> _record = new HashMap<Semester, Map<String, Map<String, Float>>>();
  private Map<String, Boolean> _choices = new HashMap<String, Boolean>();
	
  @BeforeEach
  public void init() throws Exception {
		Environments.reset();
  	EnvironmentUtils.setEnvVariables(false, 0);
  	
    var year = 2021;
    var ref = Semester.Reference.fromInt(1);

    _semester = new Semester(year, ref);
    var innerRecord = new HashMap<String, Map<String, Float>>();
    
    for (int i = 0; i < 4; i++) {
      var grades = new HashMap<String, Float>();

      grades.put("S" + (i+1), (float)20*(i+1));
      innerRecord.put("T" + (i+1), grades);
    }
    
    _record.put(_semester, innerRecord);
    
    _choices.put("A", false);
    _choices.put("B", false);
    _choices.put("C", true);
    _choices.put("D", false);
  }
	
  /**
   * Question building without any optional values nor features.
   * @throws Exception 
   */
  @Test
  void buildSuccess_defaultValues_envOff() throws Exception {
    var question = new Question.Builder()
      .theme("theme")
      .description("description")
      .build();

    assertFalse(question == null);
    assertEquals(null, question.id);
    assertEquals("theme", question.theme);
    assertEquals("description", question.description);
    assertEquals(true, question.pvt);
    assertEquals(null, question.statement);
    assertEquals(null, question.estimatedTime);
    assertEquals(null, question.difficulty);
    assertEquals(null, question.getChoices());
    assertEquals(0, question.record.size());
  }
	
  /**
   * Question building without any optional values but with features.
   * @throws Exception 
   */
  @Test
  void buildSuccess_defaultValues_envOn() throws Exception {
  	EnvironmentUtils.setEnvVariables(true, 3);
  	
    var question = new Question.Builder()
      .theme("theme")
      .description("description")
      .build();

    var statistics = question.getStatistics();
    question.calculateDifficulty();

    assertFalse(question == null);
    assertEquals(null, question.id);
    assertEquals("theme", question.theme);
    assertEquals("description", question.description);
    assertEquals(true, question.pvt);
    assertEquals(null, question.statement);
    assertEquals(0, question.record.size());
    assertEquals(Double.NaN, statistics.get("average"));
    assertEquals(-0.0, statistics.get("Std Deviation"));
    assertEquals(0.0, statistics.get("median"));
    assertEquals(null, question.estimatedTime);
    assertEquals("Easy", question.difficulty);
    assertEquals(null, question.getChoices());
  }

  /**
   * Question building with optional values and features.
   * @throws Exception 
   */
  @Test
  void buildSuccess_allValues() throws Exception {
  	EnvironmentUtils.setEnvVariables(true, 3);
  	
    var question = new Question.Builder()
    	.id("1")
      .theme("theme")
      .description("description")
      .statement("statement")
      .pvt(false)
      .record(_record)
      .estimatedTime(20)
      .choices(_choices)
      .difficulty("Average")
      .build();
    
    var statistics = question.getStatistics();
    question.calculateDifficulty();

    assertFalse(question == null);
    assertEquals("1", question.id);
    assertEquals("theme", question.theme);
    assertEquals("description", question.description);
    assertEquals("statement", question.statement);
    assertEquals(false, question.pvt);
    assertEquals(_record.size(), question.record.size());
    assertEquals(_record.get(_semester).size(), question.record.get(_semester).size());
    assertEquals(3, statistics.size());
    assertEquals(50.0, statistics.get("average"));
    assertEquals(25.81988897471611, statistics.get("Std Deviation"));
    assertEquals(50.0, statistics.get("median"));
    assertEquals(20, question.estimatedTime);
    assertEquals("Average", question.difficulty);
    assertEquals(_choices.size(), question.getChoices().size());
  }
  
  /**
   * A question mustn't have a null theme.
   */
  @Test
  void nullTheme() {
    assertThrows(
      IllegalArgumentException.class,
      () -> {
        new Question.Builder()
          .setTheme(null)
          .setDescription("desc")
          .build();
      }
    );
  }

  /**
   * A question mustn't have an empty theme.
   */
  @Test
  void emptyTheme() {
    assertThrows(
      IllegalArgumentException.class,
      () -> {
        new Question.Builder()
          .setTheme("")
          .setDescription("desc")
          .build();
      }
    );
  }

  /**
   * A question mustn't have a null description.
   */
  @Test
  void nullDescription() {
    assertThrows(
      IllegalArgumentException.class,
      () -> {
        new Question.Builder()
          .setTheme("theme")
          .setDescription(null)
          .build();
      }
    );
  }

  /**
   * A question mustn't have an empty description.
   */
  @Test
  void emptyDescription() {
    assertThrows(
      IllegalArgumentException.class,
      () -> {
        new Question.Builder()
          .setTheme("theme")
          .setDescription("")
          .build();
      }
    );
  }

  /**
   * A question mustn't have a null inner record.
   */
  @Test
  void nullInnerRecord() {
    var year = 2021;
    var ref = Semester.Reference.fromInt(1);

    var record = new HashMap<Semester, Map<String, Map<String, Float>>>();
    record.put(new Semester(year, ref), null); 

    assertThrows(
      IllegalArgumentException.class,
      () -> {
        new Question.Builder()
          .theme("theme")
          .description("description")
          .record(record)
          .build();
      }
    );
  }
}
