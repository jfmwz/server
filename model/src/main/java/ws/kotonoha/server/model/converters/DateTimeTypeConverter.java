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
package ws.kotonoha.server.model.converters;

import com.google.gson.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * @author eiennohito
 * @since 06.02.12
 */
public class DateTimeTypeConverter
      implements JsonSerializer<DateTime>, JsonDeserializer<DateTime> {

  private final DateTimeFormatter fmt =ISODateTimeFormat.basicDateTime();

  @Override
  public JsonElement serialize(DateTime src, Type srcType, JsonSerializationContext context) {
    DateTime time = src.withZone(DateTimeZone.UTC);
    return new JsonPrimitive(fmt.print(time));
  }

  @Override
  public DateTime deserialize(JsonElement json, Type type, JsonDeserializationContext context)
      throws JsonParseException {
    try {
      String str = json.getAsString();
      return fmt.parseDateTime(str);
    } catch (IllegalArgumentException e) {
      // May be it came in formatted as a java.util.Date, so try that
      Date date = context.deserialize(json, Date.class);
      return new DateTime(date);
    }
  }
}
