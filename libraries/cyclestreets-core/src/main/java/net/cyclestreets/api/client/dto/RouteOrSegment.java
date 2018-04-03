package net.cyclestreets.api.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.fasterxml.jackson.annotation.JsonSubTypes.Type;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes(value={ @Type(value = Route.class, name = "route"),
                      @Type(value = Segment.class, name = "segment") })
public abstract class RouteOrSegment {

  @JsonProperty
  private String name;

  public String getName() {
    return name;
  }

  // Workaround for weird JSON wrapping of objects.
  public static class RouteOrSegmentWrapper {
    @JsonProperty(value = "@attributes")
    private RouteOrSegment ros;

    public RouteOrSegment getContents() {
      return ros;
    }
  }
}
