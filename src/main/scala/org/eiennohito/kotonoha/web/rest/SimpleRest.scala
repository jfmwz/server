package org.eiennohito.kotonoha.web.rest

import net.liftweb.http.rest.{RestContinuation, RestHelper}
import akka.dispatch.{ExecutionContext, Future}
import org.eiennohito.kotonoha.actors.Akka


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

class SimpleRest extends RestHelper {
  implicit val executor = Akka.context

  serve("api" / "cards" prefix {
    case "get" :: number :: Nil JsonGet _ => {

      RestContinuation.async({ r =>
        val f = Future {
          2
        }
        f.onSuccess {
          case i => r(<i>{number}: {i}</i>)
        }
      })
    }
  })
}
