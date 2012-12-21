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

package ws.kotonoha.server.japanese.parsing

import net.liftweb.util.Props
import ws.kotonoha.akane.juman.PipeExecutor

/**
 * @author eiennohito
 * @since 20.08.12
 */

object Juman {

  lazy val jumanPath = {
    Props.get("juman.path", "juman") //call from $PATH by default
  }

  lazy val jumanArgs: List[String] = {
    Props.get("juman.args")
  }.toList.flatMap(_.split(" ").map(_.trim))

  lazy val jumanEncoding: Option[String] = Props.get("juman.encoding")

  def pipeExecutor = {
    new PipeExecutor(jumanPath, jumanArgs, jumanEncoding)
  }
}
