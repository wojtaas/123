import io.gatling.core.Predef.{StringBody, _}
import io.gatling.http.Predef._

import scala.concurrent.duration._

class Authandplay  extends Simulation {


  val httpConf = http
    .baseURL("https://pff.yggdrasilgaming.com/").inferHtmlResources() // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")
    .disableWarmUp

  val scn = scenario("Ygg_auth_and_play_until_win")

    .exec(http("AuthenticeRequest")
    .get("game.web/service?fn=authenticate")
    .queryParamMap(Map("org" -> "Demo",
      "gameid" -> "7331",
      "channel" -> "pc",
      "currency" -> "EUR"))
      .check(jsonPath("$.data.sessid").saveAs("UserSessionID")))

    .asLongAs(
      session => session("WonAmount").asOption[String].map(myValue => !myValue.contains("0")).getOrElse[Boolean](true)) {
      exec(http("PlayRequest")
        .get("game.web/service?fn=play")
        .queryParamMap(Map(
          "currency"-> "EUR",
          "gameid" -> "7316",
          "sessid" -> "${UserSessionID}",
          "amount" -> "100.0",
          "coin" -> "4.0"))
        .check(jsonPath("$.data.wager.status").saveAs("Status"))
        .check(jsonPath("$.data.wager.wagerid").saveAs("WagerID")))

        .doIf(
          session => session("Status").as[String].contains("Pending"))
        {
          exec(http("CollectRequest")
            .get("game.web/service?fn=play")
            .queryParamMap(Map(
              "currency" -> "EUR",
              "gameid" -> "7316",
              "sessid" -> "${UserSessionID}",
              "amount" -> "0",
              "wagerid" -> "${WagerID}",
              "betid" -> "1",
              "step" -> "2",
              "cmd" -> "C"))
            .check(jsonPath("$.data.wager.bets[0].wonamount").saveAs("WonAmount")))
        }
    }

  setUp(scn.inject(atOnceUsers(1)).protocols(httpConf))
}