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

package ws.kotonoha.server.util;

import net.liftweb.json.JsonAST;
import net.liftweb.json.Printer$;
import scala.text.Document;

/**
 * @author eiennohito
 * @since 27.06.12
 */
public class ObjectRenderer {
  public static String renderJvalue(JsonAST.JValue value) {
    Document doc = JsonAST.render(value);
    return Printer$.MODULE$.compact(doc);
  }
}
