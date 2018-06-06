/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.DateTimeException;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Plane");
				System.out.println("2. Add Pilot");
				System.out.println("3. Add Flight");
				System.out.println("4. Add Technician");
				System.out.println("5. Book Flight");
				System.out.println("6. List number of available seats for a given flight.");
				System.out.println("7. List total number of repairs per plane in descending order");
				System.out.println("8. List total number of repairs per year in ascending order");
				System.out.println("9. Find total number of passengers with a given status");
				System.out.println("10. < EXIT");
				
				switch (readChoice()){
					case 1: AddPlane(esql); break;
					case 2: AddPilot(esql); break;
					case 3: AddFlight(esql); break;
					case 4: AddTechnician(esql); break;
					case 5: BookFlight(esql); break;
					case 6: ListNumberOfAvailableSeats(esql); break;
					case 7: ListsTotalNumberOfRepairsPerPlane(esql); break;
					case 8: ListTotalNumberOfRepairsPerYear(esql); break;
					case 9: FindPassengersCountWithStatus(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddPlane(DBproject esql) {//1
		Integer id = -1;
		String make = "1";	
		String model = "1";
		String age = "a";
		String seats = "a";
		Integer currID;
		String query;
		Scanner input = new Scanner(System.in);
		List<List<String>> res;

//		System.out.print("Enter Plane ID: ");
//		id = input.nextLine();

		query = "SELECT MAX(id) FROM Plane;";
//		System.out.println(query);
		try{
			res = esql.executeQueryAndReturnResult(query);

			for (List<String> l1 : res) {
			   for (String s : l1) {
//			   	System.out.print(s + " "); 
				id = Integer.parseInt(s);
			   }
//			   System.out.println();
			} 
			id += 1;
		}
		catch(SQLException e){
			System.out.println("ERR in Getting Plane ID");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();
		}

		//while(make.matches(".*\\d+.*")){
		System.out.print("Enter Make: ");
		make = input.nextLine();
		//}
		System.out.print("Enter Model: ");
		model = input.nextLine();
		while(!age.matches("[0-9]+")){
			System.out.print("Enter Age: ");
			age = input.nextLine();
			if(!age.matches("[0-9]+")){
				System.out.println("Invalid input, please enter a Number");
			}
		}
		while(!seats.matches("[0-9]+")){
			System.out.print("Enter the number of Seats: ");
			seats = input.nextLine();
			if(!seats.matches("[0-9]+")){
				System.out.println("Invalid input, please enter a Number");
			}

		}

		System.out.println("------------------------------------------------------------------");

		System.out.println("Plane ID: " + id);
		System.out.println("Make: " + make);
		System.out.println("Model: " + model);		
		System.out.println("Age: " + age);
		System.out.println("Number of Seats: " + seats);

		query = "INSERT INTO Plane (id, make, model, age, seats) VALUES (" + id + ", \'" + make + "\', \'" + model + "\', " + age + ", " + seats + ");";
		//System.out.println(query);
		
		try{
			esql.executeUpdate(query);
		}
		catch(SQLException e){
			System.out.println("Error! Flight Already Exists. Please Try again");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();
			return;
		}		

		System.out.println("------------------------------------------------------------------");
		System.out.println();

	}

	public static void AddPilot(DBproject esql) {//2
		Integer id = -1;
		String fullName = "1";
		String nationality = "1";
		String query;
		List<List<String>> res;
		Scanner input = new Scanner(System.in);

//		System.out.print("Enter Pilot ID: ");
//		id = input.nextLine();
	
		query = "SELECT MAX(id) FROM Pilot;";
//		System.out.println(query);
		try{
			res = esql.executeQueryAndReturnResult(query);
			for (List<String> l1 : res) {
			   for (String s : l1) {
//			   	System.out.print(s + " "); 
				id = Integer.parseInt(s);
			   }
//			   System.out.println();
			} 
			id += 1;
		}
		catch(SQLException e){
			System.out.println("ERR in Getting Pilot ID");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();
			return;
		}


		while(fullName.matches(".*\\d+.*")){
			System.out.print("Enter Full Name of the Pilot (can be empty): ");
			fullName = input.nextLine();
			if(fullName.matches(".*\\d+.*")){
				System.out.println("Invalid input, please enter a String without numbers");
			}
		}

		while(nationality.matches(".*\\d+.*")){
			System.out.print("Enter the Nationality of the Pilot (can be empty): ");
			nationality = input.nextLine();
			if(nationality.matches(".*\\d+.*")){
				System.out.println("Invalid input, please enter a String without numbers");
			}
		}
		
		System.out.println("------------------------------------------------------------------");

		System.out.println("Pilot ID: " + id);
		System.out.println("Full Name: " + fullName);
		System.out.println("Nationality: " + nationality);

		query = "INSERT INTO Pilot (id, fullname, nationality) VALUES (" + id + ", \'" + fullName + "\', \'" + nationality + "\');";

		//System.out.println(query);
		try{
			esql.executeUpdate(query);
		}
		catch(SQLException e){
			System.out.println("Error, Pilot with this ID already Exists! Please try again.");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();
			return;
		}		
		System.out.println("------------------------------------------------------------------");
		System.out.println();

	}

	public static void AddFlight(DBproject esql) {//3
		// Given a pilot, plane and flight, adds a flight in the DB
		
		Scanner input = new Scanner(System.in);
		Integer flightNum = -1;
		String cost = "a";
		String num_sold = "a";
		String num_stops = "a";
		LocalDateTime actual_departure_date = null;
		LocalDateTime actual_arrival_date = null;
		String arrival_airport = "1";
		String departure_airport = "1";
		LocalDateTime sched_arrive = null;
		LocalDateTime sched_depart = null;
		String query;
		String dDate;
		String aDate;
		String sdDate;
		String saDate;
		String PilotID = "a";
		String PlaneID = "a";
		Boolean isValid = false;
		List<List<String>> res;		

		DateTimeFormatter ft = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		String tempDate;

//		System.out.print("Enter Flight Number: ");
//		flightNum = input.nextLine();

		query = "SELECT MAX(fnum) FROM Flight;";
//		System.out.println(query);
		try{
			res = esql.executeQueryAndReturnResult(query);
			for (List<String> l1 : res) {
			   for (String s : l1) {
//			   	System.out.print(s + " "); 
				flightNum = Integer.parseInt(s);
			   }
//			   System.out.println();
			} 
			flightNum += 1;
		}
		catch(SQLException e){
			System.out.println("ERR in Getting Flight number");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();
		}


		while(!cost.matches("[0-9]+")){
			System.out.print("Enter Cost: ");
			cost = input.nextLine();
			if(!cost.matches("[0-9]+")){
				System.out.println("Invalid input, please enter a Number");
			}
		}
		while(!num_sold.matches("[0-9]+")){
			System.out.print("Enter Number of Seats Sold: ");
			num_sold = input.nextLine();
			if(!num_sold.matches("[0-9]+")){
				System.out.println("Invalid input, please enter a Number");
			}
		}
		while(!num_stops.matches("[0-9]+")){
			System.out.print("Enter Number of Stops: ");
			num_stops = input.nextLine();
			if(!num_stops.matches("[0-9]+")){
				System.out.println("Invalid input, please enter a Number");
			}
		}
		while(!isValid){		
			isValid = true;
			System.out.print("Enter Scheduled Departure Date (yyyy-MM-dd HH:mm): ");
			tempDate = input.nextLine();
			try{
				sched_depart = LocalDateTime.parse(tempDate, ft);
			}
			catch(DateTimeException e){
				System.out.println("Not a Valid Date, Pleased enter Date in the format yyyy-MM-dd");
				isValid = false;
			}
		}
		isValid = false;
		sdDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(sched_depart);
		while(!isValid){
			isValid = true;
			System.out.print("Enter Scheduled Arrival Date (yyyy-MM-dd HH:mm): ");
			tempDate = input.nextLine();	
			try{
				sched_arrive = LocalDateTime.parse(tempDate, ft);
			}
			catch(DateTimeException e){
				System.out.println("Not a Valid Date, Pleased enter Date in the format yyyy-MM-dd");
				isValid = false;
			}
		}
		saDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(sched_arrive);
		isValid = false;
		while(!isValid){
			isValid = true;
			System.out.print("Enter Actual Departure Date (yyyy-MM-dd HH:mm): ");
			tempDate = input.nextLine();
			try{
				actual_departure_date = LocalDateTime.parse(tempDate, ft);
			}
			catch(DateTimeException e){
				System.out.println("Not a Valid Date, Pleased enter Date in the format yyyy-MM-dd");
				isValid = false;
			}
		}
		dDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(actual_departure_date);
		isValid = false;
		while(!isValid){
			isValid = true;
			System.out.print("Enter Actual Arrival Date (yyyy-MM-dd HH:mm): ");
			tempDate = input.nextLine();
			try{
				actual_arrival_date = LocalDateTime.parse(tempDate, ft);
			}
			catch(DateTimeException e){
				System.out.println("Not a Valid Date, Pleased enter Date in the format yyyy-MM-dd");
				isValid = false;
			}
		}
		isValid = false;
		aDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(actual_arrival_date);
		while(arrival_airport.matches(".*\\d+.*")){
			System.out.print("Enter the Airport of Arrival: ");
			arrival_airport = input.nextLine();
			if(arrival_airport.matches(".*\\d+.*")){
				System.out.println("Invalid input, please enter a String without numbers");
			}
		}
		while(departure_airport.matches(".*\\d+.*")){
			System.out.print("Enter the Airport of Departure: ");
			departure_airport = input.nextLine();
			if(departure_airport.matches(".*\\d+.*")){
				System.out.println("Invalid input, please enter a String without numbers");
			}
		}
		while(!PilotID.matches("[0-9]+")){
			System.out.print("Enter PilotID: ");
			PilotID = input.nextLine();
			if(!PilotID.matches("[0-9]+")){
				System.out.println("Invalid input, please enter a Number");
			}
		}
		while(!PlaneID.matches("[0-9]+")){
			System.out.print("Enter PlaneID: ");
			PlaneID = input.nextLine();
			if(!PlaneID.matches("[0-9]+")){
				System.out.println("Invalid input, please enter a Number");
			}
		}

		
		System.out.println();

		System.out.println("------------------------------------------------------------------");
	
		System.out.println("Adding to Flight");
		System.out.println("Flight Number: " + flightNum);
		System.out.println("Cost: " + cost);
		System.out.println("Number of Seats Sold: " + num_sold);
		System.out.println("Number of Stops: " + num_stops);
		System.out.println("Actual Departure Date: " + actual_departure_date.toString()); 
		System.out.println("Actual Arrival Date: " + actual_arrival_date.toString());
		System.out.println("Arrival Airport: " + arrival_airport);
		System.out.println("Departure Airpot: " + departure_airport);	

		query = "INSERT INTO Flight (fnum, cost, num_sold, num_stops, actual_departure_date, actual_arrival_date, arrival_airport, departure_airport) VALUES (" + flightNum + ", " + cost + ", " + num_sold + ", " + num_stops + ", \'" + dDate + "\', \'" + aDate + "\', \'" + arrival_airport + "\', \'" + departure_airport + "\');";
		//System.out.println(query);
		try{
			esql.executeUpdate(query);
		}
		catch(SQLException e){
			System.out.println("Flight Already Exists! Please try again.");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();

			return;
		}

		System.out.println();
		System.out.println("------------------------------------------------------------------");
		System.out.println("------------------------------------------------------------------");

		System.out.println("Adding to Flight Info");
		System.out.println("ID: " + flightNum);
		System.out.println("Flight Number: " + flightNum);
		System.out.println("PilotID: " + PilotID); 
		System.out.println("PlaneID: " + PlaneID);

		query = "INSERT INTO FlightInfo (fiid, flight_id, pilot_id, plane_id) VALUES (" + flightNum + ", " + flightNum + ", " + PilotID + ", " + PlaneID + ");";
		System.out.println(query);
		try{
			esql.executeUpdate(query);
		}
		catch(SQLException e){
			System.out.println("Error in adding Flight Information, please make sure PilotID, PlaneID, and Flight Number exist and the Flight is not already schedule.");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();
			return;
		}

		System.out.println("------------------------------------------------------------------");
//		System.out.println();
		System.out.println("------------------------------------------------------------------");

		System.out.println("Adding to Schedule");
		System.out.println("ID: " + flightNum);
		System.out.println("Flight Number: " + flightNum);
		System.out.println("Scheduled Departure Date: " + sdDate); 
		System.out.println("Scheduled Arrival Date: " + saDate);

		query = "INSERT INTO Schedule (id, flightNum, departure_time, arrival_time) VALUES (" + flightNum + ", " + flightNum + ", \'" + sdDate + "\', \'" + saDate + "\');";

		System.out.println(query);
		try{
			esql.executeUpdate(query);
		}
		catch(SQLException e){
			System.out.println("Flight has already been scheduled! Please try again.");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();
			return;
		}

		System.out.println("------------------------------------------------------------------");
		System.out.println();
}

	public static void AddTechnician(DBproject esql) {//4

		Integer id = -1;
		String fullName = "1";
		String query;
		Scanner input = new Scanner(System.in);
		List<List<String>> res;

		query = "SELECT MAX(id) FROM Technician;";
//		System.out.println(query);
		try{
			res = esql.executeQueryAndReturnResult(query);
			for (List<String> l1 : res) {
			   for (String s : l1) {
//			   	System.out.print(s + " "); 
				id = Integer.parseInt(s);
			   }
//			   System.out.println();
			} 
			id += 1;
		}
		catch(SQLException e){
			System.out.println("ERR in Getting Technician ID");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();

		}

/*		while(!id.matches("[0-9]+")){
			System.out.print("Enter Technician ID: ");
			id = input.nextLine();
			if(!id.matches("[0-9]+")){
				System.out.println("Invalid input, please enter a Number");
			}
		}
*/
		while(fullName.matches(".*\\d+.*")){
			System.out.print("Enter Full Name of the Technician: ");
			fullName = input.nextLine();
			if(fullName.matches(".*\\d+.*")){
				System.out.println("Invalid input, please enter a String without numbers");
			}
		}

		System.out.println();
		System.out.println("------------------------------------------------------------------");

		System.out.println("Technician ID: " + id);
		System.out.println("Full Name: " + fullName);

		query = "INSERT INTO Technician (id, full_name) VALUES (" + id + ", \'" + fullName + "\');";
//		System.out.println(quiery);
		try{
			esql.executeUpdate(query);
		}
		catch(SQLException e){
			System.out.println("Error, Technician with this ID already exists! Please try again.");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();
			return;
		}		

		System.out.println("------------------------------------------------------------------");
		System.out.println();
	}

	public static void BookFlight(DBproject esql) {//5
		// Given a customer and a flight that he/she wants to book, add a reservation to the DB
		
		Scanner input = new Scanner(System.in);
		String flightNum = "a";
		String customerID = "a";
		String tempDate;
		String sched_dep;
		LocalDate date = null;
		String query;
		DateTimeFormatter ft = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		List<List<String>> res;
		Integer numSeats = -1;
		Integer numReserve = -1;
		Integer currRNum = -1;
		Character status = 'R';
		Boolean isValid = false;

		while(!flightNum.matches("[0-9]+")){
			System.out.print("Enter Flight Number: ");
			flightNum = input.nextLine();
			if(!flightNum.matches("[0-9]+")){
				System.out.println("Invalid input, please enter a Number");
			}
		}		
		while(!(isValid)){
			isValid = true;
			System.out.print("Enter Date (yyyy-MM-dd): ");
			tempDate = input.nextLine();
			try{
				date = LocalDate.parse(tempDate, ft);
			}
			catch(DateTimeException e){
				System.out.println("Not a Valid Date, Pleased enter Date in the format yyyy-MM-dd");
				isValid = false;
			}
			
		}
		sched_dep = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date);		
		while(!customerID.matches("[0-9]+")){
			System.out.print("Enter Customer ID: ");
			customerID = input.nextLine(); 
			if(!customerID.matches("[0-9]+")){
				System.out.println("Invalid input, please enter a Number");
			}
		}		

		System.out.println();
		System.out.println("------------------------------------------------------------------");

		System.out.println("Flight Number: " + flightNum);
		System.out.println("Scheduled Date of Departure: " + sched_dep);
		System.out.println("Customer ID: " + customerID);

		while(numSeats == -1){
			query = "SELECT P.seats FROM Plane P, Schedule S, FlightInfo F WHERE S.flightNum = " + flightNum + " AND S.departure_time = \'" + sched_dep + "\' AND S.flightNum = F.flight_id AND F.plane_id = P.id;";
			try{
				res = esql.executeQueryAndReturnResult(query);
				for (List<String> l1 : res) {
				   for (String s : l1) {
					numSeats = Integer.parseInt(s);
				   }
				} 
			}
			catch(SQLException e){
				System.out.println("Error, Please make sure that the Flight exists and is properly scheduled! Please try again");
				System.out.println("Err: " + e);
				System.out.println("------------------------------------------------------------------");
				System.out.println();
				return;
			}
			if(numSeats == -1){
				System.out.println("Flight does not Exist, Please Try Again");
				System.out.println("Here is a list of dates with the chosen Flight ID, Select a number of schdule for that flight (0 to return to main menu).");
				query = "SELECT departure_time FROM Schedule WHERE id =" + flightNum;
				Integer pos = 1;
				try{
					res = esql.executeQueryAndReturnResult(query);
					for (List<String> l1 : res) {
					   for (String s : l1) {
						System.out.println(Integer.toString(pos) + ": " + s); 
						pos += 1;
					  }
					}
				} 
				catch(SQLException e){
					System.out.println("Err: " + e);
				}
				pos = Integer.parseInt(input.nextLine());
				pos -= 1;
				if(pos < 0 || pos > res.get(0).size()){
					System.out.println("Goodbye");
					System.out.println("------------------------------------------------------------------");
					System.out.println();
					return;

				}
				else{
					sched_dep = res.get(0).get(pos);
				//	System.out.println(sched_dep);
				//	return;
				}
			}
		}
	
//		query = "SELECT COUNT(*) FROM Reservation R, Schedule S WHERE S.flightNum = " + flightNum + " AND " + "S.departure_time = \'" + sched_dep + "\' AND " + "R.fid = " + flightNum + " AND R.status = 'R';";
		query = "SELECT COUNT(*) FROM Reservation WHERE fid = " + flightNum + " AND status = 'R';";
//		System.out.println(query);
		try{
			res = esql.executeQueryAndReturnResult(query);
			for (List<String> l1 : res) {
			   for (String s : l1) {
//			   	System.out.print(s + " "); 
				numReserve = Integer.parseInt(s);
			   }
//			   System.out.println();
			} 
//			System.out.println(numReserve);
		}
		catch(SQLException e){
			System.out.println("ERR in Getting Number of Reservations");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();
			return;
		}

		query = "SELECT MAX(rnum) FROM Reservation;";
		//System.out.println(query);
		try{
			res = esql.executeQueryAndReturnResult(query);

			for (List<String> l1 : res) {
			   for (String s : l1) {
//			   	System.out.print(s + " "); 
				currRNum = Integer.parseInt(s);
			   }
//			   System.out.println();
			} 
			currRNum += 1;
		//	System.out.println(currRNum);
		}
		catch(SQLException e){
			System.out.println("ERR in Getting the Number of Reservations");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();

		}

//		System.out.println(numSeats-numReserve);
		if((numSeats-numReserve) <= 0){
			System.out.print("The Flight is full, would you like to be waitlisted? (y/n)");
			String temp = input.nextLine();
			if(temp.equals("n")){
				System.out.println("Returning to main menu");
				System.out.println();
				return;
			}
			else{
				System.out.println("Placing on the waitlist");
				status = 'W';
			}
		}

		query = "INSERT INTO Reservation (rnum, cid, fid, status) VALUES (" + currRNum + ", " + customerID + ", " + flightNum + ", \'" + status + "\');";
//		System.out.println(query);
		try{
			esql.executeUpdate(query);
		}
		catch(SQLException e){
			System.out.println("Error Reserving a Seat, Please make sure CustomerID and FlightNumber are valid!");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();
			return;
		}		
		System.out.println("Done: Registered for flight " + flightNum + " with status " + status);
		System.out.println("------------------------------------------------------------------");
		System.out.println();
	}

	public static void ListNumberOfAvailableSeats(DBproject esql) {//6
		// For flight number and date, find the number of availalbe seats (i.e. total plane capacity minus booked seats )
		Scanner input = new Scanner(System.in);
		String flightNum = "a";
		String tempDate;
		String sched_dep;
		LocalDate date = null;
		String query;
		DateTimeFormatter ft = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		List<List<String>> res;
		Integer numSeats = -1;
		Integer numReserve = -1;
		Boolean isValid = false;

		while(!flightNum.matches("[0-9]+")){
			System.out.print("Enter Flight Number: ");
			flightNum = input.nextLine();
			if(!flightNum.matches("[0-9]+")){
				System.out.println("Invalid input, please enter a Number");
			}
		}
		while(!(isValid)){
			isValid = true;
			System.out.print("Enter Scheduled Date of Departure: (yyyy-MM-dd): ");
			tempDate = input.nextLine();
			try{
				date = LocalDate.parse(tempDate, ft);
			}
			catch(DateTimeException e){
				System.out.println("Not a Valid Date, Pleased enter Date in the format yyyy-MM-dd");
				isValid = false;
			}
		}
		sched_dep = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date);		
		System.out.println();
		System.out.println("------------------------------------------------------------------");

		System.out.println("Flight Number: " + flightNum);
		System.out.println("Date: " + date.toString());

		query = "SELECT P.seats FROM Plane P, Schedule S, FlightInfo F WHERE S.flightNum = " + flightNum + " AND S.departure_time = \'" + sched_dep + "\' AND S.flightNum = F.flight_id AND F.plane_id = P.id;";
//		System.out.println(query);
		try{
			res = esql.executeQueryAndReturnResult(query);
			for (List<String> l1 : res) {
			   for (String s : l1) {
//			   	System.out.print(s + " "); 
				numSeats = Integer.parseInt(s);
			   }
//			   System.out.println();
			} 
//			System.out.println(numSeats);
		}
		catch(SQLException e){
			System.out.println("ERR in Getting the Seats from the Flight. Please make sure the plane is properly scheduled. Please Try Again.");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();
			return;
		}
		while(numSeats == -1){
			query = "SELECT P.seats FROM Plane P, Schedule S, FlightInfo F WHERE S.flightNum = " + flightNum + " AND S.departure_time = \'" + sched_dep + "\' AND S.flightNum = F.flight_id AND F.plane_id = P.id;";
			try{
				res = esql.executeQueryAndReturnResult(query);
				for (List<String> l1 : res) {
				   for (String s : l1) {
					numSeats = Integer.parseInt(s);
				   }
				} 
			}
			catch(SQLException e){
				System.out.println("Error, Please make sure that the Flight exists and is properly scheduled! Please try again");
				System.out.println("Err: " + e);
				System.out.println("------------------------------------------------------------------");
				System.out.println();
				return;
			}
			if(numSeats == -1){
				System.out.println("Flight does not Exist, Please Try Again");
				System.out.println("Here is a list of dates with the chosen Flight ID, Select a number of schdule for that flight (0 to return to main menu).");
				query = "SELECT departure_time FROM Schedule WHERE id =" + flightNum;
				Integer pos = 1;
				try{
					res = esql.executeQueryAndReturnResult(query);
					for (List<String> l1 : res) {
					   for (String s : l1) {
						System.out.println(Integer.toString(pos) + ": " + s); 
						pos += 1;
					  }
					}
				} 
				catch(SQLException e){
					System.out.println("Err: " + e);
				}
				pos = Integer.parseInt(input.nextLine());
				pos -= 1;
				if(pos < 0 || pos > res.get(0).size()){
					System.out.println("Goodbye");
					System.out.println("------------------------------------------------------------------");
					System.out.println();
					return;

				}
				else{
					sched_dep = res.get(0).get(pos);
				//	System.out.println(sched_dep);
				//	return;
				}
			}
		}

		query = "SELECT COUNT(*) FROM Reservation WHERE fid = " + flightNum + " AND (status = 'R' OR status = 'C');";
//		System.out.println(query);
		try{
			res = esql.executeQueryAndReturnResult(query);
			for (List<String> l1 : res) {
			   for (String s : l1) {
//			   	System.out.print(s + " "); 
				numReserve = Integer.parseInt(s);
			   }
//			   System.out.println();
			} 
//			System.out.println(numReserve);
		}
		catch(SQLException e){
			System.out.println("ERR in Getting the number of Reservations");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();
			return;
		}

		System.out.print("Number of Remaining Seats: ");
		System.out.println(numSeats-numReserve);

		System.out.println("------------------------------------------------------------------");
		System.out.println();
		

	}

	public static void ListsTotalNumberOfRepairsPerPlane(DBproject esql) {//7
		// Count number of repairs per planes and list them in descending order
		List<List<String>> res;
		String query;
		System.out.print("Number of Repairs per Plane: ");
		System.out.println();
		query = "SELECT R.plane_id, COUNT(*), P.model FROM Repairs R, Plane P WHERE R.plane_id = P.id GROUP BY R.plane_id, P.model ORDER BY COUNT(*) DESC;";

		try{
			System.out.println("|Plane ID                  Number of Repairs           Model");
			res = esql.executeQueryAndReturnResult(query);
			System.out.println("--------------------------------------------------------------------------------------------------------------");

			for (List<String> l1 : res) {
		          System.out.print("|");
			  for (String s : l1) {
			      s = s + "                                    ";
			      s = s.substring(0, 26);
			      System.out.print(s); 			   
			   }
  		            System.out.println();
			    System.out.println("--------------------------------------------------------------------------------------------------------------");
	              
			} 
//			System.out.println(numReserve);
       		}
       		catch(SQLException e){
		   	System.err.println(e.getMessage());
       		}	
		System.out.println();
	}

	public static void ListTotalNumberOfRepairsPerYear(DBproject esql) {//8
		// Count repairs per year and list them in ascending order
		List<List<String>> res;
		String query;
		
		System.out.println("------------------------------------------------------------------");
		System.out.println();
		query = "SELECT EXTRACT(YEAR FROM R.repair_date), COUNT(*) FROM Repairs R GROUP BY EXTRACT(YEAR FROM (R.repair_date)) ORDER BY COUNT(*) ASC;";

		System.out.println("Number of Repairs per Year: ");
		System.out.println();
		try{
			System.out.println("|Year             Number of Repairs           ");
			res = esql.executeQueryAndReturnResult(query);
			System.out.println("----------------------------------------------------------");

			for (List<String> l1 : res) {
		          System.out.print("|");
			  for (String s : l1) {
			      s = s + "                                    ";
			      s = s.substring(0, 17);
			      System.out.print(s); 			   
			   }
  		            System.out.println();
			    System.out.println("----------------------------------------------------------");    
			} 
//			System.out.println(numReserve);
       		}
       		catch(SQLException e){
		   	System.err.println(e.getMessage());
       		}	
		System.out.println();
	}
	
	public static void FindPassengersCountWithStatus(DBproject esql) {//9
		// Find how many passengers there are with a status (i.e. W,C,R) and list that number.

		Scanner input = new Scanner(System.in);
		String flightNum = "a";
		String tempDate;
		String sched_dep;
//		System.out.println("------------------------------------------------------------------");
//		System.out.println();
		Boolean isValid = false;	

		LocalDate date = null;
		String query;
		DateTimeFormatter ft = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		List<List<String>> res;
		Integer numSeats = -1;
		Integer numReserve = 0;
		Integer numWaitlist = 0;
		Integer Completed = 0;

		while(!flightNum.matches("[0-9]+")){
			System.out.print("Enter Flight Number: ");
			flightNum = input.nextLine();
			if(!flightNum.matches("[0-9]+")){
				System.out.println("Invalid input, please enter a Number");
			}
		}
		while(!(isValid)){
			isValid = true;
			System.out.print("Enter Date (yyyy-MM-dd): ");
			tempDate = input.nextLine();
			try{
				date = LocalDate.parse(tempDate, ft);
			}
			catch(DateTimeException e){
				System.out.println("Not a Valid Date, Pleased enter Date in the format yyyy-MM-dd");
				isValid = false;
			}
			
		}
		sched_dep = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date);

		System.out.println();	
		System.out.println("------------------------------------------------------------------");

		System.out.println("Flight Number: " + flightNum);
		System.out.println("Scheduled Date of Departure: " + sched_dep);

		while(numSeats == -1){
				query = "SELECT P.seats FROM Plane P, Schedule S, FlightInfo F WHERE S.flightNum = " + flightNum + " AND S.departure_time = \'" + sched_dep + "\' AND S.flightNum = F.flight_id AND F.plane_id = P.id;";
				try{
					res = esql.executeQueryAndReturnResult(query);
					for (List<String> l1 : res) {
					   for (String s : l1) {
						numSeats = Integer.parseInt(s);
					   }
					} 
				}
				catch(SQLException e){
					System.out.println("Error, Please make sure that the Flight exists and is properly scheduled! Please try again");
					System.out.println("Err: " + e);
					System.out.println("------------------------------------------------------------------");
					System.out.println();
					return;
				}
				if(numSeats == -1){
					System.out.println("Flight does not Exist, Please Try Again");
					System.out.println("Here is a list of dates with the chosen Flight ID, Select a number of schdule for that flight (0 to return to main menu).");
					query = "SELECT departure_time FROM Schedule WHERE id =" + flightNum;
					Integer pos = 1;
					try{
						res = esql.executeQueryAndReturnResult(query);
						for (List<String> l1 : res) {
						   for (String s : l1) {
							System.out.println(Integer.toString(pos) + ": " + s); 
							pos += 1;
						  }
						}
					} 
					catch(SQLException e){
						System.out.println("Err: " + e);
					}
					pos = Integer.parseInt(input.nextLine());
					pos -= 1;
					if(pos < 0 || pos > res.get(0).size()){
						System.out.println("Goodbye");
						System.out.println("------------------------------------------------------------------");
						System.out.println();
						return;

					}
					else{
						sched_dep = res.get(0).get(pos);
					//	System.out.println(sched_dep);
					//	return;
					}
				}
			}

		query = "SELECT COUNT(*) FROM Reservation R, Schedule S, FlightInfo F WHERE S.flightNum = " + flightNum + " AND S.departure_time = \'" + sched_dep + "\' AND S.flightNum = F.flight_id AND R.fid = S.flightNum AND R.status = 'R';";
		//System.out.println(query);
		try{
			res = esql.executeQueryAndReturnResult(query);
			for (List<String> l1 : res) {
			   for (String s : l1) {
//			   	System.out.print(s + " "); 
				numReserve = Integer.parseInt(s);
			   }
//			   System.out.println();
			} 
//			System.out.println(numSeats);
		}
		catch(SQLException e){
			System.out.println("Err in getting the number of reservations for the flight. Please make sure that the flight exists and is properly scheduled. Please try again.");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();
			return;
		}

		query = "SELECT COUNT(*) FROM Reservation R, Schedule S, FlightInfo F WHERE S.flightNum = " + flightNum + " AND S.departure_time = \'" + sched_dep + "\' AND S.flightNum = F.flight_id AND R.fid = S.flightNum AND R.status = 'W';";
		//System.out.println(query);
		try{
			res = esql.executeQueryAndReturnResult(query);
			for (List<String> l1 : res) {
			   for (String s : l1) {
//			   	System.out.print(s + " "); 
				numWaitlist = Integer.parseInt(s);
			   }
//			   System.out.println();
			} 
//			System.out.println(numSeats);
		}
		catch(SQLException e){
			System.out.println("Err in getting the number of waitlist for the flight. Please make sure that the flight exists and is properly scheduled. Please try again.");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();
			return;
		}

		query = "SELECT COUNT(*) FROM Reservation R, Schedule S, FlightInfo F WHERE S.flightNum = " + flightNum + " AND S.departure_time = \'" + sched_dep + "\' AND S.flightNum = F.flight_id AND R.fid = S.flightNum AND R.status = 'C';";
		//System.out.println(query);
		try{
			res = esql.executeQueryAndReturnResult(query);
			for (List<String> l1 : res) {
			   for (String s : l1) {
//			   	System.out.print(s + " "); 
				Completed = Integer.parseInt(s);
			   }
//			   System.out.println();
			} 
//			System.out.println(numSeats);
		}
		catch(SQLException e){
			System.out.println("Err in getting the number of completed for the flight. Please make sure that the flight exists and is properly scheduled. Please try again.");
			System.out.println("Err: " + e);
			System.out.println("------------------------------------------------------------------");
			System.out.println();
			return;
		}

		System.out.print("Number of Reserved Seats: ");
		System.out.println(numReserve);	
		System.out.print("Number of Waitlisted Seats: ");
		System.out.println(numWaitlist);	
		System.out.print("Number of Completed Seats: ");
		System.out.println(Completed);	
		
		System.out.println("------------------------------------------------------------------");
		System.out.println();
	}
}
