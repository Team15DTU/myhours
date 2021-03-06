package db.connectionPools;

import dao.DALException;
import db.IConnPool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alfred Röttger Rydahl
 */
public class ConnPoolV1 implements IConnPool
{
    /*------------------------------------------------------------
    | Fields                                                     |
    -------------------------------------------------------------*/
    protected static ConnPoolV1 instance ;
    
    //region keepAlive()
    protected int refreshRate 	= 30000; 	// 30 seconds
	protected int validTimeout	= 2;		// 02 seconds
	protected boolean stop		= false;
	//endregion
 
	/*
	Hibernate uses 2 connections, and this uses 2. This means we have
	1 connection free to use with Workbench and Datagrip.
	 */
    public static final int MAXCONNS = 3;
    
    //region DB Info
	protected static String url = "ec2-52-30-211-3.eu-west-1.compute.amazonaws.com/s185097?";
	protected static String user = "s185097";
	protected static String password = "qsNAphOJ13ySzlpn1kh6Y";
	//endregion
    
    protected List<Connection> freeConnList;
    protected List<Connection> usedConnList;
    
    /*------------------------------------------------------------
    | Constructors                                               |
    -------------------------------------------------------------*/
	/**
	 * Creates the ConnPoolV1 object, and initializing everything.
	 */
	protected ConnPoolV1()
	{
		// Instantiating Lists
		freeConnList = new ArrayList<>(MAXCONNS);
		usedConnList = new ArrayList<>(MAXCONNS);
		
		// Create all Connections
		try
		{
			// Specify Driver
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			for (int i = 0; i < MAXCONNS; i++)
				freeConnList.add(createConnection());
		}
		catch (DALException e)
		{
			System.err.println("ERROR: Creating Connection Pool - " + e.getMessage());
		}
		catch ( ClassNotFoundException e )
		{
			System.err.println("ERROR: Creating Connection Pool, Can't find class - " + e.getMessage());
		}
		
		//Start thread to keep connections alive
		keepAlive();
	}
	
    /*------------------------------------------------------------
    | Properties                                                 |
    -------------------------------------------------------------*/
	/**
	 * Gets the total amount of connections in this pool, both in-use and
	 * free connections.
	 * @return Amount of connections in pool as int
	 */
	public int getSize()
	{
		return freeConnList.size() + usedConnList.size();
	}
	
	/**
	 * Gets the total amount of available connections in the connection
	 * pool.
	 * @return amount of available connections as int
	 */
	public int getFreeConns()
	{
		return freeConnList.size();
	}
	
	/**
	 * Gets the total amount of connections in use.
	 * @return Amount of connections in use
	 */
	public int getUsedConns()
	{
		return usedConnList.size();
	}
	
	/**
	 * Gets the current refresh rate of every connection update.
	 * @return Rate in milliseconds as int
	 */
	public int getRefreshRate()
	{
		return refreshRate;
	}
	
	/**
	 * Sets the refresh rate of the connection update.
	 * @param millis The time to wait in milliseconds
	 */
	public synchronized void setRefreshRate(int millis)
	{
		this.refreshRate = millis;
	}
	
	/**
	 * Gets the time which is used to determine how long to wait for a
	 * connection to return valid.
	 * @return The time in seconds as int
	 */
	public int getValidTimeout()
	{
		return validTimeout;
	}
	
	/**
	 * Sets the time to wait for a connection to be determined alive.
	 * @param validTimeout Time in seconds
	 */
	public synchronized void setValidTimeout(int validTimeout)
	{
		this.validTimeout = validTimeout;
	}
	
	/**
	 * Gets the SQL DB user.
	 * @return A String
	 */
	public String getUser() {
		return user;
	}

	/*------------------------------------------------------------
    | Public Methods                                             |
    -------------------------------------------------------------*/
	/**
	 * Gives the instance of the Connection Pool.
	 * @return ConnPoolV1 object
	 */
	public synchronized static ConnPoolV1 getInstance()
	{
		try
		{
			if (instance == null)
				instance = new ConnPoolV1();
			
			return instance;
		}
		catch (Exception e)
		{
			System.err.println( String.format("ERROR: Couldn't get ConnPoolV1 instance - %s \n \t Cause: %s",
												e.getMessage(), e.getCause()) );
			throw e;
		}
	}
	
	/**
	 * Returns the Connection to the connection pool.
	 * @param connection The Connection to return
	 */
	@Override
	public synchronized void releaseConnection(Connection connection)
	{
		// Make sure connection given back isn't null
		if ( connection == null )
			return;
		
		try
		{
			// Roll back unfinished transactions
			if ( !connection.getAutoCommit() )
				connection.rollback();
		}
		catch (SQLException e)
		{
			System.err.println("ERROR: Release connection and Rollback failure - " + e.getMessage());
		}
		catch (Exception e)
		{
			System.err.println("ERROR: Release connection - " + e.getMessage());
		}
		finally
		{
			// Put connection back into freeConnList and remove from usedConnList
			usedConnList.remove(connection);
			freeConnList.add(connection);
		}
	}
	
	/**
	 * Gives a live Connection to the Database, which needs to be returned after
	 * use.
	 * @return Return a Connection object
	 * @throws DALException Data Access Layer Exception
	 */
	@Override
	public synchronized Connection getConn() throws DALException
	{
		Connection connection;

		// Start loop
		while (true)
		{
			// Check if there's any free connections
			if ( freeConnList.isEmpty() )
			{
				// Wait a little and try again
				try
				{
					wait(200);	// 0,2 seconds
				}
				catch ( InterruptedException e )
				{
					System.err.println("ERROR: getConn() delay error - " + e.getMessage());
					throw new DALException(e.getMessage());
				}
			}
			
			else
			{
				try
				{
					// Get the first connection
					connection = freeConnList.get(freeConnList.size() - 1);
					
					// If connection isClosed then make a new and return
					if ( connection == null || connection.isClosed() )
					{
						connection = createConnection();
						
						usedConnList.add(connection);
						freeConnList.remove(freeConnList.size() - 1);
						
						return connection;
					}
					
					// Otherwise, return connection normally
					else
					{
						usedConnList.add(connection);
						freeConnList.remove(connection);
						
						return connection;
					}
				}
				catch ( SQLException e)
				{
					System.err.println("ERROR: getConn() failure - " + e.getMessage());
					throw new DALException(e.getMessage());
				}
				catch ( Exception e )
				{
					System.err.println("ERROR: Unknown getConn() failure - " + e.getMessage());
					throw new DALException(e.getMessage());
				}
			}
		}
	}
	
	/**
	 * This method is shutting down the connection pool. Making sure
	 * that every connection is closed correctly, and stops all
	 * related threads.
	 * @throws DALException Data Access Layer Exception
	 */
	@Override
	public void closePool() throws DALException
	{
		// Make keepAlive thread stop
		stop = true;
		
		// Close all connections in both Lists
		try
		{

			int i; int freeConns = freeConnList.size(); int usedConns = usedConnList.size();
			for ( i=0; i < freeConns; i++ ) { closeConnection(freeConnList.get(i)); }
			for ( i=0; i < usedConns; i++ ) { closeConnection(usedConnList.get(i)); }
		}
		catch ( SQLException e )
		{
			System.err.println( String.format("ERROR: Error trying to close connection pool - %s", e.getMessage()) );
			throw new DALException( e.getMessage(), e.getCause() );
		}
		catch ( Exception e )
		{
			System.err.println( String.format("ERROR: Unknown error in closePool() - %s", e.getMessage()) );
			throw new DALException( e.getMessage(), e.getCause() );
		}
		finally
		{
			// Erase reference to instance and call GC
			instance = null;
			System.gc();
		}
	}
	
    /*------------------------------------------------------------
    | Protected Methods                                            |
    -------------------------------------------------------------*/
	/**
	 * Establishes a connection with the Database.
	 * This is Thread safe.
	 * @return Connection object
	 * @throws DALException Data Access Layer Exception
	 */
	protected Connection createConnection() throws DALException
	{
		try
		{
			return DriverManager.getConnection("jdbc:mysql://"+url,user, password);
		}
		catch (SQLException e)
		{
			System.err.println("ERROR: Create Connection Failure! - " + e.getMessage());
			throw new DALException(e.getMessage(), e.getCause());
		}
	}
	
	/**
	 * Closed the given SQL Connection the correct way. If the connection
	 * is already closed, then this won't do anything.
	 * This is Thread safe.
	 * @param c The SQL Connection to close
	 * @throws SQLException Handle this
	 */
	protected void closeConnection(Connection c) throws SQLException
	{
		// Check if connection is null
		if ( c != null )
		{
			// Check if connection is already closed
			if ( !c.isClosed() )
			{
				// If autocommit == true, then just close
				if (c.getAutoCommit()) { c.close(); }
				
				// Otherwise, make sure to make rollback first
				else
				{
					c.rollback();
					c.close();
				}
			}
		}
	}
	
	/**
	 * Keeps all Connections alive in freeConnList. For every "refreshRate" milliseconds
	 * it runs through the whole List, and checks if there's any problems with any
	 * of the connections.
	 * If a problem with a connection is detected, it refreshes the connection by
	 * creating a new one, and makes the same variable point to the newly created
	 * connection.
	 * As the method is called, it starts and runs on its own thread, and the main thread
	 * continues execution. The thread is a daemon thread, that runs in the background
	 * with low priority.
	 */
	protected void keepAlive()
	{
		// Encapsulate in thread
		Thread th = new Thread(() ->
		{
			// Start forever loop
			while (!stop)
			{
				// Sleep the thread a set amount of time
				try
				{
					Thread.sleep(refreshRate);
				}
				catch (InterruptedException e)
				{
					System.err.println( String.format("ERROR: Couldn't sleep Connection refresh thread - %s", e.getMessage()) );
				}
				
				// Loop through all free connections
				Connection c = null;
				for ( int i=0; i < freeConnList.size(); i++ )
				{
					// Check if GC is closing down
					if (stop)
						break;
					
					c = freeConnList.get(i);	// Used for all checks and to close
					try
					{
						// Check if it's closed
						if ( c.isClosed() ) { freeConnList.set(i, createConnection()); }
						
						// Check if it has errors
						else if ( !(c.getWarnings() == null) || !c.isValid(validTimeout) )
						{ c.close(); freeConnList.set(i, createConnection()); }
					}
					catch (SQLException e)
					{
						System.err.println( String.format("ERROR: keepAlive error - %s", e.getMessage()) );
					}
					catch (DALException e)
					{
						System.err.println( String.format("ERROR: DALException keepAlive error - %s", e.getMessage()) );
					}
				}
			}
		});
		
		// Set it as daemon thread and start
		th.setDaemon(true);
		th.start();
	}
}
