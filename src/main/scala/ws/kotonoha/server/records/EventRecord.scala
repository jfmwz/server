package ws.kotonoha.server.records

import ws.kotonoha.server.mongodb.NamedDatabase
import net.liftweb.mongodb.record.{MongoRecord, MongoMetaRecord}
import ws.kotonoha.server.model.EventTypes
import net.liftweb.record.field._
import net.liftweb.mongodb.record.field._

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
 * @since 30.01.12
 */

trait EventRecord[OwnerType <: MongoRecord[OwnerType]] extends ObjectIdPk[OwnerType] { self : OwnerType =>
  protected def myType: Int

  object eventType extends IntField(this.asInstanceOf[OwnerType], myType)
  object datetime extends DateTimeField(this.asInstanceOf[OwnerType]) with DateJsonFormat
  object user extends ObjectIdRefField(this.asInstanceOf[OwnerType], UserRecord)
}

class MarkEventRecord private() extends MongoRecord[MarkEventRecord] with ObjectIdPk[MarkEventRecord] with EventRecord[MarkEventRecord] {
  def meta = MarkEventRecord

  protected def myType = EventTypes.MARK

  object card extends ObjectIdRefField(this, WordCardRecord)
  object mode extends IntField(this)
  object mark extends DoubleField(this)
  object time extends DoubleField(this)

  object diff extends DoubleField(this) //my difficulty when learning
  object interval extends DoubleField(this)
  object lapse extends IntField(this)
  object rep extends IntField(this)
}

object MarkEventRecord extends MarkEventRecord with MongoMetaRecord[MarkEventRecord] with NamedDatabase

class AddWordRecord private() extends MongoRecord[AddWordRecord] with ObjectIdPk[AddWordRecord] with EventRecord[AddWordRecord] {
  def meta = AddWordRecord

  protected def myType = EventTypes.ADD
  object processed extends BooleanField(this, false)
  object writing extends StringField(this, 100)
  object reading extends OptionalStringField(this, 100)
  object meaning extends OptionalStringField(this, 500)
  object group extends LongField(this)
  object tags extends MongoListField[AddWordRecord, String](this)
}

object AddWordRecord extends AddWordRecord with MongoMetaRecord[AddWordRecord] with NamedDatabase

class ChangeWordStatusEventRecord private() extends MongoRecord[ChangeWordStatusEventRecord] with ObjectIdPk[ChangeWordStatusEventRecord] with EventRecord[ChangeWordStatusEventRecord] {
  def meta = ChangeWordStatusEventRecord

  protected def myType = EventTypes.CHANGE_WORD_STATUS
  object word extends ObjectIdRefField(this, WordRecord)
  object toStatus extends EnumField(this, WordStatus)
}

object ChangeWordStatusEventRecord extends ChangeWordStatusEventRecord with MongoMetaRecord[ChangeWordStatusEventRecord] with NamedDatabase
