const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-project";
const DATABASE_ID = "ai-studio-android-waffleop-09a8b0fe-6e50-4d32-bf9c-bf8e8f86fe76";
const OWNER_UID = "waffleo_owner_1";
const OTHER_UID = "other_user_2";
const SHARED_WORKSPACE_ID = "waffleo_master_workspace";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
  // Also push rules to the named database release used by Android Robolectric tests
  await fetch(`http://${emulatorHost}:${emulatorPort}/emulator/v1/projects/${PROJECT_ID}/ruleSets`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      release_name: `projects/${PROJECT_ID}/releases/cloud.firestore/${DATABASE_ID}`,
      rules: {
        files: [{ name: "security.rules", content: rules }],
      },
    }),
  });
});

after(async () => {
  // Keep latest firestore.rules loaded on the emulator
  // so subsequent Android unit/Robolectric tests run against the updated rules.
  setTimeout(() => process.exit(0), 50);
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read private user paths, but 1-server shared workspace allows direct POS device sync", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore(DATABASE_ID);
  // Private /users/{uid} paths remain strictly forbidden to unauthenticated callers
  await assertFails(unauthDb.collection("users").doc(OWNER_UID).collection("orders").get());
  await assertFails(unauthDb.collection("users").doc(OWNER_UID).collection("catalog").get());
  await assertFails(unauthDb.collection("users").doc(OWNER_UID).collection("audit_logs").get());

  // 1-server central workspace allows direct read from POS devices without Google Login
  await assertSucceeds(unauthDb.collection("users").doc(SHARED_WORKSPACE_ID).collection("orders").get());
  await assertSucceeds(unauthDb.collection("users").doc(SHARED_WORKSPACE_ID).collection("branch_profiles").get());
});

test("Authenticated owner can create and update a valid order (with cashReceivedAmount), while cross-user access to private path fails", async () => {
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore(DATABASE_ID);
  const otherDb = testEnv.authenticatedContext(OTHER_UID).firestore(DATABASE_ID);
  const now = new Date();

  const orderDoc = ownerDb.collection("users").doc(OWNER_UID).collection("orders").doc("order_001");
  await assertSucceeds(
    orderDoc.set({
      userId: OWNER_UID,
      orderNumber: "WFL-001",
      sequenceIndex: 1,
      branchId: "Global",
      orderMedia: "GrabFood",
      customerData: "GF - 8821",
      itemsSummary: "1x Egg Waffle (Choco / Matcha 1/2-1/2)",
      itemsPayload: "[]",
      notes: "Extra crispy",
      promoCode: "",
      subtotalAmount: 32000,
      discountAmount: 0,
      totalAmount: 32000,
      paidAmount: 32000,
      cashReceivedAmount: 32000,
      paymentMethod: "Platform",
      isPaid: true,
      isPrepared: false,
      status: "ACTIVE",
      cancelReason: "",
      cancelledBy: "",
      cancelledAtText: "",
      shortfallAmount: 0,
      changeDueAmount: 0,
      createdBy: "owner",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Update order status (e.g. Prepare -> Prepared, or Cancel Order)
  await assertSucceeds(
    orderDoc.update({
      isPrepared: true,
      status: "CANCELLED",
      cancelReason: "Pelanggan membatalkan pesanan",
      cancelledBy: "owner (Owner)",
      cancelledAtText: "02 Nov, 10:00",
      updatedAt: new Date(),
    })
  );

  // Update order status to ERASED (soft delete by Owner)
  await assertSucceeds(
    orderDoc.update({
      status: "ERASED",
      cancelReason: "Dihapus oleh Owner",
      cancelledBy: "owner (Owner)",
      cancelledAtText: "06 Nov, 06:14",
      updatedAt: new Date(),
    })
  );

  // Cross-user cannot read, update, or delete private user path
  await assertFails(
    otherDb.collection("users").doc(OWNER_UID).collection("orders").doc("order_001").get()
  );
  await assertFails(
    otherDb.collection("users").doc(OWNER_UID).collection("orders").doc("order_001").delete()
  );

  // Authorized Owner can permanently purge an ERASED order
  await assertSucceeds(orderDoc.delete());
});

test("Multi-device Shared Cloud Workspace allows Device A and Device B to sync branches, orders, and settings", async () => {
  const deviceADb = testEnv.authenticatedContext(OWNER_UID).firestore(DATABASE_ID);
  const deviceBDb = testEnv.authenticatedContext(OTHER_UID).firestore(DATABASE_ID);
  const now = new Date();

  // Device A creates "Cabang Gabek" in shared workspace
  const branchDocA = deviceADb.collection("users").doc(SHARED_WORKSPACE_ID).collection("branch_profiles").doc("cabang_gabek");
  await assertSucceeds(
    branchDocA.set({
      userId: SHARED_WORKSPACE_ID,
      branchId: "Cabang Gabek",
      location: "Pangkalpinang - Gabek",
      ownerName: "Owner Gabek",
      ownerEmail: "gabek@waffleo.id",
      ownerPhone: "0812345678",
      ownerUsername: "owner_gabek",
      initializedFrom: "Salin Data dari Global",
      createdAtText: "05 Nov 2026, 20:00",
      updatedAt: now,
    })
  );

  // Device B reads "Cabang Gabek" from shared workspace (both single doc get and collection list)
  await assertSucceeds(
    deviceBDb.collection("users").doc(SHARED_WORKSPACE_ID).collection("branch_profiles").doc("cabang_gabek").get()
  );
  await assertSucceeds(
    deviceBDb.collection("users").doc(SHARED_WORKSPACE_ID).collection("branch_profiles").get()
  );

  // Device B creates an order in "Cabang Gabek" inside shared workspace
  const orderDocB = deviceBDb.collection("users").doc(SHARED_WORKSPACE_ID).collection("orders").doc("v1_ord_gabek_001");
  await assertSucceeds(
    orderDocB.set({
      userId: SHARED_WORKSPACE_ID,
      orderNumber: "WFL-001",
      sequenceIndex: 1,
      branchId: "Cabang Gabek",
      orderMedia: "Walk In",
      customerData: "Pelanggan Gabek",
      itemsSummary: "1x Egg Waffle Belgian Choco",
      itemsPayload: "[]",
      notes: "",
      promoCode: "",
      subtotalAmount: 25000,
      discountAmount: 0,
      totalAmount: 25000,
      paidAmount: 25000,
      cashReceivedAmount: 30000,
      paymentMethod: "Tunai (Cash)",
      isPaid: true,
      isPrepared: true,
      status: "DONE",
      cancelReason: "",
      cancelledBy: "",
      cancelledAtText: "",
      shortfallAmount: 0,
      changeDueAmount: 5000,
      createdBy: "owner_gabek",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Device A reads the order created on Device B (both single doc get and collection list)
  await assertSucceeds(
    deviceADb.collection("users").doc(SHARED_WORKSPACE_ID).collection("orders").doc("v1_ord_gabek_001").get()
  );
  await assertSucceeds(
    deviceADb.collection("users").doc(SHARED_WORKSPACE_ID).collection("orders").get()
  );
});

test("Shadow update with ghost field on order is rejected", async () => {
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore(DATABASE_ID);
  const now = new Date();
  const orderDoc = ownerDb.collection("users").doc(OWNER_UID).collection("orders").doc("order_002");

  await assertFails(
    orderDoc.set({
      userId: OWNER_UID,
      orderNumber: "WFL-002",
      sequenceIndex: 2,
      branchId: "Global",
      orderMedia: "Walk In",
      customerData: "Budi",
      itemsSummary: "1x Kopi Susu",
      itemsPayload: "[]",
      notes: "",
      promoCode: "",
      subtotalAmount: 18000,
      discountAmount: 0,
      totalAmount: 18000,
      paidAmount: 0,
      paymentMethod: "Belum Bayar",
      isPaid: false,
      isPrepared: false,
      status: "ACTIVE",
      cancelReason: "",
      cancelledBy: "",
      cancelledAtText: "",
      shortfallAmount: 0,
      changeDueAmount: 0,
      createdBy: "owner",
      createdAt: now,
      updatedAt: now,
      isHackedAdmin: true,
    })
  );
});

test("Audit logs are strictly append-only (create succeeds, update and delete fail)", async () => {
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore(DATABASE_ID);
  const now = new Date();
  const logDoc = ownerDb.collection("users").doc(OWNER_UID).collection("audit_logs").doc("log_001");

  await assertSucceeds(
    logDoc.set({
      userId: OWNER_UID,
      branchId: "Global",
      actorUsername: "owner",
      actorRole: "Owner",
      actionType: "ORDER_CREATED",
      entityId: "order_001",
      summary: "Pesanan WFL-001 dibuat",
      beforeSnapshot: "{}",
      afterSnapshot: "{\"orderNumber\":\"WFL-001\"}",
      createdAt: now,
    })
  );

  await assertFails(
    logDoc.update({
      summary: "Manipulated log",
    })
  );

  await assertFails(logDoc.delete());
});
