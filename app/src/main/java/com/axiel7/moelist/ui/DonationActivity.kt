package com.axiel7.moelist.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.billingclient.api.*
import com.axiel7.moelist.R
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.InterstitialAdListener
import com.google.android.material.transition.platform.MaterialFadeThrough


class DonationActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient
    private lateinit var coffeeButton: Button
    private lateinit var burgerButton: Button
    private lateinit var adButton: Button
    private lateinit var kofiButton: ImageView
    //private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var mInterstitialAdF: com.facebook.ads.InterstitialAd
    //private lateinit var consentInformation: ConsentInformation
    //private lateinit var consentForm: ConsentForm
    private val skuList = listOf("coffee", "burger")

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        window.enterTransition = MaterialFadeThrough()
        window.allowEnterTransitionOverlap = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donations)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }


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

        /*val params = ConsentRequestParameters.Builder().build()

        consentInformation = UserMessagingPlatform.getConsentInformation(this)

        val adRequest = AdRequest.Builder().build()
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-3318264479359938/7726212160"
        mInterstitialAd.loadAd(adRequest)*/

        AudienceNetworkAds.initialize(this)
        mInterstitialAdF = com.facebook.ads.InterstitialAd(
            this,
            "3357616380981293_3357625817647016"
        )

        adButton.setOnClickListener {
            /*consentInformation.requestConsentInfoUpdate(this, params,
                {
                    // The consent information state was updated.
                    // You are now ready to check if a form is available.
                    if (consentInformation.isConsentFormAvailable) {
                        loadForm()
                    }
                },
                { // Handle the error.
                })*/
            //mInterstitialAd.loadAd(AdRequest.Builder().build())
            /*if (mInterstitialAd.isLoaded) {
                mInterstitialAd.show()
            } else {
                Log.d("MoeLog", "The interstitial wasn't loaded yet.")
            }*/
            mInterstitialAdF.loadAd(
                mInterstitialAdF.buildLoadAdConfig()
                    .withAdListener(listener(mInterstitialAdF))
                    .build()
            )
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
                acknowledgePurchase(purchase.purchaseToken)

            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Toast.makeText(this, getString(R.string.purchase_cancelled), Toast.LENGTH_SHORT).show()
        }
    }
    private fun acknowledgePurchase(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        val purchase = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build()
        billingClient.acknowledgePurchase(params) {
            //val responseCode = billingResult.responseCode
            //val debugMessage = billingResult.debugMessage
            Toast.makeText(this, getString(R.string.donation_thanked), Toast.LENGTH_SHORT).show()
        }
        billingClient.consumeAsync(purchase) { _, _ ->  }
    }
    /*private fun loadForm() {
        UserMessagingPlatform.loadConsentForm(this, { consentForm ->
            this@DonationActivity.consentForm = consentForm
            if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                consentForm.show(this@DonationActivity)
                { // Handle dismissal by reloading form.
                    loadForm()
                }
            }
        }) {
            // Handle the error
        }
    }*/
    private fun listener(interstitialAd: com.facebook.ads.InterstitialAd) : InterstitialAdListener {
        val TAG = "MoeLog"
        return object : InterstitialAdListener {
            override fun onInterstitialDisplayed(ad: Ad) {
                // Interstitial ad displayed callback
                Log.e(TAG, "Interstitial ad displayed.")
            }

            override fun onInterstitialDismissed(ad: Ad) {
                // Interstitial dismissed callback
                Log.e(TAG, "Interstitial ad dismissed.")
            }

            override fun onError(ad: Ad?, adError: AdError) {
                // Ad error callback
                Log.e(TAG, "Interstitial ad failed to load: " + adError.getErrorMessage())
            }

            override fun onAdLoaded(ad: Ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!")
                // Show the ad
                interstitialAd.show()
            }

            override fun onAdClicked(ad: Ad) {
                // Ad clicked callback
                Log.d(TAG, "Interstitial ad clicked!")
            }

            override fun onLoggingImpression(ad: Ad) {
                // Ad impression logged callback
                Log.d(TAG, "Interstitial ad impression logged!")
            }
        }
    }

    override fun onDestroy() {
        mInterstitialAdF.destroy()
        super.onDestroy()
    }
}