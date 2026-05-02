import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.valhalla.api.models.CostingModel
import kotlin.test.Test
import com.valhalla.api.models.RouteRequest
import com.valhalla.api.models.RoutingWaypoint
import io.kotest.assertions.json.shouldEqualJson

class RouteRequestTest {

  private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

  @Test
  fun testRouteRequest() {
    val request = RouteRequest(
      locations =
      listOf(
        RoutingWaypoint(lat = 45.843812, lon = -123.768205),
        RoutingWaypoint(lat = 45.869701, lon = -123.766121)
      ),
      costing = CostingModel.auto,
      format = RouteRequest.Format.json
    )

    val actualJson = moshi.adapter(RouteRequest::class.java).toJson(request)

    actualJson.shouldEqualJson {
      """
      {
        "locations": [
          {
            "lat": 45.843812,
            "lon": -123.768205,
            "type": "break",
            "heading_tolerance": 60,
            "minimum_reachability": 50,
            "radius": 0,
            "rank_candidates": true,
            "node_snap_tolerance": 5,
            "street_side_tolerance": 5,
            "street_side_max_distance": 1000
          },
          {
            "lat": 45.869701,
            "lon": -123.766121,
            "type": "break",
            "heading_tolerance": 60,
            "minimum_reachability": 50,
            "radius": 0,
            "rank_candidates": true,
            "node_snap_tolerance": 5,
            "street_side_tolerance": 5,
            "street_side_max_distance": 1000
          }
        ],
        "costing": "auto",
        "units": "km",
        "language": "en-US",
        "directions_type": "instructions",
        "format": "json",
        "shape_format": "polyline6"
      }
      """
    }
  }
}
//{"locations":[{"lat":45.843812,"lon":-123.768205},{"lat":45.869701,"lon":-123.766121}],"costing":"auto","units":"miles"}