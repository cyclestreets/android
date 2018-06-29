package net.cyclestreets.routing;

public abstract class ElevationFormatter {
  public abstract String height(int metres);
  public abstract String distance(int metres);
  public abstract double roundHeightBelow(int metres);
  public abstract double roundHeightAbove(int metres);

  static public ElevationFormatter formatter(final String name) {
    if ("miles".equals(name))
      return imperialFormatter;
    return metricFormatter;
  }

  static private ElevationFormatter metricFormatter = new MetricFormatter();
  static private ElevationFormatter imperialFormatter = new ImperialFormatter();

  static private class MetricFormatter extends ElevationFormatter {
    @Override
    public String height(int metres) {
      return String.format("%dm", metres);
    }

    @Override
    public String distance(int metres) {
      if (metres < 2000)
        return String.format("%dm", round_distance(metres));

      int km = metres / 1000;
      return String.format("%dkm", km);
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
      return String.format("%d ft", feet);
    }

    @Override
    public String distance(int metres) {
      int yards = (int)(metres * YARDS_PER_METRE);
      if (yards <= 750)
        return String.format("%d yards", round_distance(yards));
      int miles = yards / 1760;
      return String.format("%d miles", miles);
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

  static private int round_distance(int units) {
    return (units < 500) ?
              (int)(units/5.0) * 5 :
              (int)(units/10.0) * 10;
  }
}
