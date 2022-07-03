//***************************************************************
//
//  Developer:         Cory Munselle
//
//  Program #:         9
//
//  File Name:         Project9.java
//
//  Course:            COSC 4301 - Modern Programming
//
//  Due Date:          May 9, 2022
//
//  Instructor:        Fred Kumi 
//
//  Description:
//      Provides framework for letting the user interact with and
//		modify the Books database. Offers addition of authors and tables,
//		removal of authors, displaying of authors and books by authors,
//		and modifying an existing author's name.
//
//***************************************************************

import java.sql.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

@SuppressWarnings("resource")
public class Project9
{
	private Connection connection;
	private final Scanner input;

	public Project9() {
		input = new Scanner(System.in);
		connection = null;
	}

	// **************************************************************
	//
	// Method:      main
	//
	// Description: The main method of the program
	//
	// Parameters:  String array
	//
	// Returns:     N/A
	//
	// **************************************************************
	public static void main(String[] args)
	{
		Project9 dbDriver = new Project9();

		dbDriver.connectToDatabase();

		dbDriver.menu();
    }
	
	// **************************************************************
	//
	// Method:     	connectToDatabase
	//
	// Description: Connects to the oracle database
	//
	// Parameters:  None
	//
	// Returns:     N/A
	//
	// **************************************************************
	public void connectToDatabase()
	{
		ConnectToOracleDB dbConnect = new ConnectToOracleDB();
		
		try {
		   dbConnect.loadDrivers();
		   connection = dbConnect.connectDriver();
		   connection.setAutoCommit(true);
		}
		catch (Exception exp) {
            System.out.println("Something terrible went wrong");
		}
	}

	// **************************************************************
	//
	// Method:     	menu
	//
	// Description: Displays the different options for the user
	//
	// Parameters:  None
	//
	// Returns:     N/A
	//
	// **************************************************************
	public void menu() {

		int choice = 0;

		while (choice != 7) {
			System.out.println("Please make a selection: ");
			System.out.printf("%s%n%s%n%s%n%s%n%s%n%s%n%s%n",
					"1) Select all authors from the Authors table",
					"2) Add a new author to the Authors table",
					"3) Edit the existing information of an author",
					"4) Add a new book title for an author",
					"5) List all books written by an author",
					"6) Delete an author",
					"7) Exit");
			if (input.hasNextInt()) {
				try {
					choice = input.nextInt();
				}
				catch (NoSuchElementException e) {
					System.err.println("Not an integer. Please provide an integer.");
					input.next();
				}
			}
			switch (choice) {
				case 1:
					getAuthors();
					break;
				case 2:
					addAuthor();
					break;
				case 3:
					editAuthor();
					break;
				case 4:
					associateAuthorWithTitle();
					break;
				case 5:
					findBooksByAuthor();
					break;
				case 6:
					removeAuthor();
					break;
				case 7:
					System.out.println("Now exiting...");
					break;
				default:
					System.out.println("Invalid selection. Please make a selection from the options above.");
					cleanup();
					break;
			}
		}
	}

	// **************************************************************
	//
	// Method:     	getAuthors
	//
	// Description: Retrieves and displays a list of authors from the db
	//
	// Parameters:  None
	//
	// Returns:     N/A
	//
	// **************************************************************
	public void getAuthors() {
		Statement authorTable = null;
		ResultSet authors = null;

		try {
			authorTable = connection.createStatement();
		}
		catch (SQLException e) {
			System.err.println("Failed to create statement.");
			e.printStackTrace();
		}

		if (authorTable != null) {
			authors = execQuery(authorTable, authors, "SELECT * FROM AUTHORS");
		}

		if (authors != null) {
			System.out.printf("%-20s%-20s%s%n", "Author ID", "First Name", "Last Name");
			try {
				while (authors.next()) {
					int authID = authors.getInt("authorid");
					String authFn = authors.getString("firstname");
					String authLn = authors.getString("lastname");
					System.out.printf("%-20s%-20s%s%n", authID + ")", authFn, authLn);
				}
			}
			catch (SQLException e) {
				System.err.println("Failed to retrieve query results.");
				e.printStackTrace();
			}
		}
		System.out.println();
		close(authorTable, authors);
	}

	// **************************************************************
	//
	// Method:     	getTitles
	//
	// Description: Retrieves and displays a list of titles from the db
	//
	// Parameters:  None
	//
	// Returns:     N/A
	//
	// **************************************************************
	public void getTitles() {
		Statement titleTable = null;
		ResultSet titles = null;
		int count = 1;

		// create statement
		try {
			titleTable = connection.createStatement();
		}
		catch (SQLException e) {
			System.err.println("Failed to create statement.");
			e.printStackTrace();
		}

		// execute query and get resultset back
		if (titleTable != null) {
			titles = execQuery(titleTable, titles, "SELECT * FROM TITLES");
		}

		// display all of the titles
		if (titles != null) {
			System.out.printf("%-20s%-20s%-60s%-20s%s%n", "Number", "ISBN", "Title", "Edition", "Copyright Year");
			try {
				while (titles.next()) {
					String ISBN = titles.getString("ISBN");
					String title = titles.getString("title");
					int edition = titles.getInt("editionnumber");
					String copyright = titles.getString("copyright");
					System.out.printf("%-20s%-20s%-60s%-20s%s%n", count + ")", ISBN, title, edition, copyright);
					count++;
				}
			}
			catch (SQLException e) {
				System.err.println("Failed to retrieve query results.");
				e.printStackTrace();
			}
			System.out.println();
		}
		close(titleTable, titles);
	}

	// **************************************************************
	//
	// Method:     	addAuthor
	//
	// Description: Adds an author to the db
	//
	// Parameters:  None
	//
	// Returns:     N/A
	//
	// **************************************************************
	public void addAuthor() {
		// split the returned full name from getNewAuthorName
		String[] name = getNewAuthorName(1).split(" ");

		// set fname to first value in array
		String fname = name[0];

		// set lname to second value in array
		String lname = name[1];

		PreparedStatement newAuthor = null;
		ResultSet authors = null;

		// create new row in Authors table with the new first and last name
		String insertStr = "INSERT INTO AUTHORS (Firstname, Lastname) VALUES (?, ?)";

		// prepare query
		try {
			newAuthor = connection.prepareStatement(insertStr);
		}
		catch (SQLException e) {
			System.err.println("Failed to create statement.");
			e.printStackTrace();
		}

		// set first and last names into query and execute query
		if (newAuthor != null) {
			try {
				newAuthor.setString(1, fname);
				newAuthor.setString(2, lname);
				authors = newAuthor.executeQuery();
			}
			catch (SQLException e) {
				System.err.println("Failed to execute query.");
			}
		}

		// check if successful
		if (authors != null) {
			try {
				if (authors.next()) {
					System.out.println("New author added to table.");
				}
			}
			catch (SQLException e) {
				System.err.println("Failed to insert author into table.");
			}
		}
		// cleanup
		close(newAuthor, authors);
	}

	// **************************************************************
	//
	// Method:     	removeAuthor
	//
	// Description: Removes an author from the database
	//
	// Parameters:  None
	//
	// Returns:     N/A
	//
	// **************************************************************
	public void removeAuthor() {
		PreparedStatement removalQuery = null;
		ResultSet queryResult = null;
		// Remove authors from AuthorISBN that match the ID
		String delAuthorISBNSQL = "DELETE FROM authorisbn WHERE authorid = ?";

		// Remove authors from Authors that match the ID
		String delAuthorSQL = "DELETE FROM authors WHERE authorid = ?";

		// Delete all books which don't have associated authors anymore
		String delTitlesSQL = "DELETE FROM Titles WHERE ISBN IN ( SELECT ISBN FROM Titles WHERE ISBN NOT IN ( SELECT ISBN FROM AuthorISBN) GROUP BY ISBN)";

		System.out.println("Current authors:");
		getAuthors();
		System.out.println("Please select the ID of the author you wish to remove: ");
		int removeID = getInput(0);

		// Try to get resultset of passed in authorISBN deletion query
		queryResult = getResultSet(removeID, removalQuery, queryResult, delAuthorISBNSQL);

		// check if authorISBNs were succesfully deleted
		if (queryResult != null) {
			try {
				if (queryResult.next()) {
					System.out.println("Author removed from table.");
				}
			}
			catch (SQLException e) {
				System.err.println("Failed to insert author into table.");
			}
		}

		// try to get resultset of passed in author deletion query
		queryResult = getResultSet(removeID, removalQuery, queryResult, delAuthorSQL);

		// check if the author was deleted before moving on
		if (queryResult != null) {
			try {
				if (queryResult.next()) {
					System.out.println("Author removed from table.");
				}
			}
			catch (SQLException e) {
				System.err.println("Failed to insert author into table.");
			}
		}

		// try to get resultset of passed in rogue title deletion query, this time without setting any values
		queryResult = getResultSet(0, removalQuery, queryResult, delTitlesSQL);

		// check if rogue titles were successfully removed
		if (queryResult != null) {
			try {
				if (queryResult.next()) {
					System.out.println("Book titles written by only the deleted author have been removed.");
				}
			}
			catch (SQLException e) {
				System.err.println("Failed to delete titles.");
			}
		}
		close(removalQuery, queryResult);
	}

	// **************************************************************
	//
	// Method:     	getInput
	//
	// Description: Gets input from the user for choosing a title or author
	//
	// Parameters:  int option
	//
	// Returns:     int userSelection
	//
	// **************************************************************
	public int getInput(int option) {
		ArrayList<Integer> authIDs = getAuthorIDs();
		ArrayList<String> titleISBNs = getTitleISBN();
		boolean valid = false;
		int userSelection = -1;

		while (!valid) {
			// get input from user
			try {
				userSelection = input.nextInt();
			}
			catch (NoSuchElementException e) {
				System.err.println("Not a number. Please provide a number.");
				input.next();
			}
			// if this method is called in any method but associateAuthorWithTitle, make sure value is above zero
			if (userSelection <= -1 && option == 0) {
				System.out.println("Please enter a value of 0 or greater.");
			}
			// if the method is called in associateAuthorWithTitle, make sure the value is between 0 and the total number of books
			else if ((userSelection <= -1 || userSelection > (long) titleISBNs.size()) && option == 1) {
				System.out.println("Please select from the available options (between 0 and " + titleISBNs.size() + ").");
			}
			// Only need to loop through the IDs if called outside associateAuthorWithTitle
			if (option == 0 || option == 2 && userSelection > 0) {
				for (Integer id : authIDs) {
					// if any of the IDs match, user has made a valid selection
					if (id == userSelection) {
						valid = true;
					}
				}
			}
			// if user selected a title within the range of values, selection is valid
			else if (option == 1 && (userSelection > -1 && userSelection <= titleISBNs.size())) {
				valid = true;
			}
			else if (option == 2 && userSelection == 0) {
				valid = true;
			}
			if (!valid) {
				System.out.println("Author ID not found. Please enter another ID.");
			}
		}

		return userSelection;
	}

	// **************************************************************
	//
	// Method:     	getNewAuthorName
	//
	// Description: Gets a name for the new author and returns either
	//				the name itself or a query containing the name
	//
	// Parameters:  int flag
	//
	// Returns:     String resultStr
	//
	// **************************************************************
	public String getNewAuthorName(int flag) {
		ArrayList<String> names = new ArrayList<>();
		boolean valid = false;
		int option = 0;
		int count = 0;
		String returnStr = "";

		// If coming from the editAuthor method, make sure to display this
		if (flag == 0) {
			System.out.printf("%s%n%s%n%s%n%s%n", "Please select an option: ", "1) New first name", "2) New last name", "3) New first and last name");
		}
		// if coming from the addAuthor method, set the option to 3 since we'll need a first and last name anyway
		else if (flag == 1) {
			option = 3;
		}

		while (!valid) {
			// only bother asking the user for input if they need to choose which part of the name to edit
			if (flag == 0) {
				if (input.hasNextInt()) {
					option = input.nextInt();
				} else {
					input.next();
				}
			}

			switch (option) {
				// Get first name and add to arraylist
				case 1:
					System.out.print("Please enter the new first name of the author: ");
					if (input.hasNext()) {
						names.add(input.next());
						valid = true;
					}
					break;
				// Get last name and add to arraylist
				case 2:
					System.out.print("Please enter the new last name of the author: ");
					if (input.hasNext()) {
						names.add(input.next());
						valid = true;
					}
					break;
				// Get first and last name and add both to arraylist
				case 3:
					System.out.println("Please enter the new first and last name of the author, separated by a space: ");
					// Ensure the user only gets to enter two values
					while (count < 2) {
						if (input.hasNext()) {
							names.add(input.next());
							count++;
						}
					}
					valid = true;
					break;
				// if they entered an invalid value
				default:
					System.out.println("Invalid option. Please enter either 1, 2, or 3.");
					break;
			}
		}

		// build SQL queries based on what the user selected
		if (names.size() == 1 && option == 1) {
			returnStr = "UPDATE authors SET firstname = '" + names.get(0) + "' WHERE authorid = ?";
		}
		else if (names.size() == 1 && option == 2) {
			returnStr = "UPDATE authors SET lastname = '" + names.get(0) + "' WHERE authorid = ?";
		}
		else if (names.size() == 2 && option == 3 && flag == 0) {
			returnStr = "UPDATE authors SET firstname = '" + names.get(0) + "', lastname = '" + names.get(1) + "' WHERE authorid = ?";
		}
		// if the user is adding a new author, just return the string instead
		else if (flag == 1) {
			returnStr = names.get(0) + " " + names.get(1);
		}
		return returnStr;
	}

	// **************************************************************
	//
	// Method:      getAuthorIDs
	//
	// Description: Retrieves all current author IDs and returns an
	//				ArrayList containing them
	//
	// Parameters:  None
	//
	// Returns:     ArrayList<Integer> authorIDs
	//
	// **************************************************************
	public ArrayList<Integer> getAuthorIDs() {
		Statement authorTable = null;
		ResultSet authors = null;

		ArrayList<Integer> authorIDs = new ArrayList<>();

		// Try to create a statement
		try {
			authorTable = connection.createStatement();
		}
		catch (SQLException e) {
			System.err.println("Failed to create statement.");
			e.printStackTrace();
		}

		// Use helper method to execute query and get resultset back
		if (authorTable != null) {
			authors = execQuery(authorTable, authors, "SELECT authorid FROM AUTHORS");
		}

		// add all query results to authorIDs arraylist
		if (authors != null) {
			try {
				while (authors.next()) {
					authorIDs.add(authors.getInt("authorid"));
				}
			}
			catch (SQLException e) {
				System.err.println("Failed to retrieve query results.");
				e.printStackTrace();
			}
		}
		// cleanup
		close(authorTable, authors);

		// return arraylist of authorids
		return authorIDs;
	}

	// **************************************************************
	//
	// Method:     	findBooksByAuthor
	//
	// Description: Displays all books written by the author the user selected
	//
	// Parameters:  None
	//
	// Returns:     N/A
	//
	// **************************************************************
	public void findBooksByAuthor() {
		System.out.println("Current authors:");
		getAuthors();
		System.out.println("Please select the ID of the author whose books you want to view:");
		int authID = getInput(0);

		PreparedStatement displayBooks = null;
		ResultSet books = null;
		// Select query to get the name, isbn, title, and copyright from the author chosen by the user
		String insertStr = "SELECT a.firstname, a.lastname, t.isbn, t.title, t.copyright FROM authors a, authorisbn i, titles t WHERE a.authorid = i.authorid AND i.isbn = t.isbn AND a.authorid = ? ORDER BY a.lastname, a.firstname";

		// Helper method to execute query and get resultset back
		books = getResultSet(authID, displayBooks, books, insertStr);

		// Try to display the results of the previous query
		if (books != null) {
			try {
				System.out.printf("%-20s%-20s%-20s%-60s%s%n", "First Name", "Last Name", "ISBN", "Title",  "Copyright Year");
				while (books.next()) {
					String firstname = books.getString("firstname");
					String lastname = books.getString("lastname");
					String authorISBN = books.getString("isbn");
					String title = books.getString("title");
					String copyYear = books.getString("copyright");
					System.out.printf("%-20s%-20s%-20s%-60s%s%n", firstname, lastname, authorISBN, title, copyYear);
				}
			}
			catch (SQLException e) {
				System.err.println("Failed to display query results.");
				e.printStackTrace();
			}
			System.out.println();
		}
		// cleanup
		close(displayBooks, books);
	}

	// **************************************************************
	//
	// Method:     	getResultSet
	//
	// Description: Helper function for adding parameters and executing queries
	//
	// Parameters:  int authID,
	//				PreparedStatement displayBooks,
	//				ResultSet books,
	//				String insertStr
	//
	// Returns:     ResultSet books;
	//
	// **************************************************************
	public ResultSet getResultSet(int authID, PreparedStatement displayBooks, ResultSet books, String insertStr) {

		// Attempt to prepare statement
		try {
			displayBooks = connection.prepareStatement(insertStr);
		}
		catch (SQLException e) {
			System.err.println("Failed to create statement.");
			e.printStackTrace();
		}

		// try to set the authID and execute query
		if (displayBooks != null && authID != 0) {
			try {
				displayBooks.setInt(1, authID);
				books = displayBooks.executeQuery();
			}
			catch (SQLException e) {
				System.err.println("Failed to execute query.");
			}
		}
		else if (displayBooks != null) {
			try {
				books = displayBooks.executeQuery();
			}
			catch (SQLException e) {
				System.err.println("Failed to execute query.");
			}
		}
		// return the resultset
		return books;
	}

	// **************************************************************
	//
	// Method:     	editAuthor
	//
	// Description: Modifies an existing author based on the user-provided string
	//
	// Parameters:  None
	//
	// Returns:     N/A
	//
	// **************************************************************
	public void editAuthor() {
		System.out.println("Current authors:");
		getAuthors();
		System.out.println("Please select the ID of the author that you want to edit:");
		int authID = getInput(0);

		PreparedStatement editAuthor = null;
		ResultSet authors = null;
		String insertStr = getNewAuthorName(0);

		// Pass to helper method to set the values and execute the query
		authors = getResultSet(authID, editAuthor, authors, insertStr);

		// Verify that the query was successful
		if (authors != null) {
			try {
				if (authors.next()) {
					System.out.println("Author modified.");
				}
			}
			catch (SQLException e) {
				System.err.println("Failed to modify author.");
			}
		}

		// cleanup
		close(editAuthor, authors);
	}

	// **************************************************************
	//
	// Method:     	getTitles
	//
	// Description: Retrieves the ISBN of all current titles in the database
	//
	// Parameters:  None
	//
	// Returns:     ArrayList<String> titleISBNs
	//
	// **************************************************************
	public ArrayList<String> getTitleISBN() {
		Statement titleTable = null;
		ResultSet titles = null;

		ArrayList<String> titleISBNs = new ArrayList<>();

		// Try to create the statement from the connection
		try {
			titleTable = connection.createStatement();
		}
		catch (SQLException e) {
			System.err.println("Failed to create statement.");
			e.printStackTrace();
		}

		// Try to execute the query using the helper method
		if (titleTable != null) {
			titles = execQuery(titleTable, titles, "SELECT isbn FROM titles");
		}

		// Grab all the ISBNs from the query if it ran successfully
		if (titles != null) {
			try {
				while (titles.next()) {
					titleISBNs.add(titles.getString("isbn"));
				}
			}
			catch (SQLException e) {
				System.err.println("Failed to retrieve query results.");
				e.printStackTrace();
			}
		}

		// close statement and resultlist
		if (titleTable != null && titles != null) {
			close(titleTable, titles);
		}
		return titleISBNs;
	}

	// **************************************************************
	//
	// Method:     	addNewTitle
	//
	// Description: Adds a new title to the list of books based on user request
	//
	// Parameters:  None
	//
	// Returns:     String ISBN
	//
	// **************************************************************
	public String addNewTitle() {
		PreparedStatement titleTable = null;
		ResultSet titles = null;
		String sqlInsert = "INSERT INTO titles VALUES (?, ?, ?, ?)";

		ArrayList<String> titleISBNs = getTitleISBN();
		boolean valid = false;
		String ISBN = "";
		String title = "";
		int edition = 0;
		String copyright = "";

		// Get the ISBN of the new title
		System.out.println("Please enter the ISBN-10 of the book. Make sure it doesn't match any existing ISBNs, \nand make it exactly 10 characters long.");
		while (!valid) {
			if (input.hasNext()) {
				ISBN = input.next();
			}
			// Necessary to use .anyMatch
			String finalISBN = ISBN;
			// Check if any of the existing titles match the user provided ISBN and also that it's 10 characters
			if (titleISBNs.stream().anyMatch(str -> str.contains(finalISBN)) || ISBN.length() != 10) {
				System.out.println("Invalid entry. Please try again.");
			}
			// Regex for verifying that the ISBN provided matches the standard ISBN format and the format the DB expects
			else if (!ISBN.matches("^(?:ISBN(?:-10)?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3}) [- 0-9X]{13}$)[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$")) {
				System.out.println("Entry provided doesn't match ISBN-10 format. Please try again.");
			}
			else {
				valid = true;
			}
		}
		valid = false;
		System.out.println("Please enter the title of the book. When you are done, press enter.");

		// Get the title of the book. Can't really validate this one so I just grab it and move on
		while (!valid) {
			if (input.hasNext()) {
				title = input.nextLine();
			}
			if (!title.equals("")) {
				valid = true;
			}
		}
		valid = false;
		System.out.println("Please enter the edition number of the book.");
		// Get the edition number. Just check to make sure it's not negative, no other real bounds
		while (!valid) {
			try {
				edition = input.nextInt();
			}
			catch (NoSuchElementException e) {
				System.err.println("Not a number. Please provide a number.");
				input.next();
			}
			if (edition > 0) {
				valid = true;
			}
			else {
				System.out.println("Edition must be a positive number.");
			}
		}
		valid = false;
		System.out.println("Please enter the copyright year of the book in YYYY format, exactly four numbers long.");
		// Get the copyright year from the user
		while (!valid) {
			if (input.hasNext()) {
				copyright = input.next();
			}
			try {
				// Making sure the year is 4 digits long, and that it's between 1801 and the current year.
				// Note: 1801 is an arbitrary distinction, just popped into my head
				if (copyright.length() != 4 || Integer.parseInt(copyright) < 1800 || Integer.parseInt(copyright) > Year.now().getValue()) {
					System.out.println("Invalid copyright year. Please supply a valid copyright year.");
				}
				else {
					valid = true;
				}
			}
			catch (NumberFormatException e) {
				System.err.println("Value provided is not an integer. Please supply a numeric year.");
				input.next();
			}
		}
		// Try to set all the values in the SQL statement
		try {
			titleTable = connection.prepareStatement(sqlInsert);
			titleTable.setString(1, ISBN);
			titleTable.setString(2, title);
			titleTable.setInt(3, edition);
			titleTable.setString(4, copyright);
		}
		catch (SQLException e) {
			System.err.println("Failed to create statement.");
			e.printStackTrace();
		}

		// Run the query
		if (titleTable != null) {
			try {
				titles = titleTable.executeQuery();
			}
			catch (SQLException e) {
				System.err.println("Failed to execute query.");
			}
		}
		// Grab results of query to make sure it worked
		if (titles != null) {
			try {
				if (titles.next()) {
					System.out.println("New book added to table.");
				}
			}
			catch (SQLException e) {
				System.err.println("Failed to retrieve query results.");
				e.printStackTrace();
			}
		}
		// Close the statement and resultlist
		close(titleTable, titles);
		return ISBN;
	}

	// **************************************************************
	//
	// Method:     	associateAuthorWithTable
	//
	// Description: Associates an existing author with an existing book
	//
	// Parameters:  None
	//
	// Returns:     N/A
	//
	// **************************************************************
	public void associateAuthorWithTitle() {
		PreparedStatement addAuthorISBN = null;
		ResultSet resultAuthorISBN = null;

		String ISBN;
		int authorID;
		String sqlInsert = "INSERT INTO authorisbn VALUES (?, ?)";

		System.out.println("Current titles:");
		getTitles();
		System.out.println("Please select the number of the title you would like to assign an author to.");
		System.out.println("If you would like to add a new title, please select 0.");

		int titleID = getInput(1);

		// If they want to add a new title
		if (titleID == 0) {
			ISBN = addNewTitle();
		}
		else {
			ISBN = getTitleISBN().get(titleID-1);
		}

		getAuthors();
		System.out.println("Please select the ID of the author you want to associate the book with.");
		System.out.println("If you want to add a new author, please select 0.");
		authorID = getInput(2);

		if (authorID == 0) {
			addAuthor();
			ArrayList<Integer> authorIDs = getAuthorIDs();
			authorID = authorIDs.get(authorIDs.size() - 1);
		}

		// Try to prepare the statement and set the values
		try {
			addAuthorISBN = connection.prepareStatement(sqlInsert);
			addAuthorISBN.setInt(1, authorID);
			addAuthorISBN.setString(2, ISBN);
		}
		catch (SQLException e) {
			System.err.println("Failed to prepare query.");
		}

		// Try to execute the query
		if (addAuthorISBN != null) {
			try {
				resultAuthorISBN = addAuthorISBN.executeQuery();
			}
			catch (SQLException e) {
				System.err.println("Failed to execute query.");
			}
		}

		// Check if the query was executed successfully
		if (resultAuthorISBN != null) {
			try {
				if (resultAuthorISBN.next()) {
					System.out.println("Author and ISBN added to AuthorISBN table.");
				}
			}
			catch (SQLException e) {
				System.err.println("Failed to retrieve query results.");
			}
		}
		// Close the statement and resultlist
		close(addAuthorISBN, resultAuthorISBN);
	}

	// **************************************************************
	//
	// Method:     	execQuery
	//
	// Description: Helper method to execute queries and return the ResultSet
	//
	// Parameters:  Statement state,
	//				ResultSet result,
	//				String sql
	//
	// Returns:     return result
	//
	// **************************************************************
	public ResultSet execQuery(Statement state, ResultSet result, String sql) {
		try {
			result = state.executeQuery(sql);
		}
		catch (SQLException e) {
			System.err.println("Failed to execute query.");
		}
		return result;
	}

	// **************************************************************
	//
	// Method:     	close
	//
	// Description: Helper method to close unused Statements and ResultSets
	//
	// Parameters:  Statement state,
	//				ResultSet result
	//
	// Returns:     N/A
	//
	// **************************************************************
	public void close(Statement state, ResultSet result) {
		try {
			state.close();
			result.close();
		}
		catch (SQLException | NullPointerException ignored) {

		}
	}

	// **************************************************************
	//
	// Method:     	cleanup
	//
	// Description: Closes the connection upon exiting the program
	//
	// Parameters:  None
	//
	// Returns:     N/A
	//
	// **************************************************************
	public void cleanup() {
		try {
			connection.close();
		}
		catch (SQLException e) {
			System.err.println("Failed to close connection.");
		}
	}
}
