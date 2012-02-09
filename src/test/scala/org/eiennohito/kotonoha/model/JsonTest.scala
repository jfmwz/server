package org.eiennohito.kotonoha.model

import converters.DateTimeTypeConverter
import events.MarkEvent
import learning.{Container, WordCard, Word}
import net.liftweb.json.{Printer, JsonAST}
import com.google.gson.GsonBuilder
import org.joda.time.DateTime
import net.liftweb.json.JsonAST.JObject
import org.eiennohito.kotonoha.actors.learning.WordsAndCards
import org.eiennohito.kotonoha.utls.{DateTimeUtils, ResponseUtil}
import org.eiennohito.kotonoha.records.{MarkEventRecord, WordCardRecord, ExampleRecord, WordRecord}


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
 * @since 31.01.12
 */

class JsonTest extends org.scalatest.FunSuite with org.scalatest.matchers.ShouldMatchers {
  
  val gson = {
    val gb = new GsonBuilder
    gb.registerTypeAdapter(classOf[DateTime], new DateTimeTypeConverter)    
    gb.create()
  }
  
  def card = WordCardRecord.createRecord.word(5).cardMode(CardMode.READING)
  
  def word = {
    val ex1 = ExampleRecord.createRecord.example("ex").translation("tr")
    val rec = WordRecord.createRecord
    rec.writing("wr").reading("re").meaning("me").examples(List(ex1)).id(5)
    rec
  }
  
  test("word record becomes nice json") {
    val rec = WordRecord.createRecord
    rec.writing("hey").reading("guys")
    
    val js = rec.asJSON.toString()
    val rec2 = WordRecord.createRecord
    rec2.setFieldsFromJSON(js)
    rec.writing.is should equal (rec2.writing.is)
    rec.reading.is should equal (rec2.reading.is)
  }
  
  test("word record translates to java model") {
    val jv: JObject = word.asJValue
    val str = Printer.pretty(JsonAST.render(ResponseUtil.deuser(jv)))

    val obj = gson.fromJson(str, classOf[Word])
    obj.getMeaning should equal ("me")
  }
  
  test("word card saves all right") {
    val jv = card.asJValue
    val str = Printer.compact(JsonAST.render(ResponseUtil.deuser(jv)))

    val obj = gson.fromJson(str, classOf[WordCard])
    obj.getCardMode should equal (CardMode.READING)
    obj.getWord should equal (5)
  }
  
  test("container is being parsed") {
    val words = List(word, word)
    val cards = List(card, card)

    val jv = ResponseUtil.jsonResponse(WordsAndCards(words, cards))
    val str = Printer.compact(JsonAST.render(ResponseUtil.deuser(jv)))
    val obj = gson.fromJson(str, classOf[Container])
    obj.getWords.size() should be (2)
    obj.getCards.size() should be (2)
  }
  
  test("word mark event goes from java to scala world") {
    val event = new MarkEvent()
    event.setCard(5); event.setMark(5.0); event.setMode(CardMode.READING);
    event.setTime(1.0); event.setDatetime(DateTimeUtils.now)
    
    val str = gson.toJson(event)
    val jv = net.liftweb.json.parse(str)
    val rec = MarkEventRecord.createRecord
    rec.setFieldsFromJValue(jv)
    
    rec.card.is should equal (5)
    rec.mark.is should equal (5.0)
    rec.mode.is should equal (CardMode.READING)
  }
}