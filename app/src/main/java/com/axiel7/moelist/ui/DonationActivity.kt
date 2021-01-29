package com.axiel7.moelist.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.android.billingclient.api.*
import com.axiel7.moelist.R
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.transition.platform.MaterialFadeThrough
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform


class DonationActivity : BaseActivity(), PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient
    private lateinit var coffeeButton: Button
    private lateinit var burgerButton: Button
    private lateinit var adButton: Button
    private lateinit var kofiButton: ImageView
    private lateinit var consentInformation: ConsentInformation
    private lateinit var consentForm: ConsentForm
    private var mInterstitialAd: InterstitialAd? = null
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
        adButton = findViewById(R.id.ad_button)
        kofiButton = findViewById(R.id.kofi_button)
        kofiButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://ko-fi.com/axiel7")
            startActivity(intent)
        }

        val params = ConsentRequestParameters.Builder().build()
        consentInformation = UserMessagingPlatform.getConsentInformation(this)

        adButton.setOnClickListener {
            consentInformation.requestConsentInfoUpdate(this, params,
                {
                    // The consent information state was updated.
                    // You are now ready to check if a form is available.
                    if (consentInformation.isConsentFormAvailable) {
                        loadForm()
                    }
                },
                { // Handle the error.
                })
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

    private fun loadForm() {
        UserMessagingPlatform.loadConsentForm(this, { consentForm ->
            this@DonationActivity.consentForm = consentForm
            if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                consentForm.show(this@DonationActivity)
                { // Handle dismissal by reloading form.
                    loadForm()
                }
            }
            else {
                Toast.makeText(this, "Loading ad...", Toast.LENGTH_SHORT).show()
                loadInterstitialAd()
            }
        }) {
            // Handle the error
        }
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, "ca-app-pub-3318264479359938/7726212160",
            adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("MoeLog", adError.message)
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("MoeLog", "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d("MoeLog", "Ad was dismissed.")
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                            Log.d("MoeLog", "Ad failed to show.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d("MoeLog", "Ad showed fullscreen content.")
                            mInterstitialAd = null;
                        }
                    }
                    if (mInterstitialAd != null) {
                        mInterstitialAd?.show(this@DonationActivity)
                    } else {
                        Log.d("MoeLog", "The interstitial ad wasn't loaded yet.")
                    }
                }
            })
    }
}