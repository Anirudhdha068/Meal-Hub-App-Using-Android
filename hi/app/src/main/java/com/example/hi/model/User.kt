package com.example.hi.model

import com.google.firebase.firestore.PropertyName

/**
 * User Model - represents user in Firestore
 */
data class User(
    @PropertyName("uid")
    var uid: String = "",
    
    @PropertyName("name")
    var name: String = "",
    
    @PropertyName("email")
    var email: String = "",
    
    @PropertyName("phoneNumber")
    var phoneNumber: String = "",
    
    @PropertyName("profileImageUrl")
    var profileImageUrl: String = "",
    
    @PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis()
)







