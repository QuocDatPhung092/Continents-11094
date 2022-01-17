import java.io.*;
import java.util.*;

/*
	By Quoc Dat Phung (300164087) qphun092@uottawa.ca
*/

/*
java Main < testContinents1.txt > outputPartATestContinents1.txt
java Main partb < testContinents1.txt > outputPartBTestContinents1.txt
java Main < testContinentsBig.txt > outputPartATestContinentsBig.txt
java Main partb < testContinentsBig.txt > outputPartBTestContinentsBig.txt
*/

class Main{
	public static void main (String[] args) {
		try {
			boolean partB = false;
			//Check if args specifiy Part B
			if (args.length > 0) {
				if (args[0].toString().toLowerCase().trim().equals("partb")) {
					partB = true;
				}
			}

			//_____________________Step 1: Scan all maps_______________________
			Scanner inputScanner = new Scanner(System.in);
			//create a linked list of al maps
			LinkedList<Map> allMaps = new LinkedList<Map>();
			//scan all maps
			while (inputScanner.hasNext()){
				//get m and n
				int m_input = Integer.parseInt(inputScanner.next());
				int n_input = Integer.parseInt(inputScanner.next());
				String[][] map = new String[m_input][n_input];
				//populate map
				for (int row = 0; row < m_input; row++){
					//get row then convert to array
					String line = inputScanner.next();
					int index = 0;
					//put content into row
					for (int column = 0; column < n_input; column++){
						map[row][column] = String.valueOf(line.charAt(index));
						index++;
					}
				}
				//get x and y
				int x_input = Integer.parseInt(inputScanner.next());
				int y_input = Integer.parseInt(inputScanner.next());
				String landStatus = map[x_input][y_input];
				//create map
				Map mapObject = new Map(map, landStatus, m_input, n_input, x_input, y_input);
				//add map into allMaps
				allMaps.add(mapObject);
			}
			
			/*
			//Uncomment to print all maps
			//System.out.println("\n************* Step 1: Get all maps from file *************\n");
			for (int i = 0; i < allMaps.size(); i++){
				//System.out.println(">> Map " + i + " <<");
				Map mapToPrint = allMaps.get(i);
				LinkedList<Point> landCoordinates = mapToPrint.getLandCoordinates();
				mapToPrint.printMap();
				//System.out.println("Land Coordinates(" + landCoordinates.size() + "): " + landCoordinates);
				//System.out.println();
			}
			*/
			

			//_____________________Step 2: BFS all land points, record continents______________
			//System.out.println("\n************* Step 2: BFS all land points, record continent sizes *************\n");
			for (Map mapObject : allMaps){
				//get the String[][] map from mapObject
				String[][] map = mapObject.getMap(); 
				//if the map's number of rows or columns are 0, then just print 0
				if (mapObject.getM() == 0 || mapObject.getN() == 0){
					//System.out.print("Result: ");
					System.out.print("0");
					if (partB){
						System.out.print(" -1 ");
					}
					System.out.println();
					continue;
				}
				//get land coordinates from mapObject
				LinkedList<Point> landCoordinates = mapObject.getLandCoordinates();
				//get the first point which is MiJid's home
				int homeX = mapObject.getHomeX();
				int homeY = mapObject.getHomeY();
				String landStatus = map[homeX][homeY];
				LinkedList<Point> miJidHome = new LinkedList<Point>();
				miJidHome.add(landCoordinates.getFirst());
				//Mark MiJid's home with "r" for "residing"
				//System.out.println("Land status to search home: " + landStatus);
				//System.out.println(miJidHome);
				BreadthFirstSearch homeBFS = new BreadthFirstSearch(map, homeX , homeY, miJidHome, landStatus, "F");
				//remove mijid's home coordinate from land coordinates
				landCoordinates.removeFirst();
				//Mark the rest of the coordinates with "a" for available to conquer.
				//System.out.println("Land status to search continent: " + landStatus);
				//System.out.println(landCoordinates);
				BreadthFirstSearch conquerableBFS = new BreadthFirstSearch(map, homeX , homeY, landCoordinates, landStatus, "T");
				//get the sizes of the available continents to conquer
				LinkedList<Integer> availableSizes = conquerableBFS.getSizeOfContinents();
				//After BFS, print out the map to check visually
				//mapObject.printMap();
				//System.out.println(availableSizes);
				//get the maximum of sizes (biggest continent)
				//System.out.print("Result: ");
				if (!availableSizes.isEmpty()){
					int maxSize = Collections.max(availableSizes);
					System.out.print(maxSize);

					if (partB){
						if (maxSize <= 0){
							System.out.print(" -1");
						} else {
							Point northWest = conquerableBFS.getNorthWestMostPoint();
							//System.out.println(northWest);
							FarthestDistance bfsDistance = new FarthestDistance(mapObject.getMap(), northWest);
							System.out.print(" " + bfsDistance.getFarthestDistance());
						}	
					}

					System.out.println();

				} else {
					System.out.print("0");

					if (partB){
						System.out.print(" -1");
					}

					System.out.println();
				}
				//System.out.println();
				
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}

	}

}


class Point {

	//x is row, y is column
	int x, y;

	Point(int x, int y){
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString(){
		return "(" + x + ", " + y + ")";
	}

	int getX(){
		return x;
	}
	int getY(){
		return y;
	}

	public static void main (String[] args){
	}
}

class Map {
	String[][] map;

	//m for rows, n for columns
	//x (row) and y (column) are the coordinates where MiJid resides (his home)
	int m, n, x, y;

	String landStatus;
	LinkedList<Point> landCoordinates = new LinkedList<Point>();

	Map(String[][] mapInput, String landStatus_input, int m, int n, int x, int y) {
		this.m = m;
		this.n = n;
		this.x = x;
		this.y = y;
		this.landStatus = landStatus_input;
		Point home = new Point(x, y); // MiJid's home (land)

		//populate map first then add home into coordinate system
		map = new String[m][n];
		for (int row = 0; row < m; row++) {
			for (int column = 0; column < n; column++) {
				String value = mapInput[row][column];
				//check if the value is "l" for "land"
				//if we are on MiJid's home
				if (row == x && column == y) {
					//do nothing (don't add this land into the coordinate system yet)
				} else {
					//if not MiJid's home but is land, then add this land
					if(value.equals(landStatus)) {
						Point land = new Point(row, column);
						landCoordinates.add(land);
					}
				}
				//put the value into map
				map[row][column] = value;
			}
		}
		//now add MiJid's home coordinate into the beginning of landCoordinates
		landCoordinates.addFirst(home);
	}

	public static void main (String[] args){
	}

	void printMap(){
		System.out.print(m + " ");
		System.out.println(n);
		for (int row = 0; row < m; row++){
			for (int column = 0; column < n; column++){
				System.out.print(map[row][column]);
			}
			System.out.println();
		}
		System.out.print(x + " ");
		System.out.println(y + "\n");
	}

	String[][] getMap(){
		return map;
	}

	LinkedList<Point> getLandCoordinates(){
		//return copy
		LinkedList<Point> copy = new LinkedList<Point>();
		for (Point p : landCoordinates){
			copy.add(p);
		}
		return copy;
	}

	int getM(){
		return m;
	}

	int getN(){
		return n;
	}

	int getHomeX(){
		return x;
	}

	int getHomeY(){
		return y;
	}

}

class BreadthFirstSearch{

	LinkedList<Integer> sizeOfContinents = new LinkedList<Integer>();
	LinkedList<Point> northWestPoints = new LinkedList<Point>();

	private Point northWestMost;
	private LinkedList<Point> bfsqueue;
	private String[][] map;
	private int m, n;
	private String c, landStatus;

	/*
		Bread First Search
		Accepts parameters map, land coordinates_input, and String c

		for each land in land coordinate

		Check land on map

			if the land is visited (not "l"), move on

			if the land is unvisited (labeled "l" for land)
			 - perform bfs, mark unvisited land with c

	*/

	BreadthFirstSearch(String[][] map_input, int homeX, int homeY, LinkedList<Point> landCoordinates_input, String landStatus_input, String c_input){
		//check null
		if (map_input == null){
			throw new NullPointerException("Map input is null!");
		}
		if (landCoordinates_input == null){
			throw new NullPointerException("Land Coordinates input is null!");
		}
		if (c_input == null){
			throw new NullPointerException("Input String is null!");
		}
		//check if there are no land coordinates linked list is empty
		if (landCoordinates_input.size() == 0){
			sizeOfContinents.add(0); //this means there's no land on this continent, so size is 0;
			return;
			//throw new IllegalStateException("There are no land coordinates to process!");
		}

		this.map = map_input;
		this.c = c_input;
		this.landStatus = landStatus_input; //get the String that represents land
		//number of rows
		this.m = map.length;
		//number of columns
		this.n = map[0].length;

		//System.out.println("_____________________________________________________");
		//System.out.println(">> Breadth First Search for " + landCoordinates_input);
		//System.out.println("Map num rows: " + m + ", num cols: " + n);

		//For each land, check to see if visited or unvisited
		for (Point land : landCoordinates_input){
			//Check land on map
			String mapValue = getMapValue(land.getX(), land.getY());
			//if the land is unvisited (is "l")
			if (mapValue.equals(landStatus)){
				//System.out.println("*********Input Land " + land + " = '" + mapValue + "'. Perform BFS since NOT visited!");
				int continentSize = perform_bfs(map, land, c);
				sizeOfContinents.add(continentSize);
				//System.out.println("Current list sizeOfContinents: " + sizeOfContinents);
			} else {
				//System.out.println("*********Input Land " + land + " = '" + mapValue + "'. Already Visited!");
			}
			//ignore if land is already visited (mapValue isn't 'l')
		}

	}

	/*
		Perform BFS (change every "l" to c)
		accepts map (to make direct changes), one land coordinate, and String c

		add this land coordinate to bfs queue

		continentSize = 1;

		1. while bfs queue is not empty

			get land from bfs queue, marked visited
			

			Check north, south, east, and west, 

		   if unvisited (value 'l')

		   - add BFS queue
		   - continentSize++;
		   - mark map value coordinate with c
		   - removeFirst because already visited

		continent size if c is 'r' don't add it to sizeOfContinents
	*/
	private int perform_bfs(String[][] map, Point land_input, String c) {
		//initialize continent (discovered continent with one known land coordinate)
		int continentSize = 0;
		//on this new continent, assume that the input land is the northWestMost point
		northWestMost = new Point(land_input.getX(), land_input.getY());
		//initialize queue for bfs
		bfsqueue = new LinkedList<Point>();
		//add land coordinate to bfs queue
		bfsqueue.add(land_input);
		//mark this land coordinate to visited (set to c)
		setMapValue(land_input.getX(), land_input.getY(), c);
		continentSize++;

		//System.out.println("We discovered a land " + land_input + " on a new continent!");
		//peform bfs using queue
		while (!bfsqueue.isEmpty()){
			//System.out.println("\n----------Continent size (Before): " + continentSize);
			//get land from bfs queue
			Point land = bfsqueue.getFirst();
			//System.out.println("Queue: " + bfsqueue);
			//System.out.println(land + "'s value, now: " + map[land.getX()][land.getY()]);
			//check north, south, east, west
			//if they are 'l' (not visited before), then we visit them by changing their value to c
			//then we increment continent size (means we discovered more land on this continent)
			continentSize += checkNorth(land);
			continentSize += checkSouth(land);
			continentSize += checkEast(land);
			continentSize += checkWest(land);


			//determine if this land is the northwest point (most north and most west point)
			boolean newNorthWest = isNorthWestMost(land);
			if (newNorthWest){
				northWestMost = new Point(land.getX(), land.getY());
			}

			//since we visited this land already we can remove it from BFS queue
			//System.out.println("\nDone checking. Remove first\n");
			bfsqueue.removeFirst();
		}

		//System.out.println(">> Finished BFS!");
		//System.out.println(">> This continent's size is " + continentSize + ".\n");

		//add northwestMostPoint into list
		northWestPoints.add(northWestMost);

		return continentSize;
	}

	private boolean isNorthWestMost(Point land){

		//if the input land's row is smaller than the current northWestMost point
		//then input land is considered the new northWestMost point
		if (land.getX() < northWestMost.getX()){
			return true;
		}

		// if the input land's row is equal to the current northWestMost point
		// and input land's column is smaller than the new northWestMost point
		if ((land.getX() == northWestMost.getX()) && (land.getY() < northWestMost.getY())){
			return true;
		}

		return false;
	}

	/*
		Check the land's north. 
		Return 1 if north value is 'l' (means visit for the first time so make sure to change this to c)
		Return 0 if north value is not 'l' (means already visited before)
	*/
	private int checkNorth(Point land){
		int result = 0;
		//check north by going up
		//x coordinate gets decremented by 1
		int northX = land.getX() - 1;
		int northY = land.getY();

		//System.out.print("\nChecking " + land + "'s north at (" + northX + ", " + northY + ")...");
		if (northX >= 0) {
			//get north value
			String northValue = getMapValue(northX, northY);
			//if unvisited (marked with 'l')
			if (northValue.equals(landStatus)) {
				//System.out.println("Visit for first time! Add to BFS queue and increment continent size!");
				//add to bfs queue
				bfsqueue.add(new Point(northX, northY));
				//System.out.println("Queue: " + bfsqueue);
				//visit it replace 'l' with c
				setMapValue(northX, northY, c);
				//we visited one new land
				result++;
			}
		}
		return result;
	}

	/*
		Check the land's south. 
		Return 1 if north value is 'l' (means visit for the first time so make sure to change this to c)
		Return 0 if north value is not 'l' (means already visited before)
	*/
	private int checkSouth(Point land){
		int result = 0;
		//check south by incrementing x, y coordinate stays the same
		int southX = land.getX() + 1;
		int southY = land.getY();
		//number of rows may be m=5, but we can only accesst the land from 0 to 4
		//so south is valid as long as it's in the range from 0 to 4 inclusive.
		//System.out.print("\nChecking " + land + "'s south at (" + southX + ", " + southY + ")...");
		if (southX <= m - 1) {
			//get south value
			String southValue = getMapValue(southX, southY);
			//if unvisited (marked with 'l')
			if (southValue.equals(landStatus)){
				//System.out.println("Visit for first time! Add to BFS queue and increment continent size!");
				//add to bfs queue
				bfsqueue.add(new Point(southX, southY));
				//System.out.println("Queue: " + bfsqueue);
				//visit it replace 'l' with c
				setMapValue(southX, southY, c);
				//we visited one new land
				result++;
			}
		}
		return result;
	}

	/*
		Check the land's east. 
		Return 1 if north value is 'l' (means visit for the first time so make sure to change this to c)
		Return 0 if north value is not 'l' (means already visited before)
	*/
	private int checkEast(Point land){
		int result = 0;
		//east by going right, so increment y by 1
		int eastX = land.getX();
		int eastY = land.getY() + 1;
		//the number of columns may be n=5, but is valid [0, 4]
		//so if we at 4 and go east, we will be at 0 (because the earth is round)
		if (eastY > n - 1){
			eastY = 0;
		}
		//System.out.print("\nChecking " + land + "'s east at (" + eastX + ", " + eastY + ")...");
		//get east value
		String eastValue = getMapValue(eastX, eastY);
		//if unvisited (marked with 'l')
		if (eastValue.equals(landStatus)){
			//System.out.println("Visit for first time! Add to BFS queue and increment continent size!");
			//add to bfs queue
			bfsqueue.add(new Point(eastX, eastY));
			//System.out.println("Queue: " + bfsqueue);
			//visit it replace 'l' with c
			setMapValue(eastX, eastY, c);
			//we visited one new land
			result++;
		}
		return result;
	}


	private int checkWest(Point land){
		int result = 0;
		//east by going left, so decrement y by 1
		int westX = land.getX();
		int westY = land.getY() - 1;
		//the number of columns may be n=5, but is valid [0, 4]
		//so if we at 0 and go west, we will be at 4 (because the earth is round)
		if (westY < 0){
			westY = n - 1;
		}
		//System.out.print("\nChecking " + land + "'s west at (" + westX + ", " + westY + ")...");
		//get east value
		String westValue = getMapValue(westX, westY);
		//if unvisited (marked with 'l')
		if (westValue.equals(landStatus)){
			//System.out.println("Visit for first time! Add to BFS queue and increment continent size!");
			//add to bfs queue
			bfsqueue.add(new Point(westX, westY));
			//System.out.println("Queue: " + bfsqueue);
			//visit it replace 'l' with c
			setMapValue(westX, westY, c);
			//we visited one new land
			result++;
		}
		return result;
	}

	Point getNorthWestMostPoint(){
		//traverse through list of continent size
		//get the index where the continent size is biggest
		//this is our biggest cotinent's northwestpoint
		int maxIndex = 0;
		for (int i = 0 ; i < sizeOfContinents.size(); i++){
			if (sizeOfContinents.get(i) > sizeOfContinents.get(maxIndex)){
				maxIndex = i;
			}
		}

		return northWestPoints.get(maxIndex);
	}

	private String getMapValue(int x, int y){
		String mapValue = map[x][y];
		return mapValue;
	}

	private void setMapValue(int x, int y, String c){
		map[x][y] = c;
	}

	LinkedList<Integer> getSizeOfContinents(){
		return sizeOfContinents;
	}

	public static void main (String[] args){
	}

}

class FarthestDistance{

	int farthestDistance = 0;
	private String[][] map;
	private Point northWest;
	private LinkedList<Point> bfsqueue;
	private int m, n;



	FarthestDistance(String[][] map_input, Point northWestPoint_input){
		//check null
		if (map_input == null){
			throw new NullPointerException("Map input is null!");
		}

		if (northWestPoint_input == null){
			throw new NullPointerException("Map input is null!");
		}

		this.map = map_input;
		this.northWest = northWestPoint_input;
		//number of rows
		this.m = map.length;
		//number of columns
		this.n = map[0].length;


		//Perform BFS search
		bfsqueue = new LinkedList<Point>();
		//the northWestPoint will have the value of 0
		setMapValue(northWest.getX(), northWest.getY(), "0");
		//add northWest to queue
		bfsqueue.add(northWest);

		//Visit north, south, east, and west land. They will be updated with previous value++
		while (!bfsqueue.isEmpty()){
			Point land = bfsqueue.getFirst();
			updateNorth(land);
			updateSouth(land);
			updateWest(land);
			updateEast(land);

			bfsqueue.removeFirst();
		}
	}

	private void updateEast(Point land){
		int landValue = Integer.parseInt(getMapValue(land.getX(), land.getY()));
		//east by going right, so increment y by 1
		int eastX = land.getX();
		int eastY = land.getY() + 1;
		//the number of columns may be n=5, but is valid [0, 4]
		//so if we at 4 and go east, we will be at 0 (because the earth is round)
		if (eastY > n - 1){
			eastY = 0;
		}
		//get east value
		String eastValue = getMapValue(eastX, eastY);
		//if unvisited 
		if (!isNumber(eastValue) && eastValue.equals("T")){
			//add to bfs queue
			bfsqueue.add(new Point(eastX, eastY));
			//update south with incremented value
			landValue++;
			setMapValue(eastX, eastY, String.valueOf(landValue));
			//determine if this is the farthest value
			if (landValue > farthestDistance){
				farthestDistance = landValue;
			}
		}
	}

	private void updateWest(Point land){
		int landValue = Integer.parseInt(getMapValue(land.getX(), land.getY()));
		//west by going left, so decrement y by 1
		int westX = land.getX();
		int westY = land.getY() - 1;
		//the number of columns may be n=5, but is valid [0, 4]
		//so if we at 0 and go west, we will be at 4 (because the earth is round)
		if (westY < 0){
			westY = n - 1;
		}
		//get west value
		String westValue = getMapValue(westX, westY);
		//if unvisited 
		if (!isNumber(westValue) && westValue.equals("T")){
			//add to bfs queue
			bfsqueue.add(new Point(westX, westY));
			//update south with incremented value
			landValue++;
			setMapValue(westX, westY, String.valueOf(landValue));
			//determine if this is the farthest value
			if (landValue > farthestDistance){
				farthestDistance = landValue;
			}
		}
	}

	private void updateSouth(Point land){
		int landValue = Integer.parseInt(getMapValue(land.getX(), land.getY()));
		//check south by incrementing x, y coordinate stays the same
		int southX = land.getX() + 1;
		int southY = land.getY();
		//number of rows may be m=5, but we can only accesst the land from 0 to 4
		//so south is valid as long as it's in the range from 0 to 4 inclusive.
		if (southX <= m - 1) {
			//get south value
			String southValue = getMapValue(southX, southY);
			//if unvisited
			if (!isNumber(southValue) && southValue.equals("T")){
				//add to bfs queue
				bfsqueue.add(new Point(southX, southY));
				//update south with incremented value
				landValue++;
				setMapValue(southX, southY, String.valueOf(landValue));
				//determine if this is the farthest value
				if (landValue > farthestDistance){
					farthestDistance = landValue;
				}
			}
		}
	}

	private void updateNorth(Point land){
		int landValue = Integer.parseInt(getMapValue(land.getX(), land.getY()));
		//check north by going up
		//x coordinate gets decremented by 1
		int northX = land.getX() - 1;
		int northY = land.getY();

		if (northX >= 0) {
			//get north value
			String northValue = getMapValue(northX, northY);
			//if unvisited
			if (!isNumber(northValue) && northValue.equals("T")) {
				//add to bfs queue
				bfsqueue.add(new Point(northX, northY));
				//update north with incremented value
				landValue++;
				setMapValue(northX, northY, String.valueOf(landValue));
				//determine if this is the farthest value
				if (landValue > farthestDistance){
					farthestDistance = landValue;
				}
			}
		}
	}

	int getFarthestDistance(){
		return farthestDistance;
	}

	private boolean isNumber(String s) {
	    try {
	        int num = Integer.parseInt(s);
	    } catch (Exception e) {
	        return false;
	    }
	    return true;
	}

	private String getMapValue(int x, int y){
		String mapValue = map[x][y];
		return mapValue;
	}

	private void setMapValue(int x, int y, String c){
		map[x][y] = c;
	}

	public static void main (String[] args){
	}

}