package pl.piomin.services.model

data class Customer(var id: Int, val name: String) {
    var accounts: MutableList<Account> = ArrayList()
}