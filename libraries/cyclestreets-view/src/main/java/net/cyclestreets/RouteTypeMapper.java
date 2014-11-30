package net.cyclestreets;

import java.util.Map;
import java.util.HashMap;

import net.cyclestreets.view.R;

public final class RouteTypeMapper {
	static private final Map<Integer, String> mappings = initialiseMappings();

	static private Map<Integer, String> initialiseMappings() {
		final Map<Integer, String> map = new HashMap<>();
		map.put(R.id.routeQuietest, RoutePlans.PLAN_QUIETEST);
		map.put(R.id.routeBalanced, RoutePlans.PLAN_BALANCED);
		map.put(R.id.routeFastest, RoutePlans.PLAN_FASTEST);
		map.put(R.id.routeShortest, RoutePlans.PLAN_SHORTEST);
		return map;
	} // initialiseMappings

	static public String nameFromId(int id) {
		return mappings.get(id);
	} // nameFromId

	static public int idFromName(final String name) {
		for(final Map.Entry<Integer,String> entry : mappings.entrySet())
			if(entry.getValue().equals(name))
				return entry.getKey();
		return -1;
	} // idFromName

	private RouteTypeMapper() {
		// prevent instantiation
	} // RouteTypeMapper
} // class RouteTypeMapper
