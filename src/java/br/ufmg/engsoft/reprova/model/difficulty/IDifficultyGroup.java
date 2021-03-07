package br.ufmg.engsoft.reprova.model.difficulty;

import java.util.List;

public interface IDifficultyGroup {
	public int getDifficultyGroup(double avg);

	public List<String> getDifficulties();
}