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

package org.eiennohito.kotonoha.web.snippet

import xml.NodeSeq
import net.liftweb.http.S
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json._
import net.liftweb.mongodb.Limit
import net.liftweb.common.Full
import org.eiennohito.kotonoha.util.LangUtil
import com.weiglewilczek.slf4s.Logging
import org.eiennohito.kotonoha.records.dictionary.{JMDictAnnotations, JMDictMeaning, JMString, JMDictRecord}

/**
 * @author eiennohito
 * @since 23.04.12
 */

object JMDict extends Logging {
  import net.liftweb.util.Helpers._
  import org.eiennohito.kotonoha.util.KBsonDSL._

  def fld(in: NodeSeq): NodeSeq = {
    val q = S.param("query").openOr("")
    bind("frm", in, AttrBindParam("value", q, "value"))
  }

  def reduce(in: List[JMString], sep: String): String = {
    in.map(_.value).mkString(sep)
  }

  def rendMeaning(m: JMDictMeaning): NodeSeq = {
    val pos: NodeSeq = m.pos.valueBox match {
      case Full(x) => {
        val lng = JMDictAnnotations.safeValueOf(x.toString()).long
        <div title={lng} class="dict-pos">{x.toString()}</div>
      }
      case _ => Nil
    }
    val bdy = m.vals.is.filter(l => LangUtil.okayLang(l.loc)).flatMap(l => <span class="dict-mean">{l.str}</span>)
    pos ++ bdy
  }

  def list(in: NodeSeq): NodeSeq = {
    val q = S.param("query").openOr("")
    val re = "^" + q
    val inner = "$regex" -> re
    val query: JObject = "$or" -> List(("writing.value" -> inner), ("reading.value" -> inner))
    val jse = compact(render(query))
    logger.debug("query = " + jse)
    val objs = JMDictRecord.findAll(query, Limit(50))
    objs flatMap { o =>
      bind("je", in,
        "writing" -> reduce(o.writing.is, ", "),
        "reading" -> reduce(o.reading.is, ", "),
        "body" -> o.meaning.is.flatMap(m => <div class="m-ent">{rendMeaning(m)}</div>))
    }
  }
}
