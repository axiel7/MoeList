package com.axiel7.moelist.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.android.billingclient.api.*
import com.axiel7.moelist.R
import com.google.android.material.transition.platform.MaterialFadeThrough


class DonationActivity : BaseActivity(), PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient
    private lateinit var coffeeButton: Button
    private lateinit var burgerButton: Button
    private lateinit var githubButton: Button
    private val skuList = listOf("coffee", "burger")

    override fun onCreate(savedInstanceState: Bundle?) {
        window.enterTransition = MaterialFadeThrough()
        window.allowEnterTransitionOverlap = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donations)

        val toolbar = findViewById<Toolbar>(R.id.donations_toolbar)
        setSupportActionBar(toolbar)
        val supportActionBar = supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        coffeeButton = findViewById(R.id.coffee_button)
        burgerButton = findViewById(R.id.burger_button)
        githubButton = findViewById(R.id.github_button)
        githubButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://github.com/axiel7/MoeList")
            startActivity(intent)
        }

        setupBillingClient()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is setup successfully
                    loadAllSKUs()
                    billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.

            }
        })
    }
    private fun loadAllSKUs() = if (billingClient.isReady) {
        val params = SkuDetailsParams
            .newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)
            .build()
        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            // Process the result.
            if (skuDetailsList != null) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList.isNotEmpty()) {
                    for (skuDetails in skuDetailsList) {
                        //this will return both the SKUs from Google Play Console
                        if (skuDetails.sku == "coffee") {
                            coffeeButton.setOnClickListener {
                                val billingFlowParams = BillingFlowParams
                                    .newBuilder()
                                    .setSkuDetails(skuDetails)
                                    .build()
                                billingClient.launchBillingFlow(this, billingFlowParams)
                            }
                        }
                        if (skuDetails.sku == "burger") {
                            burgerButton.setOnClickListener {
                                val billingFlowParams = BillingFlowParams
                                    .newBuilder()
                                    .setSkuDetails(skuDetails)
                                    .build()
                                billingClient.launchBillingFlow(this, billingFlowParams)
                            }
                        }
                    }
                }
            }
        }

    } else {
        println("Billing Client not ready")
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Toast.makeText(this, getString(R.string.purchase_cancelled), Toast.LENGTH_SHORT).show()
        }
    }
    private fun handlePurchase(purchase: Purchase) {

        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

        billingClient.consumeAsync(consumeParams) { billingResult, _ ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Handle the success of the consume operation.
                Toast.makeText(this, getString(R.string.donation_thanked), Toast.LENGTH_SHORT).show()
            }
        }
    }
}