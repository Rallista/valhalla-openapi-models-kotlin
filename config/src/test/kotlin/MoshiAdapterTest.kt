import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.valhalla.config.ValhallaConfigBuilder
import com.valhalla.config.models.ValhallaConfig
import kotlin.test.Test
import kotlin.test.assertNotNull

class MoshiAdapterTest {

  private val moshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

  @Test
  fun testEncode() {
    val config = ValhallaConfigBuilder.DEFAULT
    val encoded = moshi.adapter(ValhallaConfig::class.java).toJson(config)
    assertNotNull(encoded)
  }
}