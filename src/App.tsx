import React from 'react';
import { useApp } from './context/AppContext';
import { TacticalHeader } from './components/common/TacticalHeader';
import { TacticalBottomNav } from './components/common/TacticalBottomNav';
import { RadarScreen } from './components/screens/RadarScreen';
import { ChatsListScreen } from './components/screens/ChatsListScreen';
import { SosScreen } from './components/screens/SosScreen';
import { SettingsScreen } from './components/screens/SettingsScreen';
import { ChatScreen } from './components/screens/ChatScreen';
import { VerifySecurityScreen } from './components/screens/VerifySecurityScreen';
import { CallScreen } from './components/screens/CallScreen';

export const AppContent: React.FC = () => {
  const { activeTab, activeChatPeerId, verifyingContact, callSession } = useApp();

  return (
    <div className="min-h-screen bg-[#090D12] text-[#F9FAFB] flex flex-col font-sans relative selection:bg-[#F59E0B] selection:text-black">
      {/* Tactical Top Bar */}
      <TacticalHeader />

      {/* Main Tab Screen */}
      <main className="flex-1 w-full">
        {activeTab === 'radar' && <RadarScreen />}
        {activeTab === 'chats' && <ChatsListScreen />}
        {activeTab === 'sos' && <SosScreen />}
        {activeTab === 'settings' && <SettingsScreen />}
      </main>

      {/* Overlays */}
      {activeChatPeerId && <ChatScreen />}
      {verifyingContact && <VerifySecurityScreen />}
      {callSession.active && <CallScreen />}

      {/* Tactical Bottom Navigation Bar */}
      <TacticalBottomNav />
    </div>
  );
};
