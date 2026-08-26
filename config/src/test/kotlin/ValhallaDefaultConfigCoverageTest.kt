import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.valhalla.config.ValhallaConfigBuilder
import com.valhalla.config.models.ValhallaConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Pins these models against a real valhalla config.
 *
 * `valhalla-default.json` in the test resources is the output of valhalla's own
 * `scripts/valhalla_build_config`, copied from valhalla-mobile, which generates it from the
 * version of valhalla it is pinned to.
 *
 * Every key valhalla writes has to survive a round trip through these models. When it does not,
 * the config an app hands to the engine quietly loses settings: three misspelled keys
 * (`heirarchy_limits`, `max_interations`, `allow_modifications`) used to drop all 32 of the
 * hierarchy-limit keys on the floor, on both platforms, without any error.
 *
 * A failure here means the spec has drifted from valhalla. Refresh the fixture from
 * valhalla-mobile's `scripts/generate_default_config.sh`, then reconcile `openapi.yaml` with it.
 */
class ValhallaDefaultConfigCoverageTest {

  private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

  private val defaultConfigJson: String =
      requireNotNull(javaClass.getResourceAsStream("/valhalla-default.json")) {
            "valhalla-default.json is missing from the test resources"
          }
          .use { it.readBytes().decodeToString() }

  /**
   * Every key in valhalla's config is one these models know.
   *
   * `failOnUnknown` is the whole test: without it Moshi skips a key it does not recognise, which
   * is exactly how the misspellings went unnoticed.
   */
  @Test
  fun `models cover every key valhalla writes`() {
    val adapter = moshi.adapter(ValhallaConfig::class.java).failOnUnknown()

    assertNotNull(adapter.fromJson(defaultConfigJson), "the valhalla default config should parse")
  }

  /** Nothing valhalla wrote is missing after a parse and a re-serialize. */
  @Test
  fun `round trip keeps every key`() {
    val adapter = moshi.adapter(ValhallaConfig::class.java)

    val config = assertNotNull(adapter.fromJson(defaultConfigJson))
    val roundTripped = adapter.toJson(config)

    // Only one direction is asserted. Serializing also writes keys valhalla left out, because it
    // omits values that are empty and the models carry a default for them regardless.
    val lost = keyPaths(parse(defaultConfigJson)) - keyPaths(parse(roundTripped))
    assertEquals(emptySet(), lost, "these keys did not survive the round trip")
  }

  /** The builder's hand-written defaults agree with valhalla's own. */
  @Test
  fun `builder default agrees with valhalla`() {
    val valhallaDefault = assertNotNull(moshi.adapter(ValhallaConfig::class.java).fromJson(defaultConfigJson))

    val built = ValhallaConfigBuilder().build()

    // A sample rather than every value: the builder is a starting point callers override, and
    // pinning all of it here would just restate the fixture.
    assertEquals(valhallaDefault.mjolnir?.maxCacheSize, built.mjolnir?.maxCacheSize)
    assertEquals(valhallaDefault.mjolnir?.idTableSize, built.mjolnir?.idTableSize)
    assertEquals(valhallaDefault.mjolnir?.hierarchy, built.mjolnir?.hierarchy)
  }

  /** `withTileUrl` sets the keys valhalla reads for on-demand tile fetching. */
  @Test
  fun `withTileUrl configures fetching`() {
    val config =
        ValhallaConfigBuilder()
            .withTileUrl("https://tiles.example/{tilePath}", "/tmp/tiles", tileUrlGz = true)
            .build()

    assertEquals("https://tiles.example/{tilePath}", config.mjolnir?.tileUrl)
    assertEquals(true, config.mjolnir?.tileUrlGz)
    assertEquals("/tmp/tiles", config.mjolnir?.tileDir)
    // The connectivity map cannot answer for tiles that are not downloaded yet.
    assertEquals(false, config.loki?.useConnectivity)
  }

  /** `fromJson` reads a whole config back, and rejects one that is not JSON. */
  @Test
  fun `fromJson round trips`() {
    val config = ValhallaConfigBuilder.fromJson(defaultConfigJson)

    assertEquals("/data/valhalla", config.mjolnir?.tileDir)

    val failure = runCatching { ValhallaConfigBuilder.fromJson("not json") }
    assertNotNull(failure.exceptionOrNull(), "malformed JSON should be rejected")
  }

  private fun parse(json: String): Map<*, *> =
      assertNotNull(moshi.adapter(Map::class.java).fromJson(json))

  /** Every leaf path in the document, as dotted keys. */
  private fun keyPaths(map: Map<*, *>, prefix: String = ""): Set<String> = buildSet {
    for ((key, value) in map) {
      val path = if (prefix.isEmpty()) "$key" else "$prefix.$key"
      if (value is Map<*, *>) addAll(keyPaths(value, path)) else add(path)
    }
  }
}
