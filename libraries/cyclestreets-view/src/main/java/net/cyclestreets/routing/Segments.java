package net.cyclestreets.routing;

import java.util.Iterator;
import java.util.LinkedList;

import net.cyclestreets.util.Turn;
import org.osmdroid.api.IGeoPoint;

public class Segments implements Iterable<Segment>
{
  private final LinkedList<Segment> segments = new LinkedList<>();

  public Segments() {}

  public int count() { return segments.size(); }
  public boolean isEmpty() { return segments.isEmpty(); }

  public IGeoPoint startPoint() { return segments.getFirst().start(); }
  public IGeoPoint finishPoint() { return segments.getLast().finish(); }
  public Segment.Start first() { return (Segment.Start) segments.getFirst(); }
  public Segment.End last() { return (Segment.End) segments.getLast(); }

  public void add(final Segment seg) {
    if (seg instanceof Segment.Start) {
      segments.addFirst(seg);
      return;
    }

    if (count() != 0) {
      final Segment previous = segments.get(count() - 1);

      // Meld "Join Roundabout" instructions
      if (Turn.JOIN_ROUNDABOUT.equals(previous.turn)) {
        segments.remove(previous);
        segments.add(new Segment.Step(previous, seg, seg.turn, seg.turnInstruction));
        return;
      }

      // Meld staggered crossroads
      if (previous.distance_ < 20) {
        if (Turn.TURN_LEFT.equals(previous.turn) && Turn.TURN_RIGHT.equals(seg.turn)) {
          segments.remove(previous);
          segments.add(new Segment.Step(previous, seg, Turn.LEFT_RIGHT, Turn.LEFT_RIGHT.getTextInstruction()));
          return;
        }
        if (Turn.TURN_RIGHT.equals(previous.turn) && Turn.TURN_LEFT.equals(seg.turn)) {
          segments.remove(previous);
          segments.add(new Segment.Step(previous, seg, Turn.RIGHT_LEFT, Turn.RIGHT_LEFT.getTextInstruction()));
          return;
        }
      }

      // Meld bridges
      if (previous.distance_ < 100 && "Bridge".equalsIgnoreCase(previous.name_) && seg.turn == Turn.STRAIGHT_ON) {
        segments.remove(previous);
        segments.add(new Segment.Step(previous, seg, previous.turn, previous.turnInstruction + " over Bridge"));
        return;
      }
    }

    segments.add(seg);
  }

  public Segment get(final int i) { return segments.get(i); }

  @Override
  public Iterator<Segment> iterator() { return segments.iterator(); }

  public Iterator<IGeoPoint> pointsIterator() { return new PointsIterator(this); }

  private static class PointsIterator implements Iterator<IGeoPoint>  {
    private final Iterator<Segment> segments_;
    private Iterator<IGeoPoint> points_;

    PointsIterator(final Segments segments) {
      segments_ = segments.iterator();
      if (!segments_.hasNext())
        return;

      points_ = segments_.next().points();
    }

    @Override
    public boolean hasNext() {
      return points_ != null && points_.hasNext();
    }

    @Override
    public IGeoPoint next() {
      if (!hasNext())
        throw new IllegalStateException();

      final IGeoPoint p = points_.next();

      if (!hasNext()) {
        if (segments_.hasNext())
          points_ = segments_.next().points();
        else
          points_ = null;
      }

      return p;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

}
