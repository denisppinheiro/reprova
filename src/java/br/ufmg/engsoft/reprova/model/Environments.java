package br.ufmg.engsoft.reprova.model;

import java.util.Optional;
/* class Enviroments */ 
public class Environments {

	private static Environments environments;
	/* String */
	private String token;
	/* port */
	private int port;
	/* difficultyGroup */
	private int difficultyGroup;
	/* enableAnswers */
	private boolean enableAnswers;
	/* enableQuestionnaires */
	private boolean enableQuestionnaires;
	/* enableEstimatedTime */
	private boolean enableEstimatedTime;
	/* enableMultipleChoice */
	private boolean enableMultipleChoice;
	/* enableQuestionStatistics */
	private boolean enableQuestionStatistics;
	
	public static final String TRUE = "true";

	/* Enviroments */ 
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
	/* Enviroments getInstance */ 
	public static Environments getInstance() {
		if (environments == null) {
			environments = new Environments();
		}
		
		return environments;
	}
	/* isEnableAnswers */ 
	public boolean isEnableAnswers() {
		return this.enableAnswers;
	}
	/* isEnableQuestionnaires */ 
	public boolean isEnableQuestionnaires() {
		return this.enableQuestionnaires;
	}

	/* isEnableEstimatedTime */ 
	public boolean isEnableEstimatedTime() {
		return this.enableEstimatedTime;
	}
	/* isEnableMultipleChoice */
	public boolean isEnableMultipleChoice() {
		return this.enableMultipleChoice;
	}
	/* isEnableQuestionStatistics */
	public boolean isEnableQuestionStatistics() {
		return this.enableQuestionStatistics;
	}

	/* getDifficultyGroup */
	public int getDifficultyGroup() {
		return this.difficultyGroup;
	}
	/* getToken */
	public String getToken() {
		return this.token;
	}
	
	public int getPort() {
		return this.port;
	}

}
