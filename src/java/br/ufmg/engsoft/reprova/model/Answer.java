package br.ufmg.engsoft.reprova.model;

public class Answer extends ReprovaModel {

	/**
	 * Reference to the parent question
	 */
	private String questionId;

	/**
	 * Description of the answer. Mustn't be null nor empty.
	 */
	private final String description;

	/**
	 * Attachment to the answer. May be null or empty.
	 */
	private final String attachment;

	/**
	 * Whether the question is private.
	 */
	private final boolean pvt;

	public String getDescription() {
		return this.description;
	}

	public String getAttachment() {
		return this.attachment;
	}

	public boolean getPvt() {
		return this.pvt;
	}

	public String getQuestionId() {
		return this.questionId;
	}

	/**
	 * Protected constructor, should only be used by the builder.
	 */
	protected Answer(String description, String attachment, boolean pvt, String questionId) {
		this.description = description;
		this.attachment = attachment;
		this.pvt = pvt;
		this.questionId = questionId;
	}

	public static class Builder implements ReprovaModelBuilder<Answer> {
		protected String id;
		protected String description;
		protected boolean pvt;
		protected String questionId;

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder pvt(boolean pvt) {
			this.pvt = pvt;
			return this;
		}

		public Builder questionId(String questionId) {
			this.questionId = questionId;
			return this;
		}

		public Answer build() {
			return new Answer(this.id, this.description, this.pvt, this.questionId);
		}
	}
}
