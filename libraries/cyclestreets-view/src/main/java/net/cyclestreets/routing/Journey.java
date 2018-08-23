package net.cyclestreets.routing;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import android.text.TextUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.cyclestreets.CycleStreetsPreferences;

import net.cyclestreets.routing.domain.GeoPointDeserializer;
import net.cyclestreets.routing.domain.JourneyDomainObject;
import net.cyclestreets.routing.domain.SegmentDomainObject;
import org.osmdroid.api.IGeoPoint;

public class Journey
{
  private Waypoints waypoints_;
  private Segments segments_;
  private ElevationProfile elevations_;
  private int activeSegment_;

  public static final Journey NULL_JOURNEY;
  static {
    NULL_JOURNEY = new Journey();
    NULL_JOURNEY.activeSegment_ = -1;
  }

  private Journey() {
    waypoints_ = new Waypoints();
    segments_ = new Segments();
    activeSegment_ = 0;
    elevations_ = new ElevationProfile();
  }

  private Journey(final Waypoints waypoints) {
    this();
    if (waypoints != null)
      waypoints_ = waypoints;
  }

  public boolean isEmpty() { return segments_.isEmpty(); }
  public Segments segments() { return segments_; }
  public ElevationProfile elevation() { return elevations_; }

  private Segment.Start s() { return segments_.first(); }
  private Segment.End e() { return segments_.last(); }

  public Waypoints waypoints() { return waypoints_; }

  public String url() { return "http://cycle.st/j" + itinerary(); }
  public int itinerary() { return s().itinerary(); }
  public String name() { return s().name(); }
  public String plan() { return s().plan(); }
  public int speed() { return s().speed(); }
  public int total_distance() { return e().total_distance(); }

  /////////////////////////////////////////
  public void setActiveSegmentIndex(int index) { activeSegment_ = index; }
  public void setActiveSegment(final Segment seg) {
    for (int i = 0; i != segments_.count(); ++i)
      if (seg == segments_.get(i)) {
        setActiveSegmentIndex(i);
        break;
      }
  }
  public int activeSegmentIndex() { return activeSegment_; }

  public Segment activeSegment() { return activeSegment_ >= 0 ? segments_.get(activeSegment_) : null; }
  public Segment nextSegment() {
    if (atEnd())
      return activeSegment();
    return segments_.get(activeSegment_+1);
  }

  public boolean atStart() { return activeSegment_ <= 0; }
  public boolean atWaypoint() { return activeSegment() instanceof Segment.Waymark; }
  public boolean atEnd() { return activeSegment_ == segments_.count()-1; }

  public void regressActiveSegment() {
    if (!atStart())
      --activeSegment_;
  }
  public void advanceActiveSegment() {
    if (!atEnd())
      ++activeSegment_;
  }

  public Iterator<IGeoPoint> points() {
    return segments_.pointsIterator();
  }

  ////////////////////////////////////////////////////////////////
  private static IGeoPoint pD(final IGeoPoint a1, final IGeoPoint a2) {
    return a1 != null ? a1 : a2;
  }

  public static Journey loadFromJson(final String domainJson,
                                     final Waypoints waypoints,
                                     final String name) {
    return new JourneyFactory(waypoints, name).parse(domainJson);
  }

  ////////////////////////////////////////////////////////////////////////////////
  private static class JourneyFactory  {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Journey journey;
    private final String name;

    // Variables to maintain state as we process the JourneyDomainObject
    private int leg = 1;
    private int totalDistance = 0;
    private int totalTime = 0;

    JourneyFactory(final Waypoints waypoints,
                   final String name) {
      journey = new Journey(waypoints);
      this.name = name;

      SimpleModule module = new SimpleModule();
      module.addDeserializer(IGeoPoint.class, new GeoPointDeserializer());
      objectMapper.registerModule(module);
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    Journey parse(String domainJson) {
      // I guess this is in case the units have changed without the app restarting
      Segment.formatter = DistanceFormatter.formatter(CycleStreetsPreferences.units());

      JourneyDomainObject jdo;
      try {
        jdo = objectMapper.readValue(domainJson, JourneyDomainObject.class);
      } catch (IOException e) {
        throw new RuntimeException("Coding error - unable to parse domain JSON");
      }

      populateWaypoints(jdo);
      populateSegments(jdo);
      generateStartAndFinishSegments(jdo);

      return journey;
    }

    private void populateWaypoints(JourneyDomainObject jdo) {
      if (journey.waypoints().count() == 0) {
        for (IGeoPoint gp : jdo.waypoints) {
          journey.waypoints().add(gp);
        }
      }
    }

    private void populateSegments(JourneyDomainObject jdo) {
      for (SegmentDomainObject seg : jdo.segments) {
        if (seg.legNumber != leg) {
          journey.segments_.add(new Segment.Waymark(leg, totalDistance, seg.points.get(0)));
          leg = seg.legNumber;
        }

        totalTime += seg.time;
        totalDistance += seg.distance;
        journey.segments_.add(new Segment.Step(seg.name,
                                               seg.turn,
                                               seg.shouldWalk,
                                               totalTime,
                                               seg.distance,
                                               totalDistance,
                                               seg.points));
        journey.elevations_.add(seg.segmentProfile);
      }
    }

    private void generateStartAndFinishSegments(JourneyDomainObject jdo) {
      final IGeoPoint from = journey.waypoints().first();
      final IGeoPoint to = journey.waypoints().last();

      final IGeoPoint pstart = journey.segments_.startPoint();
      final IGeoPoint pend = journey.segments_.finishPoint();

      final Segment startSeg = new Segment.Start(jdo.route.itinerary,
                                                 TextUtils.isEmpty(name) ? jdo.route.name : name,
                                                 jdo.route.plan,
                                                 jdo.route.speed,
                                                 totalTime,
                                                 totalDistance,
                                                 jdo.route.calories,
                                                 jdo.route.grammesCO2saved,
                                                 Arrays.asList(pD(from, pstart), pstart));
      final Segment endSeg = new Segment.End(jdo.route.finish,
                                             totalTime,
                                             totalDistance,
                                             Arrays.asList(pend, pD(to, pend)));

      journey.segments_.add(startSeg);
      journey.segments_.add(endSeg);
    }
  }
}
