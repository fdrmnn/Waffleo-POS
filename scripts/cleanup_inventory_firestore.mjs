#!/usr/bin/env node
import fs from "node:fs";
import path from "node:path";
import process from "node:process";
import { initializeApp, applicationDefault, cert } from "firebase-admin/app";
import { getFirestore, FieldValue } from "firebase-admin/firestore";

const DEFAULT_DATABASE_ID = "ai-studio-android-waffleop-09a8b0fe-6e50-4d32-bf9c-bf8e8f86fe76";
const SHARED_WORKSPACE_ID = "waffleo_master_workspace";

const CATALOG_STOCK_FIELDS = ["stockQty", "stockUnit", "costPerUnit"];
const MONEY_TX_INVENTORY_FIELDS = [
  "expenseClassification",
  "categoryId",
  "subCategoryId",
  "linkedCanonicalId",
  "linkedInventoryItemId",
  "purchaseQty",
  "purchaseUnit",
  "receivedQty",
  "receivingStatus",
  "linkedMovementIdsJson"
];

// Subcollections belonging ONLY to legacy inventory/recipe (none are Orders/MoneyManager/AuditLogs/Catalog/Staff/Branch/Settings)
const LEGACY_INVENTORY_SUBCOLLECTIONS = new Set([
  "unit_conversions",
  "recipes",
  "inventory_movements",
  "stock_audits",
  "stock_transfers",
  "closing_verifications",
  "inventory",
  "ingredients"
]);

// Core collections that MUST NEVER have their documents wiped by cleanup
const PROTECTED_CORE_COLLECTIONS = [
  "orders",
  "money_transactions",
  "catalog",
  "audit_logs",
  "staff_accounts",
  "branch_profiles",
  "workspace_settings"
];

const BATCH_LIMIT = 400;

function parseArgs(argv) {
  const args = {
    mode: null,
    databaseId: process.env.FIRESTORE_DATABASE_ID || DEFAULT_DATABASE_ID,
    backupFile: null,
    serviceAccount: process.env.GOOGLE_APPLICATION_CREDENTIALS || null
  };

  for (const token of argv) {
    if (token === "--dry-run") args.mode = "dry-run";
    else if (token === "--backup") args.mode = "backup";
    else if (token === "--execute") args.mode = "execute";
    else if (token.startsWith("--database=")) args.databaseId = token.slice("--database=".length);
    else if (token.startsWith("--backup-file=")) args.backupFile = token.slice("--backup-file=".length);
    else if (token.startsWith("--service-account=")) args.serviceAccount = token.slice("--service-account=".length);
    else if (token === "--help" || token === "-h") args.mode = "help";
  }
  return args;
}

function printUsage() {
  console.log(`
Waffleo POS — Firestore Inventory Cleanup Tool
Database target default: ${DEFAULT_DATABASE_ID}

Penggunaan:
  1. Simulasi (tanpa mengubah Cloud Firestore):
     node cleanup_inventory_firestore.mjs --dry-run

  2. Backup penuh sebelum eksekusi (wajib):
     node cleanup_inventory_firestore.mjs --backup

  3. Eksekusi pembersihan di Cloud Firestore:
     node cleanup_inventory_firestore.mjs --execute --backup-file=./backups/<nama-file-backup>.json

Opsi tambahan:
  --database=<databaseId>           Override Firestore database ID
  --service-account=<path.json>     Path ke Service Account JSON (atau set GOOGLE_APPLICATION_CREDENTIALS)
`);
}

function initAdminFirestore(args) {
  let credential;
  if (args.serviceAccount) {
    const resolved = path.resolve(args.serviceAccount);
    if (!fs.existsSync(resolved)) {
      throw new Error(`File service account tidak ditemukan: ${resolved}`);
    }
    const raw = JSON.parse(fs.readFileSync(resolved, "utf8"));
    credential = cert(raw);
  } else {
    credential = applicationDefault();
  }
  const app = initializeApp({ credential });
  return getFirestore(app, args.databaseId);
}

async function discoverUserDocIds(db) {
  const refs = await db.collection("users").listDocuments();
  const ids = new Set(refs.map((r) => r.id));
  ids.add(SHARED_WORKSPACE_ID);
  return Array.from(ids).sort();
}

function serializeDocData(data) {
  return JSON.parse(
    JSON.stringify(data, (_key, value) => {
      if (value && typeof value === "object" && typeof value.toDate === "function") {
        return { __type: "Timestamp", iso: value.toDate().toISOString() };
      }
      return value;
    })
  );
}

async function commitBatchedOps(db, ops) {
  for (let i = 0; i < ops.length; i += BATCH_LIMIT) {
    const chunk = ops.slice(i, i + BATCH_LIMIT);
    const batch = db.batch();
    for (const op of chunk) {
      if (op.type === "delete") {
        batch.delete(op.ref);
      } else if (op.type === "update") {
        batch.update(op.ref, op.payload);
      }
    }
    await batch.commit();
  }
}

async function runBackup(db, userIds, databaseId) {
  const backupDir = path.resolve(path.dirname(new URL(import.meta.url).pathname), "backups");
  fs.mkdirSync(backupDir, { recursive: true });
  const stamp = new Date().toISOString().replace(/[:.]/g, "-");
  const outPath = path.join(backupDir, `waffleo-firestore-backup-${stamp}.json`);

  const snapshot = {
    createdAt: new Date().toISOString(),
    databaseId,
    users: {}
  };

  for (const uid of userIds) {
    const userRef = db.collection("users").doc(uid);
    const subCols = await userRef.listCollections();
    const subNames = new Set([...PROTECTED_CORE_COLLECTIONS, ...subCols.map((c) => c.id)]);
    snapshot.users[uid] = {};

    for (const colName of subNames) {
      const snap = await userRef.collection(colName).get();
      if (!snap.empty) {
        snapshot.users[uid][colName] = snap.docs.map((d) => ({
          id: d.id,
          data: serializeDocData(d.data())
        }));
      }
    }
  }

  fs.writeFileSync(outPath, JSON.stringify(snapshot, null, 2), "utf8");
  console.log(`✅ Backup selesai disimpan ke: ${outPath}`);
  for (const uid of userIds) {
    const cols = snapshot.users[uid] || {};
    const summary = Object.entries(cols)
      .map(([k, docs]) => `${k}=${docs.length}`)
      .join(", ");
    console.log(`   - users/${uid}: ${summary || "(kosong)"}`);
  }
}

async function inspectAndOptionallyClean(db, userIds, execute) {
  const totals = {
    catalogIngredientDocsDeleted: 0,
    catalogDocsFieldsCleaned: 0,
    moneyTxDocsFieldsCleaned: 0,
    legacySubcollectionDocsDeleted: 0,
    preservedOrdersCount: 0,
    preservedMoneyTxCount: 0,
    preservedCatalogMenuOtherCount: 0,
    preservedAuditLogsCount: 0
  };

  for (const uid of userIds) {
    const userRef = db.collection("users").doc(uid);
    const ops = [];

    // 1. Inspect /catalog
    const catalogSnap = await userRef.collection("catalog").get();
    let ingCount = 0;
    let catFieldCleanCount = 0;
    let catKeepCount = 0;

    for (const doc of catalogSnap.docs) {
      const data = doc.data();
      const recordType = String(data.recordType || "").toUpperCase();
      if (recordType === "INGREDIENT") {
        ingCount++;
        ops.push({ type: "delete", ref: doc.ref });
      } else {
        catKeepCount++;
        const fieldDeletes = {};
        for (const field of CATALOG_STOCK_FIELDS) {
          if (Object.prototype.hasOwnProperty.call(data, field)) {
            fieldDeletes[field] = FieldValue.delete();
          }
        }
        if (Object.keys(fieldDeletes).length > 0) {
          catFieldCleanCount++;
          ops.push({ type: "update", ref: doc.ref, payload: fieldDeletes });
        }
      }
    }

    // 2. Inspect /money_transactions (NEVER delete documents — only strip 10 inventory integration fields)
    const moneySnap = await userRef.collection("money_transactions").get();
    let moneyFieldCleanCount = 0;
    for (const doc of moneySnap.docs) {
      const data = doc.data();
      const fieldDeletes = {};
      for (const field of MONEY_TX_INVENTORY_FIELDS) {
        if (Object.prototype.hasOwnProperty.call(data, field)) {
          fieldDeletes[field] = FieldValue.delete();
        }
      }
      if (Object.keys(fieldDeletes).length > 0) {
        moneyFieldCleanCount++;
        ops.push({ type: "update", ref: doc.ref, payload: fieldDeletes });
      }
    }

    // 3. Check if any unexpected/legacy inventory subcollections exist under /users/{uid}
    const subCols = await userRef.listCollections();
    let legacyDocsCount = 0;
    for (const col of subCols) {
      if (LEGACY_INVENTORY_SUBCOLLECTIONS.has(col.id)) {
        const snap = await col.get();
        for (const doc of snap.docs) {
          legacyDocsCount++;
          ops.push({ type: "delete", ref: doc.ref });
        }
      }
    }

    // 4. Count preserved core collections (read-only audit)
    const ordersSnap = await userRef.collection("orders").count().get();
    const auditSnap = await userRef.collection("audit_logs").count().get();
    const ordersCount = ordersSnap.data().count;
    const auditCount = auditSnap.data().count;

    totals.catalogIngredientDocsDeleted += ingCount;
    totals.catalogDocsFieldsCleaned += catFieldCleanCount;
    totals.moneyTxDocsFieldsCleaned += moneyFieldCleanCount;
    totals.legacySubcollectionDocsDeleted += legacyDocsCount;
    totals.preservedOrdersCount += ordersCount;
    totals.preservedMoneyTxCount += moneySnap.size;
    totals.preservedCatalogMenuOtherCount += catKeepCount;
    totals.preservedAuditLogsCount += auditCount;

    console.log(`\nWorkspace: users/${uid}`);
    console.log(`  [HAPUS DOKUMEN] catalog (recordType == INGREDIENT): ${ingCount}`);
    console.log(`  [HAPUS FIELD]   catalog non-INGREDIENT (stockQty/stockUnit/costPerUnit): ${catFieldCleanCount} dari ${catKeepCount} dok`);
    console.log(`  [HAPUS FIELD]   money_transactions (10 field integrasi stok): ${moneyFieldCleanCount} dari ${moneySnap.size} dok`);
    console.log(`  [HAPUS SUBCOL]  subcollection legacy inventori: ${legacyDocsCount} dok`);
    console.log(`  [AMAN TERJAGA]  orders=${ordersCount}, money_transactions=${moneySnap.size}, catalog(non-INGREDIENT)=${catKeepCount}, audit_logs=${auditCount}`);

    if (execute && ops.length > 0) {
      await commitBatchedOps(db, ops);
      console.log(`  ✅ Executed ${ops.length} operasi batch pada users/${uid}.`);
    }
  }

  console.log("\n=== RINGKASAN TOTAL ===");
  console.log(`Mode: ${execute ? "EXECUTE (PERMANEN)" : "DRY-RUN (SIMULASI)"}`);
  console.log(`- Dokumen catalog INGREDIENT dihapus      : ${totals.catalogIngredientDocsDeleted}`);
  console.log(`- Dokumen catalog dibersihkan field stok  : ${totals.catalogDocsFieldsCleaned}`);
  console.log(`- Dokumen money_transactions dibersihkan  : ${totals.moneyTxDocsFieldsCleaned}`);
  console.log(`- Dokumen subcollection legacy dihapus    : ${totals.legacySubcollectionDocsDeleted}`);
  console.log(`- Dokumen orders TERJAGA AMAN             : ${totals.preservedOrdersCount}`);
  console.log(`- Dokumen money_transactions TERJAGA AMAN : ${totals.preservedMoneyTxCount}`);
  console.log(`- Dokumen catalog Menu/Mod/Promo TERJAGA  : ${totals.preservedCatalogMenuOtherCount}`);
  console.log(`- Dokumen audit_logs TERJAGA AMAN         : ${totals.preservedAuditLogsCount}`);
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  if (!args.mode || args.mode === "help") {
    printUsage();
    process.exit(args.mode === "help" ? 0 : 1);
  }

  if (args.mode === "execute") {
    if (!args.backupFile) {
      console.error("❌ ERROR: --execute wajib menyertakan --backup-file=<path> agar data aman sebelum eksekusi.");
      process.exit(1);
    }
    const resolvedBackup = path.resolve(args.backupFile);
    if (!fs.existsSync(resolvedBackup)) {
      console.error(`❌ ERROR: File backup tidak ditemukan: ${resolvedBackup}`);
      process.exit(1);
    }
    const stat = fs.statSync(resolvedBackup);
    if (stat.size < 10) {
      console.error(`❌ ERROR: File backup tampak kosong/tidak valid: ${resolvedBackup}`);
      process.exit(1);
    }
    // Verify valid JSON
    JSON.parse(fs.readFileSync(resolvedBackup, "utf8"));
    console.log(`✅ Backup terverifikasi: ${resolvedBackup} (${stat.size} bytes)`);
  }

  const db = initAdminFirestore(args);
  const userIds = await discoverUserDocIds(db);
  console.log(`Target Database: ${args.databaseId}`);
  console.log(`Workspace ditemukan (${userIds.length}): ${userIds.join(", ")}`);

  if (args.mode === "backup") {
    await runBackup(db, userIds, args.databaseId);
  } else if (args.mode === "dry-run") {
    await inspectAndOptionallyClean(db, userIds, false);
  } else if (args.mode === "execute") {
    await inspectAndOptionallyClean(db, userIds, true);
    console.log("\n⚠️ Langkah berikutnya: Deploy scripts/firestore.rules.post_cleanup sebagai firestore.rules (Phase 2).");
  }
}

main().catch((err) => {
  console.error("❌ Cleanup script gagal:", err);
  process.exit(1);
});
