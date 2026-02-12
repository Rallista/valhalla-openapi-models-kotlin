import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.valhalla.config.ValhallaConfigBuilder
import com.valhalla.config.models.ValhallaConfig
import kotlin.test.Test
import kotlin.test.assertNotNull

class ValhallaConfigTest {

  private val moshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

  @Test
  fun testDefault() {
    val config = ValhallaConfigBuilder.DEFAULT
    val configJson = moshi.adapter(ValhallaConfig::class.java).toJson(config)

    // Verify serialization works and produces non-null output
    assertNotNull(configJson)
    assert(configJson.contains("mjolnir")) { "Expected JSON to contain mjolnir config" }
    assert(configJson.contains("httpd")) { "Expected JSON to contain httpd config" }
  }
}