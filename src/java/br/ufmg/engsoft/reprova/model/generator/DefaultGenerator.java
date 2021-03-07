package br.ufmg.engsoft.reprova.model.generator;

import java.util.ArrayList;
import java.util.Collections;

import br.ufmg.engsoft.reprova.model.Question;
import br.ufmg.engsoft.reprova.model.Questionnaire;
import br.ufmg.engsoft.reprova.database.QuestionsDAO;

/*
* DefaultGenerator
*/
public class DefaultGenerator implements IQuestionnaireGenerator{

  /* Generate */
  @Override
  public Questionnaire generate(QuestionsDAO questionsDAO, String averageDifficulty, int questionsCount, int totalEstimatedTime){
    if (totalEstimatedTime == 0){
      totalEstimatedTime = Questionnaire.DEFAULT_ESTIMATED_TIME_MINUTES;
    }
    if (questionsCount == 0){
      questionsCount = Questionnaire.DEFAULT_QUESTIONS_COUNT;
    }

    ArrayList<Question> questions = new ArrayList<Question>();
    ArrayList<Question> allQuestions = new ArrayList<Question>(questionsDAO.list(null, null));

    Collections.shuffle(allQuestions);
    for (int i = 0; i < questionsCount; i++){
      if (i >=  allQuestions.size()){
        break;
      }

      questions.add(allQuestions.get(i));
    }

    return new Questionnaire.Builder()
                .setAverageDifficulty(averageDifficulty)
                .setTotalEstimatedTime(totalEstimatedTime)
                .setQuestions(questions)
                .build();
  };
}
