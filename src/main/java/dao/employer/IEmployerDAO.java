package dao.employer;

import dao.DALException;
import dto.employer.IEmployerDTO;

import java.util.List;

/**
 * @author Rasmus Sander Larsen
 */
public interface IEmployerDAO {

    /*
    ---------------------- Public Methods -----------------------
     */

    // returns a EmployerDTO from an workplaceID
    IEmployerDTO getIEmployer(int employerID) throws DALException;

    // Returns a List of ALL EmployerDTO object.
    List<IEmployerDTO> getiEmployerList() throws DALException;

    // Returns a List of ONE workers EmployerDTO object.
    List<IEmployerDTO> getiEmployerList(int workerID) throws DALException;

    // Inserts the data from a EmployerDTO into DB.
    void createiEmployer(IEmployerDTO employerDTO) throws DALException;

    // Updates the data on a employer row in the DB.
    void updateiEmployer(IEmployerDTO employerDTO) throws DALException;

    // Deletes all information about one employer.
    void deleteiEmployer(int employerID) throws DALException;

}
