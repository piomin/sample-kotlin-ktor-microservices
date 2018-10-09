package pl.piomin.services.model

data class Account(val id: Int, val balance: Int, val number: String) {
    var customerId: Int = 0
}