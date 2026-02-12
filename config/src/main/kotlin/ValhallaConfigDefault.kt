package com.valhalla.config

import com.valhalla.config.models.AdditionalData
import com.valhalla.config.models.Httpd
import com.valhalla.config.models.HttpdService
import com.valhalla.config.models.Logging
import com.valhalla.config.models.Loki
import com.valhalla.config.models.LokiLogging
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
import com.valhalla.config.models.ThorLogging
import com.valhalla.config.models.ThorService
import com.valhalla.config.models.ValhallaConfig

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

  fun build(): ValhallaConfig {
    return config
  }

  companion object {
    val DEFAULT = ValhallaConfig(
      additionalData = AdditionalData(),
      httpd = Httpd(
        HttpdService(
          // TODO: We can remove this if we fix the asterisk becoming a _* from openapi-generator.
          listen = "tcp://*:8002"
        )
      ),
      loki = Loki(
        logging = LokiLogging(),
        service = LokiService(),
        serviceDefaults = LokiServiceDefaults(),
      ),
      meili = Meili(
        auto = MeiliAuto(),
        bicycle = MeiliBicycle(),
        default = MeiliDefault(),
        grid = MeiliGrid(),
        logging = Logging(),
        multimodal = MeiliMultimodal(),
        pedestrian = MeiliPedestrian(),
        service = MeiliService()
      ),
      mjolnir = Mjolnir(
        dataProcessing = MjolnirDataProcessing(),
        logging = Logging(),
      ),
      odin = Odin(
        logging = Logging(),
        markupFormatter = OdinMarkupFormatter(),
        service = OdinService()
      ),
      serviceLimits = ServiceLimits(
        auto = ServiceLimitsAuto(),
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
        logging = ThorLogging(),
        service = ThorService()
      )
    )
  }
}