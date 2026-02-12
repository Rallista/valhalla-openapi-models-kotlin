import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.valhalla.config.ValhallaConfigBuilder
import com.valhalla.config.models.ValhallaConfig
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertNotNull

class MoshiAdapterTest {

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
  fun testEncode() {
    val config = ValhallaConfigBuilder.DEFAULT
    val encoded = moshi.adapter(ValhallaConfig::class.java).toJson(config)
    assertNotNull(encoded)
  }
}