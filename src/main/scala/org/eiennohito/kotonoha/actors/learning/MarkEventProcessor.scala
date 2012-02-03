package org.eiennohito.kotonoha.learning

import akka.actor.{ActorLogging, Props, Actor}
import org.eiennohito.kotonoha.utls.DateTimeUtils._
import org.eiennohito.kotonoha.supermemo.{SM6, ItemUpdate}
import net.liftweb.common.{Failure, Empty, Full}
import org.eiennohito.kotonoha.actors.learning.{SchedulePaired, CardScheduler}
import org.eiennohito.kotonoha.actors.{UpdateRecord, SaveRecord, MongoDBActor}
import org.eiennohito.kotonoha.records.{ItemLearningDataRecord, MarkEventRecord}

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

/**
 * @author eiennohito
 * @since 01.02.12
 */

case class ProcessMarkEvents(marks: List[MarkEventRecord])
case class ProcessMarkEvent(mark: MarkEventRecord)

class MarkEventProcessor extends Actor with ActorLogging {
  val sched = context.actorOf(Props[CardScheduler], "scheduler")
  var mongo = context.actorOf(Props[MongoDBActor], "mongoActor")

  protected def receive = {
    case ProcessMarkEvents(evs) => evs.foreach(self ! _)
    case ProcessMarkEvent(ev) => {
      mongo ! SaveRecord(ev)
      ev.card.obj match {
        case Empty => log.debug("invalid mark event: {}", ev)
        case Full(card) => {
          sched ! SchedulePaired(card.word.is, card.cardMode.is)
          val it = ItemUpdate(card.learning.is, ev.mark.is, ev.datetime.is, card.user.is)
          card.learning(SM6.update(it))
          //mongo ! UpdateRecord(card)
          card.update
        }
        case Failure(msg, e, c) => log.error(e.openTheBox, msg)
      }
    }
  }
}
