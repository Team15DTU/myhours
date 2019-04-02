package DAO.job;

import DAO.DALException;
import DTOs.job.JobDTO;

import java.util.List;

/**
 * @author Rasmus Sander Larsen
 */
public interface IJobDAO {

    /**
     * This methods returns a JobDTO object with details from DB matching the inputted jobID.
     * @param jobID This is the jobID we are looking for in the DB and the details that is loaded into the returned JobDTO.
     * @return A JobDTO object with details matching the information in DB on th inputted jobID.
     * @throws DALException Will throw a DALException.
     */
    JobDTO getJob(int jobID) throws DALException;

    /**
     * This method returns a List of JobDTOs. This list should contain all jobs existing in the DB.
     * @return A List of JobDTO objects.
     * @throws DALException Will throw a DALException.
     */
    List<JobDTO> getJobList () throws DALException;

    /**
     * This method returns a List of JobDTOs matching the inputted condition.
     * @param condition This sting will be inserted after the "WHERE" clause in the "SELECT * FROM" query.
     * @return A List object of JobDTO objects matching the inputted Condition.
     * @throws DALException Will throw a DALException.
     */
    List<JobDTO> getJobList (String condition) throws DALException;

    /**
     * This method inserts the details from the inputted JobDTO object into the DB.
     * @param jobDTO The values of the JobDTO object variables are inserted into the correct table in the DB.
     * @throws DALException Will throw a DALException.
     */
    void createJob (JobDTO jobDTO) throws DALException;

    /**
     * This method updates a Job details in the DB, with the values from the inputted JobDTO object.
     * @param jobDTO This is the JobDTO object which values the Job is updated with.
     * @return Returns a number saying how many columns which had its value changed as a result of the query.
     * @throws DALException Will throw a DALException.
     */
    int updateJob (JobDTO jobDTO) throws DALException;

    /**
     * This method will delete all information about the inputted jobID.
     * @param jobID This jobID is the job that will be deleted.
     * @throws DALException Will throw a DALException.
     */
    void deleteJob (int jobID) throws DALException;

}