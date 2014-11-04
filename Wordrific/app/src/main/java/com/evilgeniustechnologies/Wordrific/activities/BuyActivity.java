package com.evilgeniustechnologies.Wordrific.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import com.evilgeniustechnologies.Wordrific.adapters.ListAnimationAdapter;
import com.evilgeniustechnologies.Wordrific.adapters.SellAdapter;
import com.evilgeniustechnologies.Wordrific.helpers.*;
import com.evilgeniustechnologies.Wordrific.R;
import com.evilgeniustechnologies.Wordrific.utilties.DawnUtilities;
import com.evilgeniustechnologies.Wordrific.utilties.DialogManager;
import com.evilgeniustechnologies.Wordrific.utilties.L;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuyActivity extends ListServiceActivity {
    private static final int BUY_REQUEST_CODE = 10001;
    private IabHelper mHelper;
    private List<SkuDetails> skuList;
    private List<String> skuIds;
    private String developerPayload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onExecute() {
        setContentView(R.layout.buy);

        skuIds = Arrays.asList(getResources().getStringArray(R.array.sell_sku_ids));

        // Compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(this, database.getConfigurationDao().loadAll().get(0).getBase64EncodedPublicKey());
        mHelper.startSetup(setupListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mHelper == null) {
            return;
        }
        // Handle result return by calling consume
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String generatePayload() {
        return new BigInteger(130, new SecureRandom()).toString(32);
    }

    private IabHelper.OnIabSetupFinishedListener setupListener = new IabHelper.OnIabSetupFinishedListener() {
        @Override
        public void onIabSetupFinished(IabResult result) {
            if (result.isFailure()) {
                L.e("Setup error", result.getMessage());
            } else {
                mHelper.queryInventoryAsync(true, skuIds, queryListener);
            }
        }
    };

    private IabHelper.QueryInventoryFinishedListener queryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inv) {
            if (result.isFailure()) {
                L.e("Query error", result.getMessage());
            } else {
                L.e("result", result.getMessage());
                skuList = new ArrayList<SkuDetails>();
                for (String skuId : skuIds) {
                    // Check and consume any owned items
                    if (inv.hasPurchase(skuId)) {
                        mHelper.consumeAsync(inv.getPurchase(skuId), consumeListener);
                    }
                    // Retrieve item's details
                    SkuDetails details = inv.getSkuDetails(skuId);
                    if (details != null) {
                        skuList.add(details);
                    }
                }

                // If the the setup fails to retrieve the inventory, do nothing
                if (skuList == null || skuList.isEmpty()) {
                    return;
                }

                List<String> titles = new ArrayList<String>();
                List<String> prices = new ArrayList<String>();

                for (SkuDetails skuDetails : skuList) {
                    titles.add(skuDetails.getTitle());
                    prices.add(skuDetails.getPrice());
                }

                // Otherwise, construct the adapter
                rootList = (ListView) findViewById(R.id.buy_list_view);
                rootAdapter = new SellAdapter(BuyActivity.this, database, titles, prices) {
                    @Override
                    public void onBuy(int position) {
                        // If no Internet connection, return
                        if (!DawnUtilities.isConnected(BuyActivity.this)) {
                            DialogManager.show(BuyActivity.this, DialogManager.Alert.NO_INTERNET);
                            return;
                        }
                        // If no one is currently logged in
                        if (database.getCurrentUser() == null) {
                            Intent intent = new Intent(BuyActivity.this, LoginActivity.class);
                            startActivity(intent);
                            return;
                        }
                        // If user reaches the max number of set
                        if (database.getCurrentUser().getNextSetToBuy() - 1 > database.getHighestSet()) {
                            DialogManager.show(BuyActivity.this, "You have purchased all available set. Please come back later");
                            return;
                        }
                        // It's time
                        SkuDetails sku = skuList.get(position);
                        developerPayload = generatePayload();
                        mHelper.launchPurchaseFlow(BuyActivity.this, sku.getSku(), BUY_REQUEST_CODE, purchaseListener, developerPayload);
                    }
                };

                ListAnimationAdapter animationAdapter = new ListAnimationAdapter(rootAdapter);
                animationAdapter.setAbsListView(rootList);
                rootList.setAdapter(animationAdapter);
            }
        }
    };

    private IabHelper.OnIabPurchaseFinishedListener purchaseListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase info) {
            if (result.isFailure()) {
                L.e("Purchase error", result.getMessage());
            } else {
                // Check developer payload
                if (!info.getDeveloperPayload().equals(developerPayload)) {
                    L.e("Red alert", "Intruder detected!!!");
                    return;
                }
                // Consume the item
                mHelper.consumeAsync(info, consumeListener);
            }
        }
    };

    private IabHelper.OnConsumeFinishedListener consumeListener = new IabHelper.OnConsumeFinishedListener() {
        @Override
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isFailure()) {
                L.e("Consume error", result.getMessage());
            } else {
                // If no one is currently logged in
                if (database.getCurrentUser() == null) {
                    return;
                }
                // Get the current user
                final ParseUser currentUser = ParseUser.getCurrentUser();
                int nextSetToBuy = database.getCurrentUser().getNextSetToBuy();
                // Unlock the sets
                if (purchase.getSku().equals(skuIds.get(0))) {
                    // Buy 5 sets
                    nextSetToBuy += 5;
                } else if (purchase.getSku().equals(skuIds.get(1))) {
                    // Buy 10 sets
                    nextSetToBuy += 10;
                } else if (purchase.getSku().equals(skuIds.get(2))) {
                    // Buy 15 sets
                    nextSetToBuy += 15;
                } else if (purchase.getSku().equals(skuIds.get(3))) {
                    // Buy 20 sets
                    nextSetToBuy += 20;
                } else if (purchase.getSku().equals(skuIds.get(4))) {
                    // Buy 30 sets
                    nextSetToBuy += 30;
                } else if (purchase.getSku().equals(skuIds.get(5))) {
                    // Buy 50 sets
                    nextSetToBuy += 50;
                }
                // Save the new nextSetToBuy
                currentUser.put("nextSetToBuy", nextSetToBuy);
                currentUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            try {
                                // Re-fetch current user
                                database.fetchUser(currentUser);
                                // Refresh data from database
                                database.refresh();
                            } catch (ParseException e1) {
                                L.e("Fetch user", e1.getMessage(), e1);
                            }
                        } else {
                            L.e("Save nextSetToBuy", e.getMessage(), e);
                        }
                    }
                });
            }
        }
    };
}
