package dto.worker;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dto.employer.IEmployerDTO;

import java.time.LocalDate;
import java.util.List;

@JsonDeserialize(as = WorkerDTO.class)
public interface IWorkerDTO {
	
	// Methods
	
	String getFirstName();
	void setFirstName(String firstName);
	
	String getSurName();
	void setSurName(String surName);
	
	String getEmail();
	void setEmail(String email);
	
	String getPassword();
	void setPassword(String password);

	LocalDate getBirthday();
	void setBirthday(LocalDate birthday);
	
	List<IEmployerDTO> getIEmployers();
	void setIEmployers(List<IEmployerDTO> workPlaces);
	
	int getWorkerID();
	void setWorkerID(int workerID);
}
