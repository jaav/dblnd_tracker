package be.virtualsushi.tick5.datatracker.model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class GoogleSearchCachedResult extends BaseEntity {

	private static final long serialVersionUID = 1057138665142946179L;

	@Column(name = "QUERY_STRING")
	private String queryString;

	@Column(name = "RESULT_LINK")
	private String resultLink;

	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public String getResultLink() {
		return resultLink;
	}

	public void setResultLink(String resultLink) {
		this.resultLink = resultLink;
	}

}
