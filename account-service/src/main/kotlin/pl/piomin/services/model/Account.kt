package pl.piomin.services.model

data class Account(var id: Int = 0,
                   val balance: Int = 0,
                   val number: String = "",
                   val customerId: Int = 0)