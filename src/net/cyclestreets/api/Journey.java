package net.cyclestreets.api;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import net.cyclestreets.CycleStreetsPreferences;
import net.cyclestreets.util.Collections;

import org.osmdroid.util.GeoPoint;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

public class Journey 
{
	private List<Segment> segments_;
  private int activeSegment_;
	  
  static public final Journey NULL_JOURNEY;
  static {
    NULL_JOURNEY = new Journey();
    NULL_JOURNEY.activeSegment_ = -1;
  }

  
  private Journey() 
  {
    segments_ = new ArrayList<Segment>();
    activeSegment_ = 0;   
	} // PlannedRoute

	public boolean isEmpty() { return segments_.isEmpty(); }
	public List<Segment> segments() { return segments_; }
	
	private Segment.Start s() { return (Segment.Start)segments_.get(0); }
	private Segment.End e() { return (Segment.End)segments_.get(segments_.size()-1); }
	  
	public GeoPoint start() { return s().start(); }
	public GeoPoint finish() { return e().end(); }
	  
	public String url() { return "http://cycle.st/j" + itinerary(); }
	public int itinerary() { return s().itinerary(); }
	public String name() { return s().name(); }
	public String plan() { return s().plan(); }
	public int speed() { return s().speed(); }
	public int total_distance() { return e().total_distance(); }

  /////////////////////////////////////////
  public void setActiveSegmentIndex(int index) { activeSegment_ = index; }
  public int activeSegmentIndex() { return activeSegment_; }
  
  public Segment activeSegment() { return activeSegment_ >= 0 ? segments_.get(activeSegment_) : null; }
  
  public boolean atStart() { return activeSegment_ <= 0; }
  public boolean atEnd() { return activeSegment_ == segments_.size()-1; }
  
  public void regressActiveSegment() 
  { 
    if(!atStart()) 
      --activeSegment_; 
  } // regressActiveSegment
  public void advanceActiveSegment() 
  { 
    if(!atEnd()) 
      ++activeSegment_; 
  } // advanceActiveSegment
  
  public Iterator<GeoPoint> points()
  {
    return new PointsIterator(segments_);
  } // points
    
  static class PointsIterator implements Iterator<GeoPoint>
  {
    private final Iterator<Segment> segments_;
    private Iterator<GeoPoint> points_;
    
    PointsIterator(final List<Segment> segments)
    {
      segments_ = segments.iterator();
      if(!segments_.hasNext())
        return;
      
      points_ = segments_.next().points();
    } // PointsIterator
    
    @Override
    public boolean hasNext() 
    {
      return points_ != null && points_.hasNext();
    } // hasNext

    @Override
    public GeoPoint next() 
    {
      if(!hasNext())
        throw new IllegalStateException();
      
      final GeoPoint p = points_.next();
      
      if(!hasNext())
      {
        if(segments_.hasNext())
          points_ = segments_.next().points();
        else
          points_ = null;
      } // if ...
      
      return p;
    } // next

    @Override
    public void remove() 
    {
      throw new UnsupportedOperationException();
    } // remove
  } // class PointsIterator

  ////////////////////////////////////////////////////////////////
	static private GeoPoint pD(final GeoPoint a1, final GeoPoint a2)
	{
	  return a1 != null ? a1 : a2;
	} // pD
	  
  private final static int DEFAULT_SPEED = 20;
  
  /////////////////////////////////////////////////////////////////
  static public String getJourneyXml(final String plan, 
                                     final List<GeoPoint> waypoints)
    throws Exception
  {
    return getJourneyXml(plan, DEFAULT_SPEED, waypoints);
  } // getJourneyXml
	
  static public String getJourneyXml(final String plan, 
                                     final int speed,
                                     final List<GeoPoint> waypoints) 
    throws Exception 
  {
    final double[] lonLat = new double[waypoints.size()*2];
    for(int i = 0; i != waypoints.size(); ++i)
    {
      int l = i*2;
      lonLat[l] = waypoints.get(i).getLongitudeE6() / 1E6;
      lonLat[l+1] = waypoints.get(i).getLatitudeE6() / 1E6;
    } // for ...
    return ApiClient.getJourneyXml(plan,
                                   null, 
                                   null, 
                                   speed,
                                   lonLat);
  } // getJourneyXml
	
  static public String getJourneyXml(final String plan, 
                                     final long itinerary) 
    throws Exception
  {
    return ApiClient.getJourneyXml(plan, itinerary);
  } // getJourneyXml
    

  static public Journey loadFromXml(final String xml, 
                                    final GeoPoint from, 
                                    final GeoPoint to,
                                    final String name) 
    throws Exception
  {
    final Factory<Journey> factory = factory(from, to, name);
    
    try {
      Xml.parse(xml, factory.contentHandler());
    } // try
    catch(final Exception e) {
      factory.parseException(e);
    } // catch
      
    return factory.get();
  } // loadString

  
	////////////////////////////////////////////////////////////////////////////////
	/*
As at 01 December 2011
<markers>
  <marker start="King's Parade" finish="Orchard Tea Rooms, Grantchester" startBearing="360" startSpeed="0" start_longitude="0.117776" start_latitude="52.205296" finish_longitude="0.096399" finish_latitude="52.177109" crow_fly_distance="3461" event="depart" whence="2007-05-22 14:33:40" speed="16" clientRouteId="0" plan="balanced" note="" length="3949" time="1037" busynance="4471" quietness="88" signalledJunctions="0" signalledCrossings="0" south="52.176655" west="0.095602" north="52.205254" east="0.117861" name="King's Parade to Orchard Tea Rooms, Grantchester" walk="0" leaving="2007-05-22 14:33:40" arriving="2007-05-22 14:50:57" coordinates="0.117805,52.205254 0.117805,52.20517 0.117861,52.205139 0.117709,52.204628 0.117633,52.204128 0.117592,52.203735 0.117546,52.203403 0.117854,52.202404 0.117547,52.202351 0.117497,52.202339 0.116524,52.202164 0.116417,52.202141 0.11608,52.202072 0.115952,52.202053 0.1159,52.202038 0.116158,52.201519 0.116089,52.201496 0.116089,52.201424 0.115928,52.201378 0.115757,52.201298 0.115713,52.201279 0.115822,52.201069 0.115856,52.200619 0.115865,52.200504 0.11575,52.200047 0.115453,52.19978 0.114588,52.199219 0.114528,52.199203 0.114459,52.199165 0.114428,52.199123 0.114785,52.197998 0.114759,52.197975 0.114725,52.197956 0.114657,52.197933 0.114631,52.197929 0.114354,52.197842 0.114265,52.197811 0.112782,52.197372 0.112045,52.19717 0.111377,52.196945 0.111051,52.196774 0.110775,52.196556 0.110695,52.196445 0.110435,52.19648 0.110376,52.195969 0.110382,52.195675 0.110266,52.195194 0.110296,52.195114 0.110304,52.195007 0.108518,52.193333 0.108135,52.193214 0.106687,52.192776 0.106258,52.192623 0.105371,52.192345 0.104757,52.191967 0.103775,52.191292 0.103657,52.191154 0.103604,52.19109 0.103463,52.190895 0.103368,52.190716 0.103274,52.190453 0.103197,52.190189 0.103188,52.189888 0.103081,52.189617 0.102622,52.189152 0.10221,52.188732 0.101935,52.188522 0.101652,52.188236 0.101411,52.187943 0.101137,52.18763 0.100802,52.187153 0.100579,52.186707 0.100321,52.186256 0.100021,52.185848 0.099729,52.185631 0.099326,52.185196 0.098923,52.18475 0.098374,52.184139 0.09815,52.183842 0.097832,52.182858 0.097776,52.1828 0.09722,52.181778 0.097175,52.181702 0.097017,52.181332 0.096708,52.180386 0.096631,52.180122 0.096562,52.179935 0.096433,52.179558 0.096296,52.179333 0.09609,52.179111 0.095901,52.17886 0.096201,52.178658 0.096313,52.17852 0.09634,52.178406 0.096305,52.178154 0.09579,52.177231 0.095645,52.176991 0.095602,52.176872 0.095688,52.1768 0.095877,52.176723 0.096243,52.176655 0.096493,52.177063" grammesCO2saved="736" calories="50" itinerary="1" type="route"/>
  <waypoint longitude="0.117776" latitude="52.205296" sequenceId="1" note="Experimental addition to the API do not use yet."/>
  <waypoint longitude="0.096399" latitude="52.177109" sequenceId="2" note="Experimental addition to the API do not use yet."/>
  <marker name="Link joining St Mary's Passage, King's Parade, NCN" legNumber="0" distance="14" time="4" busynance="14" flow="" walk="0" provisionName="Cycle Track" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#ff0000" points="0.117805,52.205254 0.117805,52.20517 0.117861,52.205139" distances="0,9,5" elevations="17,17,17" type="segment"/>
  <marker name="King's Parade, NCN 11" legNumber="0" distance="158" time="43" busynance="226" flow="" walk="0" provisionName="Road" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#33aa33" points="0.117861,52.205139 0.117709,52.204628 0.117633,52.204128 0.117592,52.203735" distances="0,58,56,44" elevations="17,17,16,17" type="segment"/>
  <marker name="Trumpington Street, NCN 11" legNumber="0" distance="150" time="30" busynance="215" flow="" walk="0" provisionName="Road" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#33aa33" points="0.117592,52.203735 0.117546,52.203403 0.117854,52.202404" distances="0,37,113" elevations="17,17,16" type="segment"/>
  <marker name="Silver Street" legNumber="0" distance="140" time="29" busynance="202" flow="" walk="0" provisionName="Road" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#33aa33" points="0.117854,52.202404 0.117547,52.202351 0.117497,52.202339 0.116524,52.202164 0.116417,52.202141 0.11608,52.202072 0.115952,52.202053 0.1159,52.202038" distances="0,22,4,69,8,24,9,4" elevations="16,16,16,15,15,12,12,12" type="segment"/>
  <marker name="Laundress Lane" legNumber="0" distance="60" time="14" busynance="100" flow="" walk="0" provisionName="Service Road" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#7777cc" points="0.1159,52.202038 0.116158,52.201519" distances="0,60" elevations="12,12" type="segment"/>
  <marker name="Mill Lane, NCN 11" legNumber="0" distance="5" time="2" busynance="8" flow="" walk="0" provisionName="Road" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#33aa33" points="0.116158,52.201519 0.116089,52.201496" distances="0,5" elevations="12,12" type="segment"/>
  <marker name="Granta Place, NCN 11" legNumber="0" distance="8" time="2" busynance="12" flow="" walk="0" provisionName="Road" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#33aa33" points="0.116089,52.201496 0.116089,52.201424" distances="0,8" elevations="12,12" type="segment"/>
  <marker name="Link with Granta Place, NCN 11" legNumber="0" distance="12" time="3" busynance="12" flow="" walk="0" provisionName="Cycle Track" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#ff0000" points="0.116089,52.201424 0.115928,52.201378" distances="0,12" elevations="12,12" type="segment"/>
  <marker name="Unknown street" legNumber="0" distance="15" time="4" busynance="15" flow="" walk="0" provisionName="Cycle Track" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#ff0000" points="0.115928,52.201378 0.115757,52.201298" distances="0,15" elevations="12,12" type="segment"/>
  <marker name="Unknown street" legNumber="0" distance="78" time="16" busynance="78" flow="" walk="0" provisionName="Cycle Track" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#ff0000" points="0.115757,52.201298 0.115713,52.201279 0.115822,52.201069 0.115856,52.200619" distances="0,4,25,50" elevations="12,12,11,11" type="segment"/>
  <marker name="Unknown street" legNumber="0" distance="13" time="3" busynance="13" flow="" walk="0" provisionName="Cycle Track" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#ff0000" points="0.115856,52.200619 0.115865,52.200504" distances="0,13" elevations="11,11" type="segment"/>
  <marker name="Link with The Fen Causeway" legNumber="0" distance="52" time="10" busynance="52" flow="" walk="0" provisionName="Cycle Track" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#ff0000" points="0.115865,52.200504 0.11575,52.200047" distances="0,51" elevations="11,12" type="segment"/>
  <marker name="Unknown street" legNumber="0" distance="122" time="26" busynance="122" flow="" walk="0" provisionName="Cycle Track" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#ff0000" points="0.11575,52.200047 0.115453,52.19978 0.114588,52.199219" distances="0,36,86" elevations="12,12,10" type="segment"/>
  <marker name="Unknown street" legNumber="0" distance="4" time="7" busynance="4" flow="" walk="0" provisionName="Cycle Track" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#ff0000" points="0.114588,52.199219 0.114528,52.199203" distances="0,4" elevations="10,10" type="segment"/>
  <marker name="Unknown street" legNumber="0" distance="6" time="2" busynance="6" flow="" walk="0" provisionName="Cycle Track" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#ff0000" points="0.114528,52.199203 0.114459,52.199165" distances="0,6" elevations="10,10" type="segment"/>
  <marker name="Link with The Fen Causeway" legNumber="0" distance="133" time="31" busynance="133" flow="" walk="0" provisionName="Cycle Track" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#ff0000" points="0.114459,52.199165 0.114428,52.199123 0.114785,52.197998" distances="0,5,127" elevations="10,10,11" type="segment"/>
  <marker name="Unknown street" legNumber="0" distance="3" time="1" busynance="3" flow="" walk="0" provisionName="Cycle Track" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#ff0000" points="0.114785,52.197998 0.114759,52.197975" distances="0,3" elevations="11,11" type="segment"/>
  <marker name="Link with The Fen Causeway" legNumber="0" distance="8" time="8" busynance="8" flow="" walk="0" provisionName="Cycle Track" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#ff0000" points="0.114759,52.197975 0.114725,52.197956 0.114657,52.197933" distances="0,3,5" elevations="11,11,11" type="segment"/>
  <marker name="Unknown street" legNumber="0" distance="309" time="82" busynance="309" flow="" walk="0" provisionName="Cycle Track" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#ff0000" points="0.114657,52.197933 0.114631,52.197929 0.114354,52.197842 0.114265,52.197811 0.112782,52.197372 0.112045,52.19717 0.111377,52.196945 0.111051,52.196774 0.110775,52.196556" distances="0,2,21,7,112,55,52,29,31" elevations="11,11,13,13,12,12,11,12,12" type="segment"/>
  <marker name="Link with Lammas Land access road" legNumber="0" distance="13" time="3" busynance="13" flow="" walk="0" provisionName="Cycle Track" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#ff0000" points="0.110775,52.196556 0.110695,52.196445" distances="0,13" elevations="12,12" type="segment"/>
  <marker name="Lammas Land access road" legNumber="0" distance="18" time="5" busynance="30" flow="" walk="0" provisionName="Service Road" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#7777cc" points="0.110695,52.196445 0.110435,52.19648" distances="0,18" elevations="12,12" type="segment"/>
  <marker name="Grantchester Street" legNumber="0" distance="165" time="64" busynance="197" flow="" walk="0" provisionName="Quiet Street" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#000000" points="0.110435,52.19648 0.110376,52.195969 0.110382,52.195675 0.110266,52.195194 0.110296,52.195114 0.110304,52.195007" distances="0,57,33,54,9,12" elevations="12,13,13,13,13,13" type="segment"/>
  <marker name="Eltisley Avenue" legNumber="0" distance="223" time="57" busynance="263" flow="" walk="0" provisionName="Quiet Street" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#000000" points="0.110304,52.195007 0.108518,52.193333" distances="0,222" elevations="13,13" type="segment"/>
  <marker name="Grantchester Meadows" legNumber="0" distance="139" time="30" busynance="165" flow="" walk="0" provisionName="Quiet Street" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#000000" points="0.108518,52.193333 0.108135,52.193214 0.106687,52.192776" distances="0,29,110" elevations="13,13,13" type="segment"/>
  <marker name="Link between Grantchester Meadows and South Gre..." legNumber="0" distance="102" time="21" busynance="114" flow="" walk="0" provisionName="Track" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#888833" points="0.106687,52.192776 0.106258,52.192623 0.105371,52.192345" distances="0,34,68" elevations="13,13,11" type="segment"/>
  <marker name="Grantchester Meadows" legNumber="0" distance="1661" time="462" busynance="1661" flow="" walk="0" provisionName="Cycle Track" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#ff0000" points="0.105371,52.192345 0.104757,52.191967 0.103775,52.191292 0.103657,52.191154 0.103604,52.19109 0.103463,52.190895 0.103368,52.190716 0.103274,52.190453 0.103197,52.190189 0.103188,52.189888 0.103081,52.189617 0.102622,52.189152 0.10221,52.188732 0.101935,52.188522 0.101652,52.188236 0.101411,52.187943 0.101137,52.18763 0.100802,52.187153 0.100579,52.186707 0.100321,52.186256 0.100021,52.185848 0.099729,52.185631 0.099326,52.185196 0.098923,52.18475 0.098374,52.184139 0.09815,52.183842 0.097832,52.182858 0.097776,52.1828 0.09722,52.181778 0.097175,52.181702 0.097017,52.181332 0.096708,52.180386 0.096631,52.180122 0.096562,52.179935 0.096433,52.179558 0.096296,52.179333 0.09609,52.179111 0.095901,52.17886" distances="0,59,101,17,8,24,21,30,30,33,31,60,55,30,37,36,40,58,52,53,50,31,56,57,78,36,112,7,120,9,43,107,30,21,43,27,28,31" elevations="11,9,11,12,12,12,12,12,10,10,10,11,10,11,11,11,12,12,10,11,10,10,12,12,13,13,16,16,16,16,16,16,16,16,17,17,18,18" type="segment"/>
  <marker name="High Street" legNumber="0" distance="290" time="67" busynance="416" flow="" walk="0" provisionName="Road" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#33aa33" points="0.095901,52.17886 0.096201,52.178658 0.096313,52.17852 0.09634,52.178406 0.096305,52.178154 0.09579,52.177231 0.095645,52.176991 0.095602,52.176872 0.095688,52.1768 0.095877,52.176723 0.096243,52.176655" distances="0,30,17,13,28,109,28,14,10,15,26" elevations="18,18,17,17,17,15,17,17,17,17,17" type="segment"/>
  <marker name="Link with High Street" legNumber="0" distance="48" time="11" busynance="80" flow="" walk="0" provisionName="Service Road" signalledJunctions="0" signalledCrossings="0" turn="straight on" startBearing="360" color="#7777cc" points="0.096243,52.176655 0.096493,52.177063" distances="0,48" elevations="17,15" type="segment"/>
</markers>
	 */
	
  static public Factory<Journey> factory(final GeoPoint from, 
                                         final GeoPoint to,
                                         final String name) 
  { 
    return new JourneyFactory(from, to, name);
  } // factory
  
  static private class JourneyFactory extends Factory<Journey>
  {    
    private final Journey journey_;
    private final GeoPoint from_;
    private final GeoPoint to_;
    private final String name_;
    private int total_time = 0;
    private int total_distance = 0;
    private int itinerary_ = 0;
    private int grammesCO2saved_ = 0;
    private int calories_ = 0;
    private String plan_;
    private int speed_;
    private String start_;
    private String finish_;    

    public JourneyFactory(final GeoPoint from, 
                          final GeoPoint to,
                          final String name) 
    {
      journey_ = new Journey();
      
      from_ = from;
      to_ = to;
      name_ = name;
    } // JourneyFactory
    
    @Override
    protected ContentHandler contentHandler()
    {
      Segment.formatter = DistanceFormatter.formatter(CycleStreetsPreferences.units());

      final RootElement root = new RootElement("markers");
      final Element item = root.getChild("marker");
      item.setStartElementListener(new StartElementListener() {
        @Override
        public void start(final Attributes attr)
        {
          final String type = s(attr, "type");
          final String name = s(attr, "name");
          
          if(type.equals("segment"))
          {
            final String points = s(attr, "points");
            
            final String turn = s(attr, "turn");
             
            final int distance = i(attr, "distance");
            final int time = i(attr, "time");
            final boolean shouldWalk = "1".equals(s(attr, "walk"));

            total_time += time;
            total_distance += distance;
            final Segment seg = new Segment.Step(name,
                                                 turn,
                                                 shouldWalk,
                                                 total_time,
                                                 distance,
                                                 total_distance,
                                                 pointsList(points));
            journey_.segments_.add(seg);
          } // if ...
          if(type.equals("route"))
          {
            grammesCO2saved_ = i(attr, "grammesCO2saved");
            calories_ = i(attr, "calories");
            plan_ = s(attr, "plan");
            speed_ = i(attr, "speed");
            itinerary_ = i(attr, "itinerary");
            start_ = s(attr, "name");
            finish_ = s(attr, "finish");
          } // if ...
        } // start
        
        private String s(final Attributes attr, final String name) { return attr.getValue(name); }
        private int i(final Attributes attr, final String name) 
        { 
          final String v = s(attr, name);
          return v != null ? Integer.parseInt(v) : 0; 
        } // i
      });
      
      root.setEndElementListener(new EndElementListener() {
        @Override
        public void end()
        {
          final GeoPoint pstart = journey_.segments_.get(0).start();
          final GeoPoint pend = journey_.segments_.get(journey_.segments_.size()-1).end();
          final Segment startSeg = new Segment.Start(itinerary_,
                                 name_ != null ? name_ : start_,
                                 plan_, 
                                 speed_,
                                 total_time, 
                                 total_distance, 
                                 calories_,
                                 grammesCO2saved_,
                                 Collections.list(pD(from_, pstart), pstart));
          final Segment endSeg = new Segment.End(finish_, 
                               total_time, 
                               total_distance, 
                               Collections.list(pend, pD(to_, pend)));
          journey_.segments_.add(0, startSeg);
          journey_.segments_.add(endSeg);
        } // end
      });

      return root.getContentHandler();
    } // contentHandler
    
    @Override
    protected Journey get()
    {
      return journey_;
    } // get
    
    private List<GeoPoint> pointsList(final String points)
    {
      final List<GeoPoint> pl = new ArrayList<GeoPoint>();
      final String[] coords = points.split(" ");
      for (final String coord : coords) 
      {
        final String[] yx = coord.split(",");
        final GeoPoint p = new GeoPoint(Double.parseDouble(yx[1]), Double.parseDouble(yx[0]));
        pl.add(p);
      } // for ...
      return pl;
    } // points

  } // class JourneyFactory
	
} // class Journey
