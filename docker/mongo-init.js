// mongo-init.js
// Script ini akan dijalankan saat container pertama kali dibuat

// Membuat user untuk aplikasi
db = db.getSiblingDB('psp_integrator');

// Membuat user dengan username: noverryjs dan password: Sorosutan@54
db.createUser({
    user: "noverryjs",
    pwd: "Sorosutan@54",
    roles: [
        {
            role: "readWrite",
            db: "psp_integrator"
        },
        {
            role: "dbAdmin",
            db: "psp_integrator"
        }
    ]
});

// Membuat koleksi-koleksi yang diperlukan
db.createCollection("request_logs");
db.createCollection("users");
db.createCollection("audit_logs");

// Membuat index untuk performa query
db.request_logs.createIndex({ "timestamp": -1 });
db.request_logs.createIndex({ "url": 1 });
db.request_logs.createIndex({ "method": 1 });
db.request_logs.createIndex({ "statusCode": 1 });

// Optional: Buat TTL index untuk auto-delete log setelah 30 hari
db.request_logs.createIndex(
    { "timestamp": 1 },
    { expireAfterSeconds: 2592000 } // 30 days = 30 * 24 * 60 * 60
);

print("MongoDB initialization completed successfully!");
print("User 'noverryjs' created with password 'Sorosutan@54'");