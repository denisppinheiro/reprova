package br.ufmg.engsoft.reprova.model.difficulty;

import java.util.List;
import java.util.Arrays;

/* DifficultyGroup3 */
public class DifficultyGroup3 implements IDifficultyGroup{

  /* getDifficultyGroup */
  @Override
  public int getDifficultyGroup(double avg){
    if (avg < 33.3) {return 0;}
    if (avg < 66.6) {return 1;}
    return 2;
  };

  /* getDifficulties */
  @Override
  public List<String> getDifficulties(){
    String[] group = {"Hard", "Average", "Easy"};
    return Arrays.asList(group);
  };
}