package net.cyclestreets.api.client;

import net.cyclestreets.api.client.dto.UserJourneysDto;

import org.geojson.FeatureCollection;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface V2Api {

  @GET("/v2/pois.locations?fields=id,name,notes,website,latitude,longitude")
  Call<FeatureCollection> getPOIs(@Query("type") String type,
                                  @Query("bbox") String bbox);

  @GET("/v2/pois.locations?fields=id,name,notes,website,latitude,longitude&limit=150")
  Call<FeatureCollection> getPOIs(@Query("type")  String type,
                                  @Query("longitude") double lon,
                                  @Query("latitude") double lat,
                                  @Query("radius") int radius);

  @GET("/v2/photomap.locations?fields=id,caption,categoryId,metacategoryId,hasVideo,videoFormats,thumbnailUrl,shortlink&limit=45&thumbnailsize=640")
  Call<FeatureCollection> getPhotos(@Query("bbox") String bbox);

  @GET("/v2/journeys.user?format=flat&datetime=friendly")
  Call<UserJourneysDto> getUserJourneys(@Query("username") String username);
}
