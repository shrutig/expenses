package models

case class PaymentForm(vendor: String, amount: Int, description: String)

case class PaymentReview(id: Int, user: String, vendor: String, amount: Int, description: String,admin:String)