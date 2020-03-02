package pl.piomin.services.repository

import pl.piomin.services.model.Account

class AccountRepository {

    val accounts: MutableList<Account> = ArrayList()

    fun addAccount(account: Account) {
        accounts.add(account)
    }

}