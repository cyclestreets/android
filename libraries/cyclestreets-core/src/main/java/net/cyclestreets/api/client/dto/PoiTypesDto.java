package net.cyclestreets.api.client.dto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.cyclestreets.api.POICategories;
import net.cyclestreets.api.POICategory;
import net.cyclestreets.util.Base64;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PoiTypesDto {
  @JsonProperty(value = "validuntil")
  private long validUntil;
  @JsonProperty
  private Map<String, PoiTypeDto> types;

  public static class PoiTypeDto {
    @JsonProperty
    private String id;
    @JsonProperty
    private String name;
    @JsonProperty
    private String total;
    @JsonProperty
    private String icon;

    private POICategory toPOICategory(Context context) {
      return new POICategory(id, name, poiIcon(context, icon));
    }
  }

  public POICategories toPOICategories(Context context) {
    List<POICategory> categories = new ArrayList<>();
    for (Map.Entry<String, PoiTypeDto> entry : types.entrySet()) {
      categories.add(entry.getValue().toPOICategory(context));
    }
    return new POICategories(categories);
  }

  private static Drawable poiIcon(final Context context,
                                  final String iconAsBase64) {
    final Bitmap bmp = decodeIcon(iconAsBase64);
    return new BitmapDrawable(context.getResources(), bmp);
  }

  private static Bitmap decodeIcon(final String iconAsBase64) {
    try {
      byte[] bytes = Base64.decode(iconAsBase64);
      return BitmapFactory.decodeStream(new ByteArrayInputStream(bytes));
    } catch (Exception e) {
      return null;
    }
  }
}
