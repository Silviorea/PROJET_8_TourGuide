package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.DTO.AttractionDTO;
import tourGuide.DTO.LocationDTO;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;

	RewardCentral rewardCentral = new RewardCentral();
	
	ExecutorService executor = Executors.newFixedThreadPool(1000);
	
	
	
	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		
		if(testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}
	
	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}
	
	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
			user.getLastVisitedLocation() :
			trackUserLocation(user);
		return visitedLocation;
	}
	
	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}
	
	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}
	
	public void addUser(User user) {
		if(!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}
	
	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(), 
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}
	
	/*
	 * Prends la visited location cr�ee via gpsUtil et l'int�gre dans la liste des User
	 * Calcule ensuite le rewards
	 */
	
	public VisitedLocation trackUserLocation(User user)
	{
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}
	
	public void trackUserLocationAsync(User user) {
	
		CompletableFuture.supplyAsync(() -> gpsUtil.getUserLocation(user.getUserId()), executor)
				.thenAccept(vl -> {
					user.addToVisitedLocations(vl);
					rewardsService.calculateRewards(user);
				});
    }

	public ExecutorService getExecutor()
	{
		return executor;
	}


//	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
//		List<Attraction> nearbyAttractions = new ArrayList<>();
//		for(Attraction attraction : gpsUtil.getAttractions()) {
//			if(rewardsService.isWithinAttractProximity(attraction, visitedLocation.location)) {
//				nearbyAttractions.add(attraction);
//			}
//		}
//		
//		return nearbyAttractions;
//	}
	
	public List<AttractionDTO> getNearByAttractions(VisitedLocation visitedLocation) {
		
		List<Attraction> nearbyAttractions = new ArrayList<>();
		List<AttractionDTO> nearbyAttractionsDTO = new ArrayList<>();
		
	
		for(Attraction attraction : gpsUtil.getAttractions()) {
			if(rewardsService.isWithinAttractProximity(attraction, visitedLocation.location)) {
				nearbyAttractions.add(attraction);
			}
		}
		
		nearbyAttractions = nearbyAttractions.stream()
				.sorted(Comparator.comparing(
						attraction -> rewardsService.getDistance(attraction, visitedLocation.location))) 
				.limit(5)
				.collect(Collectors.toList());
		
		for (Attraction attraction : nearbyAttractions)
		{
			nearbyAttractionsDTO.add( new AttractionDTO(
					attraction.attractionName, 
					attraction.latitude, 
					attraction.longitude, 
					visitedLocation.location.latitude,
					visitedLocation.location.longitude,
					rewardsService.getDistance(visitedLocation.location, attraction), 
					rewardCentral.getAttractionRewardPoints(attraction.attractionId, visitedLocation.userId)));
		}
		
		return nearbyAttractionsDTO;
	}
	
	
	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() { 
		      public void run() {
		        tracker.stopTracking();
		      } 
		    }); 
	}
	
	
	public List<LocationDTO> getAllCurrentLocations(User user) {
		
		List<LocationDTO> locationDTOList = new ArrayList<>();
		List<VisitedLocation> visitedLocationList = new ArrayList<>();
		visitedLocationList.addAll(user.getVisitedLocations());
		
		visitedLocationList = visitedLocationList.stream()
				.sorted(Comparator.comparing(
						loc -> user.getLatestLocationTimestamp())) 
				.collect(Collectors.toList());
		
		for (VisitedLocation sortedVisitedLocationList : visitedLocationList)
		{
			locationDTOList.add(new LocationDTO(
					sortedVisitedLocationList.userId, 
					sortedVisitedLocationList.location.longitude, 
					sortedVisitedLocationList.location.latitude));
		}
		
		return locationDTOList;
	}
	
	
	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();
	
	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);
			
			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}
	
	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i-> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}
	
	private double generateRandomLongitude() {
		double leftLimit = -180;
	    double rightLimit = 180;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
	    double rightLimit = 85.05112878;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
	    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}
	
}
