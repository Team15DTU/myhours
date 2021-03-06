package db.connectionPools;

import dao.DALException;
import db.IConnPool;
import db.TestConnPoolV1;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class ConnPoolV1Test {
	
	private ConnPoolV1 connPool;
	private int connSize;
	
	/**
	 * This eliminates the possibility failures caused by
	 * other tests that doesn't release all connections
	 * or doesn't close the pool after use.
	 * @throws Exception Could be thrown from closing the connection pool
	 */
	@BeforeClass
	public static void beforeClass() throws Exception
	{
		ConnPoolV1 c = TestConnPoolV1.getInstance();
		if ( c != null )
			c.closePool();
	}
	
	@Before
	public void setUp() throws Exception
	{
		connPool = TestConnPoolV1.getInstance();
		connSize = connPool.getSize();
	}
	
	@After
	public void tearDown()
	{
		try
		{
			connPool.closePool();
		}
		catch (Throwable e)
		{
			System.err.println("ERROR: Couldn't call closePool() in tearDown() - " + e.getMessage());
		}
	}
	
	@Test
	public void getSize()
	{
		assertEquals(connSize, connPool.getSize());
	}
	
	@Test
	public void getFreeConns()
	{
		assertEquals(connSize, connPool.getFreeConns());
		
		// Take one connection
		Connection c = null;
		try
		{
			c = connPool.getConn();
		}
		catch (DALException e)
		{
			fail("ERROR: Couldn't get a connection from the pool - " + e.getMessage());
		}
		
		assertEquals(connSize-1, connPool.getFreeConns());
		
		// Put back connection
		connPool.releaseConnection(c);

		assertEquals(connSize, connPool.getFreeConns());
	}
	
	@Test
	public void getUsedConns()
	{
		// Make sure it's zero at the start
		assertEquals(0, connPool.getUsedConns());
		
		// Take a connection, and check that there's one used connection now
		Connection c = null;
		try
		{
			c = connPool.getConn();
		}
		catch ( DALException e )
		{
			System.err.println("ERROR: Couldn't get a connection from pool - " + e.getMessage());
		}
		assertEquals(1, connPool.getUsedConns());
		
		// Release connection again
		connPool.releaseConnection(c);

		assertEquals(0, connPool.getUsedConns());
	}
	
	@Test
	public void getAndsetRefreshRate()
	{
		int change = 2000;
		
		// Change it, and make sure it's correct
		connPool.setRefreshRate(change);
		assertEquals(change, connPool.getRefreshRate());
		
		// Change again and check
		connPool.setRefreshRate(change + 50);
		assertEquals(change+50, connPool.getRefreshRate());
		
		// Change again and check
		connPool.setRefreshRate(change + 100);
		assertEquals(change+100, connPool.getRefreshRate());
	}
	
	@Test
	public void getAndsetValidTimeout()
	{
		int change = 2000;
		
		// Change it, and make sure it's correct
		connPool.setValidTimeout(change);
		assertEquals(change, connPool.getValidTimeout());
		
		// Change it, and make sure it's correct
		connPool.setValidTimeout(change + 50);
		assertEquals(change+50, connPool.getValidTimeout());
		
		// Change it, and make sure it's correct
		connPool.setValidTimeout(change + 100);
		assertEquals(change+100, connPool.getValidTimeout());
	}
	
	@Test
	public void releaseConnection() throws SQLException
	{
		//region Get two connections and check parameters
		Connection c1 = null, c2 = null;
		
		try
		{
			c1 = connPool.getConn();
			c2 = connPool.getConn();
		}
		catch ( DALException e )
		{
			System.err.println("ERROR: Couldn't get connections - " + e.getMessage());
		}
		
		assertEquals(connSize-2, connPool.getFreeConns());
		assertEquals(2, connPool.getUsedConns());
		assertEquals(connSize, connPool.getSize());
		assertTrue(c1.isValid(1000));
		assertTrue(c2.isValid(1000));
		
		//endregion
		
		//region Release first connection and check parameters
		connPool.releaseConnection(c1);

		assertEquals(connSize-1, connPool.getFreeConns());
		assertEquals(1, connPool.getUsedConns());
		assertEquals(connSize, connPool.getSize());
		
		//endregion
		
		//region Release second connection and check parameters
		connPool.releaseConnection(c2);

		
		assertEquals(connSize, connPool.getFreeConns());
		assertEquals(0, connPool.getUsedConns());
		assertEquals(connSize, connPool.getSize());
		
		//endregion
		
		//region Try to release a connection which is null
		Connection nullConnection = null;
		
		connPool.releaseConnection(nullConnection);

		assertEquals(connSize, connPool.getFreeConns());
		assertEquals(0, connPool.getUsedConns());
		assertEquals(connSize, connPool.getSize());
		
		//endregion
	}
	
	@Test
	public void getConn() throws SQLException
	{
		// Get a connection and check parameters
		Connection c1 = null;
		try
		{
			c1 = connPool.getConn();
		}
		catch ( DALException e )
		{
			System.err.println("ERROR: Can't get first connection from pool - " + e.getMessage());
		}
		
		assertTrue(c1.isValid(1000));
		assertEquals(connSize-1, connPool.getFreeConns());
		assertEquals(1, connPool.getUsedConns());
		assertEquals(connSize, connPool.getSize());
		
		// Get one more connection and check parameters
		Connection c2 = null;
		try
		{
			c2 = connPool.getConn();
		}
		catch ( DALException e )
		{
			System.err.println("ERROR: Can't get second connection from pool - " + e.getMessage());
		}
		
		assertTrue(c2.isValid(1000));
		assertEquals(connSize-2, connPool.getFreeConns());
		assertEquals(2, connPool.getUsedConns());
		assertEquals(connSize, connPool.getSize());
	}
}