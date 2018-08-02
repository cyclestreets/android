package net.cyclestreets.api.client;

import net.cyclestreets.api.client.dto.BlogFeedDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface V1Api {

  @Deprecated
  @GET("/api/journey.xml")
  Call<String> getJourneyXml(@Query("plan") String plan,
                             @Query("itinerarypoints") String itineraryPoints,
                             @Query("leaving") String leaving,
                             @Query("arriving") String arriving,
                             @Query("speed") int speed);

  @GET("/api/journey.json")
  Call<String> getJourneyJson(@Query("plan") String plan,
                              @Query("itinerarypoints") String itineraryPoints,
                              @Query("leaving") String leaving,
                              @Query("arriving") String arriving,
                              @Query("speed") int speed);

  @Deprecated
  @GET("/api/journey.xml")
  Call<String> retrievePreviousJourneyXml(@Query("plan") String plan,
                                          @Query("itinerary") long itineraryId);

  @GET("/api/journey.json")
  Call<String> retrievePreviousJourneyJson(@Query("plan") String plan,
                                           @Query("itinerary") long itineraryId);

  @GET("/blog/feed/")
  Call<BlogFeedDto> getBlogEntries();
}
