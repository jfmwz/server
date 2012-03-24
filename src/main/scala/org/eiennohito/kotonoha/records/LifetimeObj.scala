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

package org.eiennohito.kotonoha.records

import net.liftweb.mongodb.record.field.LongPk
import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import org.eiennohito.kotonoha.mongodb.NamedDatabase
import org.eiennohito.kotonoha.actors.LifetimeObjects
import net.liftweb.record.field.{DateTimeField, EnumField, LongField}

/**
 * @author eiennohito
 * @since 24.03.12
 */

class LifetimeObj private() extends MongoRecord[LifetimeObj] with LongPk[LifetimeObj] {
  def meta = LifetimeObj

  object obj extends LongField(this)
  object objtype extends EnumField(this, LifetimeObjects)
  object deadline extends DateTimeField(this)
}

object LifetimeObj extends LifetimeObj with MongoMetaRecord[LifetimeObj] with NamedDatabase
