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

package ws.kotonoha.server.actors.dict

import akka.actor.Actor
import ws.kotonoha.server.actors.KotonohaMessage
import java.io.File
import net.liftweb.util.{Props => LP}
import ws.kotonoha.server.records.dictionary.ExampleSentenceRecord
import ws.kotonoha.server.dict.{TatoebaLink, TatoebaLinks}
import akka.event.LoggingReceive

/**
 * @author eiennohito
 * @since 29.04.12
 */

trait ExampleMessage extends KotonohaMessage

case class LoadExamples(data: List[ExampleIds]) extends ExampleMessage

case class TranslationsWithLangs(ids: List[Long], langs: List[String]) extends ExampleMessage

case class ExampleIds(jap: Long, other: List[TatoebaLink])

case class ExampleEntry(jap: ExampleSentenceRecord, other: List[ExampleSentenceRecord])

class ExampleActor extends Actor {

  val exampleSearcher = //new TatoebaLinks(new File(LP.get("example.index").get))
    ExampleSearcher

  override def postStop() {
    super.postStop()
    //exampleSearcher.close()
  }

  override def receive = LoggingReceive {
    case LoadExamples(eids) => {
      sender ! ExampleSearch.loadExamples(eids)
    }
    case TranslationsWithLangs(ids, lang) => {
      sender ! ExampleSearch.translationsWithLangs(ids, lang)
    }
  }
}

object ExampleSearcher extends TatoebaLinks(new File(LP.get("example.index").get))

object ExampleSearch {
  def translationsWithLangs(ids: List[Long], langs: List[String]) = {
    ids map {
      id =>
        val other = ExampleSearcher.from(id).filter(t => langs.contains(t.rightLang))
        ExampleIds(id, other.toList)
    }
  }

  def loadExamples(eids: List[ExampleIds]) = {
    import com.foursquare.rogue.LiftRogue._
    val ids = (eids flatMap (ei => List(ei.jap) ++ ei.other.map(_.right))).distinct
    val recs = (ExampleSentenceRecord where (_.id in ids) fetch() map (r => r.id.is -> r)).toMap
    eids.flatMap(r => {
      val exr = recs.get(r.jap)
      val o = r.other.flatMap(t => recs.get(t.right))
      exr.map(e => ExampleEntry(e, o))
    })
  }
}
