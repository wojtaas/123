package functional.general

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import main._
import main.helpers.custom.BaseTestUtils._
import main.requests.AuthenticateRequest
import main.requests.PlayRequest._



class cheattest extends BaseTestWithBlock {

  val currency = "EUR"
  val org = defaultOrg



  testCase(name = "Should not return errors") {
    exec(AuthenticateRequest.authenticate())
      .exec(tryplaywithcheats())
  }


  def tryplaywithcheats() = {
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
