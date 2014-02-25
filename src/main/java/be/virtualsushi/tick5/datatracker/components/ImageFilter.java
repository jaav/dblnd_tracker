package be.virtualsushi.tick5.datatracker.components;

import java.io.File;

public interface ImageFilter {

	//void applyFilters(File imageFile, int filterset);
	void applyFancyFilters(String imageName, int filterset);
	//void applySquareFilter(File imageFile);
	void applySquareFilter(String imageFileName);

}
