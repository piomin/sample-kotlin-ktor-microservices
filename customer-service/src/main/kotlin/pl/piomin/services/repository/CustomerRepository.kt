package pl.piomin.services.repository

import pl.piomin.services.model.Customer

class CustomerRepository {

    val customers: MutableList<Customer>  = ArrayList()

    fun addCustomer(customer: Customer) {
        customers.add(customer)
    }

}