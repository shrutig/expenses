package models

case class PaymentForm(vendor: String, amount: Int, description: String)

case class PaymentReview(id: Int, userName: String, vendor: String, amount: Int, description: String,admin:String,
                         fileName:String)

