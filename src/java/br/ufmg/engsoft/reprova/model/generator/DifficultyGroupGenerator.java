package br.ufmg.engsoft.reprova.model.generator;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.List;

import br.ufmg.engsoft.reprova.model.Environments;
import br.ufmg.engsoft.reprova.model.Question;
import br.ufmg.engsoft.reprova.model.Questionnaire;
import br.ufmg.engsoft.reprova.database.QuestionsDAO;
import br.ufmg.engsoft.reprova.model.difficulty.DifficultyFactory;

/*
* The type DifficultyGroupGenerator
*/
public class DifficultyGroupGenerator implements IQuestionnaireGenerator{

  /**
   * Auxiliary function that returns a list of questions with the given difficulty.
   * Attempts to fill the returned list with the given count of questions.
   */
  private List<Question> getQuestionsOfDifficulty(ArrayList<Question> allQuestions, int count, String difficulty){
    List<Question> questions = new ArrayList<Question>();
    List<Question> questionsOfDifficulty = allQuestions.stream()
                                        .filter(q -> q.difficulty.equals(difficulty))
                                        .collect(Collectors.toList());

    Collections.shuffle(questionsOfDifficulty);
    for (int i = 0; i < count; i++){
      if (i >= questionsOfDifficulty.size()){
        break;
      }
      questions.add(questionsOfDifficulty.get(i));
    }

    return questions;
  }

  /**
   * Generate a new Quesitonnaire based on the parameters.
   * Selects a collection of questions the best fit the parameters.
   * Calls the Questionnaire's Builder.
   */
  @Override
  public Questionnaire generate(QuestionsDAO questionsDAO, String averageDifficulty, int questionsCount, int totalEstimatedTime){
    Environments environments = Environments.getInstance();
    int valueDifficultyGroup = environments.getDifficultyGroup();
    List<String> difficultyGroup = new DifficultyFactory()
                                .getDifficulty(valueDifficultyGroup)
                                .getDifficulties();

    if (averageDifficulty == null){
      averageDifficulty = "Average";
    } else {
      if (!difficultyGroup.contains(averageDifficulty)){
        throw new IllegalArgumentException("invalid average difficulty");
      }
    }
    if (totalEstimatedTime == 0){
      totalEstimatedTime = Questionnaire.DEFAULT_ESTIMATED_TIME_MINUTES;
    }
    if (questionsCount == 0){
      questionsCount = Questionnaire.DEFAULT_QUESTIONS_COUNT;
    }

    ArrayList<Question> questions = new ArrayList<Question>();
    ArrayList<Question> allQuestions = new ArrayList<Question>(questionsDAO.list(null, null));

    if (allQuestions.size() <= questionsCount){
      for(Question question : allQuestions){
        questions.add(question);
      }
    } else {
      int averageQuestionsCount = (int)Math.ceil(questionsCount * 0.5);
      List<Question> averageQuestions = getQuestionsOfDifficulty(allQuestions, averageQuestionsCount, averageDifficulty);
      questions.addAll(averageQuestions);

      int remainingQuestionsCount = questionsCount - questions.size();
      int easierQuestionsCount = remainingQuestionsCount % 2 == 1 ? remainingQuestionsCount/2 + 1 : remainingQuestionsCount/2;
      int harderQuestionsCount = remainingQuestionsCount - easierQuestionsCount;

      int easierDifficultyIndex = difficultyGroup.indexOf(averageDifficulty);
      int harderDifficultyIndex = easierDifficultyIndex;
      while (remainingQuestionsCount > 0){
        if (harderDifficultyIndex == 0){
          easierQuestionsCount += harderQuestionsCount;
          harderQuestionsCount = -1;
          harderDifficultyIndex = -1;
        } else {
          harderDifficultyIndex--;
        }
        
        if (easierDifficultyIndex == difficultyGroup.size()-1){
          harderQuestionsCount += easierQuestionsCount;
          easierQuestionsCount = -1;
          easierDifficultyIndex = -1;
        } else {
          easierDifficultyIndex++;
        }
        
        if (easierDifficultyIndex == -1 && harderDifficultyIndex == -1) {
        	averageQuestions = getQuestionsOfDifficulty(allQuestions, remainingQuestionsCount, "Average");
          remainingQuestionsCount -= averageQuestions.size();
          questions.addAll(averageQuestions);
        }
        else {
	        if (harderQuestionsCount != -1){
	          List<Question> harderQuestions = getQuestionsOfDifficulty(allQuestions, harderQuestionsCount, difficultyGroup.get(harderDifficultyIndex));
	          harderQuestionsCount -= harderQuestions.size();
	          remainingQuestionsCount -= harderQuestions.size();
	          questions.addAll(harderQuestions);
	        }
	
	        if (easierQuestionsCount != -1){
	          List<Question> easierQuestions = getQuestionsOfDifficulty(allQuestions, easierQuestionsCount, difficultyGroup.get(easierDifficultyIndex));
	          easierQuestionsCount -= easierQuestions.size();
	          remainingQuestionsCount -= easierQuestions.size();
	          questions.addAll(easierQuestions);
	        }
        }
      }
    }

    return new Questionnaire.Builder()
                .setAverageDifficulty(averageDifficulty)
                .setTotalEstimatedTime(totalEstimatedTime)
                .setQuestions(questions)
                .build();
  }
}
