# OffGrid Connect

Offline peer-to-peer messaging and video calling app over Bluetooth and Wi-Fi Direct with end-to-end encryption, store-and-forward queue, and SOS broadcast.

## Features

- **P2P Radar & Discovery**: Live radar scanner, connection state chips, RSSI signal telemetry, and distance estimation.
- **End-to-End Encryption (E2EE)**: Hardware-backed ECDH P-256 key exchange with AES-256-GCM authenticated encryption.
- **Out-of-Band Key Verification**: Deterministic 6-digit SAS (Short Authentication String) code and 16-character hex fingerprints to protect against Man-in-the-Middle (MITM) attacks.
- **Offline Store-and-Forward Queue**: Messages queued locally when peers are out of RF range, auto-flushed upon radio reconnection.
- **P2P Voice & Video Calling**: WebRTC-powered tactical calls with full HUD telemetry overlays, bandwidth-saving voice-only fallback, and picture-in-picture local camera.
- **Emergency Distress Beacon (SOS)**: Multi-hop mesh flood with triage classification (Medical, Rescue, Hazard, Supply), urgency levels, GPS grid coordinates, and automated Search & Rescue simulation.
- **Single-Device Demo Simulation**: Field simulation engine with active virtual nodes (Ranger Sarah & Medic Dave) for testing all interactive flows on a single device.
- **Tactical Dark Aesthetic**: Monospace telemetry, HUD reticles, dark background (`#090D12`), and tactical color palette.
