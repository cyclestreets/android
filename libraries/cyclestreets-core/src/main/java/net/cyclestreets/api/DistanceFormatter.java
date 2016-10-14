package net.cyclestreets.api;

public abstract class DistanceFormatter 
{
	public abstract String distance(int metres);
	public abstract String total_distance(int metres);
	
	static public DistanceFormatter formatter(final String name)
	{
		if("miles".equals(name))
			return milesFormatter;			
		return kmFormatter;
	} // formatter
	
	static private DistanceFormatter kmFormatter = new KmFormatter();
	static private DistanceFormatter milesFormatter = new MilesFormatter();
	
	static private class KmFormatter extends DistanceFormatter
	{
		public String distance(int metres)
		{		
			if(metres < 2000)
				return String.format("%dm", metres);
			return total_distance(metres);
		} // distance
		
		public String total_distance(int metres)
		{
			int km = metres / 1000;
			int frackm = (int)((metres % 1000) / 10.0);
			return String.format("%d.%02dkm", km, frackm);
		} // total_distance
	} // class KmFormatter
	
	static private class MilesFormatter extends DistanceFormatter
	{
		private int metresToYards(int metres) { return (int)(metres * 1.0936133); }
		
		public String distance(int metres)
		{
			int yards = metresToYards(metres);
			if(yards <= 750)
				return String.format("%dyds", yards);
			return total_distance(metres);
		} // distance
		
		public String total_distance(int metres)
		{
			int yards = metresToYards(metres);
			int miles = yards / 1760;
			int frackm = (int)((yards % 1760) / 17.6);
			return String.format("%d.%02d miles", miles, frackm);
		} // total_distance
	} // class MilesFormatter
} // DistanceFormatter
