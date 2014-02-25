package be.virtualsushi.tick5.datatracker.model;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class GoogleLanguageDetection {

	@JsonProperty("data")
	private GoogleLanguageDetectionResult root;

	public GoogleLanguageDetectionResult getRoot() {
		return root;
	}

	public void setRoot(GoogleLanguageDetectionResult root) {
		this.root = root;
	}
}
