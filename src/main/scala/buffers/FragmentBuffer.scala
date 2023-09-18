package buffers
import packets.PhotonCommandTypes.SendReliableType
import packets.{PhotonCommand, ReliableFragment}

import scala.collection.mutable
import scala.collection.mutable.Map
class FragmentBuffer {
  private val cache: Map[Int, FragmentBufferEntity] = mutable.LinkedHashMap.empty[Int, FragmentBufferEntity]

  def offer(msg: ReliableFragment): Option[PhotonCommand] = {
    val entry = cache.getOrElseUpdate(msg.sequenceNumber, new FragmentBufferEntity(msg.sequenceNumber, msg.fragmentCount))

    entry.add(msg)

    if (entry.finished)
      Some(entry.makePhotonCommand)
    else None
  }

}

case class FragmentBufferEntity(sequenceNumber: Int, fragmentsNeeded: Int) {
  private val fragmentBuffer: Map[Int, Array[Byte]] = mutable.Map.empty[Int, Array[Byte]]

  def add(fragment: ReliableFragment): Unit = {
    fragmentBuffer += (fragment.fragmentNumber -> fragment.data)
  }

  def finished: Boolean = fragmentBuffer.size == fragmentsNeeded

  def makePhotonCommand: PhotonCommand = {
    val data: Array[Byte] = (0 until fragmentsNeeded).flatMap(fragmentNumber => fragmentBuffer(fragmentNumber)).toArray
    PhotonCommand(commandType = SendReliableType.toByte, data = data, reliableSequenceNumber = sequenceNumber)
  }
}

object FragmentBuffer {
  def apply(): FragmentBuffer = new FragmentBuffer()
}
