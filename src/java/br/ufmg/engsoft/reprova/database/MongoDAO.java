package br.ufmg.engsoft.reprova.database;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.fields;

import java.util.ArrayList;
import java.util.Collection;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoCollection;

import br.ufmg.engsoft.reprova.mime.json.Json;
import br.ufmg.engsoft.reprova.model.ReprovaModel;
import br.ufmg.engsoft.reprova.model.ReprovaModelBuilder;

public abstract class MongoDAO<M extends ReprovaModel, B extends ReprovaModelBuilder<M>> {

	/**
	 * Logger instance.
	 */
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Json formatter.
	 */
	protected final Json json;

	/**
	 * Questions collection.
	 */
	protected final MongoCollection<Document> collection;

	/**
	 * Basic constructor.
	 * 
	 * @param db   the database, mustn't be null
	 * @param json the json formatter for the database's documents, mustn't be null
	 * @throws IllegalArgumentException if any parameter is null
	 */
	protected MongoDAO(Mongo db, Json json) {
		if (db == null) {
			throw new IllegalArgumentException("db mustn't be null");
		}

		if (json == null) {
			throw new IllegalArgumentException("json mustn't be null");
		}

		this.collection = db.getCollection(getCollectionName());

		this.json = json;
	}

	/**
	 * @return
	 */
	protected abstract String getCollectionName();

	/**
	 * @return
	 */
	protected abstract Class<B> getBuilderClass();

	/**
	 * Parse the given document.
	 * 
	 * @param document mustn't be null
	 * @throws IllegalArgumentException if any parameter is null
	 * @throws IllegalArgumentException if the given document is an invalid Question
	 */
	protected M parseDoc(Document document) {
		if (document == null) {
			throw new IllegalArgumentException("document mustn't be null");
		}

		var doc = document.toJson();

		logger.info("Fetched " + getCollectionName() + ": " + doc);

		try {
			var obj = (M) json.parse(doc, getBuilderClass()).build();

			logger.info("Parsed \" + getCollectionName() + \": " + obj);

			return obj;
		} catch (Exception e) {
			logger.error("Invalid document in database!", e);
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Get the model with the given id.
	 * 
	 * @param id the model's id in the database.
	 * @return The model, or null if no such question.
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public M get(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id mustn't be null");
		}

		var answer = this.collection.find(eq(new ObjectId(id))).map(this::parseDoc).first();

		if (answer == null) {
			logger.info("No such " + getCollectionName() + " " + id);
		}

		return answer;
	}

	protected Boolean add(Document doc, String modelId) {
		String id = modelId;
		if (id != null) {
			var result = this.collection.replaceOne(eq(new ObjectId(id)), doc);

			if (!result.wasAcknowledged()) {
				logger.warn("Failed to replace in " + getCollectionName() + " " + id);
				return false;
			}
		} else {
			this.collection.insertOne(doc);
		}

		logger.info("Stored in " + getCollectionName() + " " + doc.get("_id"));
		return true;
	}

	/**
	 * Remove the model with the given id from the collection.
	 * 
	 * @param id the model id
	 * @return Whether the given model was removed.
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public boolean remove(String id) {
		if (id == null) {
			throw new IllegalArgumentException("id mustn't be null");
		}

		var result = this.collection.deleteOne(eq(new ObjectId(id))).wasAcknowledged();

		if (result) {
			logger.info("Deleted " + getCollectionName() + " " + id);
		} else {
			logger.warn("Failed to delete " + getCollectionName() + " " + id);
		}

		return result;
	}

	/**
	 * List all the models that match the given non-null parameters. The models's
	 * statement is ommited.
	 * 
	 * @return The models in the collection, possibly empty.
	 * @throws IllegalArgumentException if there is an invalid Model
	 */
	public Collection<M> list() {
		var doc = this.collection.find();

		var result = new ArrayList<M>();

		doc.projection(fields()).map(this::parseDoc).into(result);

		return result;
	}

}
