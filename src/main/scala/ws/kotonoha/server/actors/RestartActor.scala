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
import learning._
import lift.{LiftMessage, LiftActorService}
import model.{CardMessage, WordMessage, CardActor, WordActor}
import net.liftweb.http.CometMessage
import com.fmpwizard.cometactor.pertab.namedactor.{NamedCometMessage, PertabCometManager}
import ws.kotonoha.server.learning.{EventMessage, EventProcessor}

/**
 * @author eiennohito
 * @since 25.04.12
 */

trait KotonohaMessage
trait DbMessage extends KotonohaMessage
trait LifetimeMessage extends KotonohaMessage
trait ClientMessage extends KotonohaMessage
trait TokenMessage extends KotonohaMessage
trait DictionaryMessage extends KotonohaMessage
trait SelectWordsMessage extends KotonohaMessage

class RestartActor extends Actor with ActorLogging {
  import SupervisorStrategy._
  import akka.util.duration._
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 1500, withinTimeRange = 1 day) {
    case e: Exception => log.error(e, "Caught an exception in root actor"); Restart
  }

  val mongo = context.actorOf(Props[MongoDBActor], "mongo")
  lazy val wordSelector = context.actorOf(Props[WordSelector])
  lazy val markProcessor = context.actorOf(Props[EventProcessor])
  lazy val lifetime = context.actorOf(Props[LifetimeActor])
  lazy val qractor = context.actorOf(Props[QrCreator])
  lazy val clientActor = context.actorOf(Props[ClientRegistry])
  lazy val userToken = context.actorOf(Props[UserTokenActor])
  lazy val luceneActor = context.actorOf(Props[ExampleSearchActor])
  lazy val wordActor = context.actorOf(Props[WordActor])
  lazy val cardActor = context.actorOf(Props[CardActor])
  lazy val liftActor = context.actorOf(Props[LiftActorService])
  lazy val dictActor = context.actorOf(Props[DictionaryActor])
  lazy val exampleActor = context.actorOf(Props[ExampleActor])
  lazy val cometActor = context.actorOf(Props[PertabCometManager])
  lazy val securityActor = context.actorOf(Props[SecurityActor])


  def dispatch(msg: KotonohaMessage) {
    msg match {
      case _: SelectWordsMessage => wordSelector.forward(msg)
      case _: EventMessage => markProcessor.forward(msg)
      case _: DbMessage => mongo.forward(msg)
      case _: LifetimeMessage => lifetime.forward(msg)
      case _: QrMessage => qractor.forward(msg)
      case _: ClientMessage => clientActor.forward(msg)
      case _: TokenMessage => userToken.forward(msg)
      case _: SearchMessage => luceneActor.forward(msg)
      case _: WordMessage => wordActor.forward(msg)
      case _: CardMessage => cardActor.forward(msg)
      case _: LiftMessage => liftActor.forward(msg)
      case _: DictionaryMessage => dictActor.forward(msg)
      case _: ExampleMessage => exampleActor.forward(msg)
      case _: NamedCometMessage => cometActor.forward(msg)
      case _: SecurityMessage => securityActor.forward(msg)
    }
  }

  protected def receive = {
    case TopLevelActors => sender ! (wordSelector, markProcessor)
    case msg : KotonohaMessage => dispatch(msg)
  }
}
