package net.cyclestreets.api.client;

import net.cyclestreets.api.client.dto.GeoPlacesDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface V1Api {

  @GET("/api/geocoder.xml?countrycodes=gb,ie")
  Call<GeoPlacesDto> geoCoder(@Query("street") String search,
                              @Query("n") double n,
                              @Query("s") double s,
                              @Query("e") double e,
                              @Query("w") double w);
}
