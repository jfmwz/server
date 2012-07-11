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

package org.eiennohito.kotonoha.actors.lift

import akka.actor.{Actor, ActorRef}
import akka.pattern.{ask, pipe}
import com.fmpwizard.cometactor.pertab.namedactor.NamedCometMessage
import net.liftweb.http.js.JsCmd

/**
 * @author eiennohito
 * @since 11.07.12
 */

trait PerUserMessage extends NamedCometMessage
case class RegisterPerUserActor(user: Long, actor: ActorRef) extends PerUserMessage
case class DestroyActor(user: Long) extends PerUserMessage
case class ForUser(user: Long, cmd: JsCmd) extends PerUserMessage
case class Bcast(cmd: JsCmd) extends PerUserMessage

case class ExecJs(cmd: JsCmd)

class PerUserActorSvc extends Actor {

  val storage = new collection.mutable.HashMap[Long, ActorRef]

  protected def receive = {
    case RegisterPerUserActor(user, actor) => storage += user -> actor
    case DestroyActor(user) => storage -= user
    case ForUser(user, cmd) => storage.get(user) map {a => a.forward(ExecJs(cmd))}
    case Bcast(cmd) => storage.foreach {case (_, a) => a.forward(ExecJs(cmd))}
  }
}
