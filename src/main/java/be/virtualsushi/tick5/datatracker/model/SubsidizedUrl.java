package be.virtualsushi.tick5.datatracker.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class SubsidizedUrl extends BaseEntity {

	private static final long serialVersionUID = 2576971152325801237L;

	@Column(name = "WORD")
	private String word;

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
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
		SubsidizedUrl other = (SubsidizedUrl) obj;
		if (word != other.word)
			return false;
		return true;
	}

}
