package net.cyclestreets.api.client;

import net.cyclestreets.api.client.dto.BlogFeedDto;

import retrofit2.Call;
import retrofit2.http.GET;

public interface BlogApi {
    @GET("/news/feed/")
    Call<BlogFeedDto> getBlogEntries();
}
