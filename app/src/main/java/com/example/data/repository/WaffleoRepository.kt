package com.example.data.repository

import android.util.Log
import com.example.data.model.AuditLogRecord
import com.example.data.model.BranchProfile
import com.example.data.model.CatalogRecord
import com.example.data.model.MoneyTransactionRecord
import com.example.data.model.MoneyTransactionType
import com.example.data.model.OrderRecord
import com.example.data.model.StaffAccount
import com.example.data.model.StaffRole
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class WaffleoRepository(private val db: FirebaseFirestore) {

    private fun Query.safeSnapshots(): Flow<QuerySnapshot> = callbackFlow {
        val registration = addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    this@callbackFlow.close(error)
                } else {
                    Log.w("WaffleoRepo", "SnapshotListener notice: ${error.code} - ${error.message}")
                }
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot)
            }
        }
        awaitClose { registration.remove() }
    }

    private fun Throwable.isPermissionDenied(): Boolean {
        var current: Throwable? = this
        while (current != null) {
            if (current is com.google.firebase.firestore.FirebaseFirestoreException &&
                current.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED
            ) {
                return true
            }
            current = current.cause
        }
        return false
    }

    fun observeOrders(userId: String): Flow<List<OrderRecord>> {
        if (userId.isEmpty()) return flowOf(emptyList())
        return db.collection("users").document(userId).collection("orders")
            .orderBy("sequenceIndex", Query.Direction.ASCENDING)
            .safeSnapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(OrderRecord::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.w("WaffleoRepo", "Notice mapping OrderRecord ${doc.id}: ${e.message}")
                        null
                    }
                }
            }
            .catch { e ->
                if (e.isPermissionDenied()) throw e
                if (e is CancellationException) throw e
                Log.w("WaffleoRepo", "Notice observing orders: ${e.message}")
                emit(emptyList())
            }
    }

    fun observeCatalog(userId: String): Flow<List<CatalogRecord>> {
        if (userId.isEmpty()) return flowOf(emptyList())
        return db.collection("users").document(userId).collection("catalog")
            .safeSnapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(CatalogRecord::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.w("WaffleoRepo", "Notice mapping CatalogRecord ${doc.id}: ${e.message}")
                        null
                    }
                }
            }
            .catch { e ->
                if (e.isPermissionDenied()) throw e
                if (e is CancellationException) throw e
                Log.w("WaffleoRepo", "Notice observing catalog: ${e.message}")
                emit(emptyList())
            }
    }

    fun observeAuditLogs(userId: String): Flow<List<AuditLogRecord>> {
        if (userId.isEmpty()) return flowOf(emptyList())
        return db.collection("users").document(userId).collection("audit_logs")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(300)
            .safeSnapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(AuditLogRecord::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.w("WaffleoRepo", "Notice mapping AuditLogRecord ${doc.id}: ${e.message}")
                        null
                    }
                }
            }
            .catch { e ->
                if (e.isPermissionDenied()) throw e
                if (e is CancellationException) throw e
                Log.w("WaffleoRepo", "Notice observing audit_logs: ${e.message}")
                emit(emptyList())
            }
    }

    fun observeMoneyTransactions(userId: String): Flow<List<MoneyTransactionRecord>> {
        if (userId.isEmpty()) return flowOf(emptyList())
        return db.collection("users").document(userId).collection("money_transactions")
            .safeSnapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val typeStr = doc.getString("type") ?: MoneyTransactionType.EXPENSE.name
                        val txType = try {
                            MoneyTransactionType.valueOf(typeStr)
                        } catch (e: Exception) {
                            MoneyTransactionType.EXPENSE
                        }
                        MoneyTransactionRecord(
                            id = doc.id,
                            branchId = doc.getString("branchId") ?: "Global",
                            type = txType,
                            timestampMillis = doc.getLong("timestampMillis") ?: System.currentTimeMillis(),
                            dateKey = doc.getString("dateKey") ?: "",
                            monthKey = doc.getString("monthKey") ?: "",
                            timeFormatted = doc.getString("timeFormatted") ?: "08:00",
                            amount = doc.getLong("amount") ?: 0L,
                            transferFee = doc.getLong("transferFee") ?: 0L,
                            category = doc.getString("category") ?: "",
                            subCategory = doc.getString("subCategory") ?: "",
                            accountGroup = doc.getString("accountGroup") ?: "Uang Tunai",
                            accountName = doc.getString("accountName") ?: "Cash",
                            toAccountGroup = doc.getString("toAccountGroup") ?: "",
                            toAccountName = doc.getString("toAccountName") ?: "",
                            note = doc.getString("note") ?: "",
                            description = doc.getString("description") ?: "",
                            tag = doc.getString("tag") ?: "",
                            repetition = doc.getString("repetition") ?: "None",
                            isAutoPosSync = doc.getBoolean("isAutoPosSync") ?: false,
                            posSyncKey = doc.getString("posSyncKey") ?: "",
                            createdBy = doc.getString("createdBy") ?: "owner",
                            feeAccountChoice = doc.getString("feeAccountChoice") ?: "FROM"
                        )
                    } catch (e: Exception) {
                        Log.w("WaffleoRepo", "Notice mapping MoneyTransactionRecord ${doc.id}: ${e.message}")
                        null
                    }
                }
            }
            .catch { e ->
                if (e.isPermissionDenied()) throw e
                if (e is CancellationException) throw e
                Log.w("WaffleoRepo", "Notice observing money_transactions: ${e.message}")
                emit(emptyList())
            }
    }

    fun observeStaffAccounts(userId: String): Flow<List<StaffAccount>> {
        if (userId.isEmpty()) return flowOf(emptyList())
        return db.collection("users").document(userId).collection("staff_accounts")
            .safeSnapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val roleStr = doc.getString("role") ?: StaffRole.KASIR.name
                        val role = try {
                            StaffRole.valueOf(roleStr)
                        } catch (e: Exception) {
                            StaffRole.KASIR
                        }
                        StaffAccount(
                            branchId = doc.getString("branchId") ?: "Global",
                            username = doc.getString("username") ?: "",
                            password = doc.getString("password") ?: "",
                            role = role,
                            displayName = doc.getString("displayName") ?: "",
                            email = doc.getString("email") ?: "",
                            phone = doc.getString("phone") ?: ""
                        )
                    } catch (e: Exception) {
                        Log.w("WaffleoRepo", "Notice mapping StaffAccount ${doc.id}: ${e.message}")
                        null
                    }
                }
            }
            .catch { e ->
                if (e.isPermissionDenied()) throw e
                if (e is CancellationException) throw e
                Log.w("WaffleoRepo", "Notice observing staff_accounts: ${e.message}")
                emit(emptyList())
            }
    }

    fun observeBranchProfiles(userId: String): Flow<List<BranchProfile>> {
        if (userId.isEmpty()) return flowOf(emptyList())
        return db.collection("users").document(userId).collection("branch_profiles")
            .safeSnapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        val branchId = doc.getString("branchId") ?: return@mapNotNull null
                        val rawName = doc.getString("name") ?: ""
                        val cleanName = com.example.data.model.formatCleanBranchName(rawName, branchId)
                        val rawInitFrom = doc.getString("initializedFrom") ?: "Master Seed"
                        BranchProfile(
                            branchId = branchId,
                            name = cleanName,
                            location = doc.getString("location") ?: "",
                            ownerName = doc.getString("ownerName") ?: "",
                            ownerEmail = doc.getString("ownerEmail") ?: "",
                            ownerPhone = doc.getString("ownerPhone") ?: "",
                            ownerUsername = doc.getString("ownerUsername") ?: "owner",
                            initializedFrom = com.example.data.model.sanitizeBranchIdsInText(rawInitFrom),
                            createdAtText = doc.getString("createdAtText") ?: "Default System"
                        )
                    } catch (e: Exception) {
                        Log.w("WaffleoRepo", "Notice mapping BranchProfile ${doc.id}: ${e.message}")
                        null
                    }
                }
            }
            .catch { e ->
                if (e.isPermissionDenied()) throw e
                if (e is CancellationException) throw e
                Log.w("WaffleoRepo", "Notice observing branch_profiles: ${e.message}")
                emit(emptyList())
            }
    }

    fun observeWorkspaceSettings(userId: String): Flow<Map<String, String>> {
        if (userId.isEmpty()) return flowOf(emptyMap())
        return db.collection("users").document(userId).collection("workspace_settings")
            .safeSnapshots()
            .map { snapshot ->
                val map = mutableMapOf<String, String>()
                snapshot.documents.forEach { doc ->
                    val key = doc.getString("settingKey")?.takeIf { it.isNotBlank() } ?: doc.id
                    val payload = doc.getString("payloadJson") ?: ""
                    if (payload.isNotBlank()) {
                        map[key] = payload
                    }
                }
                map.toMap()
            }
            .catch { e ->
                if (e.isPermissionDenied()) throw e
                if (e is CancellationException) throw e
                Log.w("WaffleoRepo", "Notice observing workspace_settings: ${e.message}")
                emit(emptyMap())
            }
    }

    /**
     * Upserts an order idempotently using deterministic `order.id`.
     * Note: Orders can NEVER be deleted (blocked by Firestore Security Rules `allow delete: if false;`).
     * To cancel an order, transition `order.status = "CANCELLED"` and call `updateOrder`.
     */
    suspend fun createOrder(userId: String, order: OrderRecord): Boolean {
        if (userId.isEmpty()) return false
        return upsertOrderInternal(userId, order, isNew = true)
    }

    suspend fun updateOrder(userId: String, order: OrderRecord): Boolean {
        if (userId.isEmpty() || order.id.isEmpty()) return false
        return upsertOrderInternal(userId, order, isNew = false)
    }

    suspend fun deleteOrder(userId: String, orderId: String): Boolean {
        if (userId.isEmpty() || orderId.isEmpty()) return false
        return try {
            db.collection("users").document(userId).collection("orders").document(orderId).delete().await()
            true
        } catch (e: Exception) {
            Log.e("WaffleoRepo", "Failed to delete order $orderId in Cloud", e)
            false
        }
    }

    private suspend fun upsertOrderInternal(userId: String, order: OrderRecord, isNew: Boolean): Boolean {
        return try {
            val collection = db.collection("users").document(userId).collection("orders")
            val docRef = if (order.id.isNotBlank()) collection.document(order.id) else collection.document()
            val payload = hashMapOf<String, Any>(
                "userId" to userId,
                "orderNumber" to order.orderNumber,
                "sequenceIndex" to order.sequenceIndex,
                "branchId" to order.branchId,
                "orderMedia" to order.orderMedia,
                "customerData" to order.customerData,
                "itemsSummary" to order.itemsSummary,
                "itemsPayload" to order.itemsPayload,
                "notes" to order.notes,
                "promoCode" to order.promoCode,
                "subtotalAmount" to order.subtotalAmount,
                "discountAmount" to order.discountAmount,
                "totalAmount" to order.totalAmount,
                "paidAmount" to order.paidAmount,
                "cashReceivedAmount" to order.cashReceivedAmount,
                "paymentMethod" to order.paymentMethod,
                "isPaid" to order.isPaid,
                "isPrepared" to order.isPrepared,
                "status" to order.status,
                "cancelReason" to order.cancelReason,
                "cancelledBy" to order.cancelledBy,
                "cancelledAtText" to order.cancelledAtText,
                "shortfallAmount" to order.shortfallAmount,
                "changeDueAmount" to order.changeDueAmount,
                "splitCashAmount" to order.splitCashAmount,
                "splitQrisAmount" to order.splitQrisAmount,
                "createdBy" to order.createdBy,
                "paymentLedgerPayload" to order.paymentLedgerPayload,
                "orderTimelinePayload" to order.orderTimelinePayload,
                "createdAt" to (if (isNew) (order.createdAt ?: FieldValue.serverTimestamp()) else (order.createdAt ?: FieldValue.serverTimestamp())),
                "updatedAt" to (order.updatedAt ?: FieldValue.serverTimestamp())
            )
            val task = docRef.set(payload)
            val ack = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
                task.await()
                true
            }
            ack == true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("WaffleoRepo", "Failed to upsert order ${order.id}", e)
            false
        }
    }

    suspend fun saveCatalogItem(userId: String, item: CatalogRecord): Boolean {
        if (userId.isEmpty()) return false
        return try {
            val collection = db.collection("users").document(userId).collection("catalog")
            val docRef = if (item.id.isNotBlank()) collection.document(item.id) else collection.document()
            val payload = hashMapOf<String, Any>(
                "userId" to userId,
                "recordType" to item.recordType,
                "branchId" to item.branchId,
                "name" to item.name,
                "category" to item.category,
                "priceWalkIn" to item.priceWalkIn,
                "priceGrabFood" to item.priceGrabFood,
                "priceGoFood" to item.priceGoFood,
                "priceShopeeFood" to item.priceShopeeFood,
                "stockQty" to item.stockQty,
                "stockUnit" to item.stockUnit,
                "costPerUnit" to item.costPerUnit,
                "metadataJson" to item.metadataJson.ifBlank { "{}" },
                "imageUrl" to item.imageUrl,
                "isActive" to item.isActive,
                "createdAt" to (item.createdAt ?: FieldValue.serverTimestamp()),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val task = docRef.set(payload)
            val ack = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
                task.await()
                true
            }
            ack == true
        } catch (e: Exception) {
            Log.e("WaffleoRepo", "Failed to save catalog item ${item.id}", e)
            false
        }
    }

    suspend fun adjustCatalogStockAtomic(userId: String, itemId: String, deltaQty: Double): Boolean {
        if (userId.isEmpty() || itemId.isEmpty()) return false
        return try {
            val docRef = db.collection("users").document(userId).collection("catalog").document(itemId)
            val task = docRef.update(
                mapOf(
                    "stockQty" to FieldValue.increment(deltaQty),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val ack = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
                task.await()
                true
            }
            ack == true
        } catch (e: Exception) {
            Log.w("WaffleoRepo", "Notice atomic stock update for $itemId (fallback to full upsert): ${e.message}")
            false
        }
    }

    suspend fun deleteCatalogItem(userId: String, itemId: String) {
        if (userId.isEmpty() || itemId.isEmpty()) return
        try {
            val task = db.collection("users").document(userId).collection("catalog")
                .document(itemId)
                .delete()
            withTimeoutOrNull(WRITE_TIMEOUT_MS) { task.await() }
        } catch (e: Exception) {
            Log.e("WaffleoRepo", "Failed to delete catalog item $itemId", e)
        }
    }

    suspend fun appendAuditLog(userId: String, log: AuditLogRecord): Boolean {
        if (userId.isEmpty()) return false
        return try {
            val collection = db.collection("users").document(userId).collection("audit_logs")
            val docRef = if (log.id.isNotBlank()) collection.document(log.id) else collection.document()
            val cleanBranch = log.branchId.trim().ifBlank { "Global" }.take(80)
            val cleanUser = log.actorUsername.trim().ifBlank { "system" }.take(80)
            val cleanRole = log.actorRole.trim().ifBlank { "Kasir" }.take(60)
            val cleanAction = log.actionType.trim().ifBlank { "SYSTEM_EVENT" }.take(80)
            val cleanEntity = log.entityId.trim().ifBlank { "system" }.take(128)
            val cleanSummary = log.summary.trim().ifBlank { "Aktivitas sistem tercatat." }.take(2000)
            val cleanBefore = log.beforeSnapshot.ifBlank { "{}" }.take(10000)
            val cleanAfter = log.afterSnapshot.ifBlank { "{}" }.take(10000)

            val payload = hashMapOf<String, Any>(
                "userId" to userId,
                "branchId" to cleanBranch,
                "actorUsername" to cleanUser,
                "actorRole" to cleanRole,
                "actionType" to cleanAction,
                "entityId" to cleanEntity,
                "summary" to cleanSummary,
                "beforeSnapshot" to cleanBefore,
                "afterSnapshot" to cleanAfter,
                "createdAt" to (log.createdAt ?: FieldValue.serverTimestamp())
            )
            val task = docRef.set(payload)
            val ack = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
                task.await()
                true
            }
            ack == true
        } catch (e: Exception) {
            Log.e("WaffleoRepo", "Failed to append audit log", e)
            false
        }
    }

    suspend fun saveMoneyTransaction(userId: String, tx: MoneyTransactionRecord): Boolean {
        if (userId.isEmpty() || tx.id.isEmpty()) return false
        return try {
            val docRef = db.collection("users").document(userId)
                .collection("money_transactions").document(tx.id)
            val payload = hashMapOf<String, Any>(
                "userId" to userId,
                "branchId" to tx.branchId,
                "type" to tx.type.name,
                "timestampMillis" to tx.timestampMillis,
                "dateKey" to tx.dateKey,
                "monthKey" to tx.monthKey,
                "timeFormatted" to tx.timeFormatted,
                "amount" to tx.amount,
                "transferFee" to tx.transferFee,
                "category" to tx.category,
                "subCategory" to tx.subCategory,
                "accountGroup" to tx.accountGroup,
                "accountName" to tx.accountName,
                "toAccountGroup" to tx.toAccountGroup,
                "toAccountName" to tx.toAccountName,
                "note" to tx.note,
                "description" to tx.description,
                "tag" to tx.tag,
                "repetition" to tx.repetition,
                "isAutoPosSync" to tx.isAutoPosSync,
                "posSyncKey" to tx.posSyncKey,
                "createdBy" to tx.createdBy,
                "feeAccountChoice" to tx.feeAccountChoice,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val task = docRef.set(payload)
            val ack = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
                task.await()
                true
            }
            ack == true
        } catch (e: Exception) {
            Log.e("WaffleoRepo", "Failed to save money transaction ${tx.id}", e)
            false
        }
    }

    suspend fun deleteMoneyTransaction(userId: String, txId: String) {
        if (userId.isEmpty() || txId.isEmpty()) return
        try {
            val task = db.collection("users").document(userId)
                .collection("money_transactions").document(txId)
                .delete()
            withTimeoutOrNull(WRITE_TIMEOUT_MS) { task.await() }
        } catch (e: Exception) {
            Log.e("WaffleoRepo", "Failed to delete money transaction $txId", e)
        }
    }

    suspend fun saveStaffAccount(userId: String, account: StaffAccount): Boolean {
        if (userId.isEmpty()) return false
        return try {
            val docId = staffAccountDocId(account.branchId, account.username)
            val docRef = db.collection("users").document(userId)
                .collection("staff_accounts").document(docId)
            val payload = hashMapOf<String, Any>(
                "userId" to userId,
                "branchId" to account.branchId,
                "username" to account.username,
                "password" to account.password,
                "role" to account.role.name,
                "displayName" to account.displayName,
                "email" to account.email,
                "phone" to account.phone,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val task = docRef.set(payload)
            val ack = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
                task.await()
                true
            }
            ack == true
        } catch (e: Exception) {
            Log.e("WaffleoRepo", "Failed to save staff account ${account.username}", e)
            false
        }
    }

    suspend fun deleteStaffAccount(userId: String, branchId: String, username: String): Boolean {
        if (userId.isEmpty() || branchId.isEmpty() || username.isEmpty()) return false
        return try {
            val docId = staffAccountDocId(branchId, username)
            val task = db.collection("users").document(userId)
                .collection("staff_accounts").document(docId)
                .delete()
            val ack = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
                task.await()
                true
            }
            ack == true
        } catch (e: Exception) {
            Log.e("WaffleoRepo", "Failed to delete staff account $username in $branchId", e)
            false
        }
    }

    suspend fun saveBranchProfile(userId: String, profile: BranchProfile): Boolean {
        if (userId.isEmpty() || profile.branchId.isBlank()) return false
        return try {
            val docId = branchProfileDocId(profile.branchId)
            val docRef = db.collection("users").document(userId)
                .collection("branch_profiles").document(docId)
            val payload = hashMapOf<String, Any>(
                "userId" to userId,
                "branchId" to profile.branchId,
                "name" to profile.displayName,
                "location" to profile.location,
                "ownerName" to profile.ownerName,
                "ownerEmail" to profile.ownerEmail,
                "ownerPhone" to profile.ownerPhone,
                "ownerUsername" to profile.ownerUsername.ifBlank { "owner" },
                "initializedFrom" to com.example.data.model.sanitizeBranchIdsInText(profile.initializedFrom.ifBlank { "Master Seed" }),
                "createdAtText" to profile.createdAtText.ifBlank { "Default System" },
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val task = docRef.set(payload)
            val ack = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
                task.await()
                true
            }
            ack == true
        } catch (e: Exception) {
            Log.e("WaffleoRepo", "Failed to save branch profile ${profile.branchId}", e)
            false
        }
    }

    suspend fun deleteBranchProfile(userId: String, branchId: String): Boolean {
        if (userId.isEmpty() || branchId.isBlank() || branchId.equals("Global", ignoreCase = true)) return false
        return try {
            val docId = branchProfileDocId(branchId)
            val docRef = db.collection("users").document(userId)
                .collection("branch_profiles").document(docId)
            val task = docRef.delete()
            val ack = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
                task.await()
                true
            }
            ack == true
        } catch (e: Exception) {
            Log.w("WaffleoRepo", "Failed to delete branch profile $branchId", e)
            false
        }
    }

    private suspend fun batchDeleteDocs(docs: List<DocumentSnapshot>) {
        if (docs.isEmpty()) return
        docs.chunked(450).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { doc ->
                batch.delete(doc.reference)
            }
            try {
                withTimeoutOrNull(8000L) {
                    batch.commit().await()
                }
            } catch (e: Exception) {
                Log.w("WaffleoRepo", "Batch commit error: ${e.message}")
            }
        }
    }

    /**
     * Permanently deletes a branch and all of its associated documents (branch_profiles,
     * staff_accounts, catalog items, orders, and money_transactions) from Cloud Firestore
     * so that deleted branches never reappear on any other device.
     */
    suspend fun deleteBranchCascadeInCloud(userId: String, branchId: String): Boolean {
        val cleanBranch = branchId.trim()
        if (userId.isEmpty() || cleanBranch.isBlank() || cleanBranch.equals("Global", ignoreCase = true)) return false
        val cleanLower = cleanBranch.lowercase()
        val isUuidBranch = cleanLower.startsWith("branch_")
        val baseSlug = cleanLower.removePrefix("cabang ").trim()
        val slugPrefix = baseSlug.replace(Regex("[^a-z0-9]"), "_").take(12) + "_"
        val branchVariants = if (isUuidBranch) {
            setOf(cleanLower)
        } else {
            setOf(
                cleanLower,
                baseSlug,
                "cabang $baseSlug"
            )
        }

        fun matchesBranch(candidate: String?): Boolean {
            if (candidate.isNullOrBlank()) return false
            val low = candidate.trim().lowercase()
            return if (isUuidBranch) {
                low == cleanLower
            } else {
                low in branchVariants || low.removePrefix("cabang ").trim() == baseSlug
            }
        }

        return try {
            val userRef = db.collection("users").document(userId)
            // 1. Delete branch_profiles variants directly
            for (v in branchVariants) {
                withTimeoutOrNull(3000L) {
                    userRef.collection("branch_profiles").document(branchProfileDocId(v)).delete().await()
                }
            }
            val subcollections = listOf("branch_profiles", "staff_accounts", "catalog", "orders", "money_transactions")
            for (sub in subcollections) {
                try {
                    val snap = withTimeoutOrNull(6000L) {
                        userRef.collection(sub).get().await()
                    } ?: continue
                    val docsToDelete = snap.documents.filter { doc ->
                        val docBranch = doc.getString("branchId")
                        val isSlugMatch = sub == "catalog" && docBranch.isNullOrBlank() &&
                            slugPrefix.length > 1 && (
                                doc.id.lowercase().startsWith(slugPrefix) ||
                                    doc.id.lowercase().startsWith("cat_$slugPrefix")
                            )
                        matchesBranch(docBranch) || isSlugMatch
                    }
                    batchDeleteDocs(docsToDelete)
                } catch (e: Exception) {
                    Log.w("WaffleoRepo", "Cascade delete partial skip on $sub for $cleanBranch: ${e.message}")
                }
            }
            true
        } catch (e: Exception) {
            Log.e("WaffleoRepo", "Failed cascade delete for branch $cleanBranch", e)
            false
        }
    }

    /**
     * Purges all non-Global branches (such as Gabek, Gabe, etc.) across all subcollections
     * in Cloud Firestore to guarantee a clean slate where only Cabang Global remains.
     */
    suspend fun purgeAllNonGlobalBranchesInCloud(
        userId: String,
        branchesToKeep: Set<String> = setOf("global")
    ): Boolean {
        if (userId.isEmpty()) return false
        return try {
            val userRef = db.collection("users").document(userId)
            val subcollections = listOf("branch_profiles", "staff_accounts", "catalog", "orders", "money_transactions")
            for (sub in subcollections) {
                try {
                    val snap = withTimeoutOrNull(8000L) {
                        userRef.collection(sub).get().await()
                    } ?: continue
                    val docsToDelete = snap.documents.filter { doc ->
                        val branchId = doc.getString("branchId")?.trim()?.lowercase()
                        val id = doc.id.lowercase()
                        if (branchId != null) {
                            val isKeep = branchId in branchesToKeep || branchId.removePrefix("cabang ").trim() in branchesToKeep
                            !isKeep
                        } else {
                            // If no explicit branchId field, check if doc id matches known non-global slugs
                            id.contains("gabek") || id.contains("gabe")
                        }
                    }
                    batchDeleteDocs(docsToDelete)
                } catch (e: Exception) {
                    Log.w("WaffleoRepo", "Error purging non-global from $sub: ${e.message}")
                }
            }
            true
        } catch (e: Exception) {
            Log.e("WaffleoRepo", "Failed purgeAllNonGlobalBranchesInCloud", e)
            false
        }
    }

    suspend fun saveWorkspaceSetting(userId: String, settingKey: String, payloadJson: String): Boolean {
        if (userId.isEmpty() || settingKey.isBlank() || payloadJson.isBlank()) return false
        return try {
            val docId = settingKey.lowercase().replace(Regex("[^a-z0-9_\\-]"), "_").take(100)
            val docRef = db.collection("users").document(userId)
                .collection("workspace_settings").document(docId)
            val payload = hashMapOf<String, Any>(
                "userId" to userId,
                "settingKey" to settingKey.take(100),
                "payloadJson" to payloadJson,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val task = docRef.set(payload)
            val ack = withTimeoutOrNull(WRITE_TIMEOUT_MS) {
                task.await()
                true
            }
            ack == true
        } catch (e: Exception) {
            Log.e("WaffleoRepo", "Failed to save workspace setting $settingKey", e)
            false
        }
    }

    /**
     * Seamlessly copies any legacy per-user documents under `/users/{legacyUserId}/...`
     * into the unified `/users/waffleo_master_workspace/...` so no prior cloud data is ever lost.
     * Note: Non-Global branches are skipped to prevent resurrecting deleted branch data.
     */
    suspend fun migrateLegacyUserToSharedWorkspace(
        legacyUserId: String,
        sharedWorkspaceId: String = SHARED_WORKSPACE_ID
    ) {
        if (legacyUserId.isBlank() || legacyUserId == sharedWorkspaceId) return
        val subcollections = listOf("orders", "catalog", "money_transactions", "staff_accounts", "branch_profiles", "audit_logs")
        for (sub in subcollections) {
            try {
                val snap = withTimeoutOrNull(3500L) {
                    db.collection("users").document(legacyUserId).collection(sub).get().await()
                } ?: continue
                for (doc in snap.documents) {
                    val branchId = doc.getString("branchId") ?: ""
                    if (!branchId.equals("Global", ignoreCase = true) && branchId.isNotBlank()) {
                        continue // Skip non-Global legacy items to avoid restoring old deleted branches
                    }
                    val data = doc.data?.toMutableMap() ?: continue
                    data["userId"] = sharedWorkspaceId
                    withTimeoutOrNull(2000L) {
                        db.collection("users").document(sharedWorkspaceId)
                            .collection(sub).document(doc.id).set(data).await()
                    }
                }
            } catch (e: Exception) {
                Log.w("WaffleoRepo", "Legacy migration skipped for $sub: ${e.message}")
            }
        }
    }

    companion object {
        const val SHARED_WORKSPACE_ID = "waffleo_master_workspace"
        const val WRITE_TIMEOUT_MS = 5000L

        fun staffAccountDocId(branchId: String, username: String): String {
            return "${branchId.trim().lowercase()}_${username.trim().lowercase()}"
                .replace(Regex("[^a-z0-9_\\-]"), "_")
                .take(120)
        }

        fun branchProfileDocId(branchId: String): String {
            return "branch_${branchId.trim().lowercase()}"
                .replace(Regex("[^a-z0-9_\\-]"), "_")
                .take(120)
        }
    }
}
