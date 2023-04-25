package pl.piomin.services.repository

import pl.piomin.services.model.Customer

class CustomerRepository {

    companion object {
        private val customers: MutableList<Customer> = ArrayList()

        fun addCustomer(customer: Customer) {
            customers.add(customer)
        }

        fun getCustomers() = customers
    }

}