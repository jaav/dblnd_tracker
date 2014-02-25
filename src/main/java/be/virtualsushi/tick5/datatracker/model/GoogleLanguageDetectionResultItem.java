package be.virtualsushi.tick5.datatracker.model;

import org.codehaus.jackson.annotate.JsonProperty;

public class GoogleLanguageDetectionResultItem {

	@JsonProperty("language")
	private String language;

	@JsonProperty("confidence")
	private double confidence;

	@JsonProperty("isReliable")
	private boolean isReliable;

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public boolean isReliable() {
		return isReliable;
	}

	public void setReliable(boolean reliable) {
		isReliable = reliable;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

}
