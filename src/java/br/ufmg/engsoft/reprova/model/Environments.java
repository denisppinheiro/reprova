package br.ufmg.engsoft.reprova.model;

import java.util.Optional;

public class Environments {

	private static Environments environments;
	
	private String token;
	private int port;
	
	private int difficultyGroup;
	
	private boolean enableAnswers;
	private boolean enableQuestionnaires;
	private boolean enableEstimatedTime;
	private boolean enableMultipleChoice;
	private boolean enableQuestionStatistics;
	
	public static final String TRUE = "true";

	private Environments() {		
		Optional<String> enableAnswersEnv = Optional.ofNullable(System.getenv("ENABLE_ANSWERS"));
		enableAnswersEnv.ifPresentOrElse(
			enableAnswers -> this.enableAnswers = enableAnswers.toLowerCase().equals(TRUE),
			() -> this.enableAnswers = false
		);
		
		Optional<String> enableQuestionStatisticsEnv = Optional.ofNullable(System.getenv("ENABLE_STATISTICS"));
		enableQuestionStatisticsEnv.ifPresentOrElse(
				enableQuestionStatistics -> this.enableQuestionStatistics = enableQuestionStatistics.toLowerCase().equals(TRUE),
			() -> this.enableQuestionStatistics = false
		);
		
		Optional<String> enableQuestionnairesEnv = Optional.ofNullable(System.getenv("ENABLE_QUESTIONNAIRES"));
		enableQuestionnairesEnv.ifPresentOrElse(
			enableQuestionnaires -> this.enableQuestionnaires = enableQuestionnaires.toLowerCase().equals(TRUE),
			() -> this.enableQuestionnaires = false
		);
								
		Optional<String> enableEstimatedTimeEnv = Optional.ofNullable(System.getenv("ENABLE_ESTIMATED_TIME"));
		enableEstimatedTimeEnv.ifPresentOrElse(
			enableEstimatedTime -> this.enableEstimatedTime = enableEstimatedTime.toLowerCase().equals(TRUE),
			() -> this.enableEstimatedTime = false
		);
		
		Optional<String> enableMultipleChoiceEnv = Optional.ofNullable(System.getenv("ENABLE_MULTIPLE_CHOICE"));
		enableMultipleChoiceEnv.ifPresentOrElse(
			enableMultipleChoice -> this.enableMultipleChoice = enableMultipleChoice.toLowerCase().equals(TRUE),
			() -> this.enableMultipleChoice = false
		);

		Optional<String> envDifficultyGroup = Optional.ofNullable(System.getenv("DIFFICULTY_GROUP"));
		envDifficultyGroup.ifPresentOrElse(
			difficultyGroup -> this.difficultyGroup = Integer.parseInt(envDifficultyGroup.get()),
			() -> this.difficultyGroup = 0
		);
		
		this.port = Integer.parseInt(System.getenv("PORT"));
		
		this.token = System.getenv("REPROVA_TOKEN");
	}
	
	public static Environments getInstance() {
		if (environments == null) {
			environments = new Environments();
		}
		
		return environments;
	}
	
	public boolean isEnableAnswers() {
		return this.enableAnswers;
	}
	
	public boolean isEnableQuestionnaires() {
		return this.enableQuestionnaires;
	}

	public boolean isEnableEstimatedTime() {
		return this.enableEstimatedTime;
	}
	
	public boolean isEnableMultipleChoice() {
		return this.enableMultipleChoice;
	}
	
	public boolean isEnableQuestionStatistics() {
		return this.enableQuestionStatistics;
	}

	public int getDifficultyGroup() {
		return this.difficultyGroup;
	}
	
	public String getToken() {
		return this.token;
	}
	
	public int getPort() {
		return this.port;
	}

}
