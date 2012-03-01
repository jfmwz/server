package org.eiennohito.kotonoha.utls

import java.util.Calendar

import akka.util.FiniteDuration
import net.liftweb.util.Helpers.TimeSpan
import org.joda.time.{ReadableInstant, DateTimeZone, DateTime, Duration => JodaDuration}

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

class MultipliableDuration(val dur: JodaDuration) {
  def * (times: Int) = new JodaDuration(dur.getMillis * times)
}

object DateTimeUtils {

  implicit def dateTime2Calendar(dt: DateTime) : Calendar = dt.toCalendar(null)
  implicit def akkaToJodaDurations(dur: FiniteDuration): JodaDuration = new JodaDuration(dur.toMillis)
  implicit def calendar2DateTime(c: Calendar) = new DateTime(c.getTimeInMillis)
  implicit def akkaDurationToLiftTimeSpan(dur: FiniteDuration) : TimeSpan = TimeSpan(dur.toMillis)

  implicit def dur2Multipliable(dur: JodaDuration) = new MultipliableDuration(dur)

  val UTC = DateTimeZone.forID("UTC")

  def ts(dur: FiniteDuration) = akkaDurationToLiftTimeSpan(dur)

  def now = new DateTime(UTC)

  def d(date: DateTime) = date.toDate

  def tonight = {
    val dt = now
    dt.toDateMidnight
  }

  def intervals(begin: ReadableInstant, dur: JodaDuration, times: Int): List[DateTime] = {
    val beg = new DateTime(begin.getMillis)
    (0 until times).map{i => beg.withDurationAdded(dur, i)}.toList
  }
}
