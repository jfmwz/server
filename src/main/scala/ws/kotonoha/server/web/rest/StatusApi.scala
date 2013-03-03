/*
 * Copyright 2012 eiennohito
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ws.kotonoha.server.web.rest

import ws.kotonoha.server.actors.ioc.ReleaseAkka
import net.liftweb.common.Full
import net.liftweb.http.{JsonResponse, PlainTextResponse}
import ws.kotonoha.server.records.{WordRecord, WordCardRecord, UserRecord}
import concurrent.Future
import ws.kotonoha.server.actors.schedulers.RepetitionStateResolver
import net.liftweb.json.{Extraction, DefaultFormats}
import ws.kotonoha.server.util.Stat

/**
 * @author eiennohito
 * @since 01.04.12
 */

trait StatusTrait extends KotonohaRest with OauthRestHelper {

  import com.foursquare.rogue.LiftRogue._
  import ws.kotonoha.server.util.DateTimeUtils._

  serve {
    case "api" :: "status" :: Nil Get req => {
      UserRecord.currentId match {
        case Full(id) => {
          val user = UserRecord.find(id).openOrThrowException("I am here?")
          val cards = WordCardRecord where (_.user eqs id) and (_.notBefore lt now) and
            (_.learning subfield (_.intervalEnd) before now) count()
          PlainTextResponse("You are user " + user.username.is + " and have " + cards + " cards scheduled")
        }
        case _ => PlainTextResponse("Should not get this", 320)
      }
    }
    case "api" :: "stats" :: Nil Get req => {
      implicit val formats = DefaultFormats
      val t = new Timer

      async(userId) {
        uid =>
          val f = Future.successful(new RepetitionStateResolver(uid))
          f map {
            o =>
              val words = WordRecord where (_.user eqs uid) count()
              val cards = WordCardRecord where (_.user eqs uid) count()
              val resp = Resp(
                o.resolveState().toString,
                o.last.toList,
                o.lastStat.stat,
                words,
                cards,
                o.today,
                o.badCount,
                o.newAvailable,
                o.scheduledCnt,
                o.unavailable,
                o.learnt,
                o.next
              )
              t.print(req)
              Full(JsonResponse(Extraction.decompose(resp)))
          }
      }
    }
  }
}

case class Resp(
                 state: String, last: List[Int],
                 lastStat: Stat,
                 words: Long, cards: Long,
                 today: Long,
                 badCards: Long, newCards: Long,
                 schedCards: Long, unvailableCards: Long, learntCards: Long,
                 next: List[Int]
                 )

class StatusApi extends StatusTrait with ReleaseAkka
