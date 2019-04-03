package com.andreacioccarelli.androoster.interfaces

interface BillingCodes {
    companion object {
        val BILLING_RESPONSE_RESULT_OK = 0 // Success
        val BILLING_RESPONSE_RESULT_USER_CANCELED = 1 // User pressed back or canceled a dialog
        val BILLING_RESPONSE_RESULT_SERVICE_UNAVAILABLE = 2 // Network connection is down
        val BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3 // Billing API version is not supported for the type requested
        val BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4 // Requested product is not available for purchase
        val BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5 // Invalid arguments provided to the API. This error can also indicate that the application was not correctly signed or properly set up for In-app Billing in Google Play, or does not have the necessary permissions in its manifest
        val BILLING_RESPONSE_RESULT_ERROR = 6 // Fatal error during the API action
        val BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7 // Failure to purchase since item is already owned
        val BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8 // Failure to consume since item is not owned
    }
}
