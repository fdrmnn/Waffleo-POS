package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.AuditLogRecord
import com.example.data.model.BranchProfile
import com.example.data.model.CatalogRecord
import com.example.data.model.MoneyTransactionRecord
import com.example.data.model.MoneyTransactionType
import com.example.data.model.OrderRecord
import com.example.data.model.StaffAccount
import com.example.data.model.StaffRole
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WaffleoRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveOrder_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(OWNER_EMAIL)
        val repository = WaffleoRepository(firestore)
        val orderId = "v1_ord_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val order = OrderRecord(
            id = orderId,
            userId = uid,
            orderNumber = "WFL-101",
            sequenceIndex = 1L,
            branchId = "Global",
            orderMedia = "GrabFood",
            customerData = "GF - 9012",
            itemsSummary = "1x Egg Waffle 1/2-1/2 (Choco + Matcha)",
            itemsPayload = "[]",
            notes = "Jangan terlalu manis",
            promoCode = "",
            subtotalAmount = 34000L,
            discountAmount = 0L,
            totalAmount = 34000L,
            paidAmount = 34000L,
            cashReceivedAmount = 50000L,
            paymentMethod = "Platform",
            isPaid = true,
            isPrepared = false,
            status = "ACTIVE"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) { repository.createOrder(uid, order) }
        assertTrue(result)

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeOrders(uid).first { list -> list.any { it.id == orderId } }
        }
        assertTrue(emitted.any { it.orderNumber == "WFL-101" && it.customerData == "GF - 9012" && it.cashReceivedAmount == 50000L })
    }

    @Test
    fun multiDeviceSharedWorkspace_branchAndOrderAndMoneySync_succeeds() = runBlocking {
        // Device A signs in as Developer / Owner
        signInTestUser(OWNER_EMAIL)
        val repository = WaffleoRepository(firestore)
        val sharedId = WaffleoRepository.SHARED_WORKSPACE_ID
        val uniqueSuffix = UUID.randomUUID().toString().replace("-", "").take(8)
        val branchName = "Cabang Gabek $uniqueSuffix"
        val orderId = "v1_ord_gabek_$uniqueSuffix"
        val txId = "v1_mm_tx_gabek_$uniqueSuffix"
        val catId = "cat_gabek_$uniqueSuffix"

        // 1. Device A creates "Cabang Gabek" BranchProfile, StaffAccount, CatalogRecord, OrderRecord, and MoneyTransactionRecord
        val profileSaved = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveBranchProfile(
                sharedId,
                BranchProfile(
                    branchId = branchName,
                    location = "Gabek, Pangkalpinang",
                    ownerName = "Owner Gabek",
                    ownerEmail = "gabek@waffleo.id",
                    ownerPhone = "08123456789",
                    ownerUsername = "ownergabek",
                    initializedFrom = "Salinan dari Cabang Global",
                    createdAtText = "05 Nov 2026"
                )
            )
        }
        assertTrue(profileSaved)

        val staffSaved = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveStaffAccount(
                sharedId,
                StaffAccount(
                    branchId = branchName,
                    username = "ownergabek",
                    password = "secret",
                    role = StaffRole.OWNER,
                    displayName = "Owner Gabek"
                )
            )
        }
        assertTrue(staffSaved)

        val catSaved = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveCatalogItem(
                sharedId,
                CatalogRecord(
                    id = catId,
                    userId = sharedId,
                    recordType = "MENU_ITEM",
                    branchId = branchName,
                    name = "Waffle Belgian Choco",
                    category = "Egg Waffle",
                    priceWalkIn = 26000L
                )
            )
        }
        assertTrue(catSaved)

        val orderSaved = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createOrder(
                sharedId,
                OrderRecord(
                    id = orderId,
                    userId = sharedId,
                    orderNumber = "WFL-777",
                    sequenceIndex = 77L,
                    branchId = branchName,
                    orderMedia = "Walk In",
                    customerData = "Budi",
                    itemsSummary = "1x Waffle Belgian Choco",
                    subtotalAmount = 26000L,
                    totalAmount = 26000L,
                    paidAmount = 26000L,
                    cashReceivedAmount = 30000L,
                    changeDueAmount = 4000L,
                    paymentMethod = "Tunai (Cash)",
                    isPaid = true,
                    isPrepared = true,
                    status = "DONE"
                )
            )
        }
        assertTrue(orderSaved)

        val txSaved = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveMoneyTransaction(
                sharedId,
                MoneyTransactionRecord(
                    id = txId,
                    branchId = branchName,
                    type = MoneyTransactionType.INCOME,
                    dateKey = "2026-11-05",
                    monthKey = "2026-11",
                    timeFormatted = "14:21",
                    amount = 26000L,
                    category = "Penjualan",
                    subCategory = "Cash",
                    accountGroup = "Uang Tunai",
                    accountName = "Cash",
                    note = "Penjualan Tunai Cabang Gabek"
                )
            )
        }
        assertTrue(txSaved)

        // 2. Device B signs in with a DIFFERENT Google account (or fresh install) and observes Shared Cloud Workspace
        signInTestUser(OTHER_EMAIL)

        val observedProfiles = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeBranchProfiles(sharedId).first { list -> list.any { it.branchId == branchName } }
        }
        assertTrue(observedProfiles.any { it.branchId == branchName && it.location == "Gabek, Pangkalpinang" })

        val observedStaff = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeStaffAccounts(sharedId).first { list -> list.any { it.branchId == branchName && it.username == "ownergabek" } }
        }
        assertTrue(observedStaff.any { it.branchId == branchName && it.username == "ownergabek" })

        val observedOrders = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeOrders(sharedId).first { list -> list.any { it.id == orderId } }
        }
        assertTrue(observedOrders.any { it.id == orderId && it.branchId == branchName && it.cashReceivedAmount == 30000L })

        // 3. Verify Owner soft deletion: order transitions to status = 'ERASED' and updates in Shared Cloud Workspace
        val erasedOrder = observedOrders.first { it.id == orderId }.copy(
            status = "ERASED",
            cancelReason = "Dihapus oleh Owner",
            cancelledBy = "ownergabek (Owner)",
            cancelledAtText = "05 Nov 2026, 15:00"
        )
        val erasedUpdated = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateOrder(sharedId, erasedOrder)
        }
        assertTrue(erasedUpdated)

        val observedAfterErase = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeOrders(sharedId).first { list -> list.any { it.id == orderId && it.status == "ERASED" } }
        }
        val targetOrder = observedAfterErase.first { it.id == orderId }
        assertEquals("ERASED", targetOrder.status)
        assertEquals("Dihapus oleh Owner", targetOrder.cancelReason)

        val observedMoney = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeMoneyTransactions(sharedId).first { list -> list.any { it.id == txId } }
        }
        assertTrue(observedMoney.any { it.id == txId && it.amount == 26000L })
    }

    @Test
    fun saveCatalogAndAuditLog_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(OWNER_EMAIL)
        val repository = WaffleoRepository(firestore)
        val itemId = "cat_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val logId = "log_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val catalogItem = CatalogRecord(
            id = itemId,
            userId = uid,
            recordType = "MENU_ITEM",
            branchId = "Global",
            name = "Classic Choco Waffle",
            category = "Egg Waffle",
            priceWalkIn = 26000L,
            priceGrabFood = 31000L,
            priceGoFood = 31000L,
            priceShopeeFood = 30000L,
            metadataJson = "{}",
            isActive = true
        )

        val catResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.saveCatalogItem(uid, catalogItem) }
        assertTrue(catResult)

        val auditLog = AuditLogRecord(
            id = logId,
            userId = uid,
            branchId = "Global",
            actorUsername = "owner",
            actorRole = "Owner",
            actionType = "CATALOG_UPDATED",
            entityId = itemId,
            summary = "Menambahkan menu Classic Choco Waffle",
            beforeSnapshot = "{}",
            afterSnapshot = "{\"name\":\"Classic Choco Waffle\"}"
        )

        val logResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.appendAuditLog(uid, auditLog) }
        assertTrue(logResult)
    }

    @Test
    fun observeOrders_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(OWNER_EMAIL)
        val repository = WaffleoRepository(firestore)
        val orderId = "ord_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        val created = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createOrder(
                aliceUid,
                OrderRecord(
                    id = orderId,
                    userId = aliceUid,
                    orderNumber = "WFL-202",
                    sequenceIndex = 2L,
                    branchId = "Global",
                    orderMedia = "Walk In",
                    customerData = "Alice",
                    itemsSummary = "1x Waffle",
                    itemsPayload = "[]",
                    notes = "",
                    promoCode = "",
                    subtotalAmount = 25000L,
                    discountAmount = 0L,
                    totalAmount = 25000L,
                    paidAmount = 25000L,
                    cashReceivedAmount = 25000L,
                    paymentMethod = "Tunai (Cash)",
                    isPaid = true,
                    isPrepared = false,
                    status = "ACTIVE"
                )
            )
        }
        assertTrue(created)

        signInTestUser(OTHER_EMAIL)
        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeOrders(aliceUid).first()
            }
        } catch (e: Exception) {
            caught = e
        }
        assertNotNull(caught)
        val firestoreEx = caught?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun observeOrders_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val uid = signInTestUser(OWNER_EMAIL)
        signOutAndIdle()
        val repository = WaffleoRepository(firestore)

        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeOrders(uid).first()
            }
        } catch (e: Exception) {
            caught = e
        }
        assertNotNull(caught)
        val firestoreEx = caught?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    private companion object {
        const val OWNER_EMAIL = "owner@waffleo.test"
        const val OTHER_EMAIL = "intruder@waffleo.test"
        const val DEFAULT_TIMEOUT_MS = 10000L
        const val FLOW_TIMEOUT_MS = 5000L
    }
}
