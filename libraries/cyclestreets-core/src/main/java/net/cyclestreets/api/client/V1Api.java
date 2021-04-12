package net.cyclestreets.api.client;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface V1Api {

  @GET("/api/journey.json")
  Call<String> getJourneyJson(@Query("plan") String plan,
                              @Query("itinerarypoints") String itineraryPoints,
                              @Query("leaving") String leaving,
                              @Query("arriving") String arriving,
                              @Query("speed") int speed,
                              @Query("reporterrors") String reportErrors);

  @GET("/api/journey.json")
  Call<String> retrievePreviousJourneyJson(@Query("plan") String plan,
                                           @Query("itinerary") long itineraryId,
                                           @Query("reporterrors") String reportErrors);

  @GET("/api/journey.json")
  Call<String> getCircularJourneyJson(@Query("plan") String plan,
                                      @Query("itinerarypoints") String itineraryPoints,
                                      @Query("distance") Integer distance,
                                      @Query("duration") Integer duration,
                                      @Query("poitypes") String poitypes,
                                      @Query("reporterrors") String reportErrors);

}
