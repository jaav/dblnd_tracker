package be.virtualsushi.tick5.datatracker.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class PopularSource extends BaseEntity {

	private static final long serialVersionUID = 2576971152325801235L;



	@Column(name = "SCREENNAME")
	private String screenName;


	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((screenName == null) ? 0 : screenName.hashCode());
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
		PopularSource other = (PopularSource) obj;
		if (screenName != other.screenName)
			return false;
		return true;
	}

}
