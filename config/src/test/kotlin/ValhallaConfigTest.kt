import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.valhalla.config.ValhallaConfigBuilder
import com.valhalla.config.models.ValhallaConfig
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertNotNull

class ValhallaConfigTest {

  object BigDecimalAdapter {
    @FromJson
    fun fromJson(value: String): BigDecimal = BigDecimal(value)

    @ToJson
    fun toJson(value: BigDecimal): String = value.toPlainString()
  }

  private val moshi: Moshi = Moshi.Builder()
    .add(BigDecimalAdapter)
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