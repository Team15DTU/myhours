package db;

import DAO.DALException;
import DAO.job.IJobDAO;
import DAO.job.JobDAO;
import DAO.shift.IShiftDAO;
import DAO.shift.ShiftDAO;
import DAO.workPlace.IWorkPlaceDAO;
import DAO.workPlace.WorkPlaceDAO;
import DAO.worker.IWorkerDAO;
import DAO.worker.WorkerDAO;
import DTOs.worker.WorkerDTO;

import java.sql.*;
import java.util.TimeZone;

/**
 * @author Rasmus Sander Larsen
 */
public class DBController {

    /*
    -------------------------- Fields --------------------------
     */
    
    private IConnPool iConnPool;
    private IWorkerDAO iWorkerDAO;
    private IWorkPlaceDAO iWorkPlaceDAO;
    private IJobDAO iJobDAO;
    private IShiftDAO iShiftDAO;

    
    /*
    ----------------------- Constructor -------------------------
     */
    
    public DBController (IConnPool iConnPool) throws DALException {

        this.iConnPool = iConnPool;

        TimeZone.setDefault(TimeZone.getTimeZone(setTimeZoneFromSQLServer()));

        iWorkerDAO = new WorkerDAO(this.iConnPool);
        iWorkPlaceDAO = new WorkPlaceDAO(this.iConnPool);
        iJobDAO = new JobDAO(this.iConnPool);
        iShiftDAO = new ShiftDAO(this.iConnPool);

    }
    
    /*
    ------------------------ Properties -------------------------
     */

    // <editor-folder desc="Properties"

    public IWorkerDAO getiWorkerDAO() {
        return iWorkerDAO;
    }

    public void setiWorkerDAO(IWorkerDAO iWorkerDAO) {
        this.iWorkerDAO = iWorkerDAO;
    }

    public IWorkPlaceDAO getiWorkPlaceDAO() {
        return iWorkPlaceDAO;
    }

    public void setiWorkPlaceDAO(IWorkPlaceDAO iWorkPlaceDAO) {
        this.iWorkPlaceDAO = iWorkPlaceDAO;
    }

    public IJobDAO getiJobDAO() {
        return iJobDAO;
    }

    public void setiJobDAO(IJobDAO iJobDAO) {
        this.iJobDAO = iJobDAO;
    }

    public IShiftDAO getiShiftDAO() {
        return iShiftDAO;
    }

    public void setiShiftDAO(IShiftDAO iShiftDAO) {
        this.iShiftDAO = iShiftDAO;
    }


    // </editor-folder>
    
    /*
    ---------------------- Public Methods -----------------------
     */

    public int getNextAutoIncremental(String tableName) throws DALException {

        try (Connection c = iConnPool.getConn()) {

            Statement statement = c.createStatement();
            statement.executeQuery("ANALYZE TABLE " + tableName);

            PreparedStatement pStatement = c.prepareStatement(
                    "SELECT `auto_increment` FROM INFORMATION_SCHEMA.TABLES " +
                            " WHERE table_name = ?");
            pStatement.setString(1, tableName);

            ResultSet resultset = pStatement.executeQuery();

            resultset.next();

            return resultset.getInt(1);

        } catch (SQLException e) {
            throw new DALException(e.getMessage());
        }
    }
    
    /*
    ---------------------- Support Methods ----------------------
     */

    private String setTimeZoneFromSQLServer ()  throws DALException{
        Connection c = iConnPool.getConn();
        try {
            Statement statement = c.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT @@system_time_zone");
            resultSet.next();
            return resultSet.getString(1);

        } catch (SQLException e) {
            throw new DALException(e.getMessage());
        } finally {
            iConnPool.releaseConnection(c);
        }
    }

    public void createWorker (WorkerDTO workerDTO, String password) throws DALException {

        iWorkerDAO.createWorker(workerDTO,password);

    }

}