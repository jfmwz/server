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

import ws.kotonoha.server.actors.ioc.{Akka, ReleaseAkka}
import ws.kotonoha.server.records.ClientRecord
import ws.kotonoha.server.actors.auth.AddClient
import scala.concurrent.Await
import concurrent.duration._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import ws.kotonoha.server.test.TestWithAkka

class ClientTest extends TestWithAkka with FunSuite with ShouldMatchers {
  test("clients creates and deletes") {
    val client = ClientRecord.name("test")
    Await.result(kta ? AddClient(client), 2 seconds)
    val fromdb = ClientRecord.find(client.id.is)
    fromdb.isEmpty should be (false)
    client.delete_!
  }
}
