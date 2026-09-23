import React, { useState } from 'react';
import { Bluetooth, AlertCircle, CheckCircle2, X, RefreshCw, Smartphone } from 'lucide-react';
import { radioService } from '../../services/radioService';
import { useApp } from '../../context/AppContext';

interface BluetoothPairingModalProps {
  isOpen: boolean;
  onClose: () => void;
  onDevicePaired: (device: { id: string; name: string; rssi: number }) => void;
}

export const BluetoothPairingModal: React.FC<BluetoothPairingModalProps> = ({
  isOpen,
  onClose,
  onDevicePaired
}) => {
  const { userProfile } = useApp();
  const [isScanning, setIsScanning] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successDevice, setSuccessDevice] = useState<string | null>(null);

  if (!isOpen) return null;

  const support = radioService.checkBluetoothSupport();

  const handleRequestBluetooth = async () => {
    setIsScanning(true);
    setErrorMessage(null);
    setSuccessDevice(null);

    try {
      const result = await radioService.requestBluetoothScan();
      setSuccessDevice(result.name);
      onDevicePaired({
        id: result.deviceId,
        name: result.name,
        rssi: result.rssi || -60
      });
      setTimeout(() => {
        onClose();
      }, 1500);
    } catch (err: any) {
      if (err.name === 'NotFoundError') {
        setErrorMessage('No Bluetooth device selected. Ensure Bluetooth is switched ON on both phones and you select your friend’s device in the system prompt.');
      } else if (err.name === 'SecurityError') {
        setErrorMessage('Bluetooth permission was blocked. Check browser site permissions in URL bar.');
      } else {
        setErrorMessage(err.message || 'Failed to scan nearby Bluetooth. Make sure Bluetooth is enabled.');
      }
    } finally {
      setIsScanning(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fadeIn">
      <div className="w-full max-w-md bg-[#111822] border border-[#26354A] rounded-2xl shadow-2xl overflow-hidden flex flex-col font-mono-tactical">
        {/* Header */}
        <div className="bg-[#1B2636] border-b border-[#26354A] px-5 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-lg bg-[#06B6D4]/20 border border-[#06B6D4]/40 flex items-center justify-center text-[#06B6D4]">
              <Bluetooth className="w-5 h-5 animate-pulse" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-[#F9FAFB] tracking-wide">
                BLUETOOTH DEVICE DISCOVERY
              </h3>
              <p className="text-[11px] text-[#9CA3AF]">
                Radio frequency pairing for calls & chat
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="w-7 h-7 rounded-md bg-[#243348] text-[#9CA3AF] hover:text-white flex items-center justify-center transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Content */}
        <div className="p-5 flex flex-col gap-4 text-xs">
          {/* Status summary */}
          <div className="bg-[#182332] border border-[#26354A] rounded-xl p-3.5 flex flex-col gap-2">
            <div className="flex items-center justify-between">
              <span className="text-[#9CA3AF]">Your Node Callout:</span>
              <span className="text-[#F59E0B] font-bold">{userProfile.displayName} ({userProfile.callsign})</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-[#9CA3AF]">Web Bluetooth API:</span>
              <span className={support.supported ? 'text-[#10B981] font-bold' : 'text-[#F59E0B] font-bold'}>
                {support.supported ? 'HARDWARE READY' : 'CROSS-MESH MODE'}
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-[#9CA3AF]">Encryption Protocol:</span>
              <span className="text-[#06B6D4] font-bold">ECDH P-256 + AES-GCM</span>
            </div>
          </div>

          {/* Quick instructions in Hindi and English */}
          <div className="bg-[#0B141E] border border-[#06B6D4]/30 rounded-xl p-3 text-[11px] text-[#D1D5DB] leading-relaxed flex flex-col gap-1.5">
            <div className="flex items-center gap-1.5 text-[#06B6D4] font-bold">
              <Smartphone className="w-3.5 h-3.5" />
              <span>How to connect with friend's phone:</span>
            </div>
            <p>
              1. Dono phones me Bluetooth ON rakhein.<br/>
              2. Niche <strong className="text-[#06B6D4]">"Scan Nearby Bluetooth"</strong> button dabayein.<br/>
              3. System popup me apne dost ka phone ya nearby device select karke <strong className="text-[#10B981]">Pair/Allow</strong> karein.<br/>
              4. Device list me connect hote hi encrypted chat aur video call shuru karein!
            </p>
          </div>

          {/* Error message */}
          {errorMessage && (
            <div className="bg-[#EF4444]/15 border border-[#EF4444]/40 rounded-xl p-3 flex items-start gap-2 text-[#EF4444]">
              <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
              <div className="text-[11px] leading-tight">
                {errorMessage}
              </div>
            </div>
          )}

          {/* Success message */}
          {successDevice && (
            <div className="bg-[#10B981]/15 border border-[#10B981]/40 rounded-xl p-3 flex items-center gap-2 text-[#10B981]">
              <CheckCircle2 className="w-4 h-4 shrink-0" />
              <div className="text-[11px] font-bold">
                Paired with "{successDevice}"! Establishing encrypted channel...
              </div>
            </div>
          )}

          {/* Action Button */}
          <button
            type="button"
            disabled={isScanning}
            onClick={handleRequestBluetooth}
            className={`w-full py-3 px-4 rounded-xl font-bold flex items-center justify-center gap-2 transition-all shadow-lg ${
              isScanning
                ? 'bg-[#243348] text-[#9CA3AF] cursor-not-allowed'
                : 'bg-gradient-to-r from-[#06B6D4] to-[#0284C7] hover:from-[#0891B2] hover:to-[#0369A1] text-black cursor-pointer'
            }`}
          >
            {isScanning ? (
              <>
                <RefreshCw className="w-4 h-4 animate-spin" />
                <span>SCANNING RADIO FREQUENCIES...</span>
              </>
            ) : (
              <>
                <Bluetooth className="w-4 h-4" />
                <span>SCAN NEARBY BLUETOOTH DEVICES</span>
              </>
            )}
          </button>

          <p className="text-[10px] text-center text-[#6B7280]">
            Web Bluetooth utilizes standard BLE GATT profiles. Ensure HTTPS & Chrome/Edge for direct OS Bluetooth dialog.
          </p>
        </div>
      </div>
    </div>
  );
};
