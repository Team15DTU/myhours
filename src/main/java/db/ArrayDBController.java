package db;

import dto.activity.ActivityDTO;
import dto.activity.IActivityDTO;
import dto.employer.EmployerDTO;
import dto.employer.IEmployerDTO;
import dto.job.IJobDTO;
import dto.job.JobDTO;
import dto.worker.IWorkerDTO;
import dto.worker.WorkerDTO;
import dto.worker.WorkerHiberDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Rasmus Sander Larsen
 */
@Path("/ArrayDBController")
public class ArrayDBController implements IDBController {

    /*
    -------------------------- Fields --------------------------
     */
    
    private static ArrayList<IWorkerDTO> workerList;
    public static int workerID = 1;
    public static int employerID = 1;
    public static int jobID = 1;
    public static int activityID = 1;
    
    /*
    ----------------------- Constructor -------------------------
     */

    public ArrayDBController () {
        if (workerList == null){
            workerList = setArrayListWithStartData();
        }
    }
    
    /*
    ------------------------ Properties -------------------------
     */

    // <editor-folder desc="Properties"

    public ArrayList<IWorkerDTO> getWorkerList() {
        return workerList;
    }

    public void setWorkerList(ArrayList<IWorkerDTO> workerList) {
        this.workerList = workerList;
    }

    // </editor-folder>
    
    /*
    ---------------------- Public Methods -----------------------
     */
    
    /**
     * This method takes an object that implements the IWorkerDTO
     * interface, and saves it in the database.
     * @param workerDTO Object that implements the IWorkerDTO interface
     */
    @POST
    @Path("/createWorker")
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public synchronized void createWorker(IWorkerDTO workerDTO) {

        workerList.add(workerDTO);
    }
    
    /**
     * This methods returns a FULL IWorkerDTO Object.
     * Including:
     * 1) A list of the workers Workplaces.
     * 2) Each of those Workplaces contains a list of its Jobs
     * 3) Each of those Jobs contains a list of its Shifts
     * @param email We find the Worker, from its email as it is unique
     * @return A IWorkerDTO
     */
    @GET
    @Path("/getWorkerEmail/{email}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public IWorkerDTO getIWorkerDTO(@PathParam("email") String email) {

        IWorkerDTO workerDTOToReturn = new WorkerHiberDTO();

        for (IWorkerDTO workerDTO : workerList) {
            if (workerDTO.getEmail().equals(email)) {
                workerDTOToReturn = workerDTO;
                break;
            }
        }

        return workerDTOToReturn;
    }
    
    /**
     * Method finds a Worker from an ID, and returns
     * a full IWorkerDTO object.
     * @param id The unique ID of the Worker
     * @return Object that implements IWorkerDTO interface
     */
    @GET
    @Path("/getWorker/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public IWorkerDTO getIWorkerDTO(@PathParam("id") int id)
    {
        IWorkerDTO workerDTOToReturn = new WorkerHiberDTO();

        for (IWorkerDTO workerDTO : workerList ){
            if (workerDTO.getWorkerID() == id) {
                workerDTOToReturn = workerDTO;
                break;
            }
        }

        return workerDTOToReturn;
    }
    
    /**
     * Method get a full list of Workers in the
     * database.
     * @return List<IWorkerDTO>
     */
    @GET
    @Path("/getWorkersList")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public List<IWorkerDTO> getIWorkerDTOList() {
        return workerList;
    }

    @Override
    public List<IWorkerDTO> getIWorkerDTOList(int minID, int maxID) {
        return null;
    }

    @Override
    public List<IWorkerDTO> getIWorkerDTOList(String name) {
        return null;
    }
    
    @PUT
    @Path("/updateWorker")
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public synchronized boolean updateWorker(IWorkerDTO updatedWorkerDTO) {
        boolean success = false;

        int listSize = workerList.size();

        for (int i = 0; i < listSize; i++){
            if (workerList.get(i).getWorkerID() == updatedWorkerDTO.getWorkerID()){
                workerList.get(i).setFirstName(updatedWorkerDTO.getFirstName());
                workerList.get(i).setSurName(updatedWorkerDTO.getSurName());
                workerList.get(i).setEmail(updatedWorkerDTO.getEmail());
                if (updatedWorkerDTO.getBirthday() == null){
                    workerList.get(i).setBirthday(updatedWorkerDTO.getBirthday());
                } else {
                    workerList.get(i).setBirthday(null);
                }
                workerList.get(i).setPassword(updatedWorkerDTO.getPassword());

                success = true;
                break;
            }
        }

        return success;
    }
    
    @DELETE
    @Path("/deleteWorker/{email}")
    @Override
    public synchronized boolean deleteWorker(@PathParam("email") String email) {

        return workerList.removeIf(workerDTO -> (workerDTO.getEmail().equals(email)));
    }
    
    @POST
    @Path("/createEmployer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public void createEmployer(IEmployerDTO employer) {

        for (IWorkerDTO workerDTO : workerList) {
            if (workerDTO.getWorkerID() == employer.getWorkerID()) {
                workerDTO.getIEmployers().add(employer);
                break;
            }
        }
    }
    
    @GET
    @Path("/getEmployer/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public IEmployerDTO getIEmployerDTO(@PathParam("id") int id) {
        IEmployerDTO employerDTOToReturn = null;

        outLoop:
        for (IWorkerDTO workerDTO : workerList) {
            for (IEmployerDTO employerDTO : workerDTO.getIEmployers()){
                if (employerDTO.getEmployerID() == id){
                    employerDTOToReturn = employerDTO;
                    break outLoop;
                }
            }
        }

        return employerDTOToReturn;
    }
    
    @GET
    @Path("/getEmployerList")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public List<IEmployerDTO> getIEmployerList() {
        List<IEmployerDTO> fullEmployerList = new ArrayList<>();
        for (IWorkerDTO workerDTO : workerList){
            fullEmployerList.addAll(workerDTO.getIEmployers());
        }
        return fullEmployerList;
    }

    @Override
    public List<IEmployerDTO> getIEmployerList(int minID, int maxID) {
        return null;
    }

    @Override
    public List<IEmployerDTO> getIEmployerList(String name) {
        return null;
    }
    
    @PUT
    @Path("/updateEmployer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public boolean updateEmployer(IEmployerDTO updatedEmployerDTO) {
        boolean succes = false;

        outLoop:
        for (IWorkerDTO workerDTO : workerList ) {
            for (IEmployerDTO employerDTO : workerDTO.getIEmployers()){
                if (employerDTO.getEmployerID() == updatedEmployerDTO.getEmployerID()){

                    employerDTO.setName(updatedEmployerDTO.getName());
                    if (updatedEmployerDTO.getColor() != null) {
                        employerDTO.setColor(updatedEmployerDTO.getColor());
                    } else {
                        employerDTO.setColor(null);
                    }
                    if (updatedEmployerDTO.getTelephone() != null) {
                        employerDTO.setTelephone(updatedEmployerDTO.getTelephone());
                    } else {
                        employerDTO.setTelephone(null);
                    }
                    succes = true;
                    break outLoop;
                }
            }
        }
        return succes;
    }
    
    @DELETE
    @Path("/deleteEmployer/{employerID}")
    @Override
    public boolean deleteEmployer(@PathParam("employerID") int employerID) {
        boolean success = false;

        outLoop:
        for (IWorkerDTO workerDTO : workerList) {
            int employerListSize = workerDTO.getIEmployers().size();
            for (int i = 0; i < employerListSize; i++) {
                if (workerDTO.getIEmployers().get(i).getEmployerID() == employerID) {
                    workerDTO.getIEmployers().remove(i);
                    success = true;
                    break outLoop;
                }
            }
        }

        return success;
    }
    
    @POST
    @Path("/createJob")
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public void createJob(IJobDTO job) {

        outLoop:
        for (IWorkerDTO workerDTO : workerList) {
            for (IEmployerDTO employerDTO : workerDTO.getIEmployers()){
                if (job.getEmployerID() == employerDTO.getEmployerID()) {
                    employerDTO.getIJobList().add(job);
                    break outLoop;
                }
            }
        }
    }
    
    @GET
    @Path("/getJob/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public IJobDTO getIJobDTO(@PathParam("id") int id) {
        IJobDTO jobDTOToReturn = null;

        outLoop:
        for (IWorkerDTO workerDTO : workerList) {
            for (IEmployerDTO employerDTO : workerDTO.getIEmployers()){
                for (IJobDTO jobDTO : employerDTO.getIJobList()) {
                    if (jobDTO.getJobID() == id) {
                        jobDTOToReturn = jobDTO;
                        break outLoop;
                    }
                }
            }
        }
        return jobDTOToReturn;
    }
    
    @GET
    @Path("/getJobList")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public List<IJobDTO> getIJobDTOList() {
        List<IJobDTO> fullJobList = new ArrayList<>();

        for (IWorkerDTO workerDTO : workerList) {
            for (IEmployerDTO employerDTO : workerDTO.getIEmployers()) {
                fullJobList.addAll(employerDTO.getIJobList());
            }
        }
        return fullJobList;
    }
    
    @GET
    @Path("/getJobList/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public List<IJobDTO> getIJobDTOList(@PathParam("id") int employerID) {

        List<IJobDTO> listToReturn = new ArrayList<>();
        outLoop:
        for (IWorkerDTO workerDTO : workerList) {
            for (IEmployerDTO employerDTO : workerDTO.getIEmployers()){
                if (employerDTO.getEmployerID() == employerID) {
                    listToReturn = employerDTO.getIJobList();
                    break outLoop;
                }
            }
        }
        return listToReturn;
     }

    @Override
    public List<IJobDTO> getIJobDTOList(String name) {
        return null;
    }

    @Override
    public List<IJobDTO> getIJobDTOList(double minSalary, double maxSalary) {
        return null;
    }
	
	@PUT
	@Path("/updateJob")
	@Produces(MediaType.APPLICATION_JSON)
    @Override
    public boolean updateJob(IJobDTO updateJobDTO) {
        boolean success = false;
        outLoop:
        for (IWorkerDTO workerDTO : workerList) {
            for (IEmployerDTO employerDTO : workerDTO.getIEmployers()) {
                for (IJobDTO jobDTO : employerDTO.getIJobList()){
                    if (jobDTO.getJobID() == updateJobDTO.getJobID()){

                        jobDTO.setJobName(updateJobDTO.getJobName());
                        if (updateJobDTO.getHireDate() == null) {
                            jobDTO.setHireDate(updateJobDTO.getHireDate());
                        } else {
                            jobDTO.setHireDate(null);
                        }
                        if (updateJobDTO.getFinishDate() == null) {
                            jobDTO.setFinishDate(updateJobDTO.getFinishDate());
                        } else {
                            jobDTO.setFinishDate(null);
                        }
                        jobDTO.setStdSalary(updateJobDTO.getStdSalary());
                        success = true;
                        break outLoop;

                    }
                }
            }

        }
        return success;
    }
	
	@DELETE
	@Path("/deleteJob/{jobID}")
    @Override
    public boolean deleteJob(@PathParam("jobID") int jobID) {
        boolean success = false;

        outLoop:
        for (IWorkerDTO workerDTO : workerList) {
            for (IEmployerDTO employerDTO : workerDTO.getIEmployers()) {
                int jobListSize = employerDTO.getIJobList().size();
                for (int i = 0; i < jobListSize ; i++) {
                    if (employerDTO.getIJobList().get(i).getJobID() == jobID) {
                        employerDTO.getIJobList().remove(i);
                        success = true;
                        break outLoop;
                    }
                }
            }
        }

        return success;
    }
	
	/**
	 * Takes an object that implements IActivityDTO interface, and
	 * saving the activity in the database.
	 * @param activity Object implementing IActivityDTO
	 */
	@POST
	@Path("/createActivity")
	@Consumes(MediaType.APPLICATION_JSON)
    @Override
    public void createActivity(IActivityDTO activity) {
	    outLoop:
	    for (IWorkerDTO workerDTO : workerList) {
	        for (IEmployerDTO employerDTO : workerDTO.getIEmployers()) {
	            for (IJobDTO jobDTO : employerDTO.getIJobList()) {
	                if (jobDTO.getJobID() == activity.getJobID()) {
	                    jobDTO.getiActivityDTOList().add(activity);
	                    break outLoop;
                    }
                }
            }
        }

    }
	
	@GET
	@Path("/getActivity/{id}")
	@Produces(MediaType.APPLICATION_JSON)
    @Override
    public IActivityDTO getIActivity(@PathParam("id") int id) {
        IActivityDTO activityDTO = null;
        outLoop:
        for (IWorkerDTO workerDTO : workerList) {
            for (IEmployerDTO employerDTO : workerDTO.getIEmployers()) {
                for (IJobDTO jobDTO : employerDTO.getIJobList()) {
                    for (IActivityDTO activity : jobDTO.getiActivityDTOList()) {
                        if (activity.getActivityID() == id) {
                            activityDTO = activity;
                            break outLoop;
                        }
                    }
                }
            }
        }
	    return  activityDTO;
    }
	
	@GET
	@Path("/getActivityList")
	@Produces(MediaType.APPLICATION_JSON)
    @Override
    public List<IActivityDTO> getIActivityList() {
	    List<IActivityDTO> fullActivityList = new ArrayList<>();

        for (IWorkerDTO workerDTO : workerList) {
            for (IEmployerDTO employerDTO : workerDTO.getIEmployers()) {
                for (IJobDTO jobDTO : employerDTO.getIJobList()) {
                    fullActivityList.addAll(jobDTO.getiActivityDTOList());
                }
            }
        }

        return fullActivityList;
    }

    @Override
    public List<IActivityDTO> getIActivityList(int jobID) {
	    List<IActivityDTO> activityList = new ArrayList<>();

        outLoop:
        for (IWorkerDTO workerDTO : workerList) {
            for (IEmployerDTO employerDTO : workerDTO.getIEmployers()) {
                for (IJobDTO jobDTO : employerDTO.getIJobList()) {
                    if (jobDTO.getJobID() == jobID) {
                        activityList = jobDTO.getiActivityDTOList();
                        break outLoop;
                    }
                }
            }
        }

        return activityList;
    }

    @Override
    public List<IActivityDTO> getIActivityList(Date date) {
        return null;
    }

    @Override
    public List<IActivityDTO> getIActivityList(double minVal, double maxVal) {
        return null;
    }
	
	@PUT
	@Path("/updateActivity")
	@Consumes(MediaType.APPLICATION_JSON)
    @Override
    public boolean updateActivity(IActivityDTO updatedActivityDTO) {
	    boolean success = false;

	    outLoop:
	    for (IWorkerDTO workerDTO : workerList){
	        for (IEmployerDTO employerDTO : workerDTO.getIEmployers()) {
	            for (IJobDTO jobDTO : employerDTO.getIJobList()){
	                for (IActivityDTO activityDTO : jobDTO.getiActivityDTOList()) {
	                    if (activityDTO.getActivityID() == updatedActivityDTO.getActivityID()) {
	                        activityDTO.setStartingDateTime(updatedActivityDTO.getStartingDateTime());
	                        activityDTO.setEndingDateTime(updatedActivityDTO.getEndingDateTime());
	                        activityDTO.setActivityValue(updatedActivityDTO.getActivityValue());
	                        success = true;
	                        break outLoop;
                        }
                    }
                }
            }
        }

        return success;
    }
	
	@DELETE
	@Path("/deleteActivity/{activityID}")
    @Override
    public void deleteActivity(@PathParam("activityID") int activityID) {

        outLoop:
        for (IWorkerDTO workerDTO : workerList){
            for (IEmployerDTO employerDTO : workerDTO.getIEmployers()) {
                for (IJobDTO jobDTO : employerDTO.getIJobList()) {
                    if(jobDTO.getiActivityDTOList().removeIf(iActivityDTO -> (iActivityDTO.getActivityID() == activityID))){
                        break outLoop;
                    }
                }
            }
        }
    }

    @Override
    public String setTimeZoneFromSQLServer() {
        return null;
    }
    
    @POST
    @Path("/Logout")
    @Override
    public void logOut(@Context HttpServletRequest request)
    {
        HttpSession session = request.getSession();
        session.invalidate();
    }
    
    @POST
    @Path("/isSessionActive")
    @Override
    public boolean isSessionActive(@Context HttpServletRequest request)
    {
        boolean sessionStatus=false;
        if (request.getSession(false) != null){
            sessionStatus=true;
        }
        return sessionStatus;
    }

	/**
	 * This method checks if there's a correlation between the
	 * provided email and password. All exceptions is handled by
	 * the method.
	 * @return True if there's a correlation
	 */
    @POST
    @Path("/loginCheck")
    @Consumes(MediaType.APPLICATION_JSON)
    @Override
    public boolean loginCheck(IWorkerDTO user, @Context HttpServletRequest request)
    {
    	// Initialize variables
    	String email = user.getEmail();
    	String pass = user.getPassword();
    	boolean success = false;

    	// Check if the email matches
		IWorkerDTO worker = getIWorkerDTO(email);
		if ( worker.getEmail() != null )
		{
			// Check if password matches
			if ( worker.getPassword().equals(pass) )
				success = true;
		}

		// Take care of session
		if (success)
		{
			HttpSession oldSession = request.getSession();

			if (oldSession != null)
				oldSession.invalidate();

			HttpSession session = request.getSession(true);

			// Store users email in session
			session.setAttribute("userEmail",email);

			// Set the the time before the session expires to 10 minutes
			session.setMaxInactiveInterval(10*60);
		}

        return success;
    }
    
    /*
    ---------------------- Support Methods ----------------------
     */

    private ArrayList<IWorkerDTO> setArrayListWithStartData () {
        ArrayList<IWorkerDTO> preloadedWorkerList = new ArrayList<>();

        IWorkerDTO workerNo1 = new WorkerDTO();
        workerNo1.setFirstName("WorkerFornavn");
        workerNo1.setSurName("WorkerEfternavn");
        workerNo1.setEmail("worker1@test.dk");
        workerNo1.setBirthday(LocalDate.of(1992,1,6));
        workerNo1.setPassword("password");

        IEmployerDTO employerNo1 = new EmployerDTO();
        employerNo1.setWorkerID(workerNo1.getWorkerID());
        employerNo1.setName("DTU");
        employerNo1.setTelephone("12345678");
        employerNo1.setColor(Color.decode("#FAEBD7"));

        IJobDTO jobNo1 = new JobDTO();
        jobNo1.setEmployerID(employerNo1.getEmployerID());
        jobNo1.setJobName("Hjælpeunderviser");
        jobNo1.setStdSalary(100.0);

        IJobDTO jobNo2 = new JobDTO();
        jobNo2.setEmployerID(employerNo1.getEmployerID());
        jobNo2.setJobName("Forelæser");
        jobNo2.setStdSalary(200.0);

        IActivityDTO activityNo1 = new ActivityDTO();
        activityNo1.setJobID(jobNo1.getJobID());
        activityNo1.setStartingDateTime(Timestamp.valueOf(LocalDateTime.of(2019,6,18,8,0,0)));
        activityNo1.setEndingDateTime(Timestamp.valueOf(LocalDateTime.of(2019,6,18,16,0,0)));
        activityNo1.setPauseInMinuts(30);
        activityNo1.calculateActivityValue(jobNo1);

        IActivityDTO activityNo2 = new ActivityDTO();
        activityNo2.setJobID(jobNo1.getJobID());
        activityNo2.setStartingDateTime(Timestamp.valueOf(LocalDateTime.of(2019,6,20,10,0,0)));
        activityNo2.setEndingDateTime(Timestamp.valueOf((LocalDateTime.of(2019,6,20,18,0,0))));
        activityNo2.setPauseInMinuts(60);
        activityNo2.calculateActivityValue(jobNo1);

        IActivityDTO activityNo3 = new ActivityDTO();
        activityNo3.setJobID(jobNo2.getJobID());
        activityNo3.setStartingDateTime(Timestamp.valueOf(LocalDateTime.of(2019,6,15,10,0,0)));
        activityNo3.setEndingDateTime(Timestamp.valueOf(LocalDateTime.of(2019,6,15,12,0,0)));
        activityNo3.setPauseInMinuts(0);
        activityNo3.calculateActivityValue(jobNo2);

        // Activities til hjælpeunderviser.
        jobNo1.getiActivityDTOList().add(activityNo1);
        jobNo1.getiActivityDTOList().add(activityNo2);
        // Activities til forelæser.
        jobNo2.getiActivityDTOList().add(activityNo3);
        // Job til DTU
        employerNo1.getIJobList().add(jobNo1);
        employerNo1.getIJobList().add(jobNo2);
        // Employer til Worker
        workerNo1.getIEmployers().add(employerNo1);
        // Add Worker til PreloadedList
        preloadedWorkerList.add(workerNo1);

        return preloadedWorkerList;
    }


}
