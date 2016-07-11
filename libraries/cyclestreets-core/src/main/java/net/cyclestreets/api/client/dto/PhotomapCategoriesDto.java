package net.cyclestreets.api.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.cyclestreets.api.PhotomapCategories;
import net.cyclestreets.api.PhotomapCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PhotomapCategoriesDto extends ApiResponseDto {
  @JsonProperty(value = "validuntil")
  private long validUntil;
  @JsonProperty
  private Map<String, PhotomapCategoryDto> categories;
  @JsonProperty(value = "metacategories")
  private Map<String, PhotomapCategoryDto> metaCategories;

  public static class PhotomapCategoryDto {
    @JsonProperty
    private String name;
    @JsonProperty
    private String description;
  }

  public PhotomapCategories toPhotomapCategories() {
    List<PhotomapCategory> mapCategories = new ArrayList<>();
    for (Map.Entry<String, PhotomapCategoryDto> entry : categories.entrySet()) {
      mapCategories.add(new PhotomapCategory(entry.getKey(),
                                             entry.getValue().name,
                                             entry.getValue().description));
    }

    List<PhotomapCategory> mapMetaCategories = new ArrayList<>();
    for (Map.Entry<String, PhotomapCategoryDto> entry : metaCategories.entrySet()) {
      mapMetaCategories.add(new PhotomapCategory(entry.getKey(),
                                                 entry.getValue().name,
                                                 entry.getValue().description));
    }

    return new PhotomapCategories(mapCategories, mapMetaCategories);
  }
}
