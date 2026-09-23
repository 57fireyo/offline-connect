import React, { useState, useEffect } from 'react';
import { useApp } from '../../context/AppContext';
import { cryptoManager } from '../../crypto/cryptoManager';
import { SafetyNumberInfo } from '../../types';
import { ArrowLeft, ShieldCheck, ShieldAlert, Fingerprint, Check, Lock } from 'lucide-react';

export const VerifySecurityScreen: React.FC = () => {
  const {
    verifyingContact,
    closeVerifySecurity,
    toggleVerifyContact,
    userProfile
  } = useApp();

  const [safetyInfo, setSafetyInfo] = useState<SafetyNumberInfo>({
    numericCode: '849-201',
    hexFingerprint: '7A1F 4B22 90C3 1E58',
    sharedKeyHex: '7A1F4B2290C31E58'
  });

  useEffect(() => {
    if (verifyingContact) {
      cryptoManager
        .computeSafetyNumber(verifyingContact.peerId, verifyingContact.publicKeyBase64)
        .then(info => setSafetyInfo(info));
    }
  }, [verifyingContact]);

  if (!verifyingContact) {
    return null;
  }

  const isVerified = verifyingContact.isVerified;

  return (
    <div className="fixed inset-0 z-50 bg-[#090D12] overflow-y-auto flex flex-col max-w-2xl mx-auto">
      {/* Top Bar */}
      <div className="sticky top-0 z-10 bg-[#111822] border-b border-[#26354A] px-4 py-3 flex items-center justify-between shadow-md">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={closeVerifySecurity}
            className="p-1.5 rounded-lg text-[#9CA3AF] hover:text-[#F9FAFB] hover:bg-[#1B2636] transition-colors cursor-pointer"
          >
            <ArrowLeft className="w-5 h-5" />
          </button>
          <span className="font-bold text-[16px] text-[#F9FAFB]">
            Verify Peer Keys
          </span>
        </div>

        <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-[#1B2636] border border-[#26354A] text-[11px] font-mono-tactical text-[#10B981]">
          <Lock className="w-3 h-3" />
          <span>AIR-GAPPED SAS</span>
        </div>
      </div>

      {/* Main Content */}
      <div className="p-5 flex flex-col items-center gap-5 text-center">
        {/* Shield Icon */}
        <div
          className="w-18 h-18 rounded-full bg-[#1B2636] flex items-center justify-center border-2 transition-all shadow-lg"
          style={{
            borderColor: isVerified ? '#10B981' : '#F59E0B'
          }}
        >
          {isVerified ? (
            <ShieldCheck className="w-9 h-9 text-[#10B981]" />
          ) : (
            <ShieldAlert className="w-9 h-9 text-[#F59E0B]" />
          )}
        </div>

        <div>
          <h2 className="text-[18px] font-bold text-[#F9FAFB]">
            End-to-End Encryption Verification
          </h2>
          <p className="text-[12px] text-[#9CA3AF] leading-relaxed max-w-md mt-1.5">
            Compare this Short Authentication Code (SAS) with {verifyingContact.displayName}'s device screen
            or read it over the radio to verify neither party is subject to a Man-in-the-Middle attack.
          </p>
        </div>

        {/* Prominent 6-Digit Numeric SAS Box */}
        <div className="w-full rounded-2xl bg-[#111822] border-2 border-[#F59E0B]/60 p-6 flex flex-col items-center gap-2 shadow-xl">
          <span className="font-mono-tactical text-[11px] font-bold text-[#F59E0B] tracking-wider uppercase">
            SAFETY AUTHENTICATION CODE (SAS)
          </span>

          <span className="font-mono-tactical text-[38px] font-black text-[#F9FAFB] tracking-[4px] py-1 select-all">
            {safetyInfo.numericCode}
          </span>

          <span className="font-mono-tactical text-[10px] text-[#9CA3AF]">
            Computed deterministically via ECDH P-256 Shared Secret
          </span>
        </div>

        {/* Cryptographic Key Fingerprints Card */}
        <div className="w-full rounded-xl bg-[#1B2636] border border-[#26354A] p-4.5 flex flex-col gap-3 text-left shadow-sm">
          <div className="flex items-center gap-2 text-[#06B6D4] pb-1 border-b border-[#26354A]/60">
            <Fingerprint className="w-4 h-4" />
            <span className="font-mono-tactical text-[11px] font-bold tracking-wider">
              CRYPTOGRAPHIC FINGERPRINTS
            </span>
          </div>

          <div>
            <span className="text-[11px] text-[#9CA3AF] block">
              Session Safety Hash:
            </span>
            <span className="font-mono-tactical text-[13px] font-bold text-[#F59E0B] tracking-wider">
              {safetyInfo.hexFingerprint}
            </span>
          </div>

          <div>
            <span className="text-[11px] text-[#9CA3AF] block">
              Your Hardware Node ID:
            </span>
            <span className="font-mono-tactical text-[12px] font-medium text-[#10B981]">
              {userProfile.peerId} (WebCrypto KeyStore)
            </span>
          </div>

          <div>
            <span className="text-[11px] text-[#9CA3AF] block">
              Peer Public Key Hash:
            </span>
            <span className="font-mono-tactical text-[12px] font-medium text-[#F9FAFB] break-all">
              {verifyingContact.peerId}
            </span>
          </div>
        </div>

        {/* Trust Confirmation Button */}
        <button
          type="button"
          onClick={() => toggleVerifyContact(verifyingContact.peerId, !isVerified)}
          className={`w-full py-3.5 px-4 rounded-xl font-mono-tactical text-[13px] font-bold flex items-center justify-center gap-2 transition-all cursor-pointer shadow-md ${
            isVerified
              ? 'bg-[#243348] text-[#10B981] border border-[#10B981]/50 hover:bg-[#2c3d56]'
              : 'bg-[#10B981] text-black hover:bg-[#0ea372]'
          }`}
        >
          <Check className="w-4 h-4" />
          <span>{isVerified ? 'MARK AS UNVERIFIED' : 'MARK AS VERIFIED CONTACT'}</span>
        </button>
      </div>
    </div>
  );
};
