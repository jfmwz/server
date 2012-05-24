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

package org.eiennohito.kotonoha.supermemo

import akka.actor.{Props, ActorRef, Actor}
import org.joda.time.Duration

/**
 * @author eiennohito
 * @since 24.05.12
 */

case object TimeoutSM6
case object TerminateSM6

/**
 * Parent actor for SM6 actors, stores and manages lifteime for child objects.
 */
class SMParentActor extends Actor {
  import akka.util.duration._
  import org.eiennohito.kotonoha.util.DateTimeUtils._
  private var active: Map[Long, ActorRef] = Map()
  private var useTime: Map[Long, Long] = Map()

  def createChildFor(userId: Long): ActorRef = {
    val actor = context.actorOf(Props(new SM6(userId)))
    active += userId -> actor
    actor
  }

  val cancellable = context.system.scheduler.schedule(1 minute, 5 minutes, self, TimeoutSM6)


  override def postStop() {
    cancellable.cancel()
  }

  def time = System.currentTimeMillis()

  protected def receive = {
    case i: ItemUpdate => {
      active.get(i.userId).getOrElse(createChildFor(i.userId)) forward (i)
      useTime += i.userId -> System.currentTimeMillis()
    }
    case TimeoutSM6 => {
      val t = time
      val stale: Set[Long] = useTime filter (p => new Duration(p._2, t).isLongerThan(5 minutes)) map (_._1) toSet()
      stale map { active(_) } foreach { _ ! TerminateSM6 }

      useTime = useTime filter (s => stale.contains(s._1))
      active = active filter (s => stale.contains(s._1))
    }
  }
}
