package lila.api

import org.joda.time.DateTime
import play.api.http.HeaderNames
import play.api.mvc.RequestHeader

import lila.common.ApiVersion

object Mobile {

  object AppVersion {

    def mustUpgrade(v: String) = mustUpgradeFromVersions(v)

    // only call if a more recent version is available in both stores!
    private val mustUpgradeFromVersions = Set(
      "5.1.0", "5.1.1", "5.2.0"
    )

  }

  object Api {

    case class Old(
        version: ApiVersion,
        // date when a newer version was released
        deprecatedAt: DateTime,
        // date when the server stops accepting requests
        unsupportedAt: DateTime
    )

    val currentVersion = ApiVersion(2)

    val acceptedVersions: Set[ApiVersion] = Set(1, 2, 3) map ApiVersion.apply

    val oldVersions: List[Old] = List(
      Old( // chat messages are html escaped
        version = ApiVersion(1),
        deprecatedAt = new DateTime("2016-08-13"),
        unsupportedAt = new DateTime("2016-11-13")
      ),
      Old( // old puzzle API
        version = ApiVersion(2),
        deprecatedAt = new DateTime("2017-10-23"),
        unsupportedAt = new DateTime("2018-03-23")
      )
    )

    private val PathPattern = """/socket/v(\d++)$""".r.unanchored
    private val HeaderPattern = """application/vnd\.lichess\.v(\d++)\+json""".r

    def requestVersion(req: RequestHeader): Option[ApiVersion] = {
      (req.headers.get(HeaderNames.ACCEPT), req.path) match {
        case (Some(HeaderPattern(v)), _) => parseIntOption(v) map ApiVersion.apply
        case (_, PathPattern(v)) => parseIntOption(v) map ApiVersion.apply
        case _ => none
      }
    } filter acceptedVersions.contains

    def requested(req: RequestHeader) = requestVersion(req).isDefined
  }
}
