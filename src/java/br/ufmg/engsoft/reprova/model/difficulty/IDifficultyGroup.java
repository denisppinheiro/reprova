package br.ufmg.engsoft.reprova.model.difficulty;

import java.util.List;

/* The type IDifficultGroup */
public interface IDifficultyGroup{
  /* getDifficultyGroup */
  public int getDifficultyGroup(double avg);
  
  /* getDifficulties */
  public List<String> getDifficulties();
}
