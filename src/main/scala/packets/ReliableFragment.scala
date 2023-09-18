package packets

case class ReliableFragment(
  sequenceNumber: Int,
  fragmentCount: Int,
  fragmentNumber: Int,
  totalLength: Int,
  fragmentOffset: Int,
  data: Array[Byte]
)
