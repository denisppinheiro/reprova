package br.ufmg.engsoft.reprova.database;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bson.Document;

import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.Answer;

/**
 * DAO for Question class on mongodb.
 */
public class AnswersDAO extends MongoDAO<Answer, Answer.Builder> {

	/**
	 * Basic constructor.
	 * 
	 * @param db   the database, mustn't be null
	 * @param json the json formatter for the database's documents, mustn't be null
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public AnswersDAO(Mongo db, Json json) {
		super(db, json);
	}

	@Override
	protected String getCollectionName() {
		return "answers";
	}

	@Override
	protected Class<Answer.Builder> getBuilderClass() {
		return Answer.Builder.class;
	}

	/**
	 * List all the answers that match the given non-null parameters. The question's
	 * statement is ommited.
	 * 
	 * @param theme the expected theme, or null
	 * @param pvt   the expected privacy, or null
	 * @return The answers in the collection that match the given parameters,
	 *         possibly empty.
	 * @throws IllegalArgumentException if there is an invalid Question
	 */
	public Collection<Answer> list(String questionId, Boolean pvt) {
		var filters = Arrays
				.asList(questionId == null ? null : eq("questionId", questionId), pvt == null ? null : eq("pvt", pvt))
				.stream().filter(Objects::nonNull) // mongo won't allow null filters.
				.collect(Collectors.toList());

		var doc = filters.isEmpty() // mongo won't take null as a filter.
				? this.collection.find()
				: this.collection.find(and(filters));

		var result = new ArrayList<Answer>();

		doc.projection(fields(exclude("statement"))).map(this::parseDoc).into(result);

		return result;
	}

	/**
	 * Adds or updates the given answer in the database. If the given answer has an
	 * id, update, otherwise add.
	 * 
	 * @param answer   the answer to be stored
	 * @param question the question for which the answer must be stored
	 * @return Whether the answer was successfully added.
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public boolean add(Answer answer, String questionId) {
		if (answer == null) {
			throw new IllegalArgumentException("answer mustn't be null");
		}

		if (questionId == null) {
			throw new IllegalArgumentException("questionId must be passed");
		}

		Document doc = new Document().append("description", answer.getDescription()).append("pvt", answer.getPvt())
				.append("questionId", questionId);

		return super.add(doc, answer.getId());
	}

}
