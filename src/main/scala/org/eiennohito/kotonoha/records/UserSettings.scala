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

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.LongPk
import org.eiennohito.kotonoha.mongodb.NamedDatabase
import net.liftweb.record.field.IntField
import net.liftweb.http.SessionVar

/**
 * @author eiennohito
 * @since 06.07.12
 */

class UserSettings private() extends MongoRecord[UserSettings] with LongPk[UserSettings] {
  def meta = UserSettings

  object badCount extends IntField(this, 20)
}

object UserSettings extends UserSettings with MongoMetaRecord[UserSettings] with NamedDatabase {
  private object cached extends SessionVar[UserSettings](
    UserRecord.currentId map { forUser(_) } openTheBox
  )

  def current = cached.is

  def forUser(id: Long): UserSettings = find(id).openOr(UserSettings.createRecord.id(id).save)
}

