package com.valhalla.config

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.valhalla.config.models.AdditionalData
import com.valhalla.config.models.Httpd
import com.valhalla.config.models.HttpdService
import com.valhalla.config.models.Logging
import com.valhalla.config.models.Loki
import com.valhalla.config.models.LokiService
import com.valhalla.config.models.LokiServiceDefaults
import com.valhalla.config.models.Meili
import com.valhalla.config.models.MeiliAuto
import com.valhalla.config.models.MeiliBicycle
import com.valhalla.config.models.MeiliDefault
import com.valhalla.config.models.MeiliGrid
import com.valhalla.config.models.MeiliMultimodal
import com.valhalla.config.models.MeiliPedestrian
import com.valhalla.config.models.MeiliService
import com.valhalla.config.models.Mjolnir
import com.valhalla.config.models.MjolnirDataProcessing
import com.valhalla.config.models.Odin
import com.valhalla.config.models.OdinMarkupFormatter
import com.valhalla.config.models.OdinService
import com.valhalla.config.models.ServiceLimits
import com.valhalla.config.models.ServiceLimitsAuto
import com.valhalla.config.models.ServiceLimitsAutoPedestrian
import com.valhalla.config.models.ServiceLimitsBicycle
import com.valhalla.config.models.ServiceLimitsBus
import com.valhalla.config.models.ServiceLimitsCentroid
import com.valhalla.config.models.ServiceLimitsIsochrone
import com.valhalla.config.models.ServiceLimitsMultimodal
import com.valhalla.config.models.ServiceLimitsPedestrian
import com.valhalla.config.models.ServiceLimitsSkadi
import com.valhalla.config.models.ServiceLimitsStatus
import com.valhalla.config.models.ServiceLimitsTrace
import com.valhalla.config.models.Statsd
import com.valhalla.config.models.Thor
import com.valhalla.config.models.ThorService
import com.valhalla.config.models.ValhallaConfig
import java.io.File
import java.io.IOException

class ValhallaConfigBuilder {

  private var config = ValhallaConfigBuilder.DEFAULT

  // TODO: Add more feature rich/dynamic builder functionality

  /**
   * Set the tile extract path
   *
   * e.g. /data/user/0/com.valhalla.valhalla.test/files/valhalla_tiles.tar
   */
  fun withTileExtract(tileExtract: String): ValhallaConfigBuilder {
    config = config.copy(
      mjolnir = config.mjolnir?.copy(
        tileExtract = tileExtract
      )
    )
    return this
  }

  fun withTileDir(tileDir: String): ValhallaConfigBuilder {
    config = config.copy(
      mjolnir = config.mjolnir?.copy(
        tileDir = tileDir
      )
    )
    return this
  }

  /**
   * Set the directory of skadi elevation tiles.
   *
   * Without it valhalla's `height` action answers null for every point.
   *
   * e.g. /data/user/0/com.valhalla.valhalla.test/files/elevation
   */
  fun withElevation(elevationDir: String): ValhallaConfigBuilder {
    config = config.copy(
      additionalData = (config.additionalData ?: AdditionalData()).copy(
        elevation = elevationDir
      )
    )
    return this
  }

  /**
   * Fetch tiles from a server on demand, caching them in [tileDir].
   *
   * Valhalla fills the `{tilePath}` portion of [tileUrl] in with the tile it wants.
   *
   * The connectivity map is turned off, because it is built from the tiles that are present and so
   * cannot answer for tiles that have not been downloaded yet.
   *
   * @param tileUrl the URL pattern tiles are fetched from.
   * @param tileDir where downloaded tiles are stored.
   * @param tileUrlGz whether the server serves gzip-compressed tiles.
   */
  fun withTileUrl(
    tileUrl: String,
    tileDir: String,
    tileUrlGz: Boolean = false
  ): ValhallaConfigBuilder {
    config = config.copy(
      mjolnir = config.mjolnir?.copy(
        tileUrl = tileUrl,
        tileUrlGz = tileUrlGz,
        tileDir = tileDir
      ),
      loki = config.loki?.copy(
        useConnectivity = false
      )
    )
    return this
  }

  /**
   * Start from [config] rather than from [DEFAULT].
   *
   * Use this to layer the `with` functions on top of a config that came from somewhere else, such
   * as [fromJson].
   */
  fun startingFrom(config: ValhallaConfig): ValhallaConfigBuilder {
    this.config = config
    return this
  }

  fun build(): ValhallaConfig {
    return config
  }

  companion object {

    private val defaultMoshi: Moshi by lazy {
      Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }

    /**
     * Parse a complete valhalla config from a JSON string.
     *
     * The JSON must already carry tile paths that are correct for the device it will run on;
     * nothing here rewrites them.
     *
     * @throws IllegalArgumentException if the JSON is malformed or is not a valhalla config.
     */
    @JvmStatic
    @JvmOverloads
    fun fromJson(json: String, moshi: Moshi = defaultMoshi): ValhallaConfig =
      try {
        moshi.adapter(ValhallaConfig::class.java).fromJson(json)
          ?: throw IllegalArgumentException("the valhalla config JSON was the literal null")
      } catch (e: JsonDataException) {
        throw IllegalArgumentException("the valhalla config JSON could not be read", e)
      } catch (e: IOException) {
        throw IllegalArgumentException("the valhalla config JSON was malformed", e)
      }

    /**
     * Parse a complete valhalla config from a file.
     *
     * @throws IllegalArgumentException if the file is malformed or is not a valhalla config.
     * @throws IOException if the file cannot be read.
     */
    @JvmStatic
    @JvmOverloads
    fun fromFile(file: File, moshi: Moshi = defaultMoshi): ValhallaConfig =
      fromJson(file.readText(), moshi)

    val DEFAULT = ValhallaConfig(
      additionalData = AdditionalData(),
      httpd = Httpd(
        HttpdService(
          // TODO: We can remove this if we fix the asterisk becoming a _* from openapi-generator.
          listen = "tcp://*:8002"
        )
      ),
      logging = Logging(),
      loki = Loki(
        service = LokiService(),
        serviceDefaults = LokiServiceDefaults(),
      ),
      meili = Meili(
        auto = MeiliAuto(),
        bicycle = MeiliBicycle(),
        default = MeiliDefault(),
        grid = MeiliGrid(),
        multimodal = MeiliMultimodal(),
        pedestrian = MeiliPedestrian(),
        service = MeiliService()
      ),
      mjolnir = Mjolnir(
        dataProcessing = MjolnirDataProcessing(),
      ),
      odin = Odin(
        markupFormatter = OdinMarkupFormatter(),
        service = OdinService()
      ),
      serviceLimits = ServiceLimits(
        auto = ServiceLimitsAuto(),
        autoPedestrian = ServiceLimitsAutoPedestrian(),
        bicycle = ServiceLimitsBicycle(),
        bikeshare = ServiceLimitsBicycle(),
        bus = ServiceLimitsBus(),
        centroid = ServiceLimitsCentroid(),
        isochrone = ServiceLimitsIsochrone(),
        motorScooter = ServiceLimitsBicycle(),
        motorcycle = ServiceLimitsBicycle(),
        multimodal = ServiceLimitsMultimodal(),
        pedestrian = ServiceLimitsPedestrian(),
        skadi = ServiceLimitsSkadi(),
        status = ServiceLimitsStatus(),
        taxi = ServiceLimitsAuto(),
        trace = ServiceLimitsTrace(),
        transit = ServiceLimitsBicycle(),
        truck = ServiceLimitsAuto()
      ),
      statsd = Statsd(),
      thor = Thor(
        service = ThorService()
      )
    )
  }
}