package be.virtualsushi.tick5.datatracker.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class Language extends BaseEntity {

	private static final long serialVersionUID = 2576971152325801234L;

	@Column(name = "WORD")
	private String word;

	@Column(name = "CULTURE")
	private String culture;

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getCulture() {
		return culture;
	}

	public void setCulture(String culture) {
		this.culture = culture;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Language other = (Language) obj;
		if (word != other.word)
			return false;
		return true;
	}

}
