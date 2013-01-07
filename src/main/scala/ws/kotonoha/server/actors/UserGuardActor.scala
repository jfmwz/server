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

package ws.kotonoha.server.actors

import akka.pattern.{ask, pipe}
import akka.actor._
import auth.ClientRegistry
import dict.{ExampleMessage, ExampleActor, DictionaryActor}
import interop.{JumanRouter, JumanMessage, JumanPipeActor}
import learning._
import lift.{CreateActor, LiftMessage, LiftActorService}
import model.{CardMessage, WordMessage, CardActor, WordActor}
import net.liftweb.http.CometMessage
import com.fmpwizard.cometactor.pertab.namedactor.{NamedCometMessage, PertabCometManager}
import ws.kotonoha.server.learning.{EventMessage, EventProcessor}
import akka.routing.RoundRobinRouter

/**
 * @author eiennohito
 * @since 25.04.12
 */

class UserGuardActor extends UserScopedActor with ActorLogging {
  import SupervisorStrategy._
  import akka.util.duration._
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 1500, withinTimeRange = 1 day) {
    case e: Exception => log.error(e, "Caught an exception in guard actor"); Restart
  }

  val mongo = context.actorOf(Props[MongoDBActor], "mongo")
  val wordSelector = context.actorOf(Props[WordSelector], "wordSelector")
  val markProcessor = context.actorOf(Props[EventProcessor], "markProc")
  val qractor = context.actorOf(Props[QrCreator], "qr")
  val userToken = context.actorOf(Props[UserTokenActor], "token")
  val wordActor = context.actorOf(Props[WordActor], "word")
  val cardActor = context.actorOf(Props[CardActor], "card")
  val dictActor = context.actorOf(Props[DictionaryActor], "dict")
  val exampleActor = context.actorOf(Props[ExampleActor], "example")

  def dispatch(msg: KotonohaMessage) {
    users ! PingUser(uid)
    msg match {
      case _: SelectWordsMessage => wordSelector.forward(msg)
      case _: EventMessage => markProcessor.forward(msg)
      case _: DbMessage => mongo.forward(msg)
      case _: QrMessage => qractor.forward(msg)
      case _: TokenMessage => userToken.forward(msg)
      case _: WordMessage => wordActor.forward(msg)
      case _: CardMessage => cardActor.forward(msg)
      case _: DictionaryMessage => dictActor.forward(msg)
      case _: ExampleMessage => exampleActor.forward(msg)
    }
  }

  protected def receive = {
    case CreateActor(p, name) => name match {
      case null | "" => sender ! context.actorOf(p)
      case _ => sender ! context.actorOf(p, name)
    }
    case msg : KotonohaMessage => dispatch(msg)
  }
}
