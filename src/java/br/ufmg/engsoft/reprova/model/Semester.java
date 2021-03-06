package br.ufmg.engsoft.reprova.model;

import java.util.Map;
import java.util.Objects;
import java.util.HashMap;


/**
 * A semester class.
 * The semester is composed of an year and a reference (1 or 2).
 */
public class Semester {
  /**
   * The reference part of a semester.
   * Either 1 or 2.
   */
  public enum Reference {
    i1(1),
    i2(2);

    /* value */
    public final int value;
    Reference(int tempVal) {
      this.value = tempVal;
    }

    /**
     * The mapping of integers to Semester.Reference.
     */
    protected static final Map<Integer, Reference> VALUEMAP =
      new HashMap<Integer, Reference>(); static {
      for (var ref : Reference.values()) {
        VALUEMAP.put(ref.value, ref);
      }
    }

    /**
     * Convert a int to a Semester.Reference.
     */
    public static Reference fromInt(int tempVal) {
      Reference ref = VALUEMAP.get(Integer.valueOf(tempVal));

      if (ref == null) {
        throw new IllegalArgumentException();
      }
      return ref;
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
   * @param year  the year
   * @param ref   the reference
   * @throws IllegalArgumentException  if any parameter is null
   */
  public Semester(int year, Reference ref) {
    if (ref == null) {
      throw new IllegalArgumentException("ref mustn't be null");
    }
    this.year = year;
    this.ref = ref;
  }



  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Semester)) {
      return false;
    }
    var semester = (Semester) obj;

    return this.year == semester.year
        && this.ref == semester.ref;
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
    return String.format(
      "%d/%d",
      this.year,
      this.ref.value);
  }
}
