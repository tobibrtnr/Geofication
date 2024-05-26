package de.tobibrtnr.geofication.util.misc

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

// Source
// https://github.com/febaisi/LambdaWithWorkers

fun serializeObject(any: Any): ByteArray {
  //Serialize request
  val bos = ByteArrayOutputStream()
  ObjectOutputStream(bos).apply {
    writeObject(any)
    flush()
  }
  return bos.toByteArray()
}

fun getByteInput(byteArray: ByteArray?): Any? {
  val byteArrayInputStream = ByteArrayInputStream(byteArray)
  val objInputStream = ObjectInputStream(byteArrayInputStream)
  return objInputStream.readObject()
}