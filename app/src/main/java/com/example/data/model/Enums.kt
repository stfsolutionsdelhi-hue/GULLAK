package com.example.data.model

enum class UserRole {
    ADMIN,
    MEMBER
}

enum class AccountStatus {
    ACTIVE,
    INACTIVE,
    DELETED
}

enum class PaymentType {
    CASH,
    ONLINE
}

enum class PaymentStatus {
    PENDING,
    APPROVED,
    REJECTED,
    APPROVED_WITH_EDIT
}

enum class NotificationType {
    DUES_REMINDER,
    PAYMENT_APPROVAL,
    PAYMENT_REJECTION,
    ANNOUNCEMENT,
    GENERAL
}
