package com.axiel7.moelist.ui

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Toast
import com.android.billingclient.api.*
import com.axiel7.moelist.R
import com.axiel7.moelist.databinding.ActivityDonationsBinding
import com.axiel7.moelist.ui.base.BaseActivity
import com.google.android.material.transition.platform.MaterialFadeThrough

class DonationActivity : BaseActivity<ActivityDonationsBinding>(), PurchasesUpdatedListener {

    override val bindingInflater: (LayoutInflater) -> ActivityDonationsBinding
        get() = ActivityDonationsBinding::inflate
    private lateinit var billingClient: BillingClient
    private val skuList = listOf("coffee", "burger")

    override fun preCreate() {
        super.preCreate()
        window.enterTransition = MaterialFadeThrough()
        window.allowEnterTransitionOverlap = true
    }

    override fun setup() {
        setSupportActionBar(binding.donationsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        binding.donationsToolbar.setNavigationOnClickListener { onBackPressed() }

        binding.githubButton.setOnClickListener {
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
                            binding.coffeeButton.setOnClickListener {
                                val billingFlowParams = BillingFlowParams
                                    .newBuilder()
                                    .setSkuDetails(skuDetails)
                                    .build()
                                billingClient.launchBillingFlow(this, billingFlowParams)
                            }
                        }
                        if (skuDetails.sku == "burger") {
                            binding.burgerButton.setOnClickListener {
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