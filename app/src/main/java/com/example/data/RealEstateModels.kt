package com.example.data

data class PropertyItem(
    val id: String,
    val name: String,
    val location: String,
    val type: String,
    val basePrice: Long,
    val baseRentalIncome: Long,
    val imageUrl: String = ""
)

data class OwnedProperty(
    val propertyId: String,
    val purchasedPrice: Long,
    val currentEstimatedValue: Long
)
