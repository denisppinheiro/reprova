package br.ufmg.engsoft.reprova.model;

import java.util.Objects;

/**
 * A semester class. The semester is composed of an year and a reference (1 or
 * 2).
 */
public class Semester {
	/**
	 * The reference part of a semester. Either 1 or 2.
	 */
	public enum Reference {
		S1(1), S2(2);

		public final int value;

		Reference(int i) {
			this.value = i;
		}

		/**
		 * Convert a int to a Semester.Reference.
		 */
		public static Reference fromInt(int i) {
			for (Reference r : values()) {
				if (r.value == i) {
					return r;
				}
			}
			throw new IllegalArgumentException();
		}
	}

	/**
	 * The year of the semester.
	 */
	public final int year;
	/**
	 * The reference of the semester.
	 */
	public final Reference ref;

	/**
	 * Construct a Semester.
	 * 
	 * @param year the year
	 * @param ref  the reference
	 * @throws IllegalArgumentException if any parameter is null
	 */
	public Semester(int year, Reference ref) {
		if (ref == null)
			throw new IllegalArgumentException("ref mustn't be null");

		this.year = year;
		this.ref = ref;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (!(obj instanceof Semester))
			return false;

		var semester = (Semester) obj;

		return this.year == semester.year && this.ref == semester.ref;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.year, this.ref);
	}

	/**
	 * Convert a Semester to String for visualization purposes.
	 */
	@Override
	public String toString() {
		return String.format("%d/%d", this.year, this.ref.value);
	}
}
