package net.cyclestreets.api.client;

import net.cyclestreets.api.client.dto.FeatureCollection;

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
                                    @Query("latitude")  double lat,
                                    @Query("radius") int radius);
}
