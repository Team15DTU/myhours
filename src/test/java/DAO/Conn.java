package DAO;

import db.IConnPool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conn implements IConnPool {
	
	public Connection getConn()
	{
		// DiplomPortal Database
		String password = "iowRz3Cj5QgJ8Ok7dX7iI";
		String user = "s160107";	// Alfred
		String url = "jdbc:mysql://ec2-52-30-211-3.eu-west-1.compute.amazonaws.com";

		Connection conn = null;

		try
		{
			conn = DriverManager.getConnection("jdbc:mysql://ec2-52-30-211-3.eu-west-1.compute.amazonaws.com/s160107?"
					+ "user=s160107&password=iowRz3Cj5QgJ8Ok7dX7iI");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		// Return the Connection
		return conn;
	}

	@Override
	public void releaseConnection(Connection connection) throws DALException {

	}
}