package functional.general

import conf.GameSettingsFeeder
import io.gatling.core.Predef._
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import io.gatling.http.check.HttpCheck
import main._
import main.helpers.custom.BaseTestUtils._
import main.requests.AuthenticateRequest
import main.requests.Functions._
import main.requests.PlayRequest._

import scala.concurrent.duration._


class cheattest extends BaseTestWithBlock {

  val currency = "EUR"
  val org = defaultOrg



  testCase(name = "Should not return errors") {
    exec(AuthenticateRequest.authenticate())
      .exec(playAndSmallWin())
  }


  def playAndSmallWin() = {
    exec(session => {
      val gameId = session("gameId").as[String]
      val coin = getCoin(gameId = gameId, org = org, currency = currency)
      val name = gameFeeder.getGameSettingByGameId(gameId, "gameName")
      var bet = calcBet(gameId = gameId, coin = coin, betCoins = gameFeeder.getGameSettingByGameId(gameId, "betCoins").asInstanceOf[Int])
      session
        .set("bet", bet)
        .set("coin", coin)
        .set("currency", currency)
        .set("playData",
          new Play(
            sessionId = session("Player").as[Player].sessionid,
            bet = bet.toString,
           mode = if (gameId == "7334") "e" else "")
            .queryParam("coin", coin.toString)
            .queryParam("currency", currency.toString)
            .queryParam("fn", "play")
            .queryParam("test", "true"))
    })
      .exec(play()
        .check(jsonPath("$.code")is("-1")))
  }


  setUp(
    scenarioSetup
  )
}
