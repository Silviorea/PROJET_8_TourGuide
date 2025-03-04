package tourGuide.DTO;

import java.io.Serializable;

public class AttractionDTO implements Serializable
{

	private static final long serialVersionUID = 1L;
	private String touristAttractionName;
	private double touristAttractionLat;
	private double touristAttractionLong;
	private double touristLat;
	private double touristLong;
	private double distance;
	private int reward;



	public AttractionDTO(String touristAttractionName, double touristAttractionLat, double touristAttractionLong,
			double touristLat, double touristLong, double distance, int reward)
	{
		super();
		this.touristAttractionName = touristAttractionName;
		this.touristAttractionLat = touristAttractionLat;
		this.touristAttractionLong = touristAttractionLong;
		this.touristLat = touristLat;
		this.touristLong = touristLong;
		this.distance = distance;
		this.reward = reward;
	}




	public AttractionDTO()
	{
		super();
	}




	public String getTouristAttractionName()
	{
		return touristAttractionName;
	}


	public void setTouristAttractionName(String touristAttractionName)
	{
		this.touristAttractionName = touristAttractionName;
	}


	


	public double getTouristAttractionLat()
	{
		return touristAttractionLat;
	}







	public void setTouristAttractionLat(double touristAttractionLat)
	{
		this.touristAttractionLat = touristAttractionLat;
	}







	public double getTouristAttractionLong()
	{
		return touristAttractionLong;
	}







	public void setTouristAttractionLong(double touristAttractionLong)
	{
		this.touristAttractionLong = touristAttractionLong;
	}







	public double getDistance()
	{
		return distance;
	}


	public void setDistance(double distance)
	{
		this.distance = distance;
	}


	public int getReward()
	{
		return reward;
	}


	public void setReward(int reward)
	{
		this.reward = reward;
	}
	
	
	public double getTouristLat()
	{
		return touristLat;
	}




	public void setTouristLat(double touristLat)
	{
		this.touristLat = touristLat;
	}




	public double getTouristLong()
	{
		return touristLong;
	}




	public void setTouristLong(double touristLong)
	{
		this.touristLong = touristLong;
	}
	
	
	
	
}
