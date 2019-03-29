package DTOs.worker;

import DTOs.address.Address;
import DTOs.address.IAddress;
import DTOs.workPlace.WorkPlaceDTO;

import java.time.LocalDate;
import java.util.List;

public interface IWorkerDTO {
	
	// Methods
	
	String getFirstName();
	void setFirstName(String firstName);
	
	String getSurName();
	void setSurName(String surName);
	
	String getEmail();
	void setEmail(String email);
	
	LocalDate getBirthday();
	void setBirthday(LocalDate birthday);
	
	IAddress getHomeAddress();
	void setHomeAddress(IAddress homeAddress);
	
	List<WorkPlaceDTO> getWorkPlaces();
	void setWorkPlaces(List<WorkPlaceDTO> workPlaces);
	
	int getWorkerID();
}
