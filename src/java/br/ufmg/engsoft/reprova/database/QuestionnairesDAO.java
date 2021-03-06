package br.ufmg.engsoft.reprova.database;

import java.util.ArrayList;
import java.util.Map;
import java.util.Collection;
import java.util.stream.Collectors;

import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.fields;

import org.bson.Document;
import org.bson.types.ObjectId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Environments;
import br.ufmg.engsoft.reprova.model.Questionnaire;

/**
 * DAO for Questionnaire class on mongodb.
 */
public class QuestionnairesDAO extends MongoDAO<Questionnaire, Questionnaire.Builder> {

	@Override
	protected String getCollectionName() {
		return "questionnaires";
	}

	@Override
	protected Class<Questionnaire.Builder> getBuilderClass() {
		return Questionnaire.Builder.class;
	}

	/**
	 * Basic constructor.
	 * 
	 * @param db   the database, mustn't be null
	 * @param json the json formatter for the database's documents, mustn't be null
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public QuestionnairesDAO(Mongo db, Json json) {
		super(db, json);
	}

	/**
	 * Adds or updates the given questionnaire in the database. If the given
	 * questionnaire has an id, update, otherwise add.
	 * 
	 * @param questionnaire the questionnaire to be stored
	 * @return Whether the questionnaire was successfully added.
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public boolean add(Questionnaire questionnaire) {
		if (questionnaire == null) {
			throw new IllegalArgumentException("questionnaire mustn't be null");
		}

		ArrayList<Document> questions = new ArrayList<Document>();
		for (var question : questionnaire.questions) {
			Map<String, Object> record = null;
			if (question.record != null) {
				record = question.record // Convert the keys to string,
						.entrySet() // and values to object.
						.stream().collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue));
			}

			Document doc = new Document().append("theme", question.theme).append("description", question.description)
					.append("statement", question.statement)
					.append("record", record == null ? null : new Document(record)).append("pvt", question.pvt);

			if (Environments.getInstance().getEnableEstimatedTime()) {
				doc = doc.append("estimatedTime", question.estimatedTime);
			}

			if (Environments.getInstance().getEnableMultipleChoice()) {
				doc = doc.append("choices", question.getChoices());
			}

			if (Environments.getInstance().getDifficultyGroup() != 0) {
				doc = doc.append("difficulty", question.difficulty);
			}

			questions.add(doc);
		}

		Document doc = new Document().append("averageDifficulty", questionnaire.averageDifficulty).append("questions",
				questions);

		if (Environments.getInstance().getEnableEstimatedTime()) {
			doc = doc.append("totalEstimatedTime", questionnaire.getTotalEstimatedTime());
		}

		return super.add(doc, questionnaire.getId());
	}

	/**
	 * Remove the questionnaire with the given id from the collection.
	 * 
	 * @param id the questionnaire id
	 * @return Whether the given questionnaire was removed.
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public boolean remove(String id) {
		if (id == null)
			throw new IllegalArgumentException("id mustn't be null");

		var result = this.collection.deleteOne(eq(new ObjectId(id))).wasAcknowledged();

		if (result)
			logger.info("Deleted questionnaire " + id);
		else
			logger.warn("Failed to delete questionnaire " + id);

		return result;
	}

}
