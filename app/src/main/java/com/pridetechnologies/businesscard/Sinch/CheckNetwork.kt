package com.pridetechnologies.businesscard.Sinch

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class CheckNetwork(var mContext: Context) {
    var connMgr: ConnectivityManager? = null
    var wifi: NetworkInfo? = null
    var mobile: NetworkInfo? = null

    /**
     * Checking the network is available or not
     */
}