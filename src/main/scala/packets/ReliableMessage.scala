package packets

case class ReliableMessage(
  signature: Byte,
  messageType: Byte,
  operationCode: Byte,
  eventCode: Byte,
  operationResponseCode: Short,
  operationDebugString: String,
  parameterCount: Short,
  data: Array[Byte]
)
