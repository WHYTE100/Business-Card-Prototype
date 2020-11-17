package com.pridetechnologies.businesscard.`interface`

import com.pridetechnologies.businesscard.models.Card

interface IFirebaseLoadDone {

    fun onCardLoadSuccess(cardList: List<Card>)
    fun onCardLoadFailed(message: String)
}