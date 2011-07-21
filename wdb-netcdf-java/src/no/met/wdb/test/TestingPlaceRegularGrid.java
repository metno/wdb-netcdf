package no.met.wdb.test;

import no.met.wdb.PlaceRegularGrid;

public class TestingPlaceRegularGrid extends PlaceRegularGrid {

	public TestingPlaceRegularGrid() {
		this("POLYGON((0 0,0.1 0,0.1 0.1,0 0.1,0 0))", "test grid, regular", 2, 2, 0.1f, 0.1f, 0f, 0f, "+proj=longlat +a=6367470.0 +towgs84=0,0,0 +no_defs");
	}
	
	public TestingPlaceRegularGrid(
			String placeGeometry,
			String placeName,
			int numberX,
			int numberY,
			float incrementX,
			float incrementY,
			float startX,
			float startY,
			String projDefinition) {
		
		super(placeGeometry, placeName, numberX, numberY, incrementX, incrementY, startX, startY, projDefinition);
	}
}
