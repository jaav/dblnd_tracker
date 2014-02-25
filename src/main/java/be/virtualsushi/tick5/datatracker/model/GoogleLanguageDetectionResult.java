package be.virtualsushi.tick5.datatracker.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class GoogleLanguageDetectionResult {

	@JsonProperty("detections")
	private List<List<GoogleLanguageDetectionResultItem>> items;

	public List<List<GoogleLanguageDetectionResultItem>> getItems() {
		return items;
	}

	public void setItems(List<List<GoogleLanguageDetectionResultItem>> items) {
		this.items = items;
	}
}
