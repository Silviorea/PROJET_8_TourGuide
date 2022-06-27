package tourGuide.DTO;

import java.io.Serializable;
import java.util.UUID;

public class LocationDTO implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UUID userId;
	private double longitude;
	private double latitude;
	
	
	public LocationDTO(UUID userId, double longitude, double latitude)
	{
		super();
		this.userId = userId;
		this.longitude = longitude;
		this.latitude = latitude;
	}


	public LocationDTO()
	{
		super();
	}


	public UUID getUserId()
	{
		return userId;
	}


	public void setUserId(UUID userId)
	{
		this.userId = userId;
	}


	public double getLongitude()
	{
		return longitude;
	}


	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}


	public double getLatitude()
	{
		return latitude;
	}


	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}
	
	
	
	
	

}
