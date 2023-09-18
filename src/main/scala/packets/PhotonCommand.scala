package packets
import packets.PhotonCommandTypes._
import packets.PhotonMessageTypes._

import java.nio.ByteBuffer
case class PhotonCommand (
  commandType: Byte = 0,
  channelID: Byte = 0,
  flags: Byte = 0,
  reservedByte: Byte = 0,
  length: Int = -1,
  reliableSequenceNumber: Int = 0,
  data: Array[Byte]
) {
  /**  */
  def reliableMessage: Either[String, ReliableMessage] = {
    if(commandType != SendReliableType)
      Left("Command can't be converted to message")
    else  {
      val buf = ByteBuffer.wrap(data)
      val signature = buf.get
      val messageType = buf.get

      if(messageType > 128)
        Left("Your message is encrypted")
      else if (messageType == OtherOperationResponse)
        Right(parseReliableMessage(OtherOperationResponse.toByte, buf))
      else
        Right(parseReliableMessage(messageType, buf))
    }
  }

  /**  */
  def reliableFragment: Either[String, ReliableFragment] = {
    if(commandType != SendReliableFragmentType)
      Left("This command is not a fragment")
    else {
      val buf = ByteBuffer.wrap(data)
      val sequenceNumber = buf.getInt
      val fragmentCount = buf.getInt
      val fragmentNumber = buf.getInt
      val totalLength = buf.getInt
      val fragmentOffset = buf.getInt

      val fragmentData = new Array[Byte](buf.remaining())
      buf.get(fragmentData)

      Right(ReliableFragment(sequenceNumber, fragmentCount, fragmentNumber, totalLength, fragmentOffset, fragmentData))
    }
  }

  def parseReliableMessage(messageType: Byte, buf: ByteBuffer): ReliableMessage = {
    messageType match {
      case OperationRequest =>
        val operationCode = buf.get
        ReliableMessage(signature = 0, messageType, operationCode, 0, 0, null, 0, buf.array())
      case EventDataType =>
        val eventCode = buf.get
        ReliableMessage(signature = 0, messageType, 0, eventCode, 0, null, 0, buf.array())
      case OperationResponse =>
        formReliableMessage(buf, messageType)
      case OtherOperationResponse =>
        formReliableMessage(buf, messageType)
    }
  }

  private def formReliableMessage(buf: ByteBuffer, messageType: Byte): ReliableMessage = {
    val operationCode = buf.get
    val operationResponseCode = buf.getShort
    val debugStringLength = buf.get
    val debugString = new Array[Byte](debugStringLength)
    buf.get(debugString)
    val debugStringUtf8 = new String(debugString, "UTF-8")
    ReliableMessage(0, messageType, operationCode, 0, operationResponseCode, debugStringUtf8, 0, buf.array())
  }

}
