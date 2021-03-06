package dao.worker;

import dao.DALException;
import db.IConnPool;
import db.TestConnPoolV1;
import dto.worker.IWorkerDTO;
import dto.worker.WorkerDTO;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import testData.TestDataController;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@FixMethodOrder(MethodSorters.JVM)		// Make sure tests run in order
public class WorkerDAOTest
{
	
	//region Test Material
	
	private static IConnPool connPool;
	
	private String firstName0 = "Bo"; String surName0 = "Børgesen";
	private String email0 = String.format("%s.%s@hotmail.com", firstName0, surName0);
	private LocalDate birthday0 = LocalDate.now();
	private IWorkerDTO worker0 = new WorkerDTO(firstName0, surName0, email0, birthday0, null);
	
	private String firstName1 = "Geden"; String surName1 = "Johannes";
	private String email1 = String.format("%s.%s@hotmail.com", firstName1, surName1);
	private LocalDate birthday1 = LocalDate.now();
	private IWorkerDTO worker1 = new WorkerDTO(firstName1, surName1, email1, birthday1, null);
	
	private IWorkerDTO[] testWorkers = {worker0, worker1};

	//endregion
	
	
	@BeforeClass
	public static void setUp() throws Exception
	{ connPool = TestConnPoolV1.getInstance(); }
	
	@AfterClass
	public static void tearDown() throws Exception
	{
		Connection conn = null;
		try
		{
			conn = connPool.getConn();
			
			Statement stmt = conn.createStatement();
			
			stmt.execute( String.format("DELETE FROM %s", WorkerConstants.TABLENAME) );
			
			stmt.close();
		}
		finally
		{
			if ( conn != null )
				connPool.releaseConnection(conn);
			connPool.closePool();
		}
	}
	
	@Test
	public void createWorker() throws DALException
	{
		IWorkerDAO workerDAO = new WorkerDAO(connPool);
		
		// Try to Create them
		for (IWorkerDTO worker : testWorkers)
			workerDAO.createWorker(worker);
		
		// Delete again
		for (IWorkerDTO worker : testWorkers)
			workerDAO.deleteWorker(worker.getEmail());
	}
	
	@Test
	public void getWorker() throws DALException
	{
		IWorkerDAO workerDAO = new WorkerDAO(TestConnPoolV1.getInstance());
		IWorkerDTO worker = TestDataController.getTestWorkerNo1();
		
		// Create worker in DB
		workerDAO.createWorker( worker );
		
		// Get the worker by Email and test
		IWorkerDTO eWorker = workerDAO.getWorker( worker.getEmail() );
		
		assertEquals(worker.getFirstName(), eWorker.getFirstName());
		assertEquals(worker.getSurName(), eWorker.getSurName());
		
		/*
		-----------------------------------------------------------------------------------------
		 */
		
		// Get the worker by ID and test
		worker.setWorkerID(eWorker.getWorkerID());
		IWorkerDTO iWorker = workerDAO.getWorker(worker.getWorkerID());
		
		assertEquals(worker.getFirstName(), iWorker.getFirstName());
		assertEquals(worker.getSurName(), iWorker.getSurName());
		
		// Delete worker again
		workerDAO.deleteWorker( worker.getEmail() );
	}
	
	@Test
	public void getWorkerList() throws DALException
	{
		IWorkerDAO workerDAO = new WorkerDAO(connPool);
		
		// Create two extra workers - three total
		for (IWorkerDTO worker : testWorkers)
			workerDAO.createWorker(worker);
		
		// Get list
		List<IWorkerDTO> workerList = workerDAO.getWorkerList();
		
		// Check currently added workers
		boolean first = false;
		boolean second = false;
		for (IWorkerDTO fromDB : workerList)
		{
			if (fromDB.getEmail().equals(email0))
				first = true;
			
			else if (fromDB.getEmail().equals(email1))
				second = true;
			
			if (first && second)
				break;
		}
		assertTrue(first); assertTrue(second);
		
		// Delete the extra workers
		for (IWorkerDTO worker : testWorkers)
			workerDAO.deleteWorker(worker.getEmail());
	}
	
	@Test
	public void updateWorker()
	{
	}
	
	@Test
	public void deleteWorker()
	{}
}