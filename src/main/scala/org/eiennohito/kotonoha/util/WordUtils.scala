package org.eiennohito.kotonoha.util

import org.eiennohito.kotonoha.mongodb.MongoDbInit
import org.eiennohito.kotonoha.records.dictionary.JMDictRecord
import org.eiennohito.kotonoha.japanese._
import xml.NodeSeq

object WordUtils {
   def processWord(writing:String, reading:Option[String]): Option[NodeSeq] = try {
     val word_type = JMDictRecord.query(writing, reading, 1).head.meaning.is.firstOption.flatMap(_.info.is.firstOption)
     val cobj = ConjObj(word_type.getOrElse("exp"), writing)
     val ns1 = cobj.masuForm.data map {c => <div>{c}</div> }
     val ns2 = cobj.teForm.data map {c => <div>{c}</div>}
     ns1 flatMap{s => ns2 map {t => s ++ t}}
   } catch { case _ => None }
}
