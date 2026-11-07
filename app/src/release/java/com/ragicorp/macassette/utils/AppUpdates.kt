package com.ragicorp.macassette.utils

import com.google.firebase.Firebase
import com.google.firebase.appdistribution.appDistribution

// Signs the tester in and offers to install a newer release build, if any
fun checkForAppUpdate() {
    Firebase.appDistribution.updateIfNewReleaseAvailable()
}
