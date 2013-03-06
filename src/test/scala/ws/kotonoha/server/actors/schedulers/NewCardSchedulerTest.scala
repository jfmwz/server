package ws.kotonoha.server.actors.schedulers

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FreeSpec
import ws.kotonoha.server.akka.AkkaTest
import ws.kotonoha.server.test.TestWithAkka
import ws.kotonoha.server.records.{UserTagInfo, WordCardRecord, UserRecord}
import akka.actor.Props
import org.bson.types.ObjectId
import net.liftweb.common.Empty
import org.joda.time.DateTime
import com.mongodb.casbah.WriteConcern
import ws.kotonoha.server.records.events.NewCardSchedule

/**
 * @author eiennohito
 * @since 06.03.13 
 */

class NewCardSchedulerTest extends TestWithAkka with FreeSpec with ShouldMatchers {
  implicit val sender = testActor
  import concurrent.duration._
  import com.foursquare.rogue.LiftRogue._

  val uid = createUser()
  val usvc = kta.userContext(uid)

  def createCard(wid: ObjectId = new ObjectId(), tags: List[String] = Nil) = {
    val card = WordCardRecord.createRecord
    card.word(wid).user(uid).learning(Empty).notBefore(new DateTime).enabled(true).tags(tags)
    card.save(WriteConcern.Safe)
  }

  "newcardscheduler" - {
    val actor = usvc.userActor[NewCardScheduler]("ncs")

    "selects 2 cards" in {
      val cards = Seq.fill(2)(createCard())
      actor.receive(CardRequest(State.Normal, 20, 0, 0, 10), testActor)
      val msg = receiveOne(1 second).asInstanceOf[PossibleCards]
      msg.cards should have length (2)
      cleanup(cards)
    }

    "selects 2 cards from 8 when there is a limit on tags" - {
      val empty = Seq.fill(3)(createCard())
      val full = Seq.fill(5)(createCard(tags = List("tag")))
      val ti = UserTagInfo.createRecord.user(uid).tag("tag").limit(2).save(WriteConcern.Safe)
      actor.receive(CardRequest(State.Normal, 20, 0, 0, 10), testActor)
      val msg = receiveOne(1 second).asInstanceOf[PossibleCards]
      msg.cards should have length (5)
      actor.receive(CardsSelected(5))
      val scheds = NewCardSchedule where (_.user eqs uid) and (_.tag eqs "tag") fetch()
      scheds should have length (2)
      cleanup(empty, full, Seq(ti), scheds)
    }
  }
}
