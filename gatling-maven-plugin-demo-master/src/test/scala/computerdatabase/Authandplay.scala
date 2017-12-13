package computerdatabase


import io.gatling.core.Predef.{StringBody, _}
import io.gatling.http.Predef._

import scala.concurrent.duration._

class Authandplay  extends Simulation {


  val httpConf = http
    .baseURL("https://pff.yggdrasilgaming.com/game.web/service").inferHtmlResources() // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")
    .disableWarmUp

  val scn = scenario("Ygg_auth_and_play_until_win")

    .exec(http("AuthenticeRequest")
    .get("?fn=authenticate")
    .queryParam("org", "Demo")
    .queryParam("gameid", "7331")
    .queryParam("channel", "pc")
    .queryParam("currency", "EUR")
    .check(jsonPath("$.data.sessid").saveAs("UserSessionID")))

    .asLongAs(
      session => session("WonAmount").asOption[String].map(myValue => !myValue.contains("0")).getOrElse[Boolean](true)) {
      exec(http("PlayRequest")
        .get("?fn=play")
        .queryParam("currency", "EUR")
        .queryParam("gameid", "7316")
        .queryParam("sessid", "${UserSessionID}")
        .queryParam("amount", "100.0")
        .queryParam("coin", "4.0")
        .check(jsonPath("$.data.wager.bets[0].eventdata.wonCoins").saveAs("WonCoinsNumber"))
        .check(jsonPath("$.data.wager.wagerid").saveAs("WagerID")))

        .doIf(session => "0" != session("WonCoinsNumber").as[String]) {
          exec(http("CollectRequest")
            .get("?fn=play")
            .queryParam("currency", "EUR")
            .queryParam("gameid", "7316")
            .queryParam("sessid", "${UserSessionID}")
            .queryParam("amount", "0")
            .queryParam("wagerid", "${WagerID}")
            .queryParam("betid", "1")
            .queryParam("step", "2")
            .queryParam("cmd", "C")
            .check(jsonPath("$.data.wager.bets[0].wonamount").saveAs("WonAmount")))
        }
    }
  val httpProtocol = http.extraInfoExtractor(extraInfo => List(extraInfo.response.bodyLength))
  setUp(scn.inject(atOnceUsers(1)).protocols(httpConf))
}