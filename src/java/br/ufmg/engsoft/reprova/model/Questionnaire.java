package br.ufmg.engsoft.reprova.model;

import java.util.ArrayList;

import br.ufmg.engsoft.reprova.database.QuestionsDAO;
import br.ufmg.engsoft.reprova.model.generator.EstimatedTimeCalculator;
import br.ufmg.engsoft.reprova.model.generator.QuestionnaireGeneration;

/**
 * The Questionnaire type
 */
public class Questionnaire extends ReprovaModel {

	public static final int DEFAULT_ESTIMATED_TIME_MINUTES = 60;
	public static final int DEFAULT_QUESTIONS_COUNT = 5;

	/**
	 * The list of Questions in the Questionnaire
	 */
	public final ArrayList<Question> questions;
	/**
	 * The Questionnaire's average difficulty.
	 */
	public final String averageDifficulty;
	/**
	 * The Questionnaire's total estimated time.
	 */
	private int totalEstimatedTime;
	
	/**
	 * Protected constructor, should only be used by the builder.
	 */
	protected Questionnaire(String id, String averageDifficulty, int totalEstimatedTime,
			ArrayList<Question> questions) {
		this.id = id;
		this.questions = questions;
		this.averageDifficulty = averageDifficulty;
		this.totalEstimatedTime = totalEstimatedTime;
	}
	
	public void setTotalEstimatedTime(int totalEstimatedTime) {
		this.totalEstimatedTime = totalEstimatedTime;
	}
	
	public int getTotalEstimatedTime() {
		return totalEstimatedTime;
	}

	public static class Generator {
		protected String id;
		protected String averageDifficulty;
		protected int totalEstimatedTime;
		protected int questionsCount;

		public Generator id(String id) {
			this.id = id;
			return this;
		}

		public Generator averageDifficulty(String averageDifficulty) {
			this.averageDifficulty = averageDifficulty;
			return this;
		}

		public Generator totalEstimatedTime(int totalEstimatedTime) {
			this.totalEstimatedTime = totalEstimatedTime;
			return this;
		}

		public Generator questionsCount(int questionsCount) {
			this.questionsCount = questionsCount;
			return this;
		}

		public Questionnaire generate(QuestionsDAO questionsDAO) {
			QuestionnaireGeneration generationChain = new QuestionnaireGeneration();

			Environments environments = Environments.getInstance();
			boolean hasEstimatedTime = environments.getEnableEstimatedTime();

			if (hasEstimatedTime) {
				generationChain.setNext(new EstimatedTimeCalculator());
			}

			return generationChain.generate(questionsDAO, this.averageDifficulty, this.questionsCount,
					this.totalEstimatedTime);
		}
	}

	public static class Builder implements ReprovaModelBuilder<Questionnaire> {
		protected String id;
		protected String averageDifficulty;
		protected int totalEstimatedTime;
		protected ArrayList<Question> questions;

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder averageDifficulty(String averageDifficulty) {
			this.averageDifficulty = averageDifficulty;
			return this;
		}

		public Builder totalEstimatedTime(int totalEstimatedTime) {
			this.totalEstimatedTime = totalEstimatedTime;
			return this;
		}

		public Builder questions(ArrayList<Question> questions) {
			this.questions = questions;
			return this;
		}

		/**
		 * Build the Questionnaire;
		 * 
		 * @throws IllegalArgumentException if any parameter is invalid
		 */

		public Questionnaire build() {
			if (this.questions == null) {
				this.questions = new ArrayList<Question>();
			} else {
				for (var question : this.questions) {
					if (question == null) {
						throw new IllegalArgumentException("question mustn't be null");
					}
				}
			}

			return new Questionnaire(this.id, this.averageDifficulty, this.totalEstimatedTime, this.questions);
		}
	}

	/**
	 * Convert a Question to String for visualization purposes.
	 */
	@Override
	public String toString() {
		var builder = new StringBuilder();

		builder.append("Questionnaire:\n");
		builder.append("  id: " + this.id + "\n");
		builder.append("  averageDifficulty: " + this.averageDifficulty + "\n");
		builder.append("  totalEstimatedTime: " + this.totalEstimatedTime + "\n");
		builder.append("  questions:\n");
		for (var question : this.questions) {
			builder.append("    id: " + question.id + "\n");
			builder.append("      theme: " + question.theme + "\n");
			builder.append("      desc: " + question.description + "\n");
			builder.append("      record: " + question.record + "\n");
			builder.append("      pvt: " + question.pvt + "\n");
			builder.append("      difficulty: " + question.difficulty + "\n");

			if (question.statement != null) {
				builder.append(
						"  head: " + question.statement.substring(0, Math.min(question.statement.length(), 50)) + "\n");
			}
		}

		return builder.toString();
	}
}
