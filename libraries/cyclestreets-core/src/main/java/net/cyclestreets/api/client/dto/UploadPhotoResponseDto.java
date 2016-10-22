package net.cyclestreets.api.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.cyclestreets.api.Upload;

public class UploadPhotoResponseDto extends ApiResponseDto {
  @JsonProperty
  int id;
  @JsonProperty
  String url;
  @JsonProperty
  String shortlink;
  @JsonProperty
  String imageUrl;
  @JsonProperty
  String thumbnailUrl;

  public Upload.Result toUploadResult() {
    return wasSuccessful() ? Upload.Result.forUrl(url) : Upload.Result.error(error);
  }
}
