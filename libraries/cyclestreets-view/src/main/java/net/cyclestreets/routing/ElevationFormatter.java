package net.cyclestreets.routing;

import java.util.Locale;

public abstract class ElevationFormatter {
  public abstract String height(int metres);
  public abstract String roundedHeight(int metres);
  public abstract String distance(int metres);
  public abstract double roundHeightBelow(int metres);
  public abstract double roundHeightAbove(int metres);

  public static ElevationFormatter formatter(final String name) {
    if ("miles".equals(name))
      return imperialFormatter;
    return metricFormatter;
  }

  private static ElevationFormatter metricFormatter = new MetricFormatter();
  private static ElevationFormatter imperialFormatter = new ImperialFormatter();

  private static class MetricFormatter extends ElevationFormatter {
    @Override
    public String height(int metres) {
      return String.format(Locale.getDefault(), "%dm", metres);
    }

    @Override
    public String roundedHeight(int metres) {
      return height(metres);
    }

    @Override
    public String distance(int metres) {
      if (metres < 1000)
        return String.format(Locale.getDefault(), "%dm", roundDistance(metres));

      float km = metres / 1000f;
      if (km < 5)
        return String.format(Locale.getDefault(), "%.2fkm", km);
      else if (km < 20)
        return String.format(Locale.getDefault(), "%.1fkm", km);
      else
        return String.format(Locale.getDefault(), "%dkm", (int)km);
    }

    @Override
    public double roundHeightBelow(int metres) {
      return metres - (metres % 100);
    }

    @Override
    public double roundHeightAbove(int metres) {
      return metres + 100 - (metres % 100);
    }
  }

  static private class ImperialFormatter extends ElevationFormatter {
    private static final double YARDS_PER_METRE = 1.0936133d;
    private static final double FEET_PER_METRE = 3.2808399d;

    @Override
    public String height(int metres) {
      int feet = (int)(metres * FEET_PER_METRE);
      return String.format(Locale.getDefault(), "%d ft", feet);
    }

    @Override
    public String roundedHeight(int metres) {
      // Everything's stored in metres.  If we want to show a height in feet, then nearest-100
      // gridlines calculated from metre values may end up being e.g. 898ft instead of 900ft.
      // Therefore when labelling graph axes we round (N.B. not floor!) to the nearest 5.
      int feet = (int)(Math.round(metres * FEET_PER_METRE / 5.0) * 5);
      return String.format(Locale.getDefault(), "%d ft", feet);
    }

    @Override
    public String distance(int metres) {
      int yards = (int)(metres * YARDS_PER_METRE);
      if (yards <= 750)
        return String.format(Locale.getDefault(), "%d yards", roundDistance(yards));

      float miles = yards / 1760f;
      if (miles < 5)
        return String.format(Locale.getDefault(), "%.2f miles", miles);
      else if (miles < 20)
        return String.format(Locale.getDefault(), "%.1f miles", miles);
      else
        return String.format(Locale.getDefault(), "%d miles", (int)miles);
    }

    @Override
    public double roundHeightBelow(int metres) {
      int feet = (int)(metres * FEET_PER_METRE);
      feet -= (feet % 100);
      return feet / FEET_PER_METRE;
    }

    @Override
    public double roundHeightAbove(int metres) {
      int feet = (int)(metres * FEET_PER_METRE);
      feet += 100 - (feet % 100);
      return feet / FEET_PER_METRE;
    }
  }

  private static int roundDistance(int units) {
    return (units < 500) ?
              (int)(units/5.0) * 5 :
              (int)(units/10.0) * 10;
  }
}
