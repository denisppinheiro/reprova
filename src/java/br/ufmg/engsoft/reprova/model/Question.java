package br.ufmg.engsoft.reprova.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.ufmg.engsoft.reprova.model.difficulty.DifficultyFactory;

/**
 * The question type.
 */
public class Question extends ReprovaModel {

    /**
     * The theme of the question. Mustn't be null nor empty.
     */
    public final String theme;
    /**
     * The description of the question. Mustn't be null nor empty.
     */
    public final String description;
    /**
     * The statement of the question. May be null or empty.
     */
    public final String statement;
    /**
     * The record of the question per semester per class. Mustn't be null, may be
     * empty.
     */
    public final Map<Semester, Map<String, Map<String, Float>>> record;
    /**
     * Whether the question is private.
     */
    public final boolean pvt;
    /**
     * The difficulty of the question. May have different groupings.
     */
    public String difficulty;
    /**
     * The difficulty's possible values.
     */
    private final List<String> difficultyGroup;
    /**
     * The estimated time in minutes for the question to be solved
     */
    public final Integer estimatedTime;

    /**
     * Available choices for question, used if the multiple choice feature is
     * enabled
     */
    private final Map<String, Boolean> choices;
    /**
     * Available statistics for question
     */
    private final Map<String, Double> statistics;

    /**
     * Builder for Question.
     */
    public static class Builder implements ReprovaModelBuilder<Question> {
        protected String id;
        protected String theme;
        protected String description;
        protected String statement;
        protected Map<Semester, Map<String, Map<String, Float>>> record;
        protected boolean pvt = true;
        protected Integer estimatedTime;
        protected String difficulty;
        protected List<String> difficultyGroup;
        protected Map<String, Boolean> choices;
        protected Map<String, Double> statistics;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder theme(String theme) {
            this.theme = theme;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder statement(String statement) {
            this.statement = statement;
            return this;
        }

        public Builder record(Map<Semester, Map<String, Map<String, Float>>> record) {
            this.record = record;
            return this;
        }

        public Builder pvt(boolean pvt) {
            this.pvt = pvt;
            return this;
        }

        public Builder choices(Map<String, Boolean> choices) {
            this.choices = choices;
            return this;
        }

        public Builder estimatedTime(int estimatedTime) {
            this.estimatedTime = estimatedTime;
            return this;
        }

        public Builder difficulty(String difficulty) {
            this.difficulty = difficulty;
            return this;
        }
        
        public Builder statistics(Map<String, Double> statistics) {
        	this.statistics = statistics;
        	return this;
        }

        public Builder difficultyGroup(List<String> difficulty) {
            this.difficultyGroup = difficulty;
            return this;
				}
				
    /**
     * Calculate the difficulty based on the record and the difficultyGroup. Should
     * be called when changes are made to the record.
     */
    public Question build() {
      if (theme == null) {
        throw new IllegalArgumentException("theme mustn't be null");
      }

      if (theme.isEmpty()) {
        throw new IllegalArgumentException("theme mustn't be empty");
      }

      if (description == null) {
        throw new IllegalArgumentException("description mustn't be null");
      }

      if (description.isEmpty()) {
        throw new IllegalArgumentException("description mustn't be empty");
      }

      if (this.record == null) {
        this.record = new HashMap<Semester, Map<String, Map<String, Float>>>();
      } else {
        // All inner maps mustn't be null:
        for (var entry : this.record.entrySet()) {
        	if (entry.getValue() == null) {
                throw new IllegalArgumentException("inner record mustn't be null");
        	}
        }    
      }
      
      if (this.statistics == null && Environments.getInstance().getEnableQuestionStatistics()) {
    	  this.statistics = new HashMap<String, Double>();
      }

      if (!Environments.getInstance().getEnableEstimatedTime()){
        this.estimatedTime = null;
      } else {
        this.estimatedTime = estimatedTime;
      }
      
      if (!Environments.getInstance().getEnableMultipleChoice()){
        this.choices = null;
      } else {
        this.choices = choices;
      }

      Environments environments = Environments.getInstance();

      if (environments.getDifficultyGroup() != 0) {
    	// TODO validate possible values (3 and 5)
    	int valueDifficultyGroup = environments.getDifficultyGroup();
    	this.difficultyGroup = new DifficultyFactory()
    								.getDifficulty(valueDifficultyGroup)
    								.getDifficulties();
      } else {
        this.difficultyGroup = null;
      }

      return new Question(
        this.id,
        this.theme,
        this.description,
        this.statement,
        this.record,
        this.pvt,
        this.estimatedTime,
        this.difficulty,
		this.difficultyGroup,
		this.choices,
		this.statistics
      );
    }
  }

  /**
   * Protected constructor, should only be used by the builder.
   */
  protected Question(
    String id,
    String theme,
    String description,
    String statement,
    Map<Semester, Map<String, Map<String, Float>>> record,
    boolean pvt,
    Integer estimatedTime,
    String difficulty,
		List<String> difficultyGroup,
		Map<String, Boolean> choices,
		Map<String, Double> statistics
  ) {
    this.id = id;
    this.theme = theme;
    this.description = description;
    this.statement = statement;
    this.record = record;
    this.pvt = pvt;
    this.estimatedTime = estimatedTime;
    this.difficulty = difficulty;
	this.difficultyGroup = difficultyGroup;
	this.choices = choices;
	this.statistics = statistics;
  }
	
	public Map<String, Boolean> getChoices() {
		return this.choices;
	}
	
	
	public Map<String, Double> getStatistics(){
    	this.statistics.put("average", this.calculateGradeAverage());
    	this.statistics.put("Std Deviation", this.calculateGradeStandardDeviation());
    	this.statistics.put("median", this.calculateGradeMedian());
    	return this.statistics;
	}

  /* Calculate Grades Average */
  private double calculateGradeAverage() {
	  double acc = 0;
	    for (Map.Entry<Semester, Map<String, Map<String, Float>>> entry : this.record.entrySet()) {
	      double acc2 = 0;
	      for (Map.Entry<String, Map<String, Float>> innerEntry : entry.getValue().entrySet()){
	        acc2 += innerEntry.getValue().values().stream().mapToDouble(Float::doubleValue).average().orElse(0);
	      }
	      acc += acc2/entry.getValue().entrySet().size();
	    }

	    return acc/this.record.size();
  }
  
  /* Calculate Grades Standard Deviation */
  private double calculateGradeStandardDeviation() {
	  double average = this.calculateGradeAverage();
	  double sum = 0.0;
	  int qtdNotas = 0;
	  
	  for (Map.Entry<Semester, Map<String, Map<String, Float>>> entry : this.record.entrySet()) {
	      for (Map.Entry<String, Map<String, Float>> innerEntry : entry.getValue().entrySet()){
	        for(var notas: innerEntry.getValue().values()) {
	        	sum += Math.pow(notas - average, 2);
	        	qtdNotas++;
	        }
	      }
	    }
	  
	  double stdDev = Math.sqrt(sum/(qtdNotas - 1));

	  return stdDev;
  }
  
  /* Calculate Grades Median */
  private double calculateGradeMedian() {
	  List<Float> gradeList = new ArrayList<Float>();
	  
	  for (Map.Entry<Semester, Map<String, Map<String, Float>>> entry : this.record.entrySet()) {
	      for (Map.Entry<String, Map<String, Float>> innerEntry : entry.getValue().entrySet()){
	        for(var notas: innerEntry.getValue().values()) {
	        	gradeList.add(notas);
	        }
	      }
	    }
	  
	  Collections.sort(gradeList);
	  if(gradeList.size() == 0) {
		  return 0.0;
	  }
	  int i = gradeList.size()/2;
	  if(gradeList.size() % 2 == 0) {
		  return (gradeList.get(i-1) + gradeList.get(i))/2;
	  } else {
		  return gradeList.get(i);
	  }
	 	  
  }
  

  
  /**
   * Calculate the difficulty based on the record and the difficultyGroup.
   * Should be called when changes are made to the record.
   */
  public void calculateDifficulty(){
    if (this.difficultyGroup == null){
			return;
    }

    double avg = calculateGradeAverage();

			int difficultyIndex = new DifficultyFactory().getDifficulty(this.difficultyGroup.size())
							.getDifficultyGroup(avg);
			this.difficulty = this.difficultyGroup.get(difficultyIndex);
	}

	/**
	 * Equality comparison. Although this object has an id, equality is checked on
	 * all fields.
	 */
	@Override
	public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}

			if (!(obj instanceof Question)) {
				return false;
			}

			var question = (Question) obj;

			return this.id.equals(question.id) && this.theme.equals(question.theme)
							&& this.description.equals(question.description) && this.statement.equals(question.statement)
							&& this.record.equals(question.record) && this.pvt == question.pvt
							&& this.difficulty.equals(question.difficulty);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.theme, this.description, this.statement, this.record, this.pvt,
												this.estimatedTime, this.difficulty);
	}

	/**
	 * Convert a Question to String for visualization purposes.
	 */
	@Override
	public String toString() {
		var builder = new StringBuilder();

		builder.append("Question:\n");
		builder.append("  id: " + this.id + "\n");
		builder.append("  theme: " + this.theme + "\n");
		builder.append("  desc: " + this.description + "\n");
		builder.append("  record: " + this.record + "\n");
		builder.append("  pvt: " + this.pvt + "\n");
		builder.append("  estimatedTime: " + this.estimatedTime + "\n");
		builder.append("  difficulty: " + this.difficulty + "\n");
		builder.append("  difficultyGroup: " + this.difficultyGroup + "\n");
		// TODO add choices

		if (this.statement != null) {
			builder.append("  head: " + this.statement.substring(0, Math.min(this.statement.length(), 50)) + "\n");
		}

		return builder.toString();
	}
}
