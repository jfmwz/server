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

package org.eiennohito.kotonoha.dict

import util.parsing.combinator.RegexParsers

/**
 * @author eiennohito
 * @since 05.04.12
 */

case class Identifier(vol: Int, page: Int, num: Int)
case class Header(readings: List[String], writings: List[String], rusReadings: List[String], id: Identifier)
case class Card(header: Header, body: String)

object WarodaiParser extends RegexParsers {


  override def skipWhitespace = false

  def uint = regex("""[0-9]+""".r) ^^ (_.toInt)

  def endl = "\n\r" | "\r\n" | "\n"

  def ws = literal(" ").*

  def identifier = (("〔" ~> uint <~ ";") ~ uint <~ ";") ~ uint <~ "〕" ^^
    { case vol ~ page ~ entry =>  Identifier(vol, page, entry) }

  def rusReading = "(" ~> rep1sep( regex("[^\\,)]+".r), ", ") <~ ")"

  def reading = rep1sep(regex("[^,\\(【]+".r), ", ")

  def writing = "【" ~> rep1sep("[^,】]+".r, ", ") <~ "】"

  def header = ((reading ~ opt(writing) <~ ws) ~ rusReading <~ ws) ~ identifier ^^ {
    case r ~ w ~ rr ~ i => Header(r, w.getOrElse(Nil), rr, i)
  }

  def card = (header <~ "\n") ~ rep1sep("[^\\n]+".r, endl) ^^ {
    case hdr ~ body => Card(hdr, body.map(_.trim).mkString("\n"))
  }

  def cards = rep1sep(card, repN(2, endl))
}
