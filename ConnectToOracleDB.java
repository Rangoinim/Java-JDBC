//***************************************************************
//
//  Developer:         Instructor
//
//  Project #:         10 - Capstone 
//
//  File Name:         ConnectToOracleDB.java
//
//  Course:            COSC 4301 Modern Programming 
//
//  Due Date:          December 12, 2021
//
//  Instructor:        Fred Kumi 
//
//  Description:
//      <An explanation of what the program is designed to do>
//
//***************************************************************
import java.sql.*;
import java.util.Scanner;

public class ConnectToOracleDB
{
	/** The Constant dbURL. */
	private final static String db_Home_URL = "jdbc:oracle:thin:@localhost:1521:CSOR";
	private final static String dbACC_URL = "jdbc:oracle:thin:@coisor.austincc.edu:1527:CSOR";	

	Scanner input;

	/** The Database Utils. */
	private Connection connection;

	public ConnectToOracleDB()
	{
		connection = null;
	}
	
	//***************************************************************
	//
	// Method:      loadDrivers
	//
	// Description: Loads the Oracle Drivers
	//
	// Parameters:  None
	//
	// Returns:     N/A
	//
	//**************************************************************
	public void loadDrivers()
	{
		try {
			// Load the driver class
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			System.err.println("Failed to load SQL driver." + e);
			System.exit(1);
		}
	}

	//***************************************************************
	//
	// Method:      connectDriver
	//
	// Description: The main line routine of the program
	//              @throws Exception the exception
	//
	// Parameters:  None
	//
	// Returns:     N/A
	//
	//**************************************************************
	public Connection connectDriver() throws Exception
	{
		String connectAcc = "N";
				
		input = new Scanner(System.in);
				
		System.out.print("Are you connecting to the ACC CIT Oracle database<N>? ");
		connectAcc = input.nextLine();
		
        if (connectAcc.equalsIgnoreCase("Y"))
        	connection = dbLocationACC();
		else
			connection = dbLocationHome();
        
        return connection;
	}

	//***************************************************************
	//
	// Method:      dbLocationACC
	//
	// Description: 
	//
	// Parameters:  None
	//
	// Returns:     N/A
	//
	//**************************************************************
	private Connection dbLocationACC() throws SQLException
	{
		String dbURL = dbACC_URL;
		
		System.out.print("\nEnter your ACC Oracle user name: ");
		String userName = input.next();

		System.out.print("Enter your ACC Oracle password: ");
		String userPassword = input.next();

		System.out.println("\nConnecting to the ACC CIT Oracle database...");
		connection = connectToDb(dbURL, userName, userPassword);
		
        return connection;
    }
	
	//***************************************************************
	//
	// Method:      dbLocationHome
	//
	// Description: 
	//
	// Parameters:  None
	//
	// Returns:     N/A
	//
	//**************************************************************
	private Connection dbLocationHome() throws SQLException
	{
		String dbURL = db_Home_URL;
		
		System.out.print("\nEnter your home Oracle database user name: ");
		String userName = input.next();

		System.out.print("Enter your home Oracle database password: ");
		String userPassword = input.next();

		System.out.println("\nConnecting to your home Oracle database...");
		connection = connectToDb(dbURL, userName, userPassword);
		
        return connection;
    }

	//***************************************************************
	//
	// Method:      connectToDb
	//
	// Description: Connect to the Database.
	//              @throws Exception the exception
	//
	// Parameters:  DB URL, User ID, and DB Password
	//
	// Returns:     true, if successful
	//
	//**************************************************************
	private Connection connectToDb(String dbURL, String dbUser, String dbPasswd) throws SQLException
	{
		boolean rtnCode = false;
		DatabaseMetaData databaseMetaData = null;
		
		try {
			connection = DriverManager.getConnection(dbURL, dbUser, dbPasswd);
			if (connection != null) {
				rtnCode = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (rtnCode)
		{
			System.out.println("The database connection was successful\n");
		    databaseMetaData = connection.getMetaData();
		    System.out.println("Product Name    : " +  databaseMetaData.getDatabaseProductName());
		    System.out.println("Product Version : " +  databaseMetaData.getDatabaseProductVersion());
		    System.out.println("Driver Version  : " +  databaseMetaData.getDriverVersion());
		}
		else
		{
			System.err.println("\nThe database connection was not successful\n");
		}
		
		return connection;
	}

	//***************************************************************
	//
	// Method:      closeDBConnection
	//
	// Description: Close the Database connection
	//
    //              @throws Exception the exception
    //              @throws SQLException the sQL exception
	//
	// Parameters:  None
	//
	// Returns:     N/A
	//
	//**************************************************************
	public void closeDBConnection() throws Exception {
		System.out.println("\nClosing the Database Connection...");

		try {
			if (connection != null)
				connection.close(); // Close the database connection
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
