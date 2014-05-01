package models

/**
 * Created by FScoward on 2014/04/26.
 */
trait Authority {
  def toString: String
}
case class Normal() extends Authority {
  override def toString = "Normal"
}
case class Administrator() extends Authority {
  override def toString = "Administrator"
}